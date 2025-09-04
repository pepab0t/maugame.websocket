package dev.cerios.maugame.websocket.exception;

public class LobbyAlreadyExistsException extends ServerException {
    public LobbyAlreadyExistsException(String lobbyName) {
        super(lobbyName);
    }
}
