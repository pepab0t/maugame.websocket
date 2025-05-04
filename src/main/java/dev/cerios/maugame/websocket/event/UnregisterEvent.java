package dev.cerios.maugame.websocket.event;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UnregisterEvent extends CustomApplicationEvent {

    private final String playerId;

    public UnregisterEvent(Object source, String playerId) {
        super(source);
        this.playerId = playerId;
    }
}
