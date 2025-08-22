package dev.cerios.maugame.websocket;

import ch.qos.logback.core.joran.sanity.Pair;
import dev.cerios.maugame.mauengine.exception.GameException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.GameFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class LobbyHandler {

    private final GameFactory gameFactory;
    private final MauSettings mauSettings;
    private final ActionDistributor actionDistributor;

    private final SequencedMap<String, Game> gameQueue = new LinkedHashMap<>();
    private final Map<String, Pair<ReadyState, Game>> players = new HashMap<>();
    private final Map<String, List<Pair<String, ReadyState>>> games = new HashMap<>();


    public String registerPlayer(String username) throws GameException {
        return null;
    }

    private record Pair<T1, T2>(T1 val1, T2 val2) {
    }

    @Getter
    @Setter
    private static class ReadyState {
        private boolean ready = false;
    }
}
