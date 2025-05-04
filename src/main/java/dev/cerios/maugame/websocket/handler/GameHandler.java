package dev.cerios.maugame.websocket.handler;

import dev.cerios.maugame.websocket.event.RegisterEvent;
import dev.cerios.maugame.websocket.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class GameHandler extends TextWebSocketHandler {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        final String playerId = session.getAttributes().get("playerId").toString();
        if (playerId == null)
            return;

        // TODO validate player

        eventPublisher.publishEvent(new RegisterEvent(this, session, playerId));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
    }
}
