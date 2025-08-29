package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.websocket.exception.MauTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PlayerSessionStorage {

    private final Map<String, CompletableFuture<WebSocketSession>> playerToSession = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, Game> playerToGame = new ConcurrentHashMap<>();

    private final Map<String, PlayerConcurrentSources> playerLocks = new ConcurrentHashMap<>();

    private final long futureSessionTimeoutMs = 300;

    public WebSocketSession getSession(String playerId) {
        try {
            var sessionFuture = playerToSession.computeIfAbsent(playerId, k -> new CompletableFuture<>());
            return sessionFuture.get(futureSessionTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new MauTimeoutException("Not initialized in " + futureSessionTimeoutMs + " milliseconds.", e);
        }
    }

    public Optional<WebSocketSession> getSessionInstant(String playerId) {
        return Optional.ofNullable(playerToSession.get(playerId))
                .map(cf -> cf.getNow(null));
    }

    public String getPlayer(String sessionId) {
        var playerId = sessionToPlayer.get(sessionId);
        if (playerId == null) {
            throw new RuntimeException("Unexpected error: player should exist for session " + sessionId);
        }
        return playerId;
    }

    public PlayerConcurrentSources getPlayerSources(String playerId) {
        return playerLocks.computeIfAbsent(playerId, ignore -> PlayerConcurrentSources.create());
    }

    private String dropSession(String sessionId) {
        var playerId = sessionToPlayer.remove(sessionId);
        if (playerId == null) {
            throw new IllegalStateException("Unexpected error: player does not exist for session " + sessionId);
        }
        Optional.ofNullable(playerToSession.remove(playerId))
                .flatMap(future -> {
                    System.out.println("removed "  + playerId);
                    return Optional.ofNullable(future.getNow(null));
                })
                .ifPresent(session -> {
                    try {
                        session.close();
                    } catch (IOException e) {
                        log.debug("error when closing session", e);
                    }
                });
        playerLocks.remove(playerId);
        return playerId;
    }

    public void registerSession(String playerId, WebSocketSession session) {
        var sessionFuture = playerToSession.computeIfAbsent(playerId, k -> new CompletableFuture<>());
        sessionToPlayer.put(session.getId(), playerId);
        playerLocks.putIfAbsent(playerId, PlayerConcurrentSources.create());
        sessionFuture.complete(session);
        log.debug("registered session {} with player {}", session.getId(), playerId);
    }

    public void registerGame(String playerId, Game game) {
        playerToGame.put(playerId, game);
    }

    public Optional<Game> getGame(String playerId) {
        return Optional.ofNullable(playerToGame.get(playerId));
    }

    public String removePlayerBySession(String sessionId) {
        var playerId = dropSession(sessionId);
        Optional.ofNullable(playerToGame.get(playerId))
                .ifPresent(game -> {
                    try {
                        switch (game.getStage()) {
                            case LOBBY, FINISH -> {
                                game.removePlayer(playerId);
                                playerToGame.remove(playerId);
                            }
                        }
                    } catch (GameException e) {
                        throw new IllegalStateException(e);
                    }
                });
        return playerId;
    }

    public void removePlayerById(String playerId) {
        Optional.ofNullable(playerToSession.remove(playerId))
                .map(future -> future.getNow(null))
                .ifPresent(session -> {
                    sessionToPlayer.remove(session.getId());
                    try {
                        session.close();
                    } catch (IOException e) {
                        log.debug("error during closing session", e);
                    }
                });
        playerLocks.remove(playerId);
        playerToGame.remove(playerId);
    }

    public record PlayerConcurrentSources(Lock lock, Queue<Runnable> queue) {
        public static PlayerConcurrentSources create() {
            return new PlayerConcurrentSources(new ReentrantLock(), new ConcurrentLinkedQueue<>());
        }

        @Override
        public String toString() {
            return queue.stream().map(Object::toString).collect(Collectors.joining("\n"));
        }
    }
}
