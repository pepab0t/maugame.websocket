package dev.cerios.maugame.websocket.response;


import dev.cerios.maugame.mauengine.game.action.Action;

import java.util.List;

public class ActionResponse implements Response {
    private final List<Action> actions;

    public ActionResponse(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public ResponseType getResponseType() {
        return ResponseType.GAME_ACTION;
    }

    @Override
    public List<Action> getBody() {
        return actions;
    }
}
