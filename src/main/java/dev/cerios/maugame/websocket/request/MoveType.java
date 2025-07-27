package dev.cerios.maugame.websocket.request;

import dev.cerios.maugame.websocket.exception.InvalidCommandException;

public enum MoveType {
    PLAY,
    DRAW,
    PASS;

    public static MoveType fromString(String type) throws InvalidCommandException {
        try {
            return MoveType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandException(e.getMessage());
        }
    }
}
