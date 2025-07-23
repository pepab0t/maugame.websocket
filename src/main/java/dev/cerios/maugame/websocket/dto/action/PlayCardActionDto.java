package dev.cerios.maugame.websocket.dto.action;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.cerios.maugame.mauengine.card.Card;
import dev.cerios.maugame.mauengine.card.Color;
import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.websocket.dto.player.PlayerDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayCardActionDto extends PlayerActionDto {
    private final Card card;
    private final Color nextColor;

    public PlayCardActionDto(Action.ActionType type, PlayerDto playerDto, Card card, Color nextColor) {
        super(type, playerDto);
        this.card = card;
        this.nextColor = nextColor;
    }
}
