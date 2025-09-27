package dev.cerios.maugame.websocket.dto.action;

import dev.cerios.maugame.mauengine.game.action.Action;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DestroyActionDto extends ActionDto {
    private final UUID gameId;

    public DestroyActionDto(Action.ActionType type, UUID gameId) {
        super(type);
        this.gameId = gameId;
    }
}
