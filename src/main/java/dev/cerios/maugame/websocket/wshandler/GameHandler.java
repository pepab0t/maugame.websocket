package dev.cerios.maugame.websocket.wshandler;

import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.websocket.GameManager;
import dev.cerios.maugame.websocket.SessionGameBridge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class GameHandler extends TextWebSocketHandler {

    private final SessionGameBridge bridge;
    private final GameManager gameManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var attributes = session.getAttributes();
        String username = attributes.get("user").toString();
        var player = gameManager.registerPlayer(username);
        bridge.registerSession(player, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    }
}
