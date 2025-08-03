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
import org.springframework.web.socket.WebSocketSession;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    //    private final Map<String, Game> playerToGame = new ConcurrentHashMap<>();
    private final GameFactory gameFactory;
    private final MauSettings mauSettings;
    private final ActionDistributor actionDistributor;
    private final PlayerSessionStorage storage;

    private Game currentGame;

    public void registerPlayer(String username, WebSocketSession session) {
        if (currentGame == null || currentGame.getFreeCapacity() == 0) {
            currentGame = gameFactory.createGame(2, mauSettings.getMaxPlayers());
        }
        Player player;
        try {
            player = currentGame.registerPlayer(username, actionDistributor::distribute);
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
        if (currentGame.getFreeCapacity() == 0) {
            try {
                currentGame.start();
            } catch (MauEngineBaseException e) {
                throw new RuntimeException(e);
            }
        }
        storage.registerSession(player, session);
        storage.registerGame(player.getPlayerId(), currentGame);
    }

    public void registerPlayer(String username, WebSocketSession session, String playerId) {
        // logic
    }

    public void disconnectPlayer(String sessionId) {
        storage.removePlayer(sessionId, (player, game) -> {
            try {
                switch (game.getStage()) {
                    case RUNNING -> game.deactivatePlayer(player.getPlayerId());
                    case LOBBY -> game.removePlayer(player.getPlayerId());
                }
            } catch (GameException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public void playCard(Player player, Card card, Color nextColor) throws MauEngineBaseException {
        var game = storage.getGame(player.getPlayerId()).orElseThrow(() -> new RuntimeException("No game"));
        game.playCardMove(player.getPlayerId(), card, nextColor);
    }

    public void drawCard(Player player) throws MauEngineBaseException {
        var game = storage.getGame(player.getPlayerId()).orElseThrow(() -> new RuntimeException("No game"));
        game.playDrawMove(player.getPlayerId());
    }

    public void pass(Player player) throws MauEngineBaseException {
        var game = storage.getGame(player.getPlayerId()).orElseThrow(() -> new RuntimeException("No game"));
        game.playPassMove(player.getPlayerId());
    }
}
