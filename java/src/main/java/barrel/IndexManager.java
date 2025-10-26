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
    private List<String> activeBarrels = new ArrayList<>();
    private final boolean debug = true;

    public IndexManager() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        try {
            // Criação e exportação do Manager remoto
            IndexManager manager = new IndexManager();

            int port = 8182;
            Registry reg = LocateRegistry.createRegistry(port);
            reg.rebind("manager", manager);
            System.out.println("[IndexManager] Manager registado na porta " + port);

            // Ler ficheiro de configuração
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
                System.err.println("[IndexManager] Erro ao ler o ficheiro de config " + configFile + ": " + e.getMessage());
                return;
            }

            // Criar e registar os Barrels
            for (int p : ports) {
                try {
                    IndexBarrel barrel = new IndexBarrel();
                    Registry registry = LocateRegistry.createRegistry(p);
                    registry.rebind("index", barrel);
                    manager.activeBarrels.add("localhost:" + p);
                    System.out.println("[IndexManager] Barrel criado na porta " + p);
                } catch (ExportException ex) {
                    System.err.println("[IndexManager] Já existe um barrel na porta " + p);
                    manager.activeBarrels.add("localhost:" + p); //adiciona na mesma, caso queiramos inicializar os barrels manualmente
                } catch (RemoteException ex) {
                    System.err.println("[IndexManager] Erro ao criar barrel na porta " + p + ": " + ex.getMessage());
                }
            }

            System.out.println("[IndexManager] Todos os barrels foram lançados!");

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
    new Thread(() -> {
        while (true) {
            for (int port : ports) {
                String id = "localhost:" + port;
                boolean alive = false;

                try {
                    Registry reg = LocateRegistry.getRegistry("localhost", port);
                    Index barrel = (Index) reg.lookup("index");
                    alive = barrel.ping();
                } catch (Exception ignored) {
                    alive = false;
                }

                synchronized (activeBarrels) {
                    boolean present = activeBarrels.contains(id);

                    if (alive) {
                        if (!present) {
                            activeBarrels.add(id);
                            if (debug) System.out.println("[Monitor] Barrel reativado na porta " + port);
                        }
                    } else {
                        if (present) {
                            activeBarrels.remove(id);
                            System.err.println("[Monitor] Barrel desconectado da porta " + port);
                        }
                    }
                }
            }

            try { Thread.sleep(5000); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }, "manager-monitor").start();
}
}
