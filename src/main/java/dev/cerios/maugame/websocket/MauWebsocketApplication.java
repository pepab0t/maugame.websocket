package dev.cerios.maugame.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class MauWebsocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(MauWebsocketApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(MauSettings settings) {
        return (args) -> log.info("started with settings: {}", settings);
    }
}
