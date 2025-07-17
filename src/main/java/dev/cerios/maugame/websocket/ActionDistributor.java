package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.mauengine.game.action.*;
import dev.cerios.maugame.websocket.dto.action.ActionDto;
import dev.cerios.maugame.websocket.mapper.ActionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActionDistributor {

    @Qualifier("actionService")
    private final ExecutorService executor;
    private final SessionGameBridge bridge;
    private final ObjectMapper objectMapper;
    private final ActionMapper actionMapper;
    private final Lock lock = new ReentrantLock();
    private final Semaphore semaphore = new Semaphore(1);

    public void distribute(Player player, Action action) {
        executor.execute(() -> {
            // player lock
            var playerLock = bridge.getPlayerLock(player.getPlayerId());
            ActionDto dto;
            WebSocketSession session;
            try {
                playerLock.lock();
                session = bridge.getSession(player);
                dto = mapAction(action);
                TextMessage message;
                message = new TextMessage(objectMapper.writeValueAsString(dto));
                sendMessage(session, message);
            } catch (JsonProcessingException e) {
                log.info("error during serialization", e);
            } finally {
                playerLock.unlock();
            }
        });
    }

    private void sendMessage(WebSocketSession session, TextMessage message) {
        try {
            lock.lock();
            session.sendMessage(message);
        } catch (IOException | IllegalStateException ex) {
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
