package dev.cerios.maugame.websocket.message;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ServerMessage implements Message {
    private final MessageType messageType = MessageType.SERVER_MESSAGE;
    private final ServerMessageBody body;

    public ServerMessage(String message) {
        this.body = new ServerMessageBody(message, Instant.now());
    }

    public record ServerMessageBody(String message, Instant timestamp) {
    }
}
