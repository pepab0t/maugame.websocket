package dev.cerios.maugame.websocket.event;

import dev.cerios.maugame.websocket.request.DrawRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class PlayerDrawEvent extends ApplicationEvent {

    private final WebSocketSession session;

    public PlayerDrawEvent(Object source, WebSocketSession session) {
        super(source);
        this.session = session;
    }
}
