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
        // Simulando 1 Storage Barrel
        Registry registry = LocateRegistry.getRegistry("localhost", 8183);
        Index barrel = (Index) registry.lookup("index");
        barrels.add(barrel);
        System.out.println("Conectado ao barrel na porta 8183"); // depois meter isto de acordo com a porta certa
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    // Escolhe um Barrel aleatório
    // Temporary for testing pq só tenho 1
    private Index chooseBarrel() {
        Random rand = new Random();
        return barrels.get(rand.nextInt(barrels.size()));
    }

    // Adiciona URL para indexação
    public void addUrl(String url) {
        Index barrel = chooseBarrel();
        try {
            barrel.putNew(url);
            System.out.println("URL enviado ao Barrel para indexação: " + url);
        } catch (Exception e) {
            System.err.println("Falha ao adicionar o URL: " + e.getMessage());
        }
    }

    // Pesquisa palavra
    public List<String> search(String word) {
        Index barrel = chooseBarrel();
        try {
            return barrel.searchWord(word);
        } catch (Exception e) {
            System.err.println("Falha ao procurar a palavra: " + e.getMessage());
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
