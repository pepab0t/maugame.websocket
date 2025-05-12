package dev.cerios.maugame.websocket.service;

import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.websocket.event.DisablePlayerEvent;
import dev.cerios.maugame.websocket.event.DistributeEvent;
import dev.cerios.maugame.websocket.event.RegisterEvent;
import dev.cerios.maugame.websocket.storage.GameStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GameService {

    private final GameStorage gameStorage;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void registerPlayer(RegisterEvent event) {
        List<Action> actions = new LinkedList<>();
        var game = gameStorage.addPlayerToLatestGame(event.getPlayerId(), actions);

        if (game.getFreeCapacity() == 0) {
            try {
                actions.addAll(game.start());
            } catch (MauEngineBaseException e) {
                throw new RuntimeException("unexpected scenario", e);
            }
        }

        eventPublisher.publishEvent(new DistributeEvent(this, game.getAllPlayers(), actions));
    }

    @EventListener
    public synchronized void disablePlayer(DisablePlayerEvent event) {
        var game = gameStorage.getPlayersGame(event.getPlayerId());
        game.deactivatePlayer(event.getPlayerId());
        eventPublisher.publishEvent(new DistributeEvent(this, game.getAllPlayers(), null));
    }

//    @EventListener
    public synchronized void onPlayerMove(String message) {
        System.out.println(message);
    }
}
