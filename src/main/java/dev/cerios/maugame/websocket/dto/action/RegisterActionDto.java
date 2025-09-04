package dev.cerios.maugame.websocket.dto.action;

import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.websocket.dto.player.PlayerDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class RegisterActionDto extends PlayerActionDto {
    private final UUID gameId;

    public RegisterActionDto(
            UUID gameId,
            Action.ActionType type,
            PlayerDto playerDto
    ) {
        super(type, playerDto);
        this.gameId = gameId;
    }
}
