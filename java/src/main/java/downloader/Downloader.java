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
        try {
            // Conectar ao IndexManager (porta fixa 8182, por agora)
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

                // failover: tenta obter URL de qualquer Barrel
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
                    System.out.println("Sem URLs para indexar. À espera...");
                    Thread.sleep(2000);
                    continue;
                }

                System.out.println("A processar URL: " + url);

                try {
                    Document doc = Jsoup.connect(url).get();
                    String title = doc.title();
                    String bodyText = doc.body().text();
                    String[] words = bodyText.split("\\s+");

                    System.out.println("A indexar página: " + title);

                    //Enviar palavras para todos os Barrels (broadcast simples)
                    for (String word : words) {
                        String w = word.trim().toLowerCase();
                        if (!w.isEmpty()) {
                            for (Index b : barrels) {
                                try {
                                    b.addToIndex(w, url);
                                } catch (Exception e) {
                                    System.err.println("[Downloader] Falha ao enviar para Barrel: " + e.getMessage());
                                }
                            }
                        }
                    }

                    // Extrair links e adicionar à fila (broadcast)
                    Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        String absUrl = link.attr("abs:href");
                        if (!absUrl.isEmpty()) {
                            for (Index b : barrels) {
                                try {
                                    b.putNew(absUrl);
                                } catch (Exception e) {
                                    System.err.println("[Downloader] Falha ao adicionar URL novo: " + e.getMessage());
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    System.err.println("Erro ao processar " + url + ": " + e.getMessage());
                }

                Thread.sleep(500);
            }

        } catch (Exception e) {
            System.err.println("[Downloader] Erro geral: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
