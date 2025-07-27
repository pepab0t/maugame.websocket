package dev.cerios.maugame.websocket.request;

@FunctionalInterface
public interface MoveRequestBody {
    MoveType getMoveType();
}
