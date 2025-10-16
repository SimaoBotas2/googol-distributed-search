package barrel;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.*;

public class IndexServer extends UnicastRemoteObject implements Index {
    private ArrayList<String> urlsToIndex;
    private HashMap<String, List<String>> indexedItems;

    public IndexServer() throws RemoteException {
        super();
        // Queue of URLs to index
        urlsToIndex = new ArrayList<String>();       
        // Inverted index: word -> list of URLs
        indexedItems = new HashMap<>();
    }

    public static void main(String args[]) {
        try {
            IndexServer server = new IndexServer();
            Registry registry = LocateRegistry.createRegistry(8183);
            registry.rebind("index", server);
            System.out.println("Server ready. Waiting for input...");

            // Rudimentary console interface
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Choose an option: 1) Add URL  2) Search word  3) Exit");
                String option = scanner.nextLine();
                if (option.equals("1")) {
                    System.out.print("Enter URL: ");
                    String url = scanner.nextLine();
                    server.putNew(url);
                    System.out.println("URL added to queue.");
                } else if (option.equals("2")) {
                    System.out.print("Enter word to search: ");
                    String word = scanner.nextLine().toLowerCase();
                    List<String> results = server.searchWord(word);
                    System.out.println("Found in URLs:");
                    for (String u : results) {
                        System.out.println(u);
                    }
                } else if (option.equals("3")) {
                    System.out.println("Exiting...");
                    break;
                } else {
                    System.out.println("Invalid option.");
                }
            }
            scanner.close();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private long counter = 0, timestamp = System.currentTimeMillis();

    public String takeNext() throws RemoteException {
        synchronized (urlsToIndex) {
            if (urlsToIndex.isEmpty()) {
                return null;
            }
            return urlsToIndex.remove(0);
        }
    }

    public void putNew(String url) throws RemoteException {
        synchronized (urlsToIndex) {
            urlsToIndex.add(url);
        }
    }

    public void addToIndex(String word, String url) throws RemoteException {
        synchronized (indexedItems) {
            word = word.toLowerCase();
            indexedItems.putIfAbsent(word, new ArrayList<>());
            List<String> urls = indexedItems.get(word);
            if (!urls.contains(url)) {
                urls.add(url);
            }
        }
    }

    public List<String> searchWord(String word) throws RemoteException {
        synchronized (indexedItems) {
            word = word.toLowerCase();
            if (indexedItems.containsKey(word)) {
                return new ArrayList<>(indexedItems.get(word));
            }
            return new ArrayList<>();
        }
    }
}
