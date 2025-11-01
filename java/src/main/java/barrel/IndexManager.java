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
                    System.out.println("[IndexManager] Barrel criado na porta " + p);
                } catch (ExportException ex) {
                    System.err.println("[IndexManager] Já existe um barrel na porta " + p);
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
        Set<Integer> activeBarrelPorts = new HashSet<>();
        
        while (true) {
            // Verificar cada barrel
            for (int port : ports) {
                boolean isActive = false;
                try {
                    Registry reg = LocateRegistry.getRegistry("localhost", port);
                    Index barrel = (Index) reg.lookup("index");
                    isActive = barrel.ping();
                } catch (Exception ignored) {
                    isActive = false;
                }

                String barrelId = "localhost:" + port;
                boolean wasActive = activeBarrelPorts.contains(port);

                synchronized (activeBarrels) {
                    if (isActive && !wasActive) {
                        // Barrel acabou de ficar ativo - sincronizar
                        activeBarrels.add(barrelId);
                        activeBarrelPorts.add(port);
                        System.out.println("[Monitor] Barrel na porta " + port + " está ativo");
                        sincronizarBarrel(port);
                    }
                    else if (!isActive && wasActive) {
                        // Barrel caiu
                        activeBarrels.remove(barrelId);
                        activeBarrelPorts.remove(port);
                        System.err.println("[Monitor] Barrel na porta " + port + " caiu");
                    }
                }
            }

            try { Thread.sleep(5000); } 
            catch (InterruptedException e) { break; }
        }
    }, "monitor-thread").start();
}

private void sincronizarBarrel(int novoBarrelPort) {
    // Tentar sincronizar com qualquer barrel ativo
    for (String barrelInfo : activeBarrels) {
        String[] parts = barrelInfo.split(":");
        int portExistente = Integer.parseInt(parts[1]);
        
        if (portExistente == novoBarrelPort) continue;

        try {
            // Conectar aos barrels
            Registry regNovo = LocateRegistry.getRegistry("localhost", novoBarrelPort);
            Registry regExistente = LocateRegistry.getRegistry("localhost", portExistente);
            
            Index barrelNovo = (Index) regNovo.lookup("index");
            Index barrelExistente = (Index) regExistente.lookup("index");

            // Pegar dados do barrel existente
            Map<String, Object> dadosSinc = barrelExistente.getSynchronizationData();
            
            // Sincronizar o novo barrel
            @SuppressWarnings("unchecked")
            Map<String, List<String>> indice = (Map<String, List<String>>) dadosSinc.get("index");
            @SuppressWarnings("unchecked")
            Set<String> urlsVisitadas = (Set<String>) dadosSinc.get("visited");
            @SuppressWarnings("unchecked")
            Queue<String> urlsPendentes = (Queue<String>) dadosSinc.get("pending");

            barrelNovo.synchronizeFrom(indice, urlsVisitadas, urlsPendentes);
            System.out.println("[Monitor] Barrel " + novoBarrelPort + " sincronizado com " + portExistente);
            return;
        } catch (Exception e) {
            System.err.println("[Monitor] Erro ao sincronizar com " + barrelInfo + ": " + e.getMessage());
        }
    }
}
}



