package dev.cerios.maugame.websocket.dto.action;

import dev.cerios.maugame.mauengine.card.Card;
import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.websocket.dto.player.PlayerDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class PlayCardActionDto extends PlayerActionDto {
    private final Card card;

    public PlayCardActionDto(Action.ActionType type, PlayerDto playerDto, Card card) {
        super(type, playerDto);
        this.card = card;
    }
}
