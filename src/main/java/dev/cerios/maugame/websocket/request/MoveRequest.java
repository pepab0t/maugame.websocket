package dev.cerios.maugame.websocket.request;

public class MoveRequest implements Request {

    private final MoveRequestBody move;

    public MoveRequest(MoveRequestBody move) {this.move = move;}

    @Override
    public RequestType requestType() {
        return RequestType.MOVE;
    }
}
