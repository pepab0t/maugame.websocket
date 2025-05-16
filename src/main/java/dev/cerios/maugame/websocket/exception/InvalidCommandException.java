package dev.cerios.maugame.websocket.exception;

public class InvalidCommandException extends ServerException {
    public InvalidCommandException(String message) {
        super(message);
    }
}
