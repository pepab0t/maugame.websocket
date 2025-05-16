package dev.cerios.maugame.websocket.event;

import dev.cerios.maugame.websocket.request.PlayRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class PlayerPlayEvent extends ApplicationEvent {
    private final WebSocketSession session;
    private final PlayRequest playRequest;

    public PlayerPlayEvent(Object source, WebSocketSession session, PlayRequest playRequest) {
        super(source);
        this.session = session;
        this.playRequest = playRequest;
    }
}
