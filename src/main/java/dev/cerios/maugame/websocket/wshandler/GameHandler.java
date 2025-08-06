package dev.cerios.maugame.websocket.wshandler;

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
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameHandler extends TextWebSocketHandler {

    private final GameService gameService;
    private final RequestProcessor processor;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws NotFoundException, GameException {
        var attributes = session.getAttributes();
        String username = attributes.get("user").toString();
        var playerId = Optional.ofNullable(attributes.get("player")).map(Object::toString);
        if (playerId.isPresent()) {
            gameService.registerPlayer(username, session, playerId.get());
            log.info("{} {} reconnected on session {}", username, playerId.get(), session.getId());
        } else {
            gameService.registerPlayer(username, session);
            log.info("{} joined the game on session {}", username, session.getId());
        }
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
