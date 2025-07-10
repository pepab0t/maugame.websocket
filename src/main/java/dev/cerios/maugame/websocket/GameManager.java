package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.GameFactory;
import dev.cerios.maugame.mauengine.game.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameManager {
    private final Map<String, Game> playerToGame = new ConcurrentHashMap<>();
    private final GameFactory gameFactory;
    private final SessionGameBridge bridge;
    private final ObjectMapper objectMapper;

    private Game currentGame;

    public Player registerPlayer(String username) {
        if (currentGame == null || currentGame.getFreeCapacity() == 0) {
            currentGame = gameFactory.createGame();
        }
        Player player;
        try {
            player = currentGame.registerPlayer(username, (p, e) -> {
                Thread.ofVirtual().start(() -> {
                    synchronized (this) {
                        var session = bridge.getSession(p);
                        try {
                            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(e)));
                        } catch (IOException ex) {
                            log.warn("cannot send proper message", ex);
                        }
                    }
                });
            });
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
        playerToGame.put(player.getPlayerId(), currentGame);
        return player;
    }
}
