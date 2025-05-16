package dev.cerios.maugame.websocket.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class PlayerPassEvent extends ApplicationEvent {
    private final WebSocketSession session;

    public PlayerPassEvent(Object source, WebSocketSession session) {
        super(source);
        this.session = session;
    }
}
