package web.service;

import gateway.GatewayInterface;
import gateway.SystemStats;
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
    
    public RMIClientService() {
        gateway = null;
        
        // Ler configuração do Config
        String gatewayHost = Config.get("gateway.host");
        if (gatewayHost == null || gatewayHost.isEmpty()) {
            gatewayHost = "localhost";  // Fallback para localhost
            System.out.println("[RMIClientService] gateway.host nao configurado, usando localhost");
        }
        int gatewayPort = Config.getInt("gateway.port", 8186);
        
        // Tentar conectar à Gateway uma vez
        System.out.println("[RMIClientService] A tentar ligar à Gateway (" + gatewayHost + ":" + gatewayPort + ")...");
        try {
            Registry registry = LocateRegistry.getRegistry(gatewayHost, gatewayPort);
            gateway = (GatewayInterface) registry.lookup("gateway");
            System.out.println("[RMIClientService] ✅ Conectado à Gateway com sucesso!");
        } catch (Exception e) {
            System.err.println("[RMIClientService] ❌ ERRO ao conectar à Gateway: " + e.getMessage());
            e.printStackTrace();
            gateway = null;
        }
    }
    
    public List<String> search(String query) throws RemoteException {
        System.out.println("[RMIClientService] search chamado com query: " + query);
        if (gateway == null) {
            System.err.println("[RMIClientService] ERROR: Gateway é null!");
            throw new RemoteException("Gateway não conectada");
        }
        System.out.println("[RMIClientService] A chamar gateway.search()...");
        List<String> results = gateway.search(query);
        System.out.println("[RMIClientService] gateway.search() retornou " + results.size() + " resultados");
        return results;
    }
    
    public void indexURL(String url) throws RemoteException, InterruptedException {
        System.out.println("[RMIClientService] indexURL chamado com URL: " + url);
        if (gateway == null) {
            System.err.println("[RMIClientService] ERROR: Gateway é null!");
            throw new RemoteException("Gateway não conectada");
        }
        System.out.println("[RMIClientService] A chamar gateway.addUrl()...");
        gateway.addUrl(url);
        System.out.println("[RMIClientService] gateway.addUrl() completado");
    }

    public List<String> getPagesOrderedByInLinks(int limit, int offset) throws RemoteException {
        System.out.println("[RMIClientService] getPagesOrderedByInLinks chamado com limit=" + limit + ", offset=" + offset);
        if (gateway == null) {
            System.err.println("[RMIClientService] ERROR: Gateway é null!");
            throw new RemoteException("Gateway não conectada");
        }
        System.out.println("[RMIClientService] A chamar gateway.getPagesOrderedByInLinks()...");
        List<String> results = gateway.getPagesOrderedByInLinks(limit, offset);
        System.out.println("[RMIClientService] gateway.getPagesOrderedByInLinks() retornou " + results.size() + " páginas");
        return results;
    }
    

    public Set<String> getPagesLinkingTo(String url) throws RemoteException {
        System.out.println("[RMIClientService] getPagesLinkingTo chamado com URL: " + url);
        if (gateway == null) {
            System.err.println("[RMIClientService] ERROR: Gateway é null!");
            throw new RemoteException("Gateway não conectada");
        }
        System.out.println("[RMIClientService] A chamar gateway.getPagesLinkingTo()...");
        Set<String> results = gateway.getPagesLinkingTo(url);
        System.out.println("[RMIClientService] gateway.getPagesLinkingTo() retornou " + results.size() + " páginas");
        return results;
    }
    
    public SystemStats getSystemStats() throws RemoteException {
        System.out.println("[RMIClientService] getSystemStats chamado");
        if (gateway == null) {
            System.err.println("[RMIClientService] ERROR: Gateway é null!");
            throw new RemoteException("Gateway não conectada");
        }
        System.out.println("[RMIClientService] A chamar gateway.getSystemStats()...");
        SystemStats stats = gateway.getSystemStats();
        System.out.println("[RMIClientService] gateway.getSystemStats() completado com sucesso");
        return stats;
    }
    
    public boolean isConnected() {
        return gateway != null;
    }
}
