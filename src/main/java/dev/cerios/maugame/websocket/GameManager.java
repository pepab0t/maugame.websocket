package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.mauengine.game.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameManager {
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
        var game = playerToGame.get(player.getPlayerId());
        try {
            switch (game.getStage()) {
                case RUNNING -> {
                    game.deactivatePlayer(player.getPlayerId());
                }
                case LOBBY -> {
                    game.removePlayer(player.getPlayerId());
                }
            }
        }  catch (GameException e) {
            throw new IllegalStateException(e);
        }
    }
}
