package dev.cerios.maugame.websocket.dto.action;

import dev.cerios.maugame.mauengine.card.Card;
import dev.cerios.maugame.mauengine.game.action.Action;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CardActionDto extends ActionDto {
    private final Card card;

    public CardActionDto(Action.ActionType type, Card card) {
        super(type);
        this.card = card;
    }
}
