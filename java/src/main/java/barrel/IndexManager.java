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
                } catch (RemoteException ex) {
                    System.err.println("[IndexManager] Erro ao criar barrel na porta " + p + ": " + ex.getMessage());
                }
            }

            System.out.println("[IndexManager] Todos os barrels foram lançados!");

        } catch (RemoteException e) {
            System.err.println("[IndexManager] Erro ao iniciar Manager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getActiveBarrels() throws RemoteException {
        return new ArrayList<>(activeBarrels);
    }
}
