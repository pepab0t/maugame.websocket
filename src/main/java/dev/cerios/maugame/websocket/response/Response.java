package dev.cerios.maugame.websocket.response;

public interface Response {
    ResponseType getResponseType();
    Object getBody();
}
