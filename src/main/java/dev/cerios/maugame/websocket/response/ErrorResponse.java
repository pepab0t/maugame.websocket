package dev.cerios.maugame.websocket.response;

import java.time.Instant;

public class ErrorResponse implements Response {
    private final ExceptionBody exceptionBody;

    public ErrorResponse(final Exception exception) {
        this.exceptionBody = new ExceptionBody(exception.getClass().getSimpleName(), exception.getMessage(), Instant.now());
    }

    @Override
    public ResponseType getResponseType() {
        return null;
    }

    @Override
    public ExceptionBody getBody() {
        return exceptionBody;
    }

    public record ExceptionBody(String name, String message, Instant timestamp) {
    }
}
