package dev.cerios.maugame.websocket.storage;

import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.GameFactory;
import dev.cerios.maugame.mauengine.game.action.Action;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class GameStorage {
    private final Map<String, Game> playerToGame = new HashMap<>();
    private final SequencedMap<String, Game> gameById = new LinkedHashMap<>();
    private final GameFactory gameFactory;

    public Game addPlayerToGame(String player, String gameId, List<Action> actionCollector) throws GameException {
        if (playerToGame.get(player) != null) {
            throw new IllegalArgumentException("Player " + player + " already exists");
        }
        var game = gameById.computeIfAbsent(gameId, ignore -> gameFactory.createGame());
        actionCollector.add(game.registerPlayer(player));
        playerToGame.put(player, game);
        return game;
    }

    public Game addPlayerToLatestGame(String player, List<Action> actionCollector) {
        Game game;
        try {
            game = gameById.lastEntry().getValue();
            actionCollector.add(game.registerPlayer(player));
        } catch (GameException | NullPointerException e) {
            game = gameFactory.createGame();
            gameById.put(game.getUuid().toString(), game);
            try {
                actionCollector.add(game.registerPlayer(player));
            } catch (GameException ex) {
                throw new RuntimeException("unexpected scenario", ex);
            }
        }
        playerToGame.put(player, game);
        return game;
    }

    public Game getPlayersGame(String player) {
        Game game = playerToGame.get(player);
        if (game == null)
            throw new IllegalArgumentException("Player " + player + " not found");
        return game;
    }

    public Game getGame(String id) {
        var game = gameById.get(id);
        if (game == null)
            throw new IllegalArgumentException("Game " + id + " not found");
        return game;
    }
}
