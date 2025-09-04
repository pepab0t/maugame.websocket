package dev.cerios.maugame.websocket.clientutils;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class TestClient extends StandardWebSocketClient {

    private final String uriTemplate;

    private final TestWebSocketHandler handler;

    public TestClient(String uriTemplate, Predicate<String> messagePredicate, long timeoutMs) {
        this.uriTemplate = uriTemplate;
        this.handler = new TestWebSocketHandler(messagePredicate, timeoutMs);
    }

    public TestClient(String uriTemplate, long timeoutMs) {
        this.uriTemplate = uriTemplate;
        this.handler = new TestWebSocketHandler(timeoutMs);
    }


    public CompletableFuture<WebSocketSession> handshake() {
        return this.execute(handler, uriTemplate);
    }

    public List<String> getReceivedMessages() {
        return handler.getReceivedMessages();
    }

    public String get() {
        try {
            return this.handler.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> get(int n) {
        try {
            return this.handler.get(n);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
