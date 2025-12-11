package web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principal da aplicação Spring Boot.
 * 
 * ANOTACOES:
 * - @SpringBootApplication: Ativa auto-configuração do Spring e component scanning
 * - @ComponentScan: Configura scanning de componentes em múltiplos packages
 * - @EnableScheduling: Ativa o suporte para métodos agendados (@Scheduled)
 *   Isto é necessário para que o StatsWebSocketHandler.pollStats()
 *   seja executado a cada 5 segundos
 */
@SpringBootApplication
@ComponentScan({"web", "external_api", "gateway", "client", "common", "barrel", "downloader"})
@EnableScheduling
public class GooglelApplication {

    public static void main(String[] args) {
        SpringApplication.run(GooglelApplication.class, args);
    }

}
