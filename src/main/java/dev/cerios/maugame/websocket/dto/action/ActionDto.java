package dev.cerios.maugame.websocket.dto.action;

import dev.cerios.maugame.mauengine.game.action.Action;
import lombok.Data;

@Data
public class ActionDto {
    private final Action.ActionType type;
}
