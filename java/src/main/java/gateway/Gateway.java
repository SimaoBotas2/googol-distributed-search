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

        try {
            // Ligar ao Manager (porta 8182)
            Registry regManager = LocateRegistry.getRegistry("localhost", 8182);
            Manager manager = (Manager) regManager.lookup("manager");

            // Obter lista de Barrels ativos
            List<String> activeBarrels = manager.getActiveBarrels();

            // Ligar a cada Barrel ativo
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
                System.err.println("[Gateway] Nenhum Barrel ativo — a Gateway não pode funcionar.");
            } else {
                System.out.println("[Gateway] Total de Barrels conectados: " + barrels.size());
            }

        } catch (Exception e) {
            System.err.println("[Gateway] Erro ao ligar ao Manager: " + e.getMessage());
            e.printStackTrace();
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
    public List<String> search(String word) throws RemoteException {
        Index barrel = chooseBarrel();
        try {
            return barrel.searchWord(word);
        } catch (Exception e) {
            System.err.println("[Gateway] Falha ao procurar palavra: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        try {
            Gateway gateway = new Gateway();

            int gatewayPort = 8186;
            Registry reg = LocateRegistry.createRegistry(gatewayPort);
            reg.rebind("gateway", gateway);
            System.out.println("[Gateway] Servidor registado na porta " + gatewayPort);

        } catch (RemoteException e) {
            System.err.println("[Gateway] Erro ao iniciar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
