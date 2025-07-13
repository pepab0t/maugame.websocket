package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.mauengine.game.action.*;
import dev.cerios.maugame.websocket.dto.action.ActionDto;
import dev.cerios.maugame.websocket.mapper.ActionMapper;
import dev.cerios.maugame.websocket.mapper.ActionMapperImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
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

    public void distribute(Player player, Action action) {
        executor.execute(() -> {
            var session = bridge.getSession(player);
            var dto = mapAction(action);
            try {
                lock.lock();
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(dto)));
            } catch (IOException ex) {
                log.warn("cannot send proper message", ex);
            } finally {
                lock.unlock();
            }
        });
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
