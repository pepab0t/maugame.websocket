package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.game.action.DrawAction;
import dev.cerios.maugame.websocket.event.DisablePlayerEvent;
import dev.cerios.maugame.websocket.event.DistributeEvent;
import dev.cerios.maugame.websocket.event.RegisterEvent;
import dev.cerios.maugame.websocket.event.UnregisterEvent;
import dev.cerios.maugame.websocket.response.ActionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ActionDistributor {
    private final Map<String, WebSocketSession> playerToSession = new HashMap<>();
    private final Map<String, String> sessionToPlayer = new HashMap<>();

    private final ObjectMapper jsonMapper = new ObjectMapper();

    public synchronized String getPlayerBySession(String sessionId) {
        String player = sessionToPlayer.get(sessionId);
        if (player == null) {
            throw new RuntimeException("no player found for session " + sessionId);
        }
        return player;
    }

    public synchronized WebSocketSession getSessionByPlayer(String player) {
        var session = playerToSession.get(player);
        if (session == null) {
            throw new RuntimeException("no player found for session " + player);
        }
        return session;
    }

    @EventListener
    public synchronized void onRegister(RegisterEvent event) {
        if (event.getSession() == null)
            return;
        playerToSession.put(event.getPlayerId(), event.getSession());
        sessionToPlayer.put(event.getSession().getId(), event.getPlayerId());
    }

    @EventListener
    public synchronized DisablePlayerEvent onUnregister(UnregisterEvent event) throws IOException {
        var playerId = sessionToPlayer.remove(event.getSessionId());
        var session = playerToSession.remove(playerId);
        if (session.isOpen())
            session.close();
        return new DisablePlayerEvent(this, playerId);
    }


    @EventListener
    public synchronized void onDistribute(DistributeEvent event) {
        for (var player : event.getPlayers()) {
            var session = playerToSession.get(player);
            if (session == null) continue;

            var response = new ActionResponse(event.getActions());

            try {
                session.sendMessage(new TextMessage(jsonMapper.writeValueAsString(response)));
            } catch (IOException ignore) {
            }
        }
    }
}
