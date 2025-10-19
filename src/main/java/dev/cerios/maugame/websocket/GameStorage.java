package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.GameFactory;
import dev.cerios.maugame.mauengine.game.GamePlayer;
import dev.cerios.maugame.websocket.exception.LobbyAlreadyExistsException;
import dev.cerios.maugame.websocket.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameStorage {

    private final GameFactory gameFactory;
    private final MessageDistributor distributor;
    private final PlayerSessionStorage storage;
    private final ReentrantLock lock = new ReentrantLock();

    private final LinkedHashMap<UUID, NamedGame> publicGames = new LinkedHashMap<>();
    private final HashMap<UUID, NamedGame> privateGames = new HashMap<>();
    private final HashMap<String, UUID> gameRefs = new HashMap<>();
    private final MauSettings mauSettings;

    public GamePlayer registerToRandom(String username) throws GameException {
        Game game;
        log.debug("registering {}", username);
        try {
            lock.lock();
            game = getOrCreateRandomGame();
            log.debug("found game {} for user {}", game.getId(), username);
            try {
                var player = game.registerPlayer(username, distributor::distribute);
                storage.registerGame(player.getPlayerId(), game);
                log.info("{} registered to random game {}", player, game.getId());
                return player;
            } catch (GameException e) {
                log.error("{} : not registered", username, e);
                publicGames.putLast(game.getId(), new NamedGame(game));
                throw e;
            }
        } finally {
            lock.unlock();
        }
    }

    public GamePlayer registerToNamed(String username, String gameName) throws NotFoundException, GameException {
        NamedGame namedGame;
        try {
            lock.lock();
            var gameId = gameRefs.get(gameName);
            if (gameId == null) {
                throw new NotFoundException("Game `" + gameName + "` not found");
            }
            namedGame = publicGames.get(gameId);
            if (namedGame == null) {
                namedGame = privateGames.get(gameId);
            }
        } finally {
            lock.unlock();
        }

        var player = namedGame.game().registerPlayer(username, distributor::distribute);
        storage.registerGame(player.getPlayerId(), namedGame.game());

        return player;
    }

    public GamePlayer registerToNew(String username, String gameName, boolean isPrivate) throws LobbyAlreadyExistsException {
        Game newGame;
        try {
            lock.lock();
            newGame = gameFactory.createGame();

            if (gameRefs.containsKey(gameName)) {
                throw new LobbyAlreadyExistsException(gameName);
            }
            gameRefs.put(gameName, newGame.getId());

            if (isPrivate) {
                privateGames.put(newGame.getId(), new NamedGame(gameName, newGame));
            } else {
                publicGames.put(newGame.getId(), new NamedGame(gameName, newGame));
            }
        } finally {
            lock.unlock();
        }

        newGame.listenStart(this::remove);

        GamePlayer player;
        try {
            player = newGame.registerPlayer(username, distributor::distribute);
            storage.registerGame(player.getPlayerId(), newGame);
            return player;
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
    }

    private Game getOrCreateRandomGame() {
        var first = publicGames.firstEntry();
        if (first == null)
            return createAndRegisterPublicGame();

        var game = first.getValue().game();
        return switch (game.getFreeCapacity()) {
            case 0 -> {
                publicGames.pollFirstEntry();
                yield createAndRegisterPublicGame();
            }
            case 1 -> Objects.requireNonNull(publicGames.remove(game.getId())).game();
            default -> game;
        };
    }

    private Game createAndRegisterPublicGame() {
        var g = gameFactory.createGame(2, mauSettings.getMaxPlayers(), 60_000);
        publicGames.putLast(g.getId(), new NamedGame(g));
        g.listenStart(this::remove);
        return g;
    }

    public void remove(UUID gameId) {
        try {
            lock.lock();
            var namedGame = publicGames.remove(gameId);
            if (namedGame == null)
                namedGame = privateGames.remove(gameId);
            gameRefs.remove(namedGame.name());
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        try {
            lock.lock();
            publicGames.clear();
            privateGames.clear();
            gameRefs.clear();
        } finally {
            lock.unlock();
        }
    }

    private record NamedGame(String name, Game game) {
        private NamedGame(Game game) {
            this(null, game);
        }
    }
}
