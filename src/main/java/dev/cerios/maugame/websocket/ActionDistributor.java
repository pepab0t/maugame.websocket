package dev.cerios.maugame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.mauengine.game.action.Action;
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
    private final Lock lock = new ReentrantLock();

    public void distribute(Player player, Action action) {
        executor.execute(() -> {
            var session = bridge.getSession(player);
            try {
                lock.lock();
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(action)));
            } catch (IOException ex) {
                log.warn("cannot send proper message", ex);
            } finally {
                lock.unlock();
            }
        });
    }
}
