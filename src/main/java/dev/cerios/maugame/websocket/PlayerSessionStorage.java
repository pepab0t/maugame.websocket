package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.mauengine.game.Stage;
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
    private final Map<String, Player> sessionToPlayer = new ConcurrentHashMap<>();
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

    public Player getPlayer(String sessionId) {
        var player = sessionToPlayer.get(sessionId);
        if (player == null) {
            throw new RuntimeException("Unexpected error: player should exist for session " + sessionId);
        }
        return player;
    }

    public PlayerConcurrentSources getPlayerSources(String playerId) {
        return playerLocks.computeIfAbsent(playerId, ignore -> PlayerConcurrentSources.create());
    }

    private Player dropSession(String sessionId) {
        var player = sessionToPlayer.remove(sessionId);
        if (player == null) {
            throw new IllegalStateException("Unexpected error: player does not exist for session " + sessionId);
        }
        var playerId = player.getPlayerId();
        Optional.ofNullable(playerToSession.remove(playerId))
                .map(future -> future.getNow(null))
                .ifPresent(session -> {
                    try {
                        session.close();
                    } catch (IOException e) {
                        log.debug("error when closing session", e);
                    }
                });
        playerLocks.remove(playerId);
        return player;
    }

    public void registerSession(Player player, WebSocketSession session) {
        var sessionFuture = playerToSession.computeIfAbsent(player.getPlayerId(), k -> new CompletableFuture<>());
        sessionToPlayer.put(session.getId(), player);
        playerLocks.putIfAbsent(player.getPlayerId(), PlayerConcurrentSources.create());
        sessionFuture.complete(session);
    }

    public void registerGame(String playerId, Game game) {
        playerToGame.put(playerId, game);
    }

    public Optional<Game> getGame(String playerId) {
        return Optional.ofNullable(playerToGame.get(playerId));
    }

    public Player removePlayerBySession(String sessionId) {
        var player = dropSession(sessionId);
        var playerId = player.getPlayerId();
        Optional.ofNullable(playerToGame.get(playerId))
                .ifPresent(game -> {
                    try {
                        if (game.getStage() == Stage.FINISH) {
                            game.removePlayer(playerId);
                            playerToGame.remove(playerId);
                        }
                    } catch (GameException e) {
                        throw new IllegalStateException(e);
                    }
                });
        return player;
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

    public void clear() {
        playerToSession.clear();
        playerToGame.clear();
        sessionToPlayer.clear();
        playerLocks.clear();
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
