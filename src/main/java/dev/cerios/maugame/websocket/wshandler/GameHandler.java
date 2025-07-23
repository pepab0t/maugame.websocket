package dev.cerios.maugame.websocket.wshandler;

import dev.cerios.maugame.websocket.GameService;
import dev.cerios.maugame.websocket.SessionGameBridge;
import dev.cerios.maugame.websocket.request.MoveProcessor;
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
    private final GameService gameService;
    private final MoveProcessor processor;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var attributes = session.getAttributes();
        String username = attributes.get("user").toString();
        var player = gameService.registerPlayer(username);
        bridge.registerSession(player, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        var player = bridge.dropSession(session.getId());
        gameService.disconnectPlayer(player);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        processor.process(session, message.getPayload());
    }
}
