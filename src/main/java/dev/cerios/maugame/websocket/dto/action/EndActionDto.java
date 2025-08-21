package dev.cerios.maugame.websocket.dto.action;

import dev.cerios.maugame.mauengine.game.action.Action;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EndActionDto extends ActionDto {

    private final List<String> playerRank;

    public EndActionDto(Action.ActionType type, List<String> playerRank) {
        super(type);
        this.playerRank = playerRank;
    }
}
