package web.service;

import gateway.GatewayInterface;
import gateway.SystemStats;
import gateway.SearchResult;
import org.springframework.stereotype.Service;
import common.Config;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Set;

@Service
public class RMIClientService {
    
    private GatewayInterface gateway;
    private String gatewayHost;
    private int gatewayPort;
    
    public RMIClientService() {
        gateway = null;
        
        // Ler configuração do Config (com fallback)
        this.gatewayHost = Config.get("gateway.host");
        if (this.gatewayHost == null || this.gatewayHost.isEmpty()) {
            this.gatewayHost = "localhost";  // Fallback para localhost
            System.out.println("[RMIClientService] gateway.host nao configurado, usando localhost");
        }
        this.gatewayPort = Config.getInt("gateway.port", 8186);

        // Tentar conectar inicialmente (não bloquear se falhar)
        connectIfNeeded(false);
    }
    
    public List<String> search(String query) throws RemoteException {
        SearchResult result = searchPaginated(query, 10, 0);
        return result.getResults();
    }

    public List<String> search(String query, int limit, int offset) throws RemoteException {
        System.out.println("[RMIClientService] search chamado com query: " + query + ", limit: " + limit + ", offset: " + offset);
        connectIfNeeded(true);
        if (gateway == null) throw new RemoteException("Gateway não conectada");
        System.out.println("[RMIClientService] A chamar gateway.search()...");
        SearchResult result = gateway.search(query, limit, offset);
        System.out.println("[RMIClientService] gateway.search() retornou " + result.getResults().size() + " resultados de " + result.getTotalMatches() + " total");
        return result.getResults();
    }

    public SearchResult searchPaginated(String query, int limit, int offset) throws RemoteException {
        System.out.println("[RMIClientService] searchPaginated chamado com query: " + query + ", limit: " + limit + ", offset: " + offset);
        connectIfNeeded(true);
        if (gateway == null) throw new RemoteException("Gateway não conectada");
        System.out.println("[RMIClientService] A chamar gateway.search() (com total)...");
        SearchResult result = gateway.search(query, limit, offset);
        System.out.println("[RMIClientService] gateway.search() retornou " + result.getResults().size() + " resultados de " + result.getTotalMatches() + " total");
        return result;
    }
    
    public void indexURL(String url) throws RemoteException, InterruptedException {
        System.out.println("[RMIClientService] indexURL chamado com URL: " + url);
        connectIfNeeded(true);
        if (gateway == null) throw new RemoteException("Gateway não conectada");
        System.out.println("[RMIClientService] A chamar gateway.addUrl()...");
        gateway.addUrl(url);
        System.out.println("[RMIClientService] gateway.addUrl() completado");
    }

    public List<String> getPagesOrderedByInLinks(int limit, int offset) throws RemoteException {
        System.out.println("[RMIClientService] getPagesOrderedByInLinks chamado com limit=" + limit + ", offset=" + offset);
        connectIfNeeded(true);
        if (gateway == null) throw new RemoteException("Gateway não conectada");
        System.out.println("[RMIClientService] A chamar gateway.getPagesOrderedByInLinks()...");
        List<String> results = gateway.getPagesOrderedByInLinks(limit, offset);
        System.out.println("[RMIClientService] gateway.getPagesOrderedByInLinks() retornou " + results.size() + " páginas");
        return results;
    }
    

    public Set<String> getPagesLinkingTo(String url) throws RemoteException {
        System.out.println("[RMIClientService] getPagesLinkingTo chamado com URL: " + url);
        connectIfNeeded(true);
        if (gateway == null) throw new RemoteException("Gateway não conectada");
        System.out.println("[RMIClientService] A chamar gateway.getPagesLinkingTo()...");
        Set<String> results = gateway.getPagesLinkingTo(url);
        System.out.println("[RMIClientService] gateway.getPagesLinkingTo() retornou " + results.size() + " páginas");
        return results;
    }
    
    public SystemStats getSystemStats() throws RemoteException {
        System.out.println("[RMIClientService] getSystemStats chamado");
        connectIfNeeded(true);
        if (gateway == null) throw new RemoteException("Gateway não conectada");
        System.out.println("[RMIClientService] A chamar gateway.getSystemStats()...");
        SystemStats stats = gateway.getSystemStats();
        System.out.println("[RMIClientService] gateway.getSystemStats() completado com sucesso");
        return stats;
    }

    public boolean checkUrlIndexed(String url) throws RemoteException {
        System.out.println("[RMIClientService] checkUrlIndexed chamado com URL: " + url);
        connectIfNeeded(true);
        if (gateway == null) throw new RemoteException("Gateway não conectada");
        System.out.println("[RMIClientService] A chamar gateway.checkUrlIndexed()...");
        boolean indexed = gateway.checkUrlIndexed(url);
        System.out.println("[RMIClientService] gateway.checkUrlIndexed() retornou: " + indexed);
        return indexed;
    }
    
    public boolean isConnected() {
        return gateway != null;
    }

    // Tenta (re)conectar à Gateway quando necessário.
    // Se force=true, faz uma tentativa imediata com logging; caso contrário, tenta apenas uma vez silenciosamente.
    private synchronized void connectIfNeeded(boolean force) {
        if (!force && gateway != null) return;
        try {
            System.out.println("[RMIClientService] A tentar ligar à Gateway (" + gatewayHost + ":" + gatewayPort + ")...");
            Registry registry = LocateRegistry.getRegistry(gatewayHost, gatewayPort);
            GatewayInterface candidate = (GatewayInterface) registry.lookup("gateway");
            gateway = candidate;
            System.out.println("[RMIClientService] ✅ Conectado à Gateway com sucesso!");
        } catch (Exception e) {
            if (force) {
                System.err.println("[RMIClientService] ❌ ERRO ao conectar à Gateway: " + e.getMessage());
            }
            gateway = null;
        }
    }
}
