package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.websocket.dto.request.PlayRequestDto;
import dev.cerios.maugame.websocket.exception.InvalidCommandException;
import dev.cerios.maugame.websocket.mapper.ExceptionMapper;
import dev.cerios.maugame.websocket.request.RequestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestProcessor {

    private final ObjectMapper objectMapper;
    private final ExceptionMapper exceptionMapper;
    private final PlayerSessionStorage storage;
    private final GameService gameService;
    private final LobbyHandler lobbyHandler;

    public void process(WebSocketSession session, String request) {
        try {
            JsonNode root = objectMapper.readTree(request);
            var requestType = objectMapper.convertValue(root.get("requestType"),  RequestType.class);
            var playerId = storage.getPlayer(session.getId());

            switch (requestType) {
                case MOVE -> processMove(
                        Optional.ofNullable(root.get("move")).orElseThrow(() -> new InvalidCommandException("Missing field: move")),
                        playerId
                );
                case CONTROL -> processControl(
                        Optional.ofNullable(root.get("control")).orElseThrow(() -> new InvalidCommandException("Missing field: control")),
                        playerId
                );
            }
        } catch (JsonProcessingException | InvalidCommandException | RuntimeException | MauEngineBaseException e) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(exceptionMapper.toErrorResponse(e))));
            } catch (IOException ex) {
                log.error("error sending websocket message", ex);
            }
        }
    }

    private void processMove(JsonNode node, final String playerId) throws InvalidCommandException, MauEngineBaseException {
        var moveType = objectMapper.convertValue(node.get("moveType"), RequestType.MoveType.class);
        switch (moveType) {
            case PLAY -> {
                var dto = objectMapper.convertValue(node, PlayRequestDto.class);
                gameService.playCard(playerId, dto.getCard(), dto.getNextColor());
            }
            case DRAW -> gameService.drawCard(playerId);
            case PASS -> gameService.pass(playerId);
        }
    }

    private void processControl(JsonNode node, String playerId) throws InvalidCommandException, MauEngineBaseException {
        var controlType = objectMapper.convertValue(node.get("controlType"), RequestType.ControlType.class);

        switch (controlType) {
            case READY -> lobbyHandler.setPlayerReady(playerId);
            default -> throw new InvalidCommandException("Invalid control type");
        }
    }
}
