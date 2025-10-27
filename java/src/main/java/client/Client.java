package client;

import gateway.GatewayInterface;
import barrel.Index;
import barrel.Manager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Client {
    public static void main(String[] args) {
        try {
            // Ligação à Gateway
            Registry registry = LocateRegistry.getRegistry("localhost", 8186);
            GatewayInterface gateway = (GatewayInterface) registry.lookup("gateway");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Bem-vindo/a ao cliente Googol!");

            while (true) {
                System.out.println("\n[Client] Escolha uma opcao:");
                System.out.println("0) Sair");
                System.out.println("1) Adicionar URL");
                System.out.println("2) Procurar palavra");
                System.out.println("3) [DEBUG] Verificar sincronizacao dos Barrels");

                String option = scanner.nextLine();

                if (option.equals("0")) {
                    System.out.println("[Client] A sair.");
                    break;
                }
                else if (option.equals("1")) {
                    System.out.print("[Client] Introduza URL para indexar: ");
                    String url = scanner.nextLine();
                    gateway.addUrl(url);

                } else if (option.equals("2")) {
                    System.out.print("[Client] Introduza palavra para pesquisar: ");
                    String word = scanner.nextLine();
                    List<String> results = gateway.search(word);
                    if (results.isEmpty()) {
                        System.out.println("[Client] Nao houve resultados.");
                    } else {
                        System.out.println("[Client] Encontrado nos URLs:");
                        for (String u : results) {
                            System.out.println(" - " + u);
                        }
                    }


                } else if (option.equals("3")) {
                    // Verificar se os barrels estao sincronizados
                    try {
                        Registry regManager = LocateRegistry.getRegistry("localhost", 8182);
                        Manager manager = (Manager) regManager.lookup("manager");
                        List<String> active = manager.getActiveBarrels();

                        for (String info : active) {
                            String[] parts = info.split(":");
                            String host = parts[0];
                            int port = Integer.parseInt(parts[1]);

                            Registry reg = LocateRegistry.getRegistry(host, port);
                            Index barrel = (Index) reg.lookup("index");

                            Map<String, List<String>> snapshot = barrel.getIndexSnapshot(); //devolve um hash map com as estatiscas
                            System.out.println("\n==== BARREL " + port + " ====");
                            System.out.println("Total de palavras indexadas: " + snapshot.size());
                            snapshot.entrySet().stream() //printamos as 10 primeiras palavras
                                    .limit(10)
                                    .forEach(e ->
                                        System.out.println(e.getKey() + " -> " + e.getValue()));
                        }

                    } catch (Exception ex) {
                        System.err.println("[DEBUG] Erro ao inspecionar os Barrels: " + ex.getMessage());
                        ex.printStackTrace();
                    }

                } else {
                    System.out.println("[Client] Opção inválida, tente novamente.");
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
