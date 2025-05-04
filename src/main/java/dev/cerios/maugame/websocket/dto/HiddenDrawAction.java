package dev.cerios.maugame.websocket.dto;

import dev.cerios.maugame.mauengine.game.action.Action;

public record HiddenDrawAction(String playerId, int count) implements Action {
    @Override
    public ActionType type() {
        return ActionType.DRAW;
    }
}
