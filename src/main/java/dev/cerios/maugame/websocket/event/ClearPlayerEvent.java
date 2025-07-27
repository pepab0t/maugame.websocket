package dev.cerios.maugame.websocket.event;

import dev.cerios.maugame.mauengine.game.Player;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ClearPlayerEvent extends ApplicationEvent {

    private final String sessionId;
    private final Player player;

    public ClearPlayerEvent(Object source, String sessionId, Player player) {
        super(source);
        this.sessionId = sessionId;
        this.player = player;
    }
}
