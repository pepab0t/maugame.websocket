package dev.cerios.maugame.websocket.config;

import dev.cerios.maugame.websocket.interceptor.QueryParamInterceptor;
import dev.cerios.maugame.websocket.wshandler.GameHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebsocketConfig implements WebSocketConfigurer {

    private final GameHandler gameHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameHandler, "/game")
                .setAllowedOrigins("*")
                .addInterceptors(new QueryParamInterceptor());
    }
}
