package gateway;

import barrel.Index;
import java.rmi.registry.*;
import java.util.*;

public class Gateway {
    private List<Index> barrels;

    public Gateway() {
        barrels = new ArrayList<>();
        try {
            // Simulando 2 Storage Barrels no mesmo host, portas diferentes
            barrels.add((Index) LocateRegistry.getRegistry(8183).lookup("index"));
            barrels.add((Index) LocateRegistry.getRegistry(8184).lookup("index"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Escolhe um Barrel aleatório
    private Index chooseBarrel() {
        Random rand = new Random();
        return barrels.get(rand.nextInt(barrels.size()));
    }

    // Adiciona URL para indexação
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
        Gateway gateway = new Gateway();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Choose: 1) Add URL  2) Search word  3) Exit");
            String option = scanner.nextLine();
            if (option.equals("1")) {
                System.out.print("Enter URL: ");
                String url = scanner.nextLine();
                gateway.addUrl(url);
            } else if (option.equals("2")) {
                System.out.print("Enter word: ");
                String word = scanner.nextLine();
                List<String> results = gateway.search(word);
                System.out.println("Found in URLs:");
                for (String u : results) {
                    System.out.println(u);
                }
            } else if (option.equals("3")) {
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }

        scanner.close();
    }
}
