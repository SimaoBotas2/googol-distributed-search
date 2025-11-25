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
        try {
            // Ler configuração do Config
            String gatewayHost = Config.get("gateway.host");
            int gatewayPort = Config.getInt("gateway.port", 8186);
            
            // Tentar conectar à Gateway uma vez
            try {
                System.out.println("[RMIClientService] A tentar ligar à Gateway (" + gatewayHost + ":" + gatewayPort + ")...");
                Registry registry = LocateRegistry.getRegistry(gatewayHost, gatewayPort);
                gateway = (GatewayInterface) registry.lookup("gateway");
                System.out.println("[RMIClientService] Ligado à Gateway com sucesso!");
            } catch (Exception e) {
                System.err.println("[RMIClientService] Gateway indisponível na inicialização: " + e.getMessage());
                gateway = null;
            }
        } catch (Exception e) {
            System.err.println("[RMIClientService] Erro ao ler configuração: " + e.getMessage());
            gateway = null;
        }
    }
    
    public List<String> search(String query) throws RemoteException {
        if (gateway == null) {
            throw new RemoteException("Gateway não conectada");
        }
        return gateway.search(query);
    }
    
    public void indexURL(String url) throws RemoteException, InterruptedException {
        if (gateway == null) {
            throw new RemoteException("Gateway não conectada");
        }
        gateway.addUrl(url);
    }

    public List<String> getPagesOrderedByInLinks(int limit, int offset) throws RemoteException {
        if (gateway == null) {
            throw new RemoteException("Gateway não conectada");
        }
        return gateway.getPagesOrderedByInLinks(limit, offset);
    }
    

    public Set<String> getPagesLinkingTo(String url) throws RemoteException {
        if (gateway == null) {
            throw new RemoteException("Gateway não conectada");
        }
        return gateway.getPagesLinkingTo(url);
    }
    
    public SystemStats getSystemStats() throws RemoteException {
        if (gateway == null) {
            throw new RemoteException("Gateway não conectada");
        }
        return gateway.getSystemStats();
    }
    
    public boolean isConnected() {
        return gateway != null;
    }
}
