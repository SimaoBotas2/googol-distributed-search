package downloader;

import barrel.Index;
import java.rmi.registry.*;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Downloader {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 8183);
            Index index = (Index) registry.lookup("index");
            System.out.println("Connectado ao Index Barrel na porta 8183"); //temporario para testes, depois vai ter vários

            while (true) {
                String url = index.takeNext();

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

                    // Send words to index
                    for (String word : words) {
                        if (!word.trim().isEmpty()) {
                            index.addToIndex(word.toLowerCase(), url);
                        }
                    }

                    // Extract links and add to queue
                    Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        String absUrl = link.attr("abs:href");
                        if (!absUrl.isEmpty()) {
                            index.putNew(absUrl);
                        }
                    }

                } catch (Exception e) {
                    System.err.println("Erro ao processar " + url + ": " + e.getMessage());
                }

                Thread.sleep(500);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
