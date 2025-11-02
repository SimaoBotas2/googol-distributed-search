package client;

import gateway.GatewayInterface;
import gateway.SystemStats;
import barrel.Index;
import barrel.Manager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Client {

    private static Thread statsThread;
    private static volatile boolean statsOn;
    private static GatewayInterface gateway;
    private static int REFRESH_TIME = 8000;

    public static void main(String[] args) {
        try {
            //TODO RECEBER OS ARGS DE UM FILE, SÓ FALTA ISSO
            // Ligação à Gateway
            Registry registry = LocateRegistry.getRegistry("192.168.1.183", 8186);
            gateway = (GatewayInterface) registry.lookup("gateway");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Bem-vindo/a ao cliente Googol!");

            while (true) {
                System.out.println("\n[Client] Escolha uma opção:");
                System.out.println("0) Sair");
                System.out.println("1) Adicionar URL");
                System.out.println("2) Procurar palavra");
                System.out.println("3) [DEBUG] Verificar sincronização dos Barrels");
                System.out.println("4) Páginas ordenadas por número de ligações recebidas (backlinks)");
                System.out.println("5) Consultar páginas que apontam para uma URL");
                System.out.println("6) " + (statsOn ? "Desligar" : "Ligar") + " estatísticas em tempo real");

                String option = scanner.nextLine();

                if (option.equals("0")) {
                    System.out.println("[Client] A sair...");
                    desligarEstatisticas();
                    break;

                } else if (option.equals("1")) {
                    System.out.print("[Client] Introduza URL para indexar: ");
                    String url = scanner.nextLine();
                    gateway.addUrl(url);

                } else if (option.equals("2")) {
                    System.out.print("[Client] Introduza as palavras para pesquisar (separadas por espaço): ");
                    String query = scanner.nextLine();
                    List<String> results = gateway.search(query);
                    if (results.isEmpty()) {
                        System.out.println("[Client] Nao houve resultados.");
                    } else {
                        int pageSize = 10;
                        int index = 0;
                        while (index < results.size()) {
                            int end = Math.min(index + pageSize, results.size());
                            System.out.println("\n[Client] Resultados " + (index + 1) + "--" + end + " de " + results.size() + ":");
                            for (int i = index; i < end; i++) {
                                System.out.println(" - " + results.get(i));
                            }
                            if (end >= results.size()) break;
                            System.out.print("\n[Client] Pretende ver mais resultados? (s/n): ");
                            String cmd = scanner.nextLine().trim();
                            if (!cmd.equalsIgnoreCase("s")) break;
                            index += pageSize;
                        }
                    }

                } else if (option.equals("3")) {
                    // Verificar sincronização dos barrels
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
                            Map<String, List<String>> snapshot = barrel.getIndexSnapshot();

                            System.out.println("\n==== BARREL " + port + " ====");
                            System.out.println("Total de palavras indexadas: " + snapshot.size());
                            snapshot.entrySet().stream()
                                    .limit(10)
                                    .forEach(e ->
                                            System.out.println(e.getKey() + " -> " + e.getValue()));
                        }

                    } catch (Exception ex) {
                        System.err.println("[DEBUG] Erro ao inspecionar os Barrels: " + ex.getMessage());
                        ex.printStackTrace();
                    }

                } else if (option.equals("4")) {
                    int pageSize = 10;
                    int offset = 0;
                    while (true) {
                        List<String> ranking = gateway.getPagesOrderedByInLinks(pageSize, offset);
                        if (ranking == null || ranking.isEmpty()) {
                            if (offset == 0) {
                                System.out.println("[Client] Nao existem paginas registadas.");
                            }
                            break;
                        }
                        System.out.println("\n[Client] Top " + (offset + 1) + "--" + (offset + ranking.size())
                                + " paginas por número de ligacoes recebidas (backlinks):");
                        int pos = offset + 1;
                        for (String page : ranking) {
                            System.out.println(pos++ + ". " + page);
                        }
                        if (ranking.size() < pageSize) break;
                        System.out.print("\n[Client] Pretende ver mais resultados? (s/n): ");
                        String cmd = scanner.nextLine().trim();
                        if (!cmd.equalsIgnoreCase("s")) break;
                        offset += pageSize;
                    }

                } else if (option.equals("5")) {
                    System.out.print("[Client] Introduza o URL para consultar backlinks: ");
                    String urlConsulta = scanner.nextLine().trim();
                    if (urlConsulta.isEmpty()) {
                        System.out.println("[Client] URL não pode ser vazio.");
                    } else {
                        Set<String> backlinks = gateway.getPagesLinkingTo(urlConsulta);
                        if (backlinks == null || backlinks.isEmpty()) {
                            System.out.println("[Client] Nenhuma página aponta para " + urlConsulta);
                        } else {
                            System.out.println("\n[Client] Páginas que apontam para " + urlConsulta + ":");
                            for (String src : backlinks)
                                System.out.println(" - " + src);
                        }
                    }

                } else if (option.equals("6")) {
                    if (statsOn) desligarEstatisticas();
                    else ligarEstatisticas();

                } else {
                    System.out.println("[Client] Opção inválida, tente novamente.");
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Controlo de estatisticas
    private static void ligarEstatisticas() {
        if (statsOn) return;
        statsOn = true;
        statsThread = new Thread(Client::atualizarEstatisticas);
        statsThread.setDaemon(true);
        statsThread.start();
        System.out.println("[Client] Estatísticas em tempo real LIGADAS.");
    }

    private static void desligarEstatisticas() {
        if (!statsOn) return;
        statsOn = false;
        System.out.println("[Client] Estatísticas em tempo real DESLIGADAS.");
    }

    // Thread que periodicamente atualiza e imprime as estatísticas do sistema 
private static void atualizarEstatisticas() {
    while (statsOn) {
        try {
            SystemStats stats = gateway.getSystemStats();
            System.out.println(stats);

            // Espera REFRESH_TIME (definido no private da classe)
            if (statsOn) {
                Thread.sleep(REFRESH_TIME);
            }


        } catch (Exception e) {
            System.err.println("[Estatísticas] Erro ao obter dados: " + e.getMessage());
            try { Thread.sleep(5000); } 
            catch (InterruptedException ignored) {}
        }
    }
}

}
