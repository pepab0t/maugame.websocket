package dev.cerios.maugame.websocket.config;

import dev.cerios.maugame.mauengine.game.GameFactory;
import dev.cerios.maugame.websocket.MauSettings;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    @Bean
    public GameFactory gameFactory() {
        return new GameFactory(new Random());
    }

    @Bean
    public ExecutorService virtualExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
