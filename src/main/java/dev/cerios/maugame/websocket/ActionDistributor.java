package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.mauengine.game.action.DrawAction;
import dev.cerios.maugame.websocket.mapper.DrawActionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ActionDistributor {
    private final Map<String, WebSocketSession> playerToSession = new HashMap<>();
    private final Map<String, String> sessionToPlayer = new HashMap<>();

    private final DrawActionMapper drawActionMapper;

    private final ObjectMapper jsonMapper = new ObjectMapper();

    public void add(String player, WebSocketSession session) {
        playerToSession.put(player, session);
        sessionToPlayer.put(player, session.getId());
    }

    public void distribute(List<String> players, List<Action> actions) {
        for (var player : players) {
            var session = playerToSession.get(player);
//            if (session == null) continue;

            var messages = actions.stream()
                    .map(x ->  x instanceof DrawAction drawAction && !drawAction.playerId().equals(player) ? drawActionMapper.toHidden(drawAction) : x)
                    .toList();

            System.out.printf("send to %s actions: %s%n", player, messages);
        }
    }

}
