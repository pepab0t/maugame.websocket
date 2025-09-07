package dev.cerios.maugame.websocket.clientutils;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.Arrays;
import java.util.Collection;
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

    /**
     * Perform websocket handshake and waits for first websocket message.
     *
     * @return future with opened session
     */
    public CompletableFuture<WebSocketSession> handshakeWithCatch() {
        return this.execute(handler, uriTemplate)
                .thenApply(s -> {
                    this.get();
                    return s;
                });
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

    public static String createConnectionUri(int port, String username) {
        return String.format("ws://localhost:%d/game?user=%s", port, username);
    }

    public static String createConnectionUri(int port, String username, String lobbyName, boolean isNew, boolean isPrivate) {
        return String.format(
                "ws://localhost:%d/game?user=%s&lobby=%s&new=%s&private=%s",
                port,
                username,
                lobbyName,
                isNew,
                isPrivate
        );
    }

    public static Predicate<String> createMessageMatcher(Collection<String> containsOneOf) {
        return message -> containsOneOf.stream()
                .map(word -> "\"" + word + "\"")
                .anyMatch(message::contains);
    }

    public static Predicate<String> createMessageMatcher(String... containsOneOf) {
        return createMessageMatcher(Arrays.asList(containsOneOf));
    }
}
