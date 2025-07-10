package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.websocket.exception.MauTimeoutException;
import dev.cerios.maugame.websocket.exception.ServerException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.*;

@Component
public class SessionGameBridge {

    private final Map<String, CompletableFuture<WebSocketSession>> playerToSession = new ConcurrentHashMap<>();
    private final Map<String, Player> sessionToPlayer = new ConcurrentHashMap<>();

    public WebSocketSession getSession(Player player) {
        long timeout = 300;
        try {
            var sessionFuture = playerToSession.computeIfAbsent(player.getPlayerId(), k -> new CompletableFuture<>());
            return sessionFuture.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new MauTimeoutException("Not initialized in " + timeout + " milliseconds.", e);
        }
    }

    public void registerSession(Player player, WebSocketSession session) {
        var sessionFuture = playerToSession.computeIfAbsent(player.getPlayerId(), k -> new CompletableFuture<>());
        sessionToPlayer.put(session.getId(), player);
        sessionFuture.complete(session);
    }
}
