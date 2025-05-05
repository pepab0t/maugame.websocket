package dev.cerios.maugame.websocket.event;

import dev.cerios.maugame.mauengine.game.action.Action;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;
import java.util.List;

@Getter
@ToString
public class DistributeEvent extends CustomApplicationEvent {
    private final Collection<String> players;
    private final Collection<Action> actions;

    public DistributeEvent(Object source, Collection<String> players, Collection<Action> actions) {
        super(source);
        this.players = players;
        this.actions = actions;
    }
}
