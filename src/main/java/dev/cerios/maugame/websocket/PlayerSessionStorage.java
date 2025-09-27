package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.GamePlayer;
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
    private final Map<String, CompletableFuture<GamePlayer>> sessionToPlayer = new ConcurrentHashMap<>();
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

    public GamePlayer getPlayer(String sessionId) {
        var playerFuture = sessionToPlayer.computeIfAbsent(sessionId, __ -> new CompletableFuture<>());
        try {
            return playerFuture.get(futureSessionTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new MauTimeoutException("session %s not initialized in %d milliseconds".formatted(sessionId, futureSessionTimeoutMs), e);
        }
    }

    public PlayerConcurrentSources getPlayerSources(String playerId) {
        return playerLocks.computeIfAbsent(playerId, ignore -> PlayerConcurrentSources.create());
    }

    private GamePlayer dropSession(String sessionId) {
        var playerFuture = sessionToPlayer.remove(sessionId);
        if (playerFuture == null) {
            throw new IllegalStateException("Unexpected error: player does not exist for session " + sessionId);
        }
        var player = playerFuture.getNow(null);
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

    public void registerSession(GamePlayer player, WebSocketSession session) {
        var sessionFuture = playerToSession.computeIfAbsent(player.getPlayerId(), __ -> new CompletableFuture<>());
        var playerFuture = sessionToPlayer.computeIfAbsent(session.getId(), __ -> new CompletableFuture<>());
        playerLocks.computeIfAbsent(player.getPlayerId(), __ -> PlayerConcurrentSources.create());
        sessionFuture.complete(session);
        playerFuture.complete(player);
        log.info("{} associated with session {}", player, session.getId());
    }

    public void registerReplaceSession(GamePlayer player, WebSocketSession session) {
        playerToSession.put(player.getPlayerId(), CompletableFuture.completedFuture(session));
        sessionToPlayer.put(session.getId(), CompletableFuture.completedFuture(player));
        playerLocks.put(player.getPlayerId(), PlayerConcurrentSources.create());
    }

    public void registerGame(String playerId, Game game) {
        playerToGame.put(playerId, game);
    }

    public Optional<Game> getGame(String playerId) {
        return Optional.ofNullable(playerToGame.get(playerId));
    }

    public RemovedPair removePlayerBySession(String sessionId) {
        var player = dropSession(sessionId);
        var playerId = player.getPlayerId();
        var game = removePlayerFromGame(playerId);
        return new RemovedPair(player, game);
    }

    public record RemovedPair(GamePlayer player, Optional<Game> game) {
    }

    /**
     * remove player from game
     *
     * @param playerId id of the player to remove
     * @return <i>empty</i> if game remains empty, <i>Game</i> if it has more players left
     */
    private Optional<Game> removePlayerFromGame(String playerId) {
        return Optional.ofNullable(playerToGame.remove(playerId))
                .map(g -> {
                    g.removePlayer(playerId);
                    return g;
                });
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
        removePlayerFromGame(playerId);
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
