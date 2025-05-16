package dev.cerios.maugame.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.action.Action;
import dev.cerios.maugame.websocket.ActionDistributor;
import dev.cerios.maugame.websocket.event.*;
import dev.cerios.maugame.websocket.mapper.ExceptionMapper;
import dev.cerios.maugame.websocket.storage.GameStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameStorage gameStorage;
    private final ApplicationEventPublisher eventPublisher;
    private final ActionDistributor actionDistributor;
    private final ExceptionMapper exceptionMapper;
    private final ObjectMapper objectMapper;

    @EventListener
    public synchronized void registerPlayer(RegisterEvent event) {
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
        var action = game.deactivatePlayer(event.getPlayerId());
        eventPublisher.publishEvent(new DistributeEvent(this, game.getAllPlayers(), List.of(action)));
    }

    @EventListener
    public synchronized void onPlayMove(PlayerPlayEvent event) throws IOException {
        try {
            String player = actionDistributor.getPlayerBySession(event.getSession().getId());
            var game = gameStorage.getPlayersGame(player);
            var move = game.createPlayMove(player, event.getPlayRequest().card(), Optional.ofNullable(event.getPlayRequest().nextColor()));
            eventPublisher.publishEvent(new DistributeEvent(this, game.getAllPlayers(), move.execute()));
        } catch (MauEngineBaseException | RuntimeException e) {
            event.getSession().sendMessage(
                    new TextMessage(objectMapper.writeValueAsString(exceptionMapper.toErrorResponse(e)))
            );
        }
    }

    @EventListener
    public synchronized void onDrawMove(PlayerDrawEvent event) throws IOException {
        try {
            String player = actionDistributor.getPlayerBySession(event.getSession().getId());
            var game = gameStorage.getPlayersGame(player);
            var move = game.createDrawMove(player, 1);
            eventPublisher.publishEvent(new DistributeEvent(this, game.getAllPlayers(), move.execute()));
        } catch (MauEngineBaseException | RuntimeException e) {
            event.getSession().sendMessage(
                    new TextMessage(objectMapper.writeValueAsString(exceptionMapper.toErrorResponse(e)))
            );
        }
    }

    @EventListener
    public synchronized void onPassMove(PlayerPassEvent event) throws IOException {
        try {
            String player = actionDistributor.getPlayerBySession(event.getSession().getId());
            var game = gameStorage.getPlayersGame(player);
            var move = game.createPassMove(player);
            eventPublisher.publishEvent(new DistributeEvent(this, game.getAllPlayers(), move.execute()));
        } catch (MauEngineBaseException | RuntimeException e) {
            event.getSession().sendMessage(
                    new TextMessage(objectMapper.writeValueAsString(exceptionMapper.toErrorResponse(e)))
            );
        }
    }
}
