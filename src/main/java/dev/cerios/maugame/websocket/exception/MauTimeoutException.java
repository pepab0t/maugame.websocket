package dev.cerios.maugame.websocket.exception;

public class MauTimeoutException extends RuntimeException {
    public MauTimeoutException(String message) {
        super(message);
    }

    public MauTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public MauTimeoutException(Throwable cause) {
        super(cause);
    }
}
