package dev.cerios.maugame.websocket.event;

import dev.cerios.maugame.mauengine.game.action.Action;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@ToString
public class DistributeEvent extends CustomApplicationEvent {
    private final List<String> players;
    private final List<Action> actions;

    public DistributeEvent(Object source, List<String> players, List<Action> actions) {
        super(source);
        this.players = players;
        this.actions = actions;
    }
}
