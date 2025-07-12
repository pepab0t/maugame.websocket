package dev.cerios.maugame.websocket.config;

import dev.cerios.maugame.mauengine.game.GameFactory;
import dev.cerios.maugame.websocket.MauSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final MauSettings mauSettings;

    @Bean
    public GameFactory gameFactory() {
        return new GameFactory(new Random());
    }

    @Bean
    public ExecutorService actionService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
