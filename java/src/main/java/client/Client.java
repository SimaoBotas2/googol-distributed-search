package client;

import gateway.Gateway;
import java.util.*;

public class Client {
    public static void main(String[] args) {
        Gateway gateway = new Gateway(); // Conecta à Gateway
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to mini Googol Client!");

        while (true) {
            System.out.println("\nChoose an option: 1) Add URL  2) Search word  3) Exit");
            String option = scanner.nextLine();

            if (option.equals("1")) {
                System.out.print("Enter URL to index: ");
                String url = scanner.nextLine();
                gateway.addUrl(url);
            } else if (option.equals("2")) {
                System.out.print("Enter word to search: ");
                String word = scanner.nextLine();
                List<String> results = gateway.search(word);
                if (results.isEmpty()) {
                    System.out.println("No results found.");
                } else {
                    System.out.println("Found in URLs:");
                    for (String u : results) {
                        System.out.println(u);
                    }
                }
            } else if (option.equals("3")) {
                System.out.println("Exiting client.");
                break;
            } else {
                System.out.println("Invalid option, try again.");
            }
        }

        scanner.close();
    }
}
