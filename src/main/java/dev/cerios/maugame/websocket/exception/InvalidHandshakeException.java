package dev.cerios.maugame.websocket.exception;

public class InvalidHandshakeException extends ServerException {
    public InvalidHandshakeException(String message) {
        super(message);
    }

    public InvalidHandshakeException(Iterable<String> errors) {
        super("Several errors occurred:\n" + String.join("\n", errors));
    }
}
