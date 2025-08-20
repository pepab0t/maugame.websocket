package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.card.Card;
import dev.cerios.maugame.mauengine.card.Color;
import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.GameFactory;
import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.mauengine.game.Stage;
import dev.cerios.maugame.websocket.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static dev.cerios.maugame.websocket.message.Message.createErrorMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final GameFactory gameFactory;
    private final MauSettings mauSettings;
    private final ActionDistributor actionDistributor;
    private final PlayerSessionStorage storage;
    private final ObjectMapper objectMapper;

    private volatile Game currentGame;

    public void registerPlayer(String username, WebSocketSession session) {
        if (currentGame == null || currentGame.getStage() != Stage.LOBBY) {
            currentGame = gameFactory.createGame(2, mauSettings.getMaxPlayers());
        }
        Player player;
        try {
            player = currentGame.registerPlayer(username, actionDistributor::distribute);
        } catch (GameException e) {
            try {
                // TODO object mapper at the same place for both register methods
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(createErrorMessage(e))));
                session.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return;
        }
        if (currentGame.getFreeCapacity() == 0) {
            try {
                currentGame.start();
            } catch (MauEngineBaseException e) {
                throw new RuntimeException(e);
            }
        }
        storage.registerSession(player.getPlayerId(), session);
        storage.registerGame(player.getPlayerId(), currentGame);
    }

    public void registerPlayer(String username, WebSocketSession session, String playerId) throws NotFoundException {
        try {
            var game = storage.getGame(playerId).orElseThrow(() -> new NotFoundException("Game not found for given player."));
            storage.registerSession(playerId, session);
            game.sendCurrentStateTo(playerId, p -> p.getUsername().equals(username));
        } catch (GameException e) {
            storage.removePlayerById(playerId);
            throw new NotFoundException(e.getMessage());
        }
    }

    public void disconnectPlayer(String sessionId) {
        storage.removePlayerBySession(sessionId);
    }

    public void playCard(String playerId, Card card, Color nextColor) throws MauEngineBaseException {
        var game = storage.getGame(playerId).orElseThrow(() -> new RuntimeException("No game"));
        game.playCardMove(playerId, card, nextColor);
    }

    public void drawCard(String playerId) throws MauEngineBaseException {
        var game = storage.getGame(playerId).orElseThrow(() -> new RuntimeException("No game"));
        game.playDrawMove(playerId);
    }

    public void pass(String playerId) throws MauEngineBaseException {
        var game = storage.getGame(playerId).orElseThrow(() -> new RuntimeException("No game"));
        game.playPassMove(playerId);
    }
}
