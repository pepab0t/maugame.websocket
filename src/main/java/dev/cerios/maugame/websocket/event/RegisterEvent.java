package dev.cerios.maugame.websocket.event;

import lombok.Getter;
import lombok.ToString;
import org.springframework.web.socket.WebSocketSession;

@Getter
@ToString
public class RegisterEvent extends CustomApplicationEvent {
    private final WebSocketSession session;
    private final String playerId;

    public RegisterEvent(Object source, WebSocketSession session, String playerId) {
        super(source);
        this.session = session;
        this.playerId = playerId;
    }
}
