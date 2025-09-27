package dev.cerios.maugame.websocket.dto.action;

import dev.cerios.maugame.mauengine.game.action.Action;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReadyActionDto extends ActionDto {
    private final String username;

    public ReadyActionDto(Action.ActionType type, String username) {
        super(type);
        this.username = username;
    }
}
