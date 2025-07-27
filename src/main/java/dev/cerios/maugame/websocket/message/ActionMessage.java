package dev.cerios.maugame.websocket.message;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dev.cerios.maugame.websocket.dto.action.ActionDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonPropertyOrder({"messageType", "action"})
public class ActionMessage implements Message {
    private final MessageType messageType = MessageType.ACTION;
    private final ActionDto action;
}
