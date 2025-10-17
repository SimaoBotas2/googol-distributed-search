package gateway;

import barrel.Index;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;

public class Gateway extends UnicastRemoteObject implements GatewayInterface {
    private List<Index> barrels;

    public Gateway() throws RemoteException {
        super();
        barrels = new ArrayList<>();
        try {
            // Simulando 1 storage Barrels no mesmo host, portas diferentes
            barrels.add((Index) LocateRegistry.getRegistry(8183).lookup("index"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Escolhe um Barrel aleatório
    // Temporary for testing
    private Index chooseBarrel() {
        Random rand = new Random();
        return barrels.get(rand.nextInt(barrels.size()));
    }

    // Adiciona URL para indexação
    // Temporary for testing, dps meter isto para a queue, ou talvez aqui a queue
    public void addUrl(String url) {
        Index barrel = chooseBarrel();
        try {
            barrel.putNew(url);
            System.out.println("URL sent to Barrel for indexing: " + url);
        } catch (Exception e) {
            System.err.println("Failed to add URL: " + e.getMessage());
        }
    }

    // Pesquisa palavra
    public List<String> search(String word) {
        Index barrel = chooseBarrel();
        try {
            return barrel.searchWord(word);
        } catch (Exception e) {
            System.err.println("Failed to search word: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Demo simples de console
    public static void main(String[] args) {
        try{
            Gateway gateway = new Gateway();
            Registry registry = LocateRegistry.createRegistry(8184);
            registry.rebind("gateway", gateway);
        }
        catch (RemoteException e){
            e.printStackTrace();
        }
    }
}
