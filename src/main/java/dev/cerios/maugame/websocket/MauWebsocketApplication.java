package dev.cerios.maugame.websocket;

import dev.cerios.maugame.websocket.service.GameService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MauWebsocketApplication {
    public static void main(String[] args) {
        var app = SpringApplication.run(MauWebsocketApplication.class, args);
        app.stop();
    }

    @Bean
    public CommandLineRunner commandLineRunner(GameService gameService) {
        return args -> {
            gameService.registerPlayer("jose");
            gameService.registerPlayer("joe");
            gameService.registerPlayer("john");
            gameService.registerPlayer("juan");
            gameService.registerPlayer("jacob");
        };
    }
}
