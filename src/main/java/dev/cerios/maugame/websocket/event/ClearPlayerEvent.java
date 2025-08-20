package dev.cerios.maugame.websocket.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ClearPlayerEvent extends ApplicationEvent {

    private final String sessionId;
    private final String playerId;

    public ClearPlayerEvent(Object source, String sessionId, String playerId) {
        super(source);
        this.sessionId = sessionId;
        this.playerId = playerId;
    }
}
