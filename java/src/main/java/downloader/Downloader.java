package downloader;

import barrel.Index;
import barrel.Manager;
import common.Config;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Downloader extends UnicastRemoteObject implements Manager.WorkAvailableListener {
    
    private static final long serialVersionUID = 1L;
    
    // Semáforo para controlar quando o Downloader deve processar URLs
    // Iniciado com 1 para começar imediatamente
    private static Semaphore workAvailable = new Semaphore(1);
    
    public Downloader() throws java.rmi.RemoteException {
        super();
    }
    
    @Override
    public void onNewWorkAvailable() {
        // Callback chamado pelo Manager quando há novo trabalho
        workAvailable.release();
        System.out.println("[Downloader] Novo URL disponivel!");
    }
    
    public static void main(String[] args) {
        boolean debug = false;

        // exige argumento 1 ou 2
        if (args.length == 0 || !(args[0].equals("1") || args[0].equals("2"))) {
            System.err.println("Uso: java downloader.Downloader <1|2>");
            System.err.println("Exemplo: java downloader.Downloader 1  (para downloader.1)");
            System.exit(1);
        }

        String arg = args[0];
        String localIp;
        if (arg.equals("1")) {
            localIp = Config.get("downloader.1.ip");
        } else {
            localIp = Config.get("downloader.2.ip");
        }

        String managerIp = Config.get("manager.ip");
        int managerPort = Config.getInt("manager.port", 8182);
        System.setProperty("java.rmi.server.hostname", localIp);

        try {
            Registry regManager = LocateRegistry.getRegistry(managerIp, managerPort);
            Manager manager = (Manager) regManager.lookup("manager");
            
            // Criar instância do Downloader e registar como listener do Manager
            Downloader downloader = new Downloader();
            manager.registerWorkListener(downloader);
            System.out.println("[Downloader] Registado como listener do Manager para notificacoes de trabalho");

            List<String> barrelInfo = manager.getActiveBarrels();
            List<Index> barrels = new ArrayList<>();

            for (String info : barrelInfo) {
                try {
                    String[] parts = info.split(":");
                    String host = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    Registry reg = LocateRegistry.getRegistry(host, port);
                    Index idx = (Index) reg.lookup("index");
                    barrels.add(idx);
                    System.out.println("[Downloader] Conectado ao Barrel " + info);
                } catch (Exception e) {
                    System.err.println("[Downloader] Falha ao conectar a " + info + ": " + e.getMessage());
                }
            }

            if (barrels.isEmpty()) {
                System.err.println("[Downloader] Nenhum Barrel ativo. A sair...");
                return;
            }

            System.out.println("[Downloader] Total de Barrels conectados: " + barrels.size());

            while (true) {
                String url = null;
                barrels = atualizarBarrels(manager);

                if (barrels.isEmpty()) {
                    System.out.println("[Downloader] Nenhum Barrel ativo. A espera...");
                    Thread.sleep(5000);
                    continue;
                }

                for (Index b : barrels) {
                    try {
                        url = b.takeNext();
                        if (url != null)
                            break;
                    } catch (Exception e) {
                        System.err.println("[Downloader] Erro ao pedir URL a um Barrel: " + e.getMessage());
                    }
                }

                if (url == null) {
                    System.out.println("[Downloader] Sem URLs para indexar. A aguardar notificacao...");
                    workAvailable.tryAcquire(2, java.util.concurrent.TimeUnit.SECONDS);
                    continue;
                }

                System.out.println("A processar URL: " + url);

                try {
                    Document doc = Jsoup.connect(url).header("Accept-Charset", "UTF-8").get();
                    doc.outputSettings().charset("UTF-8");
                    String title = doc.title();
                    String bodyText = doc.body().text();
                    String[] words = bodyText.split("\\s+");

                    System.out.println("[Downloader] A indexar pagina: " + title);

                    for (String word : words) {
                        String w = word.trim().toLowerCase();
                        if (!w.isEmpty()) {
                            for (Index b : barrels) {
                                try {
                                    b.addToIndex(w, url);
                                    if (debug)
                                        System.out.println("[DEBUG] Enviado para o barrel" + b);
                                } catch (Exception e) {
                                    System.err.println("[Downloader] Falha ao enviar para Barrel: " + e.getMessage());
                                    System.out.println("BARREL : " + b);
                                }
                            }
                        }
                        barrels = atualizarBarrels(manager);
                    }

                    Elements links = doc.select("a[href]");
                    Set<String> alreadySeen = new HashSet<>();
                    List<String> outlinks = new ArrayList<>();

                    for (Element link : links) {
                        String absUrl = link.attr("abs:href");
                        absUrl = limparUrl(absUrl);

                        if (absUrl != null && !alreadySeen.contains(absUrl)) {
                            alreadySeen.add(absUrl);
                            outlinks.add(absUrl);
                            for (Index b : barrels) {
                                try {
                                    b.putNew(absUrl);
                                    // Liberta semáforo para notificar que há novo trabalho disponível
                                    workAvailable.release();
                                } catch (Exception e) {
                                    System.err.println("[Downloader] Falha ao adicionar URL novo: " + e.getMessage());
                                }
                            }
                        }
                    }

                    for (Index b : barrels) {
                        try {
                            b.registerPageLinks(url, outlinks);
                        } catch (Exception e) {
                            System.err.println("[Downloader] Falha ao registar ligacoes de paginas : " + e.getMessage());
                        }
                    }


                } catch (Exception e) {
                    System.err.println("[Downloader] Erro ao processar " + url + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("[Downloader] Erro geral: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Index> atualizarBarrels(Manager manager) {
        List<Index> ativos = new ArrayList<>();
        try {
            List<String> infoList = manager.getActiveBarrels();
            for (String info : infoList) {
                try {
                    String[] parts = info.split(":");
                    String host = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    Registry reg = LocateRegistry.getRegistry(host, port);
                    Index idx = (Index) reg.lookup("index");
                    ativos.add(idx);
                } catch (Exception e) {
                    System.err.println("[Downloader] Falha ao conectar a " + info + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[Downloader] Erro ao obter lista de Barrels: " + e.getMessage());
        }
        return ativos;
    }

    public static void notifyNewWork() {
        workAvailable.release();
        System.out.println("[Downloader] Notificacao: Novo trabalho disponivel!");
    }

    private static String limparUrl(String url) {
        if (url == null) return null;
        if (url.contains("?action") || url.contains("&oldid=") || url.contains("&printable") || url.contains("&veaction")) {
            return null;
        }
        return url;
    }
}
