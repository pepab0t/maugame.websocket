package dev.cerios.maugame.websocket.dto.action;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.websocket.dto.player.PlayerDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class RemovePlayerActionDto extends PlayerActionDto {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final int recycledCards;

    public RemovePlayerActionDto(Action.ActionType type, PlayerDto playerDto, int recycledCards) {
        super(type, playerDto);
        this.recycledCards = recycledCards;
    }
}
