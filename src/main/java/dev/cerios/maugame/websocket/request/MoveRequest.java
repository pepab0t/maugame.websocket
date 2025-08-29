package dev.cerios.maugame.websocket.request;

import lombok.Data;

@Data
public abstract class MoveRequest {
    protected final RequestType.MoveType moveType;

    public MoveRequest(RequestType.MoveType moveType) {
        this.moveType = moveType;
    }
}
