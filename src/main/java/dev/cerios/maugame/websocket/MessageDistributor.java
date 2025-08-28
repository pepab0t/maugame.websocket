package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.mauengine.game.action.*;
import dev.cerios.maugame.websocket.dto.action.ActionDto;
import dev.cerios.maugame.websocket.exception.MauTimeoutException;
import dev.cerios.maugame.websocket.mapper.ActionMapper;
import dev.cerios.maugame.websocket.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.cerios.maugame.websocket.PlayerSessionStorage.PlayerConcurrentSources;

@Component
@Slf4j
public class MessageDistributor {

    private final ExecutorService executor;
    private final PlayerSessionStorage storage;
    private final ObjectMapper objectMapper;
    private final ActionMapper actionMapper;

    private final Lock lock = new ReentrantLock();

    public MessageDistributor(ExecutorService executor, PlayerSessionStorage storage, ObjectMapper objectMapper, ActionMapper actionMapper) {
        this.executor = executor;
        this.storage = storage;
        this.objectMapper = objectMapper;
        this.actionMapper = actionMapper;
    }

    public void distribute(Player player, Action action) {
        try {
            lock.lock();
            var ps = storage.getPlayerSources(player.getPlayerId());
            if (ps == null)
                return;
            ps.queue().add(() -> distributeAction(player.getPlayerId(), action));
            System.out.printf("action %s added to player's %s queue%n", action, player.getPlayerId());
            executor.execute(() -> {
                try {
                    ps.lock().lock();
                    ps.queue().remove().run();
                } finally {
                    ps.lock().unlock();
                }
            });
        } finally {
            lock.unlock();
        }
    }

    public void enqueueMessage(String playerId, Message message) {
        final var ps = storage.getPlayerSources(playerId);
        if (ps == null)
            return;
        ps.queue().add(() -> storage.getSessionInstant(playerId)
                .ifPresentOrElse(
                        session -> {
                            try {
                                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                            } catch (IOException e) {
                                log.info("Message {} could not be serialized.", message, e);
                            }
                        },
                        () -> log.debug("Message {} will not be sent, since session for player {} not found.", message, playerId)
                )
        );
        executor.execute(() -> {
            try {
                ps.lock().lock();
                ps.queue().remove().run();
            } finally {
                ps.lock().unlock();
            }
        });
    }

    private void distributeAction(String playerId, Action a) {
        try {
            var session = storage.getSession(playerId);
            var dto = mapAction(a);

            sendMessage(
                    session,
                    new TextMessage(objectMapper.writeValueAsString(Message.createActionMessage(dto)))
            );

            if (a.getType() == Action.ActionType.END_GAME)
                storage.removePlayerById(playerId);
            if (a.getType() == Action.ActionType.DISQUALIFIED)
                storage.removePlayerById(playerId);

        } catch (JsonProcessingException e) {
            log.info("error during serialization", e);
        } catch (MauTimeoutException ignore) {
        }
    }

    private void sendMessage(WebSocketSession session, TextMessage message) {
        try {
            lock.lock();
            session.sendMessage(message);
        } catch (IOException | IllegalStateException exception) {
            log.trace("error sending message {}", message.getPayload(), exception);
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
            case DisqualifiedAction a -> actionMapper.toDto(a);
            default -> throw new IllegalStateException("Unexpected value: " + action);
        };
    }
}
