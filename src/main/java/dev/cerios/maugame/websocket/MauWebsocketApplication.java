package dev.cerios.maugame.websocket;

import dev.cerios.maugame.websocket.event.DistributeEvent;
import dev.cerios.maugame.websocket.event.RegisterEvent;
import dev.cerios.maugame.websocket.event.UnregisterEvent;
import dev.cerios.maugame.websocket.service.GameService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class MauWebsocketApplication {
    public static void main(String[] args) {
        var app = SpringApplication.run(MauWebsocketApplication.class, args);
        app.stop();
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationEventPublisher eventPublisher) {
        return args -> {
            eventPublisher.publishEvent(new RegisterEvent(this, null, "jose"));
            eventPublisher.publishEvent(new RegisterEvent(this, null, "joe"));
            eventPublisher.publishEvent(new RegisterEvent(this, null, "john"));
            eventPublisher.publishEvent(new RegisterEvent(this, null, "jacob"));
            eventPublisher.publishEvent(new RegisterEvent(this, null, "jammal"));

            Thread.sleep(1000);
        };
    }

    @EventListener
    public void showDistribute(DistributeEvent event) {
        System.out.println(event.showDetails());
    }

    @EventListener
    public void showRegister(RegisterEvent event) {
        System.out.println(event.showDetails());
    }

    @EventListener
    public void showUnregister(UnregisterEvent event) {
        System.out.println(event.showDetails());
    }
}
