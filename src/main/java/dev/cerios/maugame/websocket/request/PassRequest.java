package dev.cerios.maugame.websocket.request;

public class PassRequest implements Request {
    @Override
    public RequestType type() {
        return RequestType.PASS;
    }
}
