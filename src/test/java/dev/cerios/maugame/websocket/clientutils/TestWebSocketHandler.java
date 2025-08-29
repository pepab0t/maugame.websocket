package dev.cerios.maugame.websocket.clientutils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNullApi;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;

public class TestWebSocketHandler extends TextWebSocketHandler {
    @Getter
    private final List<String> receivedMessages = new LinkedList<>();
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final Predicate<String> messagePredicate;
    private final long timeoutMs;

    public TestWebSocketHandler(Predicate<String> messagePredicate, long timeoutMs) {
        this.messagePredicate = messagePredicate;
        this.timeoutMs = timeoutMs;
    }

    public TestWebSocketHandler(long timeoutMs) {
        this(message -> true, timeoutMs);
    }

    @Override
    protected synchronized void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload;
        if (messagePredicate.test(payload = message.getPayload())) {
            receivedMessages.add(payload);
            queue.add(payload);
        }
    }

    public String get() throws InterruptedException {
        var thisThread = Thread.currentThread();

        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(timeoutMs);
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
