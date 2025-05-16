package dev.cerios.maugame.websocket.request;

import dev.cerios.maugame.websocket.exception.InvalidCommandException;

public enum RequestType {
    PLAY, DRAW, PASS;

    public static RequestType fromString(String type) throws InvalidCommandException {
        try {
            return RequestType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandException(e.getMessage());
        }
    }
}
