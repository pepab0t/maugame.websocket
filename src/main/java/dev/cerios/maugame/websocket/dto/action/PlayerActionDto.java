package dev.cerios.maugame.websocket.dto.action;

import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.websocket.dto.player.PlayerDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PlayerActionDto extends ActionDto {
    private final PlayerDto playerDto;

    public PlayerActionDto(Action.ActionType actionType, PlayerDto playerDto) {
        super(actionType);
        this.playerDto = playerDto;
    }
}
