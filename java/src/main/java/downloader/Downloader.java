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
            Index index = (Index) LocateRegistry.getRegistry(8183).lookup("index");
            
            while (true) {
                String url = index.takeNext();

                if (url == null) {
                    System.out.println("No URLs to index. Waiting...");
                    Thread.sleep(2000);
                    continue;
                }

                System.out.println("Processing URL: " + url);

                try {
                    Document doc = Jsoup.connect(url).get();

                    String title = doc.title();
                    String bodyText = doc.body().text();
                    String words[] = bodyText.split("\\s+");

                    System.out.println("Indexing page: " + title);

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
                    System.err.println("Error processing " + url + ": " + e.getMessage());
                }

                // Small delay to avoid spamming requests
                Thread.sleep(500);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
