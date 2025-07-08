package dev.cerios.maugame.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.websocket.event.RegisterEvent;
import dev.cerios.maugame.websocket.event.UnregisterEvent;
import dev.cerios.maugame.websocket.request.RequestProcessor;
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

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    }
}
