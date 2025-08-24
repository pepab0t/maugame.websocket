package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.GameFactory;
import dev.cerios.maugame.websocket.message.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

import static dev.cerios.maugame.websocket.message.Message.createServerMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class LobbyHandler {

    private final GameFactory gameFactory;
    private final MauSettings mauSettings;
    private final ActionDistributor actionDistributor;
    private final PlayerSessionStorage storage;

    private final SequencedMap<UUID, Game> gameQueue = new LinkedHashMap<>();
    private final Map<String, Pair<ReadyState, Game>> players = new HashMap<>();
    private final Map<UUID, List<Pair<String, ReadyState>>> games = new HashMap<>();
    private final ObjectMapper objectMapper;


    public String registerPlayer(String username) throws GameException {
        var game = getOrCreateGame();
        var playerId = game.registerPlayer(username, actionDistributor::distribute);
        var ready = new ReadyState();
        players.put(playerId, new Pair<>(ready, game));
        var playerStates = games.computeIfAbsent(game.getUuid(), k -> new LinkedList<>());

        // TODO send unready
        for (var ps : playerStates) {
            storage.getSessionInstant(ps.val1())
                    .ifPresent(s -> sendSocketMessage(s, createServerMessage("unready")));
        }

        playerStates.add(new Pair<>(playerId, ready));

        if (game.getFreeCapacity() == 0)
            gameQueue.pollFirstEntry();

        return playerId;
    }

    public void setPlayerReady(String playerId) {
        var stateGame = players.get(playerId);
        if (stateGame == null) {
            throw new RuntimeException("Unexpected error: Player " + playerId + " not found in lobby registry.");
        }

        stateGame.val1().setReady(true);

        var playerStates = games.get(stateGame.val2().getUuid());
        if (playerStates == null) {
            throw new RuntimeException("Unexpected error: game " + stateGame.val2().getUuid() + " not found in lobby registry.");
        }

        for (var ps : playerStates) {
            storage.getSessionInstant(ps.val1())
                    .ifPresent(s -> sendSocketMessage(s, createServerMessage("ready")));
        }

        if (!playerStates.stream().allMatch(ps -> ps.val2().isReady()))
            return;

        var game = stateGame.val2();
        try {
            game.start();
        } catch (MauEngineBaseException e) {
            throw new RuntimeException(e);
        }

        gameQueue.remove(game.getUuid());
        games.remove(game.getUuid());
        for (var ps : playerStates) {
            var player = ps.val1();
            players.remove(player);
            storage.registerGame(player, game);
        }
    }

    public void removePlayer(String sessionId) {
        var playerId = storage.getPlayer(sessionId);
        var stateGame = players.get(playerId);
        if (stateGame == null) {
            return;
        }

        var game = stateGame.val2();
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
            var otherPlayer =  ps.val1();
            if (otherPlayer.equals(playerId)) {
                it.remove();
            } else {
                ps.val2().setReady(false);
                storage.getSessionInstant(otherPlayer)
                                .ifPresent(s -> sendSocketMessage(s, createServerMessage("unready")));
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

    private record Pair<T1, T2>(T1 val1, T2 val2) {
    }

    @Getter
    @Setter
    private static class ReadyState {
        private boolean ready = false;
    }
}
