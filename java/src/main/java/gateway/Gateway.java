package gateway;

import barrel.Index;
import barrel.Manager;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;

public class Gateway extends UnicastRemoteObject implements GatewayInterface {

    private final List<Index> barrels = new ArrayList<>();

    public Gateway() throws RemoteException {
        super();

        while (true) {
            try {
                // Ligar ao Manager (porta 8182)
                System.out.println("[Gateway] A tentar ligar ao Manager na porta 8182...");
                Registry regManager = LocateRegistry.getRegistry("localhost", 8182);
                Manager manager = (Manager) regManager.lookup("manager");
                System.out.println("[Gateway] Ligado ao Manager!");

                // Tentar ligar aos Barrels
                while (barrels.isEmpty()) {
                    try {
                        System.out.println("[Gateway] A obter lista de Barrels ativos...");
                        List<String> activeBarrels = manager.getActiveBarrels();

                        for (String info : activeBarrels) {
                            String[] parts = info.split(":");
                            String host = parts[0];
                            int port = Integer.parseInt(parts[1]);

                            try {
                                Registry reg = LocateRegistry.getRegistry(host, port);
                                Index barrel = (Index) reg.lookup("index");
                                barrels.add(barrel);
                                System.out.println("[Gateway] Conectado ao Barrel em " + info);
                            } catch (Exception ex) {
                                System.err.println("[Gateway] Falha ao conectar ao Barrel " + info + ": " + ex.getMessage());
                            }
                        }

                        if (barrels.isEmpty()) {
                            System.err.println("[Gateway] Nenhum Barrel ativo. A tentar novamente em 5 segundos...");
                            Thread.sleep(5000);
                        } else {
                            System.out.println("[Gateway] Total de Barrels conectados: " + barrels.size());
                        }

                    } catch (Exception e) {
                        System.err.println("[Gateway] Erro ao obter lista de Barrels: " + e.getMessage());
                        Thread.sleep(5000);
                    }
                }

                // Se chegou aqui, conseguiu ligar-se ao manager e a pelo menos um barrel
                break;

            } catch (Exception e) {
                System.err.println("[Gateway] Falha ao ligar ao Manager: " + e.getMessage());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private Index chooseBarrel() {
        Random rand = new Random();
        return barrels.get(rand.nextInt(barrels.size()));
    }

    @Override
    public void addUrl(String url) throws RemoteException {
        Index barrel = chooseBarrel();
        try {
            barrel.putNew(url);
            System.out.println("[Gateway] URL enviada ao Barrel para indexação: " + url);
        } catch (Exception e) {
            System.err.println("[Gateway] Falha ao adicionar URL: " + e.getMessage());
        }
    }

    @Override
    public List<String> search(String query) throws RemoteException {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }
        String[] terms = query.trim().toLowerCase().split("\\s+");
        Set<String> deduped = new LinkedHashSet<>();
        for (String term : terms) {
            for (Index barrel : barrels) {
                try {
                    List<String> partialResults = barrel.searchWord(term);
                    deduped.addAll(partialResults);
                } catch (Exception e) {
                    System.err.println("[Gateway] Falha ao pesquisar no barrel: " + e.getMessage());
                }
            }
        }
        return new ArrayList<>(deduped);
    }

    @Override
    public List<String> getPagesOrderedByInLinks(int limit, int offset) throws RemoteException {
        Index barrel = chooseBarrel();
        return barrel.getPagesOrderedByInLinks(limit, offset);
    }

    @Override
    public Set<String> getPagesLinkingTo(String url) throws RemoteException {
        Index barrel = chooseBarrel();
        try {
            return barrel.getPagesLinkingTo(url);
        } catch (Exception e) {
            throw new RemoteException("[Gateway] Erro ao obter páginas que apontam para " + url + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            try {
                Gateway gateway = new Gateway();
                int gatewayPort = 8186;
                Registry reg = LocateRegistry.createRegistry(gatewayPort);
                reg.rebind("gateway", gateway);
                System.out.println("[Gateway] Servidor registado na porta " + gatewayPort);
                break;
            } catch (RemoteException e) {
                System.err.println("[Gateway] Erro ao iniciar: " + e.getMessage());
                Thread.sleep(3000);
            }
        }
    }
}
