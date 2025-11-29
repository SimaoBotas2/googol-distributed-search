package barrel;
import java.io.Serial;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;
import common.Config;

public class IndexManager extends UnicastRemoteObject implements Manager {

    @Serial
    private static final long serialVersionUID = 1L;
    private final List<String> activeBarrels = new ArrayList<>();

    public IndexManager() throws RemoteException {
        super();
    }

    // funções auxiliars para os ips e ports
    private static String hostOf(String addr) { // "ip:port" -> "ip"
        int i = addr.lastIndexOf(':');
        return (i > 0) ? addr.substring(0, i) : addr;
    }
    private static int portOf(String addr) { // "ip:port" -> port
        int i = addr.lastIndexOf(':');
        return (i > 0) ? Integer.parseInt(addr.substring(i + 1)) : -1;
    }

    public static void main(String[] args) {
        try {
            //ler config
            final String managerIp   = Config.get("manager.ip");
            final int    managerPort = Config.getInt("manager.port", 8182);

            // barrels declarados no config (2 máquinas)
            final String barrel1 = Config.get("barrel.1.ip") + ":" + Config.get("barrel.1.port");
            final String barrel2 = Config.get("barrel.2.ip") + ":" + Config.get("barrel.2.port");
            final List<String> configuredBarrels = Arrays.asList(barrel1, barrel2);

            // hostname RMI desta máquina
            System.setProperty("java.rmi.server.hostname", managerIp);

            // registar o manager
            IndexManager manager = new IndexManager();
            Registry reg = LocateRegistry.createRegistry(managerPort);
            reg.rebind("manager", manager);
            System.out.println("[IndexManager] Manager registado em " + managerIp + ":" + managerPort);

            // ver os barrels de acordo com a info
            for (String addr : configuredBarrels) {
                String ip= hostOf(addr);
                int port = portOf(addr);

                boolean found = false;
                try {
                    Registry r = LocateRegistry.getRegistry(ip, port);
                    Index remote = (Index) r.lookup("index");
                    if (remote.ping()) {
                        found = true;
                        synchronized (manager.activeBarrels) {
                            if (!manager.activeBarrels.contains(addr)) manager.activeBarrels.add(addr);
                        }
                        System.out.println("[IndexManager] Barrel já ativo em " + addr);
                    }
                } catch (Exception ignore) { }

                // cria localmente APENAS se o barrel configurado é desta máquina
                if (!found && ip.equals(managerIp)) {
                    try {
                        IndexBarrel barrel = new IndexBarrel();
                        Registry localReg;
                        try {
                            localReg = LocateRegistry.createRegistry(port);
                        } catch (ExportException e) {
                            // registry já existia — reutilizar
                            localReg = LocateRegistry.getRegistry(port);
                        }
                        localReg.rebind("index", barrel);
                        synchronized (manager.activeBarrels) {
                            if (!manager.activeBarrels.contains(addr)) manager.activeBarrels.add(addr);
                        }
                        System.out.println("[IndexManager] Barrel LOCAL criado em " + addr);
                    } catch (RemoteException e) {
                        System.err.println("[IndexManager] Erro a criar barrel local em " + addr + ": " + e.getMessage());
                    }
                }
            }

            // listar estado inicial
            System.out.println("[IndexManager] Barrels ativos no arranque:");
            synchronized (manager.activeBarrels) {
                for (String b : manager.activeBarrels) System.out.println("  -> " + b);
            }

            // -------- iniciar monitorização com base no config --------
            manager.startMonitoring(configuredBarrels);

        } catch (RemoteException e) {
            System.err.println("[IndexManager] Erro ao iniciar Manager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getActiveBarrels() throws RemoteException {
        synchronized (activeBarrels) {
            return new ArrayList<>(activeBarrels);
        }
    }

    // monitoriza exatamente os IP:PORTA do config
    private void startMonitoring(List<String> barrelAddrs) {
        new Thread(() -> {
            Set<String> activeIds;
            synchronized (activeBarrels) {
                activeIds = new HashSet<>(activeBarrels);
            }

            while (true) {
                for (String addr : barrelAddrs) {
                    String ip = hostOf(addr);
                    int port  = portOf(addr);

                    boolean isActive = false;
                    try {
                        Registry reg = LocateRegistry.getRegistry(ip, port);
                        Index barrel = (Index) reg.lookup("index");
                        isActive = barrel.ping();
                    } catch (Exception ignored) { isActive = false; }

                    boolean wasActive;
                    synchronized (activeBarrels) { wasActive = activeIds.contains(addr); }

                    synchronized (activeBarrels) {
                        if (isActive && !wasActive) {
                            activeBarrels.add(addr);
                            activeIds.add(addr);
                            System.out.println("[Monitor] Barrel ativo em " + addr);
                            sincronizarBarrel(ip, port);
                        } else if (!isActive && wasActive) {
                            activeBarrels.remove(addr);
                            activeIds.remove(addr);
                            System.err.println("[Monitor] Barrel caiu em " + addr);
                        }
                    }
                }

                try { Thread.sleep(5000); } catch (InterruptedException e) { break; }
            }
        }, "monitor-thread").start();
    }

    private void sincronizarBarrel(String ipNovo, int novoPort) {
        for (String barrelInfo : getSafeActive()) {
            String ipExistente = hostOf(barrelInfo);
            int portExistente  = portOf(barrelInfo);

            if (ipExistente.equals(ipNovo) && portExistente == novoPort) continue;

            try {
                Registry regNovo = LocateRegistry.getRegistry(ipNovo, novoPort);
                Registry regExistente = LocateRegistry.getRegistry(ipExistente, portExistente);

                Index barrelNovo = (Index) regNovo.lookup("index");
                Index barrelExistente = (Index) regExistente.lookup("index");

                Map<String, Object> dadosSinc = barrelExistente.getSynchronizationData();

                @SuppressWarnings("unchecked")
                Map<String, List<String>> indice = (Map<String, List<String>>) dadosSinc.get("index");
                @SuppressWarnings("unchecked")
                Set<String> urlsVisitadas = (Set<String>) dadosSinc.get("visited");
                @SuppressWarnings("unchecked")
                Queue<String> urlsPendentes = (Queue<String>) dadosSinc.get("pending");

                barrelNovo.synchronizeFrom(indice, urlsVisitadas, urlsPendentes);
                System.out.println("[Monitor] Barrel " + ipNovo + ":" + novoPort +
                        " sincronizado com " + ipExistente + ":" + portExistente);
                return;
            } catch (Exception e) {
                System.err.println("[Monitor] Erro ao sincronizar com " + barrelInfo + ": " + e.getMessage());
            }
        }
    }


    //para handling de acesso as threads
    private List<String> getSafeActive() {
        synchronized (activeBarrels) { return new ArrayList<>(activeBarrels); }
    }
}
