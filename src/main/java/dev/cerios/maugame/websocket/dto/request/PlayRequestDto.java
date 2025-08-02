package dev.cerios.maugame.websocket.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.cerios.maugame.mauengine.card.Card;
import dev.cerios.maugame.mauengine.card.Color;
import dev.cerios.maugame.websocket.request.MoveRequest;
import dev.cerios.maugame.websocket.request.MoveRequestBody;
import dev.cerios.maugame.websocket.request.MoveType;
import dev.cerios.maugame.websocket.request.Request;

public record PlayRequestDto(
        Card card,
        @JsonProperty(defaultValue = "null") Color nextColor
) implements MoveRequestBody {

    public MoveType getMoveType() {
        return MoveType.PLAY;
    }
}
