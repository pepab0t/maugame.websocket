package dev.cerios.maugame.websocket.dto.action;

import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.websocket.dto.player.PlayerDto;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class PlayerShiftActionDto extends PlayerActionDto {

    private final long expireAtMs;

    public PlayerShiftActionDto(Action.ActionType type, PlayerDto playerDto, long expireAtMs) {
        super(type, playerDto);
        this.expireAtMs = expireAtMs;
    }
}
