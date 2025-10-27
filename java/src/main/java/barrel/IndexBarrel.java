package barrel;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.*;

public class IndexBarrel extends UnicastRemoteObject implements Index {

    private final Queue<String> urlsToIndex; //Url Queue
    private final HashMap<String, List<String>> indexedItems; //palavras
    private final Set<String> visitedUrls; //urls já visitados


    public static void main(String[] args) {
        try {
            int port = 8183; // valor por defeito
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }

            IndexBarrel barrel = new IndexBarrel();
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("index", barrel);
            System.out.println("[IndexBarrel] Registry criado na porta " + port + " e objeto 'index' registado.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public IndexBarrel() throws RemoteException {
        super();
        urlsToIndex = new LinkedList<>();
        indexedItems = new HashMap<>();
        visitedUrls = new LinkedHashSet<>(); 
        System.out.println("[IndexBarrel] Barrel iniciado e pronto para receber pedidos.");
    }

    @Override
    public synchronized String takeNext() throws RemoteException {
        while (!urlsToIndex.isEmpty()) {
            String next = urlsToIndex.poll();
            if (!visitedUrls.contains(next)) { 
                System.out.println("[IndexBarrel] takeNext -> " + next);
                return next;
            } else {
                System.out.println("[IndexBarrel] takeNext -> ignorado (já visitado): " + next);
            }
        }
        return null;
    }

    @Override
    public synchronized void putNew(String url) throws RemoteException {
        String norm = normalizeUrl(url);
        if(norm == null || norm.isEmpty())
            return;
        
        if(visitedUrls.contains(norm)){
            System.out.println("[IndexBarrel] putnew -> url já visitado:" + norm);
            return;
        }

        if (urlsToIndex.contains(norm)) {
        System.out.println("[IndexBarrel] putNew -> já na fila: " + norm);
        return;
        }

        urlsToIndex.add(norm);
        System.out.println("[IndexBarrel] putNew -> URL adicionada à fila: " + url);
    }

    @Override
    public synchronized void addToIndex(String word, String url) throws RemoteException {
        word = word.toLowerCase();
        visitedUrls.add(url);
        indexedItems.putIfAbsent(word, new ArrayList<>());
        
        List<String> urls = indexedItems.get(word);
        if (!urls.contains(url)) {
            urls.add(url);
            System.out.println("[IndexBarrel] addToIndex -> '" + word + "' → " + url);
        }
    }

    @Override
    public synchronized List<String> searchWord(String word) throws RemoteException {
        word = word.toLowerCase();
        List<String> results = indexedItems.getOrDefault(word, new ArrayList<>());
        System.out.println("[IndexBarrel] searchWord -> '" + word + "' (" + results.size() + " resultados)");
        return new ArrayList<>(results);
    }

    @Override
    public synchronized Map<String, List<String>> getIndexSnapshot() throws RemoteException {
        // devolve uma cópia do conteudo do barrel, funcao para debugging
        return new HashMap<>(indexedItems);
    }

    @Override
    public boolean ping() throws RemoteException{
        return true;
    }

    private String normalizeUrl(String url) {
    if (url == null) return null;
    url = url.trim();
    int hashPos = url.indexOf('#');
    if (hashPos != -1) url = url.substring(0, hashPos);
    if (url.endsWith("/") && url.length() > 1) url = url.substring(0, url.length() - 1);
    return url;
    }

}

