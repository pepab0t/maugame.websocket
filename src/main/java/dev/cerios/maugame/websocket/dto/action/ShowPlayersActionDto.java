package dev.cerios.maugame.websocket.dto.action;

import dev.cerios.maugame.mauengine.game.action.Action;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ShowPlayersActionDto extends ActionDto {
    private final List<String> players;

    public ShowPlayersActionDto(Action.ActionType type, List<String> players) {
        super(type);
        this.players = players;
    }
}
