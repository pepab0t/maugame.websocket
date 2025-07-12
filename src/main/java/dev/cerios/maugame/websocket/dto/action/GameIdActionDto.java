package dev.cerios.maugame.websocket.dto.action;

import dev.cerios.maugame.mauengine.game.action.Action;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class GameIdActionDto extends ActionDto {
    private final String gameId;

    public GameIdActionDto(Action.ActionType actionType, String gameId) {
        super(actionType);
        this.gameId = gameId;
    }
}
