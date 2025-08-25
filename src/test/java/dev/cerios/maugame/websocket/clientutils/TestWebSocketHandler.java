package dev.cerios.maugame.websocket.clientutils;

import lombok.Getter;
import org.springframework.lang.NonNullApi;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TestWebSocketHandler extends TextWebSocketHandler {
    @Getter
    private final List<String> receivedMessages = new LinkedList<>();

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        receivedMessages.add(message.getPayload());
        queue.add(message.getPayload());
    }

    public String get() throws InterruptedException {
        var thisThread = Thread.currentThread();

        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            thisThread.interrupt();
        });
        return queue.take();
    }

    public List<String> get(int n) throws InterruptedException {
        var responses = new ArrayList<String>(n);

        for (int i = 0; i < n; i++) {
            responses.add(get());
        }
        return responses;
    }
}
