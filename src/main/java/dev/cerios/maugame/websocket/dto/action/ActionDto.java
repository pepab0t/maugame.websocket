package dev.cerios.maugame.websocket.dto.action;

import lombok.Data;

import static dev.cerios.maugame.mauengine.game.action.Action.ActionType;

@Data
public class ActionDto {
    private final ActionType type;
}
