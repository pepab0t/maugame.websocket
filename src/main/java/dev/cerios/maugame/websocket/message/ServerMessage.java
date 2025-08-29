package dev.cerios.maugame.websocket.message;

import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@ToString
@Getter
public class ServerMessage implements Message {
    private final MessageType messageType = MessageType.SERVER_MESSAGE;
    private final MessageBody body;

    public ServerMessage(MessageBody body) {
        this.body = body;
    }

    public static ServerMessage ofReady(String username) {
        return new ServerMessage(new ReadyMessageBody(username));
    }

    public static ServerMessage ofUnready(String username) {
        return new ServerMessage(new UnreadyMessageBody(username));
    }

    public static ServerMessage ofInfo(String message) {
        return new ServerMessage(new InfoMessageBody(message));
    }

    @Getter
    @ToString
    public abstract static class MessageBody {

        private final BodyType bodyType;

        MessageBody(BodyType bodyType) {
            this.bodyType = bodyType;
        }
    }

    public enum BodyType {
        READY,
        UNREADY,
        INFO
    }

    @Getter
    @ToString(callSuper = true)
    public static class InfoMessageBody extends MessageBody {
        private final String message;
        private final Instant timestamp = Instant.now();

        public InfoMessageBody(String message) {
            super(BodyType.INFO);
            this.message = message;
        }
    }

    @Getter
    @ToString(callSuper = true)
    public static class ReadyMessageBody extends MessageBody {
        private final String username;

        public ReadyMessageBody(String username) {
            super(BodyType.READY);
            this.username = username;
        }

    }

    @Getter
    @ToString(callSuper = true)
    public static class UnreadyMessageBody extends MessageBody {
        private final String username;

        public UnreadyMessageBody(String username) {
            super(BodyType.UNREADY);
            this.username = username;
        }

    }
}
