package web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@ComponentScan({"web", "external_api", "gateway", "client", "common", "barrel", "downloader"})
@EnableScheduling
public class GooglelApplication {

    public static void main(String[] args) {
        SpringApplication.run(GooglelApplication.class, args);
    }

}
