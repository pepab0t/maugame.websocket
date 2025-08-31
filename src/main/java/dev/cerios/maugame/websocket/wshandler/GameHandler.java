package dev.cerios.maugame.websocket.wshandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.websocket.GameService;
import dev.cerios.maugame.websocket.RequestProcessor;
import dev.cerios.maugame.websocket.exception.InvalidHandshakeException;
import dev.cerios.maugame.websocket.exception.NotFoundException;
import dev.cerios.maugame.websocket.message.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Optional;

import static dev.cerios.maugame.websocket.message.Message.createErrorMessage;
import static dev.cerios.maugame.websocket.wshandler.ParameterParser.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameHandler extends TextWebSocketHandler {

    private final GameService gameService;
    private final RequestProcessor processor;
    private final ObjectMapper objectMapper;
    private final ParameterParser parameterParser;

    @Override
    public void afterConnectionEstablished(WebSocketSession s) {
        // TODO connection parameter parser
        final var session = new ConcurrentWebSocketSessionDecorator(s, 10_000, 4096);

        try {
            var cp = parameterParser.parse(session.getAttributes());
            switch (cp.decideOperation()) {
                case CONNECT_RANDOM -> gameService.registerPlayer(cp.username(), session);
                case CONNECT_CUSTOM -> gameService.registerPlayerToExistingCustomLobby(cp.username(), session, cp.lobbyName().get());
                case CREATE -> gameService.registerPlayerToNewCustomLobby(cp.username(), session, cp.lobbyName().get(), cp.isPrivate());
                case RECONNECT -> gameService.reconnectPlayer(cp.username(), session, cp.playerId().get());
            }
        } catch (InvalidHandshakeException | NotFoundException e) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Message.createErrorMessage(e))));
                session.close();
            } catch (IOException ex) {
                log.warn("Error interacting with session", ex);
            }
        }
    }

    record Params(Integer x) {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            gameService.disconnectPlayer(session.getId());
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        processor.process(session.getId(), message.getPayload());
    }
}
