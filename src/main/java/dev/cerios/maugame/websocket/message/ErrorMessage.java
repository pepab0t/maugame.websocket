package dev.cerios.maugame.websocket.message;

import lombok.Getter;

import java.time.Instant;

public class ErrorMessage implements Message {
    @Getter
    private final MessageType messageType = MessageType.ERROR;
    @Getter
    private final ExceptionBody exceptionBody;

    public ErrorMessage(final Exception exception) {
        this.exceptionBody = new ExceptionBody(exception.getClass().getSimpleName(), exception.getMessage(), Instant.now());
    }

    public record ExceptionBody(String name, String message, Instant timestamp) {
    }
}
