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

    public void registerPlayerToNewCustomLobby(String username, WebSocketSession session, String lobbyName, boolean isPrivate) {
        try {
            String playerId = isPrivate ?
                    lobbyHandler.registerToNewPrivateLobby(username, lobbyName) :
                    lobbyHandler.registerToNewPublicLobby(username, lobbyName);
            storage.registerSession(playerId, session);
            log.info("player `{}` assigned to id `{}` (session `{}`)", username, playerId, session.getId());
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerPlayerToExistingCustomLobby(String username, WebSocketSession session, String lobbyName) throws NotFoundException {
        try {
            String playerId = lobbyHandler.registerPlayerToExistingPrivateLobby(username, lobbyName);
            storage.registerSession(playerId, session);
            log.info("player `{}` assigned to id `{}` (session `{}`)", username, playerId, session.getId());
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerPlayer(String username, WebSocketSession session) {
        try {
            String playerId = lobbyHandler.registerPlayerToRandomLobby(username);
            storage.registerSession(playerId, session);
            log.info("player `{}` assigned to id `{}` (session `{}`)", username, playerId, session.getId());
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
    }

    public void reconnectPlayer(String username, WebSocketSession session, String playerId) throws NotFoundException {
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
