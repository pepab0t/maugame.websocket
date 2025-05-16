package dev.cerios.maugame.websocket.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.websocket.event.PlayerDrawEvent;
import dev.cerios.maugame.websocket.event.PlayerPassEvent;
import dev.cerios.maugame.websocket.event.PlayerPlayEvent;
import dev.cerios.maugame.websocket.exception.InvalidCommandException;
import dev.cerios.maugame.websocket.mapper.ExceptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RequestProcessor {

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ExceptionMapper exceptionMapper;

    public void process(WebSocketSession session, String request) {
        try {
            JsonNode root = objectMapper.readTree(request);
            RequestType requestType = RequestType.fromString(root.get("type").asText());

            switch (requestType) {
                case PLAY -> {
                    var playRequest = objectMapper.treeToValue(root, PlayRequest.class);
                    eventPublisher.publishEvent(new PlayerPlayEvent(this, session, playRequest));
                }
                case DRAW -> eventPublisher.publishEvent(new PlayerDrawEvent(this, session));
                case PASS -> eventPublisher.publishEvent(new PlayerPassEvent(this, session));
            };
        } catch (JsonProcessingException | InvalidCommandException e) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(exceptionMapper.toErrorResponse(e))));
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }
}
