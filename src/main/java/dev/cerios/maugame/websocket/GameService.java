package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.card.Card;
import dev.cerios.maugame.mauengine.card.Color;
import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.websocket.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final PlayerSessionStorage storage;
    private final LobbyHandler lobbyHandler;

    public void registerPlayer(String username, WebSocketSession session) {
        String playerId;
        try {
            playerId = lobbyHandler.registerPlayer(username);
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
        storage.registerSession(playerId, session);
        log.info("player `{}` assigned to id `{}`", username, playerId);
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
        lobbyHandler.removePlayer(sessionId);
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
