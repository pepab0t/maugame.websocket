package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.websocket.event.ClearPlayerEvent;
import dev.cerios.maugame.websocket.exception.MauTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class SessionGameBridge {

    private final Map<String, CompletableFuture<WebSocketSession>> playerToSession = new ConcurrentHashMap<>();
    private final Map<String, Player> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, PlayerConcurrentSources> playerLocks = new ConcurrentHashMap<>();

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

    public PlayerConcurrentSources getPlayerSources(String playerId) {
        return playerLocks.computeIfAbsent(playerId, k -> PlayerConcurrentSources.create());
    }

    public Player dropSession(String sessionId) {
        var player = sessionToPlayer.remove(sessionId);
        if (player == null) {
            throw new IllegalStateException("Unexpected error: session does not exist for session " + sessionId);
        }
        Optional.ofNullable(playerToSession.remove(player.getPlayerId()))
                .flatMap(future -> Optional.ofNullable(future.getNow(null)))
                .ifPresent(session -> {
                    try {
                        session.close();
                        log.info("session {} closed", sessionId);
                    } catch (IOException e) {
                        log.debug("error when closing session", e);
                    }
                });
        playerLocks.remove(player.getPlayerId());
        return player;
    }

    public void registerSession(Player player, WebSocketSession session) {
        var sessionFuture = playerToSession.computeIfAbsent(player.getPlayerId(), k -> new CompletableFuture<>());
        sessionToPlayer.put(session.getId(), player);
        playerLocks.putIfAbsent(player.getPlayerId(), PlayerConcurrentSources.create());
        sessionFuture.complete(session);
    }

    @EventListener
    public void handleClearEvent(ClearPlayerEvent event) {
        dropSession(event.getSessionId());
    }

    public record PlayerConcurrentSources(Lock lock, BlockingQueue<Action> queue) {
        public static PlayerConcurrentSources create() {
            return new PlayerConcurrentSources(new ReentrantLock(), new LinkedBlockingQueue<>());
        }
    }
}
