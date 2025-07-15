package dev.cerios.maugame.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MauWebsocketApplication {
    public static void main(String[] args) {
        var app = SpringApplication.run(MauWebsocketApplication.class, args);
    }
}
