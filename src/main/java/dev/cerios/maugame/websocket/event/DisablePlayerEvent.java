package dev.cerios.maugame.websocket.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class DisablePlayerEvent extends ApplicationEvent {

    @Getter
    private final String playerId;

    public DisablePlayerEvent(Object source, String playerId) {
        super(source);
        this.playerId = playerId;
    }
}
