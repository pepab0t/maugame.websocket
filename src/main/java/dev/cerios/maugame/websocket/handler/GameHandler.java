package dev.cerios.maugame.websocket.handler;

import dev.cerios.maugame.websocket.event.RegisterEvent;
import dev.cerios.maugame.websocket.event.UnregisterEvent;
import dev.cerios.maugame.websocket.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class GameHandler extends TextWebSocketHandler {
    private final ApplicationEventPublisher eventPublisher;
    private final GameService gameService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        final String playerId = session.getAttributes().get("playerId").toString();
        if (playerId == null)
            return;

        // TODO validate player

        eventPublisher.publishEvent(new RegisterEvent(this, session, playerId));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        eventPublisher.publishEvent(new UnregisterEvent(this, session.getId()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        gameService.onPlayerMove(message.getPayload());
    }
}
