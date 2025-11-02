package gateway;

import barrel.Index;
import barrel.Manager;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;

public class Gateway extends UnicastRemoteObject implements GatewayInterface {

    private final Map<Index, String> barrelMap = new HashMap<>();
    private volatile Manager manager;

    public Gateway() throws RemoteException {
        super(8186);

        // Thread que mantém Manager e Barrels sincronizados
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    // Tentar ligar ao Manager se não houver ligação
                    if (manager == null) {
                        try {
                            System.out.println("[Gateway] A tentar ligar ao Manager na porta 8182...");
                            Registry regManager = LocateRegistry.getRegistry("192.168.1.66", 8182);
                            manager = (Manager) regManager.lookup("manager");
                            System.out.println("[Gateway] Ligado ao Manager!");
                        } catch (Exception e) {
                            System.err.println("[Gateway] Manager indisponível: " + e.getMessage());
                            manager = null;
                            Thread.sleep(3000);
                            continue;
                        }
                    }
                    //atualizar os barrels
                    atualizarBarrels();

                    Thread.sleep(5000);

                } catch (InterruptedException ie) {
                    break;
                } catch (Exception e) {
                    System.err.println("[Gateway] Erro no monitor: " + e.getMessage());
                    try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                }
            }
        });

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /** Atualiza a lista de Barrels ativos via Manager */
    private void atualizarBarrels() {
        if (manager == null) return;

        try {
            List<String> activeBarrels = manager.getActiveBarrels();
            synchronized (barrelMap) {

                // Adicionar novos
                for (String info : activeBarrels) {
                    if (!barrelMap.containsValue(info)) {
                        ligarBarrel(info);
                    }
                }

                // Remover os que já não estão ativos
                barrelMap.entrySet().removeIf(entry -> !activeBarrels.contains(entry.getValue()));
            }

            System.out.println("[Gateway] Lista de Barrels atualizada. Total: " + barrelMap.size());

        } catch (RemoteException re) {
            System.err.println("[Gateway] Perda de conexao com Manager: " + re.getMessage());
            manager = null;
        } catch (Exception e) {
            System.err.println("[Gateway] Erro ao atualizar Barrels: " + e.getMessage());
        }
    }

    /** Tenta ligar a um novo Barrel (se não existir ainda) */
    private void ligarBarrel(String info) {
        try {
            String[] parts = info.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            Registry reg = LocateRegistry.getRegistry(host, port);
            Index barrel = (Index) reg.lookup("index");

            synchronized (barrelMap) {
                barrelMap.put(barrel, info);
            }

            System.out.println("[Gateway] Conectado ao Barrel em " + info);
        } catch (Exception ex) {
            System.err.println("[Gateway] Falha ao conectar ao Barrel " + info + ": " + ex.getMessage());
        }
    }

    private Index chooseBarrel() {
        Random rand = new Random();
        synchronized (barrelMap) {
            if (barrelMap.isEmpty()) return null;
            List<Index> lista = new ArrayList<>(barrelMap.keySet());
            return lista.get(rand.nextInt(lista.size()));
        }
    }

    @Override
    public void addUrl(String url) throws RemoteException, InterruptedException {
        if (url == null || url.isBlank()) return;
        boolean enviado = false;
        long timeWait = 1000;

        while (!enviado) {
            Index barrel = chooseBarrel();

            if (barrel == null) {
                System.err.println("[Gateway] Nenhum Barrel disponível. A aguardar reconexao...");
                Thread.sleep(2000);
                continue;
            }

            try {
                barrel.putNew(url);
                System.out.println("[Gateway] URL enviada ao Barrel para indexacao: " + url);
                enviado = true;

            } catch (Exception e) {
                System.err.println("[Gateway] Falha ao adicionar URL (" + url + "): " + e.getMessage());
                Thread.sleep(timeWait);
            }
        }
    }

    @Override
    public List<String> search(String query) throws RemoteException {
        if (query == null || query.isBlank()) return new ArrayList<>();
        String[] terms = query.trim().toLowerCase().split("\\s+");
        Set<String> deduped = new LinkedHashSet<>();

        synchronized (barrelMap) {
            for (String term : terms) {
                for (Index barrel : barrelMap.keySet()) {
                    try {
                        List<String> partialResults = barrel.searchWord(term);
                        deduped.addAll(partialResults);
                    } catch (Exception e) {
                        System.err.println("[Gateway] Falha ao pesquisar no Barrel: " + e.getMessage());
                    }
                }
            }
        }
        return new ArrayList<>(deduped);
    }

    @Override
    public List<String> getPagesOrderedByInLinks(int limit, int offset) throws RemoteException {
        Index barrel = chooseBarrel();
        if (barrel == null) throw new RemoteException("[Gateway] Nenhum Barrel ativo.");
        return barrel.getPagesOrderedByInLinks(limit, offset);
    }

    @Override
    public Set<String> getPagesLinkingTo(String url) throws RemoteException {
        Index barrel = chooseBarrel();
        if (barrel == null) throw new RemoteException("[Gateway] Nenhum Barrel ativo.");
        try {
            return barrel.getPagesLinkingTo(url);
        } catch (Exception e) {
            throw new RemoteException("[Gateway] Erro ao obter paginas que apontam para " + url + ": " + e.getMessage());
        }
    }

    @Override
    public SystemStats getSystemStats() throws RemoteException {
    SystemStats stats = new SystemStats();
    Map<String, Integer> porBarrel = new LinkedHashMap<>();
    stats.tamanhoPorBarrel = porBarrel;

    synchronized (barrelMap) {
        stats.activeBarrels = barrelMap.size();

        Set<String> urlsUnicos = new HashSet<>();

        for (Map.Entry<Index, String> entry : barrelMap.entrySet()) {
            Index barrel = entry.getKey();
            String info = entry.getValue();

            try {
                Map<String, List<String>> snapshot = barrel.getIndexSnapshot();
                long totalPalavras = snapshot.values().stream().mapToInt(List::size).sum();
                urlsUnicos.addAll(snapshot.values().stream().flatMap(List::stream).toList());
                porBarrel.put(info, snapshot.size());
                stats.totalPalavras += totalPalavras;
            } catch (Exception e) {
                System.err.println("[Gateway] Erro ao recolher estatisticas do Barrel " + info + ": " + e.getMessage());
            }
        }

        stats.totalUrls = urlsUnicos.size();
    }

    return stats;
    }   



    public static void main(String[] args) throws InterruptedException {
        while (true) {
            try {
                System.setProperty("java.rmi.server.hostname", "192.168.1.183");
                Gateway gateway = new Gateway();
                int gatewayPort = 8186;
                Registry reg = LocateRegistry.createRegistry(gatewayPort);
                reg.rebind("gateway", gateway);
                System.out.println("[Gateway] Registada na porta " + gatewayPort);
                break;
            } catch (RemoteException e) {
                System.err.println("[Gateway] Erro ao iniciar: " + e.getMessage());
                Thread.sleep(3000);
            }
        }
    }
}
