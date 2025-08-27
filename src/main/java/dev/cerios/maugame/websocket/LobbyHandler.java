package dev.cerios.maugame.websocket;

import ch.qos.logback.core.joran.sanity.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.GameFactory;
import dev.cerios.maugame.websocket.message.Message;
import dev.cerios.maugame.websocket.message.ServerMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class LobbyHandler {

    private final GameFactory gameFactory;
    private final MauSettings mauSettings;
    private final ActionDistributor actionDistributor;
    private final PlayerSessionStorage storage;

    private final SequencedMap<UUID, Game> gameQueue = new LinkedHashMap<>();
    private final Map<String, Group<String, ReadyState, Game>> players = new HashMap<>();
    private final Map<UUID, List<Group<String, String, ReadyState>>> games = new HashMap<>();
    private final ObjectMapper objectMapper;


    public synchronized String registerPlayer(String username) throws GameException {
        var game = getOrCreateGame();
        var playerId = game.registerPlayer(username, actionDistributor::distribute);
        var ready = new ReadyState();
        players.put(playerId, new Group<>(username, ready, game));
        var gameData = games.computeIfAbsent(game.getUuid(), k -> new LinkedList<>());

        // TODO send unready
        for (var original : gameData) {
            if (original.val3().setReady(false)) { // only if state changed
                for (var gd : gameData) {
                    storage.getSessionInstant(gd.val1()).ifPresent(s -> sendSocketMessage(s, ServerMessage.ofUnready(gd.val2())));
                }
            }
        }

        gameData.add(new Group<>(playerId, username, ready));

        if (game.getFreeCapacity() == 0)
            gameQueue.pollFirstEntry();

        return playerId;
    }

    public synchronized void setPlayerReady(String playerId) {
        var playerData = players.get(playerId);
        if (playerData == null) {
            throw new RuntimeException("Unexpected error: Player " + playerId + " not found in lobby registry.");
        }

        var gameData = games.get(playerData.val3().getUuid());
        if (gameData == null || gameData.isEmpty()) {
            throw new RuntimeException("Unexpected error: game " + playerData.val3().getUuid() + " not found in lobby registry.");
        }

        if (!playerData.val2().setReady(true)) { // only if state changed
            return;
        }

        for (var gd : gameData) {
            storage.getSessionInstant(gd.val1()).ifPresent(s -> sendSocketMessage(s, ServerMessage.ofReady(playerData.val1())));
        }

        if (!gameData.stream().allMatch(ps -> ps.val3().isReady()))
            return;

        var game = playerData.val3();
        gameQueue.remove(game.getUuid());
        games.remove(game.getUuid());
        for (var gd : gameData) {
            var player = gd.val1();
            players.remove(player);
            storage.registerGame(player, game);
        }
        try {
            game.start();
        } catch (MauEngineBaseException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void removePlayer(String sessionId) {
        var playerId = storage.getPlayer(sessionId);
        var stateGame = players.get(playerId);
        if (stateGame == null) {
            return;
        }

        var game = stateGame.val3();
        try {
            game.removePlayer(playerId);
        } catch (GameException e) {
            throw new RuntimeException(e);
        }

        var playerStates = games.get(game.getUuid());
        if (playerStates == null) {
            throw new RuntimeException("Unexpected error: game " + game.getUuid() + " not found in lobby registry.");
        }

        for (var it = playerStates.iterator(); it.hasNext(); ) {
            var ps = it.next();
            var otherPlayer = ps.val1();
            if (otherPlayer.equals(playerId)) {
                it.remove();
            } else {
                ps.val3().setReady(false);
                for (var anotherPs : playerStates) {
                    storage.getSessionInstant(otherPlayer)
                            .ifPresent(s -> sendSocketMessage(s, ServerMessage.ofUnready(anotherPs.val2())));
                }
            }
        }

        gameQueue.putIfAbsent(game.getUuid(), game);
    }

    private Game getOrCreateGame() {
        return Optional.ofNullable(gameQueue.firstEntry())
                .map(Map.Entry::getValue)
                .orElseGet(() -> {
                    var g = gameFactory.createGame(2, mauSettings.getMaxPlayers());
                    gameQueue.putLast(g.getUuid(), g);
                    return g;
                });
    }

    private void sendSocketMessage(WebSocketSession session, Message message) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.warn("could not send message to socket ({})", session, e);
        }
    }

    private record Group<T1, T2, T3>(T1 val1, T2 val2, T3 val3) {
    }

    @Getter
    @ToString
    private static class ReadyState {
        private boolean ready = false;

        public boolean setReady(boolean ready) {
            var out = this.ready != ready;
            this.ready = ready;
            return out;
        }
    }
}
