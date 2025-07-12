package dev.cerios.maugame.websocket.dto.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.cerios.maugame.mauengine.card.Card;
import dev.cerios.maugame.mauengine.game.action.Action;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class DrawActionDto extends ActionDto {
    private final List<Card> cards;

    public DrawActionDto(Action.ActionType actionType, List<Card> cards) {
        super(actionType);
        this.cards = Collections.unmodifiableList(cards);
    }
}
