package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.game.action.DrawAction;
import dev.cerios.maugame.websocket.event.DistributeEvent;
import dev.cerios.maugame.websocket.event.RegisterEvent;
import dev.cerios.maugame.websocket.event.UnregisterEvent;
import dev.cerios.maugame.websocket.mapper.DrawActionMapper;
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

    private final DrawActionMapper drawActionMapper;

    private final ObjectMapper jsonMapper = new ObjectMapper();

    @EventListener
    public synchronized void onRegister(RegisterEvent event) {
        if (event.getSession() == null)
            return;
        playerToSession.put(event.getPlayerId(), event.getSession());
        sessionToPlayer.put(event.getSession().getId(), event.getPlayerId());
    }

    @EventListener
    public synchronized void onUnregister(UnregisterEvent event) throws IOException {
        var session = playerToSession.remove(event.getPlayerId());
        sessionToPlayer.remove(session.getId());
        if (session.isOpen())
            session.close();
    }


    @EventListener
    public synchronized void onDistribute(DistributeEvent event) {
        for (var player : event.getPlayers()) {
            var session = playerToSession.get(player);
            if (session == null) continue;

            var messages = event.getActions().stream()
                    .map(action -> action instanceof DrawAction drawAction && !drawAction.playerId().equals(player) ?
                            drawActionMapper.toHidden(drawAction) :
                            action
                    )
                    .toList();

            try {
                session.sendMessage(new TextMessage(jsonMapper.writeValueAsString(messages)));
            } catch (IOException ignore) {
            }
        }
    }
}
