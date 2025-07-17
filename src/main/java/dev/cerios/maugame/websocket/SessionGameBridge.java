package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.websocket.exception.MauTimeoutException;
import dev.cerios.maugame.websocket.exception.ServerException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SessionGameBridge {

    private final Map<String, CompletableFuture<WebSocketSession>> playerToSession = new ConcurrentHashMap<>();
    private final Map<String, Player> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, Lock> playerLocks = new ConcurrentHashMap<>();

    public WebSocketSession getSession(Player player) {
        long timeout = 300;
        try {
            var sessionFuture = playerToSession.computeIfAbsent(player.getPlayerId(), k -> new CompletableFuture<>());
            return sessionFuture.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new MauTimeoutException("Not initialized in " + timeout + " milliseconds.", e);
        }
    }

    public Player getPlayer(String sessionId) {
        var player = sessionToPlayer.get(sessionId);
        if (player == null) {
            throw new RuntimeException("Unexpected error: player should exist for session " + sessionId);
        }
        return player;
    }

    public Lock getPlayerLock(String playerId) {
        return playerLocks.computeIfAbsent(playerId, k -> new ReentrantLock());
    }

    public Player dropSession(String sessionId) {
        var player = sessionToPlayer.remove(sessionId);
        if (player == null) {
            throw new IllegalStateException("Unexpected error: session does not exist for session " + sessionId);
        }
        playerToSession.remove(player);
        playerLocks.remove(player.getPlayerId());
        return player;
    }

    public void registerSession(Player player, WebSocketSession session) {
        var sessionFuture = playerToSession.computeIfAbsent(player.getPlayerId(), k -> new CompletableFuture<>());
        sessionToPlayer.put(session.getId(), player);
        playerLocks.putIfAbsent(player.getPlayerId(), new ReentrantLock());
        sessionFuture.complete(session);
    }
}
