package dev.cerios.maugame.websocket.config;

import dev.cerios.maugame.mauengine.game.GameFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class AppConfig {

    @Bean
    public GameFactory gameFactory() {
        return new GameFactory();
    }

    @Bean
    public Map<String, WebSocketSession> playerToSession() {
        return new ConcurrentHashMap<>();
    }
}
