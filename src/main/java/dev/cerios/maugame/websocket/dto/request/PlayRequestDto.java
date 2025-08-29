package dev.cerios.maugame.websocket.dto.request;

import dev.cerios.maugame.mauengine.card.Card;
import dev.cerios.maugame.mauengine.card.Color;

public record PlayRequestDto(Card card, Color nextColor) {
}
