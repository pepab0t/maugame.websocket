package dev.cerios.maugame.websocket.event;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UnregisterEvent extends CustomApplicationEvent {

    private final String sessionId;

    public UnregisterEvent(Object source, String sessionId) {
        super(source);
        this.sessionId = sessionId;
    }
}
