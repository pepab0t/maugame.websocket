package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.mauengine.game.action.*;
import dev.cerios.maugame.websocket.dto.action.ActionDto;
import dev.cerios.maugame.websocket.event.ClearPlayerEvent;
import dev.cerios.maugame.websocket.exception.MauTimeoutException;
import dev.cerios.maugame.websocket.mapper.ActionMapper;
import dev.cerios.maugame.websocket.message.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.cerios.maugame.mauengine.game.action.Action.ActionType.END_GAME;
import static dev.cerios.maugame.websocket.PlayerSessionStorage.PlayerConcurrentSources;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActionDistributor {

    private final ExecutorService executor;
    private final PlayerSessionStorage bridge;
    private final ObjectMapper objectMapper;
    private final ActionMapper actionMapper;
    private final ApplicationEventPublisher publisher;

    private final Lock lock = new ReentrantLock();

    public void distribute(Player player, Action action) {
        var ps = bridge.getPlayerSources(player.getPlayerId());
        try {
            executor.execute(() -> distributeAction(ps, player));
            ps.queue().put(action);
        } catch (RejectedExecutionException | InterruptedException e) {
            log.info("distribution {} to player {} rejected", action, player);
        }
    }

    private void distributeAction(PlayerConcurrentSources ps, Player player) {
        try {
            ps.lock().lock();
            var a = ps.queue().take();
            var session = bridge.getSession(player);
            var dto = mapAction(a);

            sendMessage(
                    session,
                    new TextMessage(objectMapper.writeValueAsString(Message.createActionMessage(dto)))
            );

            if (a.getType() == END_GAME)
                publisher.publishEvent(new ClearPlayerEvent(this, session.getId(), player));
        } catch (JsonProcessingException e) {
            log.info("error during serialization", e);
        } catch (MauTimeoutException | InterruptedException ignore) {
        } finally {
            ps.lock().unlock();
        }
    }

    private void sendMessage(WebSocketSession session, TextMessage message) {
        try {
            lock.lock();
            session.sendMessage(message);
        } catch (IOException | IllegalStateException exception) {
            log.debug("error sending message {}", exception.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private ActionDto mapAction(Action action) {
        return switch (action) {
            case ActivateAction a -> actionMapper.toDto(a);
            case DeactivateAction a -> actionMapper.toDto(a);
            case DrawAction a -> actionMapper.toDto(a);
            case EndAction a -> actionMapper.toDto(a);
            case HiddenDrawAction a -> actionMapper.toDto(a);
            case LoseAction a -> actionMapper.toDto(a);
            case PassAction a -> actionMapper.toDto(a);
            case PlayCardAction a -> actionMapper.toDto(a);
            case PlayersAction a -> actionMapper.toDto(a);
            case PlayerShiftAction a -> actionMapper.toDto(a);
            case RegisterAction a -> actionMapper.toDto(a);
            case RemovePlayerAction a -> actionMapper.toDto(a);
            case SendRankAction a -> actionMapper.toDto(a);
            case StartAction a -> actionMapper.toDto(a);
            case StartPileAction a -> actionMapper.toDto(a);
            case WinAction a -> actionMapper.toDto(a);
            default -> throw new IllegalStateException("Unexpected value: " + action);
        };
    }
}
