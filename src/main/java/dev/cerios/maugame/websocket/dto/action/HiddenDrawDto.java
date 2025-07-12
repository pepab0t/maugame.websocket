package dev.cerios.maugame.websocket.dto.action;

import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.websocket.dto.player.PlayerDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class HiddenDrawDto extends PlayerActionDto {
    private final int count;

    public HiddenDrawDto(Action.ActionType actionType, PlayerDto playerDto, int count) {
        super(actionType, playerDto);
        this.count = count;
    }
}
