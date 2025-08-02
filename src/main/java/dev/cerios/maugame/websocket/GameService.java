package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.card.Card;
import dev.cerios.maugame.mauengine.card.Color;
import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.GameFactory;
import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.websocket.event.ClearPlayerEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final Map<String, Game> playerToGame = new ConcurrentHashMap<>();
    private final GameFactory gameFactory;
    private final MauSettings mauSettings;
    private final ActionDistributor actionDistributor;

    private Game currentGame;

    public Player registerPlayer(String username) {
        if (currentGame == null || currentGame.getFreeCapacity() == 0) {
            currentGame = gameFactory.createGame(2, mauSettings.getMaxPlayers());
        }
        Player player;
        try {
            player = currentGame.registerPlayer(username, actionDistributor::distribute);
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
        playerToGame.put(player.getPlayerId(), currentGame);
        if (currentGame.getFreeCapacity() == 0) {
            try {
                currentGame.start();
            } catch (MauEngineBaseException e) {
                throw new RuntimeException(e);
            }
        }
        return player;
    }

    public void disconnectPlayer(Player player) {
        try {
            var game = playerToGame.remove(player.getPlayerId());
            switch (game.getStage()) {
                case RUNNING -> game.deactivatePlayer(player.getPlayerId());
                case LOBBY -> game.removePlayer(player.getPlayerId());
            }
        } catch (GameException e) {
            throw new IllegalStateException(e);
        } catch (NullPointerException ignore) {}
    }

    public void playCard(Player player, Card card, Color nextColor) throws MauEngineBaseException {
        var game = playerToGame.get(player.getPlayerId());
        if (game == null) {
            throw new RuntimeException("No game"); // TODO think about handling no game
        }
        game.playCardMove(player.getPlayerId(), card, nextColor);
    }

    public void drawCard(Player player) throws MauEngineBaseException {
        var game = playerToGame.get(player.getPlayerId());
        if (game == null) {
            throw new RuntimeException("No game"); // TODO think about handling no game
        }
        game.playDrawMove(player.getPlayerId());
    }

    public void pass(Player player) throws MauEngineBaseException {
        var game = playerToGame.get(player.getPlayerId());
        if (game == null) {
            throw new RuntimeException("No game"); // TODO think about handling no game
        }
        game.playPassMove(player.getPlayerId());
    }

    @EventListener
    public void handleClearEvent(ClearPlayerEvent event) {
        playerToGame.remove(event.getPlayer().getPlayerId());
    }
}
