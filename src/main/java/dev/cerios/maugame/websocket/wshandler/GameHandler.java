package dev.cerios.maugame.websocket.wshandler;

import dev.cerios.maugame.websocket.GameService;
import dev.cerios.maugame.websocket.RequestProcessor;
import dev.cerios.maugame.websocket.SessionGameBridge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameHandler extends TextWebSocketHandler {

    private final SessionGameBridge bridge;
    private final GameService gameService;
    private final RequestProcessor processor;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var attributes = session.getAttributes();
        String username = attributes.get("user").toString();
        var player = gameService.registerPlayer(username);
        bridge.registerSession(player, session);

        log.info("{} joined the game on session {}", username, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            var player = bridge.dropSession(session.getId());
            gameService.disconnectPlayer(player);
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        processor.process(session, message.getPayload());
    }
}
