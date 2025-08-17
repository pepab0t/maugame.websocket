package dev.cerios.maugame.websocket.wshandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.websocket.GameService;
import dev.cerios.maugame.websocket.RequestProcessor;
import dev.cerios.maugame.websocket.PlayerSessionStorage;
import dev.cerios.maugame.websocket.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Optional;

import static dev.cerios.maugame.websocket.message.Message.createErrorMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameHandler extends TextWebSocketHandler {

    private final GameService gameService;
    private final RequestProcessor processor;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession s) {
        final var session = new ConcurrentWebSocketSessionDecorator(s, 10_000, 4096);
        var attributes = session.getAttributes();
        String username = attributes.get("user").toString();
        Optional.ofNullable(attributes.get("player"))
                .map(Object::toString)
                .ifPresentOrElse(playerId -> {
                    try {
                        gameService.registerPlayer(username, session, playerId);
                        log.info("{} {} reconnected on session {}", username, playerId, session.getId());
                    } catch (NotFoundException e) {
                        try {
                            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(createErrorMessage(e))));
                        } catch (IOException ex) {
                            log.warn("cannot send websocket message", ex);
                        }
                    }
                }, () -> {
                    gameService.registerPlayer(username, session);
                    log.info("{} joined the game on session {}", username, session.getId());
                });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            gameService.disconnectPlayer(session.getId());
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        processor.process(session, message.getPayload());
    }
}
