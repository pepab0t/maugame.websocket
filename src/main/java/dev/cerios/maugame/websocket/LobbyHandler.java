package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.GameFactory;
import dev.cerios.maugame.websocket.exception.LobbyAlreadyExistsException;
import dev.cerios.maugame.websocket.exception.NotFoundException;
import dev.cerios.maugame.websocket.message.ServerMessage;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
@Slf4j
public class LobbyHandler {

    private final GameFactory gameFactory;
    private final MauSettings mauSettings;
    private final MessageDistributor messageDistributor;
    private final PlayerSessionStorage storage;
    private final MessageDistributor distributor;

    private final SequencedMap<UUID, Game> gameQueue = new LinkedHashMap<>();
    private final Map<String, UUID> lobbiesQueueReferences = new HashMap<>();
    private final Map<String, Game> privateLobbies = new HashMap<>();

    private final Map<String, PlayerData> players = new HashMap<>();
    private final Map<UUID, List<Group3<String, String, ReadyState>>> games = new HashMap<>();

    private final Lock lock = new ReentrantLock();

    public String registerToNewPublicLobby(String username, String lobbyName) throws GameException, LobbyAlreadyExistsException {
        try {
            lock.lock();
            if (lobbiesQueueReferences.containsKey(lobbyName) || privateLobbies.containsKey(lobbyName)) {
                throw new LobbyAlreadyExistsException(lobbyName);
            }
            var newGame = gameFactory.createGame(2, mauSettings.getMaxPlayers());
            lobbiesQueueReferences.put(lobbyName, newGame.getUuid());
            gameQueue.putLast(newGame.getUuid(), newGame);
            return registerPlayer(username, newGame, lobbyName);
        } finally {
            lock.unlock();
        }
    }

    public String registerToNewPrivateLobby(String username, String lobbyName) throws GameException, LobbyAlreadyExistsException {
        try {
            lock.lock();
            if (lobbiesQueueReferences.containsKey(lobbyName) || privateLobbies.containsKey(lobbyName)) {
                throw new LobbyAlreadyExistsException(lobbyName);
            }
            var newGame = gameFactory.createGame(2, mauSettings.getMaxPlayers());
            privateLobbies.put(lobbyName, newGame);
            return registerPlayer(username, newGame, lobbyName);
        } finally {
            lock.unlock();
        }
    }

    public String registerPlayerToExistingLobby(String username, String lobbyName) throws GameException, NotFoundException {
        try {
            lock.lock();
            var game = privateLobbies.get(lobbyName);
            if (game == null) {
                var gameId = lobbiesQueueReferences.get(lobbyName);
                if (gameId == null) {
                    throw new NotFoundException(String.format("Lobby with name `%s` not found.", lobbyName));
                }
                game = gameQueue.get(gameId);
            }
            return registerPlayer(username, game, lobbyName);
        } finally {
            lock.unlock();
        }
    }

    public String registerPlayerToRandomLobby(String username) throws GameException {
        try {
            lock.lock();
            var game = getOrCreateGame();
            return registerPlayer(username, game, null);
        } finally {
            lock.unlock();
        }
    }

    public void setPlayerReady(String playerId) {
        try {
            lock.lock();
            var playerData = players.get(playerId);
            if (playerData == null) {
                throw new RuntimeException("Unexpected error: Player " + playerId + " not found in lobby registry.");
            }

            var game = playerData.game();
            var gameData = games.get(game.getUuid());
            if (gameData == null || gameData.isEmpty()) {
                throw new RuntimeException("Unexpected error: game " + game.getUuid() + " not found in lobby registry.");
            }

            if (!game.hasEnoughPlayers() || !playerData.ready().setReady(true)) { // only if state changed
                return;
            }

            for (var gd : gameData) {
                distributor.enqueueMessage(gd.val1(), ServerMessage.ofReady(playerData.username()));
            }

            if (!gameData.stream().allMatch(ps -> ps.val3().isReady()))
                return;

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
        } finally {
            lock.unlock();
        }
    }

    public void removePlayer(String sessionId) {
        try {
            lock.lock();
            var playerId = storage.removePlayerBySession(sessionId);
            var playerData = players.remove(playerId);
            if (playerData == null) {
                return;
            }
            var game = playerData.game();

            var gameData = games.get(game.getUuid());
            if (gameData == null) {
                throw new RuntimeException("Unexpected error: game " + game.getUuid() + " not found in lobby registry.");
            }

            for (var it = gameData.iterator(); it.hasNext(); ) {
                var gd = it.next();
                var otherPlayer = gd.val1();
                if (otherPlayer.equals(playerId)) {
                    it.remove();
                } else {
                    if (gd.val3().setReady(false)) {
                        for (var anotherGd : gameData) {
                            distributor.enqueueMessage(anotherGd.val1(), ServerMessage.ofUnready(gd.val2()));
                        }
                    }
                }
            }

            if (gameData.isEmpty()) {
                games.remove(game.getUuid());
            }

            try {
                game.removePlayer(playerId);
            } catch (GameException e) {
                throw new RuntimeException(e);
            }

            gameQueue.putIfAbsent(game.getUuid(), game);
        } finally {
            lock.unlock();
        }
    }

    private String registerPlayer(String username, Game game, String lobbyName) throws GameException {
        var playerId = game.registerPlayer(username, messageDistributor::distribute);
        var ready = new ReadyState();
        players.put(playerId, new PlayerData(username, ready, game, lobbyName));
        var gameData = games.computeIfAbsent(game.getUuid(), k -> new LinkedList<>());

        for (var original : gameData) {
            if (original.val3().setReady(false)) { // only if state changed
                for (var gd : gameData) {
                    distributor.enqueueMessage(gd.val1(), ServerMessage.ofUnready(original.val2()));
                }
            }
        }

        gameData.add(new Group3<>(playerId, username, ready));

        if (game.getFreeCapacity() > 0)
            return playerId;

        if (lobbyName == null)
            gameQueue.pollFirstEntry();
        else {
            var lobbyId = lobbiesQueueReferences.remove(lobbyName);
            if (lobbyId == null) {
                privateLobbies.remove(lobbyName);
            } else {
                gameQueue.remove(lobbyId);
            }
        }

        return playerId;
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

    public void clear() {
        try {
            lock.lock();
            this.games.clear();
            this.players.clear();
            this.gameQueue.clear();
            this.lobbiesQueueReferences.clear();
            this.privateLobbies.clear();
        }  finally {
            lock.unlock();
        }
    }

    private record PlayerData(String username, ReadyState ready, Game game, @Nullable String lobbyName) {
    }

    private record Group3<T1, T2, T3>(T1 val1, T2 val2, T3 val3) {
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
