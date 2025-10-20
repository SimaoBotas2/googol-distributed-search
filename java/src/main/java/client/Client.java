package client;

import gateway.GatewayInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 8184);
            GatewayInterface gateway = (GatewayInterface) registry.lookup("gateway");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Bem-vindo/a ao cliente Googol!");

            while (true) {
                System.out.println("\n Escolha uma opção: 1) Adicionar URL 2) Procurar palavra  3) Exit");
                String option = scanner.nextLine();

                if (option.equals("1")) {
                    System.out.print("Introduza URL para indexar: ");
                    String url = scanner.nextLine();
                    gateway.addUrl(url);
                } else if (option.equals("2")) {
                    System.out.print("Introduza palavra para pesquisar: ");
                    String word = scanner.nextLine();
                    List<String> results = gateway.search(word);
                    if (results.isEmpty()) {
                        System.out.println("Não houve resultados.");
                    } else {
                        System.out.println("Encontrado nos URLs:");
                        for (String u : results) {
                            System.out.println(u);
                        }
                    }
                } else if (option.equals("3")) {
                    System.out.println("A sair.");
                    break;
                } else {
                    System.out.println("Opção inválida, tente novamente");
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
