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
    private final Map<String, Set<String>> incomingLinks = new HashMap<>();


    public static void main(String[] args) {
        System.setProperty("java.rmi.server.hostname", "192.168.1.183");
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
                System.out.println("[IndexBarrel] takeNext -> ignorado (ja visitado): " + next);
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
            System.out.println("[IndexBarrel] putnew -> url ja visitado:" + norm);
            return;
        }

        if (urlsToIndex.contains(norm)) {
        System.out.println("[IndexBarrel] putNew -> ja na fila: " + norm);
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


   @Override
public synchronized List<String> searchAll(List<String> terms, int page, int pageSize) throws RemoteException {
    if (terms == null || terms.isEmpty()) {
        return Collections.singletonList("Nenhum termo introduzido.");
    }

    // conjunto de URLs que contêm todas as palavras (interseção AND)
    Set<String> result = null;

    for (String term : terms) {
        if (term == null || term.isBlank()) continue;

        term = term.toLowerCase();
        List<String> urlsList = indexedItems.getOrDefault(term, Collections.emptyList());
        Set<String> urls = new HashSet<>(urlsList); // converter lista -> conjunto

        if (result == null) {
            result = new HashSet<>(urls); // primeiro termo
        } else {
            result.retainAll(urls); // interseção
        }

        if (result.isEmpty()) break; // sem resultados possíveis
    }

    if (result == null || result.isEmpty()) {
        return Collections.singletonList("Nenhum resultado encontrado.");
    }

    // ordenar alfabeticamente por URL (por agora)
    List<String> urlsOrdenadas = new ArrayList<>(result);
    Collections.sort(urlsOrdenadas);

    // Paginação (10 resultados por página, por exemplo)
    int from = Math.max(0, page * pageSize);
    int to = Math.min(urlsOrdenadas.size(), from + pageSize);
    if (from >= to) {
        return Collections.singletonList("Pagina sem resultados.");
    }

    // Gerar lista de texto formatado
    List<String> output = new ArrayList<>();
    for (String u : urlsOrdenadas.subList(from, to)) {
        output.add(u);
    }

    System.out.println("[IndexBarrel] searchAll -> termos=" + terms + ", resultados=" + output.size());
    return output;
}
    private String normalizeUrl(String url) {
        if (url == null) return null;
        url = url.trim();
        int hashPos = url.indexOf('#');
        if (hashPos != -1) url = url.substring(0, hashPos);
        if (url.endsWith("/") && url.length() > 1) url = url.substring(0, url.length() - 1);
        return url;
    }

    @Override
    public synchronized void synchronizeFrom(Map<String, List<String>> data, Set<String> newVisitedUrls, Queue<String> newPendingUrls) throws RemoteException {
        // Sincronizar o índice
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            String word = entry.getKey();
            List<String> urls = entry.getValue();
            
            indexedItems.putIfAbsent(word, new ArrayList<>());
            List<String> currentUrls = indexedItems.get(word);
            
            for (String url : urls) {
                if (!currentUrls.contains(url)) {
                    currentUrls.add(url);
                }
            }
        }

        // Sincronizar URLs visitados
        visitedUrls.addAll(newVisitedUrls);

        // Sincronizar fila de URLs pendentes
        for (String url : newPendingUrls) {
            if (!urlsToIndex.contains(url) && !visitedUrls.contains(url)) {
                urlsToIndex.add(url);
            }
        }

        System.out.println("[IndexBarrel] Sincronizacao completa:");
        System.out.println("  - Palavras indexadas: " + indexedItems.size());
        System.out.println("  - URLs visitados: " + visitedUrls.size());
        System.out.println("  - URLs pendentes: " + urlsToIndex.size());
    }

    @Override
    public synchronized Map<String, Object> getSynchronizationData() throws RemoteException {
        Map<String, Object> syncData = new HashMap<>();
        syncData.put("index", new HashMap<>(indexedItems));
        syncData.put("visited", new HashSet<>(visitedUrls));
        syncData.put("pending", new LinkedList<>(urlsToIndex));
        return syncData;
    }


    //Funções para dar handle aos backlinks
    
    @Override
    public synchronized void registerPageLinks(String sourceUrl, List<String> outlinks) throws RemoteException {
    if (sourceUrl == null || outlinks == null) return;

    for (String dest : outlinks) {
        incomingLinks.computeIfAbsent(dest, k -> new HashSet<>()).add(sourceUrl);
    }
    // garante que a página origem também existe
    incomingLinks.putIfAbsent(sourceUrl, new HashSet<>());

    System.out.println("[IndexBarrel] Registadas " + outlinks.size() + " ligacoes a partir de " + sourceUrl);
    }


    public synchronized List<String> getPagesOrderedByInLinks(int limit, int offset) throws RemoteException {
    return incomingLinks.entrySet().stream()
        .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
        .skip(offset)
        .limit(limit)
        .map(Map.Entry::getKey)
        .toList();
    }

public synchronized Set<String> getPagesLinkingTo(String url) throws RemoteException {
    if (url == null || url.isEmpty()) {
        return Collections.emptySet();
    }
    Set<String> sources = incomingLinks.get(url);
    if (sources == null) {
        return Collections.emptySet();
    }
    // Retorna uma cópia para evitar que o cliente altere os dados internos
    return new HashSet<>(sources);
}

}

