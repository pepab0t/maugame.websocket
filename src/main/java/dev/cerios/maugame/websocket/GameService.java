package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.card.Card;
import dev.cerios.maugame.mauengine.card.Color;
import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.mauengine.game.GamePlayer;
import dev.cerios.maugame.websocket.exception.LobbyAlreadyExistsException;
import dev.cerios.maugame.websocket.exception.NotFoundException;
import dev.cerios.maugame.websocket.message.ServerMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final PlayerSessionStorage storage;
    private final MessageDistributor distributor;
    private final GameStorage gameStorage;

    public void registerPlayerToNewCustomLobby(
            String username,
            WebSocketSession session,
            String gameName,
            boolean isPrivate
    ) throws LobbyAlreadyExistsException {
        var player = gameStorage.registerToNew(username, gameName, isPrivate);
        storage.registerSession(player, session);
        logPlayerAssignment(player, session);
    }

    public void registerPlayerToExistingCustomLobby(String username, WebSocketSession session, String lobbyName) throws NotFoundException {
        try {
            var player = gameStorage.registerToNamed(username, lobbyName);
            storage.registerSession(player, session);
            logPlayerAssignment(player, session);
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerPlayer(String username, WebSocketSession session) throws GameException {
        var player = gameStorage.registerToRandom(username);
        storage.registerSession(player, session);
    }

    public void reconnectPlayer(String username, WebSocketSession session, String playerId) throws NotFoundException {
        try {
            var game = storage.getGame(playerId).orElseThrow(() -> new NotFoundException("Game not found for given player."));
            storage.registerReplaceSession(game.getPlayer(playerId), session);
            game.sendCurrentStateTo(playerId, p -> p.getUsername().equals(username));
            for (var p : game.getAllPlayers()) {
                var otherId = p.getPlayerId();
                if (otherId.equals(playerId)) {
                    continue;
                }
                distributor.enqueueMessage(otherId, ServerMessage.ofReconnect(username));
            }
        } catch (GameException e) {
            storage.removePlayerById(playerId);
            throw new NotFoundException(e.getMessage());
        }
    }

    public void disconnectPlayer(String sessionId) {
        var pair = storage.removePlayerBySession(sessionId);
        var player = pair.player();
        var gameOpt = pair.game();
        if (gameOpt.isPresent()) {
            var game = gameOpt.get();
            if (game.getPlayerCount() == 0) {
                gameStorage.remove(game.getId());
            } else {
                var playerId = player.getPlayerId();
                var disconnectMessage = ServerMessage.ofDisconnect(player.getUsername());
                for (var otherPlayer : game.getAllPlayers()) {
                    if (otherPlayer.getPlayerId().equals(playerId)) {
                        continue;
                    }
                    distributor.enqueueMessage(otherPlayer.getPlayerId(), disconnectMessage);
                }
            }
        }
    }

    public void setPlayerReady(String playerId) throws NotFoundException, GameException {
        var gameOpt = storage.getGame(playerId);
        if (gameOpt.isEmpty()) {
            throw new NotFoundException("Game not found for given player.");
        }
        gameOpt.get().setReady(playerId);
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

    private void logPlayerAssignment(GamePlayer player, WebSocketSession session) {
        log.info("player `{}` assigned to session `{}`", player, session.getId());
    }
}
