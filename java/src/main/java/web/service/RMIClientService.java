package web.service;

import gateway.GatewayInterface;
import gateway.SystemStats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Set;

@Service
public class RMIClientService {
    
    @Value("${rmi.gateway.host:localhost}")
    private String gatewayHost;
    
    @Value("${rmi.gateway.port:8186}")
    private int gatewayPort;
    
    private GatewayInterface gateway;
    
    public RMIClientService() {
        // TODO: Simão - Conectar ao Gateway RMI na inicialização
    }
    
    // TODO: Simão - Implementar pesquisa via RMI
    public List<String> search(String query) throws RemoteException {
        return null;
    }
    
    // TODO: Simão - Implementar indexação de URL via RMI
    public void indexURL(String url) throws RemoteException, InterruptedException {
        // TODO: Simão - Implementação
    }
    
    // TODO: Simão - Obter páginas ordenadas por inlinks
    public List<String> getPagesOrderedByInLinks(int limit, int offset) throws RemoteException {
        return null;
    }
    
    // TODO: Simão - Obter páginas que apontam para uma URL
    public Set<String> getPagesLinkingTo(String url) throws RemoteException {
        return null;
    }
    
    // TODO: Simão - Obter estatísticas do sistema
    public SystemStats getSystemStats() throws RemoteException {
        return null;
    }
    
    // TODO: Simão - Verificar se está conectado
    public boolean isConnected() {
        return false;
    }
}
