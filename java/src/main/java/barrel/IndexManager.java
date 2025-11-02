package barrel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;

public class IndexManager extends UnicastRemoteObject implements Manager {

    private static final long serialVersionUID = 1L;
    private final List<String> activeBarrels = new ArrayList<>();

    public IndexManager() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        try {
            // Define o IP público/local desta máquina
            System.setProperty("java.rmi.server.hostname", "192.168.56.1"); 

            // Criação e exportação do Manager remoto
            IndexManager manager = new IndexManager();
            int port = 8182;
            Registry reg = LocateRegistry.createRegistry(port);
            reg.rebind("manager", manager);
            System.out.println("[IndexManager] Manager registado na porta " + port);

            // Ler ficheiro de configuração (portas dos barrels)
            String configFile = "config.txt";
            List<Integer> ports = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#"))
                        continue;
                    ports.add(Integer.parseInt(line.split("\\s+")[0]));
                }
            } catch (IOException e) {
                System.err.println("[IndexManager] Erro ao ler config " + configFile + ": " + e.getMessage());
                return;
            }

            // Lista de IPs a verificar — inclui o local e o remoto
            List<String> ips = Arrays.asList(
                "localhost", // pc portatil
                "192.168.1.183" // pc fixo
            );

            // Verifica cada porta: se já houver um barrel remoto, não cria localmente
            for (int p : ports) {
                boolean foundRemote = false;

                for (String ip : ips) {
                    try {
                        Registry regRemote = LocateRegistry.getRegistry(ip, p);
                        Index remoteBarrel = (Index) regRemote.lookup("index");

                        if (remoteBarrel.ping()) {
                            System.out.println("[IndexManager] Barrel já existente em " + ip + ":" + p);

                            // Adiciona imediatamente à lista de ativos
                            String barrelId = ip + ":" + p;
                            synchronized (manager.activeBarrels) {
                                if (!manager.activeBarrels.contains(barrelId)) {
                                    manager.activeBarrels.add(barrelId);
                                }
                            }

                            foundRemote = true;
                            break;
                        }
                    } catch (Exception ignored) {
                        // Se der erro, é porque não existe naquele IP
                    }
                }

                // Se não houver nenhum barrel ativo nessa porta, cria localmente
                if (!foundRemote) {
                    try {
                        IndexBarrel barrel = new IndexBarrel();
                        Registry localReg = LocateRegistry.createRegistry(p);
                        localReg.rebind("index", barrel);
                        System.out.println("[IndexManager] Barrel LOCAL criado na porta " + p);

                        String barrelId = "localhost:" + p;
                        synchronized (manager.activeBarrels) {
                            if (!manager.activeBarrels.contains(barrelId)) {
                                manager.activeBarrels.add(barrelId);
                            }
                        }

                    } catch (ExportException ex) {
                        System.err.println("[IndexManager] Já existe barrel local na porta " + p);
                    } catch (RemoteException ex) {
                        System.err.println("[IndexManager] Erro ao criar barrel local na porta " + p + ": " + ex.getMessage());
                    }
                }
            }

            System.out.println("[IndexManager] Verificacao concluida. Barrels ativos:");
            for (String b : manager.activeBarrels)
                System.out.println("  -> " + b);

            System.out.println("[IndexManager] Iniciando monitorizacao...");
            manager.startMonitoring(ports);

        } catch (RemoteException e) {
            System.err.println("[IndexManager] Erro ao iniciar Manager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getActiveBarrels() throws RemoteException {
        return new ArrayList<>(activeBarrels);
    }

    private void startMonitoring(List<Integer> ports) {
        // IPs a monitorizar
        List<String> ips = Arrays.asList("192.168.1.183", "localhost");

        new Thread(() -> {
            Set<String> activeIds = new HashSet<>(activeBarrels);

            while (true) {
                for (String ip : ips) {
                    for (int port : ports) {
                        boolean isActive = false;
                        try {
                            Registry reg = LocateRegistry.getRegistry(ip, port);
                            Index barrel = (Index) reg.lookup("index");
                            isActive = barrel.ping();
                        } catch (Exception ignored) {
                            isActive = false;
                        }

                        String id = ip + ":" + port;
                        boolean wasActive = activeIds.contains(id);

                        synchronized (activeBarrels) {
                            if (isActive && !wasActive) {
                                activeBarrels.add(id);
                                activeIds.add(id);
                                System.out.println("[Monitor] Barrel ativo em " + id);
                                sincronizarBarrel(ip, port);
                            } else if (!isActive && wasActive) {
                                activeBarrels.remove(id);
                                activeIds.remove(id);
                                System.err.println("[Monitor] Barrel caiu em " + id);
                            }
                        }
                    }
                }

                try { Thread.sleep(5000); }
                catch (InterruptedException e) { break; }
            }
        }, "monitor-thread").start();
    }

    private void sincronizarBarrel(String ipNovo, int novoPort) {
        for (String barrelInfo : activeBarrels) {
            String[] parts = barrelInfo.split(":");
            String ipExistente = parts[0];
            int portExistente = Integer.parseInt(parts[1]);

            if (ipExistente.equals(ipNovo) && portExistente == novoPort)
                continue;

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
}
