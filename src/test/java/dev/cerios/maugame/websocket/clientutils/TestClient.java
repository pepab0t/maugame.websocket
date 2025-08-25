package dev.cerios.maugame.websocket.clientutils;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TestClient extends StandardWebSocketClient {

    private final String uriTemplate;

    private final TestWebSocketHandler handler = new TestWebSocketHandler();

    public TestClient(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }


    public CompletableFuture<WebSocketSession> handshake() {
        return this.execute(handler, uriTemplate);
    }

    public List<String> getReceivedMessages() {
        return handler.getReceivedMessages();
    }

    public String get() throws InterruptedException {
        return this.handler.get();
    }

    public List<String> get(int n) throws InterruptedException {
        return this.handler.get(n);
    }
}
