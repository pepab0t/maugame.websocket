package dev.cerios.maugame.websocket.service;

import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.websocket.ActionDistributor;
import dev.cerios.maugame.websocket.storage.GameStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GameService {

    private final GameStorage gameStorage;
    private final ActionDistributor actionDistributor;

    public void registerPlayer(String player) throws IOException {
        List<Action> actions = new LinkedList<>();

        var game = gameStorage.addPlayerToLatestGame(player, actions);

        if (game.getFreeCapacity() == 0) {
            try {
                actions.addAll(game.start());
            } catch (MauEngineBaseException e) {
                throw new RuntimeException("unexpected scenario", e);
            }
        }

        actionDistributor.distribute(game.getAllPlayers(), actions);
    }
}
