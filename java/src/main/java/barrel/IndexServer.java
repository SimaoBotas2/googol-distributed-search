package barrel;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;


//Log no terminal para ajudar no debugging



public class IndexServer extends UnicastRemoteObject implements Index {
    private Queue<String> urlsToIndex;
    private HashMap<String, List<String>> indexedItems;

    public IndexServer() throws RemoteException {
        super();
        urlsToIndex = new LinkedList<>();

        indexedItems = new HashMap<>();
        System.out.println("[IndexServer] Servidor iniciado e pronto para receber pedidos.");
    }

    public static void main(String args[]) {
        try {
            IndexServer server = new IndexServer();
            Registry registry = LocateRegistry.createRegistry(8183);
            registry.rebind("index", server);
            System.out.println("[IndexServer] Registry criado na porta 8183 e objeto 'index' registado.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public String takeNext() throws RemoteException {
        synchronized (urlsToIndex) {
            String next = urlsToIndex.poll();
            if (next != null) {
                System.out.println("[IndexServer] takeNext -> " + next);
            } else {
                System.out.println("[IndexServer] takeNext -> fila vazia");
            }
            return next;
        }
    }

    public void putNew(String url) throws RemoteException {
        synchronized (urlsToIndex) {
            urlsToIndex.add(url);
            System.out.println("[IndexServer] putNew -> URL adicionada à fila: " + url);
        }
    }

    public void addToIndex(String word, String url) throws RemoteException {
        synchronized (indexedItems) {
            word = word.toLowerCase();
            indexedItems.putIfAbsent(word, new ArrayList<>());
            List<String> urls = indexedItems.get(word);
            if (!urls.contains(url)) {
                urls.add(url);
                System.out.println("[IndexServer] addToIndex -> '" + word + "' → " + url);
            }
        }
    }

    public List<String> searchWord(String word) throws RemoteException {
        synchronized (indexedItems) {
            word = word.toLowerCase();
            List<String> results = indexedItems.getOrDefault(word, new ArrayList<>());
            System.out.println("[IndexServer] searchWord -> '" + word + "' (" + results.size() + " resultados)");
            return new ArrayList<>(results);
        }
    }
}
