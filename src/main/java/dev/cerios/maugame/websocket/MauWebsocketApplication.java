package dev.cerios.maugame.websocket;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class MauWebsocketApplication {
    public static void main(String[] args) {
        var app = SpringApplication.run(MauWebsocketApplication.class, args);
    }
}
