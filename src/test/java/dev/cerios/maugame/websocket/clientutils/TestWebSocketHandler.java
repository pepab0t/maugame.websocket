package dev.cerios.maugame.websocket.clientutils;

import lombok.Getter;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.LinkedList;
import java.util.List;

public class TestWebSocketHandler extends TextWebSocketHandler {
    @Getter
    private final List<String> receivedMessages = new LinkedList<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        receivedMessages.add(message.getPayload());
    }
}
