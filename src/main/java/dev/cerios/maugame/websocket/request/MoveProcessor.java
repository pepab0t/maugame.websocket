package dev.cerios.maugame.websocket.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.websocket.GameService;
import dev.cerios.maugame.websocket.SessionGameBridge;
import dev.cerios.maugame.websocket.exception.InvalidCommandException;
import dev.cerios.maugame.websocket.mapper.ExceptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MoveProcessor {

    private final ObjectMapper objectMapper;
    private final ExceptionMapper exceptionMapper;
    private final SessionGameBridge bridge;
    private final GameService gameService;

    public void process(WebSocketSession session, String request) {
        try {
            JsonNode root = objectMapper.readTree(request);
            RequestType requestType = RequestType.fromString(root.get("type").asText());
            var player = bridge.getPlayer(session.getId());

            switch (requestType) {
                case PLAY -> {
                    var dto = objectMapper.convertValue(root, PlayRequestDto.class);
                    gameService.playCard(player, dto.card(), dto.nextColor());
                }
                case DRAW -> gameService.drawCard(player);
                case PASS -> gameService.pass(player);
            }
        } catch (JsonProcessingException | InvalidCommandException | MauEngineBaseException e) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(exceptionMapper.toErrorResponse(e))));
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }
}
