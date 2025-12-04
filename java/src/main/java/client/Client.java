package client;

import gateway.GatewayInterface;
import gateway.SystemStats;
import common.Config;
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
            // Ler configuração do Config
            String gatewayHost = Config.get("gateway.host");
            int gatewayPort = Config.getInt("gateway.port", 8186);
            
            // Tentativas de ligação à Gateway
            while (gateway == null) {
                try {
                    System.out.println("[Client] A tentar ligar à Gateway (" + gatewayHost + ":" + gatewayPort + ")...");
                    Registry registry = LocateRegistry.getRegistry(gatewayHost, gatewayPort);
                    gateway = (GatewayInterface) registry.lookup("gateway");
                    System.out.println("[Client] Ligado à Gateway com sucesso!");
                } catch (Exception e) {
                    System.err.println("[Client] Gateway indisponível: " + e.getMessage());
                    try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                }
            }

            Scanner scanner = new Scanner(System.in);
            System.out.println("Bem-vindo/a ao cliente Googol!");

            while (true) {
                System.out.println("\n[Client] Escolha uma opção:");
                System.out.println("0) Sair");
                System.out.println("1) Adicionar URL");
                System.out.println("2) Procurar palavra");
                System.out.println("3) Páginas ordenadas por número de ligacoes recebidas (backlinks)");
                System.out.println("4) Consultar paginas que apontam para uma URL");
                System.out.println("5) " + (statsOn ? "Desligar" : "Ligar") + " estatísticas em tempo real");

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
                    gateway.SearchResult result = gateway.search(query, Integer.MAX_VALUE, 0);
                    List<String> results = result.getResults();
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
                                + " paginas por numero de ligacoes recebidas (backlinks):");
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

                } else if (option.equals("4")) {
                    System.out.print("[Client] Introduza o URL para consultar backlinks: ");
                    String urlConsulta = scanner.nextLine().trim();
                    if (urlConsulta.isEmpty()) {
                        System.out.println("[Client] URL nao pode ser vazio.");
                    } else {
                        Set<String> backlinks = gateway.getPagesLinkingTo(urlConsulta);
                        if (backlinks == null || backlinks.isEmpty()) {
                            System.out.println("[Client] Nenhuma pagina aponta para " + urlConsulta);
                        } else {
                            System.out.println("\n[Client] Paginas que apontam para " + urlConsulta + ":");
                            for (String src : backlinks)
                                System.out.println(" - " + src);
                        }
                    }

                } else if (option.equals("5")) {
                    if (statsOn) desligarEstatisticas();
                    else ligarEstatisticas();

                } else {
                    System.out.println("[Client] Opcao invalida, tente novamente.");
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Controlo de estatísticas
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
