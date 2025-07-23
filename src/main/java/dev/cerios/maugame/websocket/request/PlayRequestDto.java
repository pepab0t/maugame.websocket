package dev.cerios.maugame.websocket.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.cerios.maugame.mauengine.card.Card;
import dev.cerios.maugame.mauengine.card.Color;

public record PlayRequestDto(
        RequestType type,
        Card card,
        @JsonProperty(defaultValue = "null") Color nextColor
) implements Request {
}
