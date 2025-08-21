package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.websocket.dto.request.PlayRequestDto;
import dev.cerios.maugame.websocket.exception.InvalidCommandException;
import dev.cerios.maugame.websocket.mapper.ExceptionMapper;
import dev.cerios.maugame.websocket.request.MoveType;
import dev.cerios.maugame.websocket.request.RequestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestProcessor {

    private final ObjectMapper objectMapper;
    private final ExceptionMapper exceptionMapper;
    private final PlayerSessionStorage bridge;
    private final GameService gameService;

    public void process(WebSocketSession session, String request) {
        try {
            JsonNode root = objectMapper.readTree(request);
            var requestType = RequestType.fromString(root.get("requestType").asText());
            var playerId = bridge.getPlayer(session.getId());

            switch (requestType) {
                case MOVE -> processMove(/*TODO handle null*/root.get("move"), playerId);
                case CONTROL -> throw new InvalidCommandException("not supported");
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
        var moveType = MoveType.fromString(node.get("moveType").asText());
        switch (moveType) {
            case PLAY -> {
                var dto = objectMapper.convertValue(node, PlayRequestDto.class);
                gameService.playCard(playerId, dto.card(), dto.nextColor());
            }
            case DRAW -> gameService.drawCard(playerId);
            case PASS -> gameService.pass(playerId);
        }
    }

}
