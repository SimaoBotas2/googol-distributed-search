package downloader;

import barrel.Index;
import barrel.Manager;
import java.rmi.registry.*;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Downloader {
    public static void main(String[] args) {

        boolean debug = false; //para comentarios de debug
        try {
            // Conectar ao IndexManager (porta fixa 8182, por agora )
            Registry regManager = LocateRegistry.getRegistry("localhost", 8182);
            Manager manager = (Manager) regManager.lookup("manager");

            // Obter lista de Barrels ativos
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

            //Ciclo principal
            while (true) {
                String url = null;
                // Atualiza lista de barrels antes de pedir nova URL
                barrels = atualizarBarrels(manager);

                if (barrels.isEmpty()) {
                    System.out.println("[Downloader] Nenhum Barrel ativo. A espera...");
                    Thread.sleep(5000);
                    continue;
                }

                // Tenta obter URL de qualquer Barrel ativo
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
                    System.out.println("[Downloader] Sem URLs para indexar. A espera...");
                    Thread.sleep(2000);
                    continue;
                }

                System.out.println("A processar URL: " + url);

                try {
                    Document doc = Jsoup.connect(url).header("Accept-Charset","UTF-8").get(); //fazer isto para garantir que (quase) todos os caracteres sao lidos
                    doc.outputSettings().charset("UTF-8");
                    String title = doc.title();
                    String bodyText = doc.body().text();
                    String[] words = bodyText.split("\\s+");

                    System.out.println("[Downloader] A indexar pagina: " + title);
 
                    //Enviar palavras para todos os Barrels (broadcast simples)
                    for (String word : words) {
                        String w = word.trim().toLowerCase();
                        if (!w.isEmpty()) {
                            for (Index b : barrels) {
                                try {
                                    b.addToIndex(w, url);
                                    if(debug)
                                    System.out.println("[DEBUG] Enviado para o barrel" + b);
                                } catch (Exception e) {
                                    System.err.println("[Downloader] Falha ao enviar para Barrel: " + e.getMessage());
                                }
                            }

                        }
                        barrels = atualizarBarrels(manager);
                    }

                    // Extrair links e adicionar à fila (broadcast)
                    Elements links = doc.select("a[href]");

                    Set<String> alreadySeen = new HashSet<>(); //evitar que o downloader vejo os mesmos urls duas vezes
                    List<String> outlinks = new ArrayList<>(); //guardar os links visto para backlinking das páginas

                    for (Element link : links) {
                        String absUrl = link.attr("abs:href");
                        absUrl = limparUrl(absUrl);

                        if (absUrl != null ||  !alreadySeen.contains(absUrl)) {
                            alreadySeen.add(absUrl);
                            outlinks.add(absUrl);
                            for (Index b : barrels) {
                                try {
                                    b.putNew(absUrl);
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

                } 
                catch (Exception e) {
                    System.err.println("[Downloader] Erro ao processar " + url + ": " + e.getMessage());
                }

                Thread.sleep(500);
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

private static String limparUrl(String url){
    if(url == null) return null;
    //Urls que o downloader deve ignorar, pois nao possuem conteúdo
    if(url.contains("?action") || url.contains("&oldid=") || url.contains("&printable") || url.contains("&veaction")){
     return null;
    }
    
    //se estiver limpa, retorna o url original
    return url;
}
}