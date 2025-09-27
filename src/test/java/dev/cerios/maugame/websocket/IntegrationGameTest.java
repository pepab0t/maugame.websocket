package dev.cerios.maugame.websocket;

import com.jayway.jsonpath.JsonPath;
import dev.cerios.maugame.websocket.clientutils.TestClient;
import lombok.SneakyThrows;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static dev.cerios.maugame.websocket.clientutils.JsonFactory.createReadyRequest;
import static dev.cerios.maugame.websocket.clientutils.TestClient.createMessageMatcher;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class IntegrationGameTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MauSettings settings;
    @Autowired
    private GameStorage gameStorage;
    @Autowired
    private PlayerSessionStorage storage;

    static final long TIMEOUT_MS = 2000;

    @BeforeEach
    public void setup() {
        settings.setMaxPlayers(3);
    }

    @AfterEach
    void tearDown() {
        gameStorage.clear();
        storage.clear();
    }

    @Test
    void whenGameStarts_thenShouldReceiveActionsInCorrectOrder() throws IOException {
        Predicate<String> messageMatcher = m -> true;
        // given
        var client1 = new TestClient(
                TestClient.createConnectionUri(port, "user1"),
                messageMatcher,
                TIMEOUT_MS
        );
        var client2 = new TestClient(
                TestClient.createConnectionUri(port, "user2"),
                messageMatcher,
                TIMEOUT_MS
        );

        try (var session1 = client1.handshakeWithCatch().join();
             var session2 = client2.handshakeWithCatch().join()) {
            client1.get(2);
            client2.get();

            session1.sendMessage(new TextMessage(createReadyRequest()));
            session2.sendMessage(new TextMessage(createReadyRequest()));

            client1.get(2);
            client2.get(2);

            var actions1 = client1.get(5);
            var actions2 = client2.get(5);

            assertThat(JsonPath.<String>read(actions1.get(0), "$.action.type")).isEqualTo("START_GAME");
            assertThat(JsonPath.<String>read(actions1.get(1), "$.action.type")).isEqualTo("START_PILE");
            assertThat(JsonPath.<String>read(actions1.get(2), "$.action.type")).isEqualTo("DRAW");
            assertThat(JsonPath.<String>read(actions1.get(3), "$.action.type")).isEqualTo("HIDDEN_DRAW");
            assertThat(JsonPath.<String>read(actions1.get(4), "$.action.type")).isEqualTo("PLAYER_SHIFT");

            assertThat(JsonPath.<String>read(actions2.get(0), "$.action.type")).isEqualTo("START_GAME");
            assertThat(JsonPath.<String>read(actions2.get(1), "$.action.type")).isEqualTo("START_PILE");
            assertThat(JsonPath.<String>read(actions2.get(2), "$.action.type")).isEqualTo("HIDDEN_DRAW");
            assertThat(JsonPath.<String>read(actions2.get(3), "$.action.type")).isEqualTo("DRAW");
            assertThat(JsonPath.<String>read(actions2.get(4), "$.action.type")).isEqualTo("PLAYER_SHIFT");
        }
    }

    @Test
    public void whenPlayerDisconnectsFromRunningGameAndProvidesPlayerIdInNewConnection_thenShouldBeReconnected() throws IOException, JSONException {
        var messageMatcher = createMessageMatcher("READY", "DISCONNECT", "RECONNECT", "START_GAME", "playerId");
        var client1 = new TestClient(TestClient.createConnectionUri(port, "user1"), messageMatcher, TIMEOUT_MS);
        var client2 = new TestClient(TestClient.createConnectionUri(port, "user2"), messageMatcher, TIMEOUT_MS);
        var client3 = new TestClient(TestClient.createConnectionUri(port, "user3"), messageMatcher, TIMEOUT_MS);

        WebSocketSession session1 = null;
        WebSocketSession session2 = null;
        WebSocketSession session3 = null;

        var readyRequest = new TextMessage(createReadyRequest());

        try {
            session1 = client1.handshake().join();
            session2 = client2.handshake().join();
            session3 = client3.handshake().join();

            var playerId1 = JsonPath.<String>read(client1.get(), "$.action.playerDto.playerId");
            client2.get();
            client3.get();

            session1.sendMessage(readyRequest);
            session2.sendMessage(readyRequest);
            session3.sendMessage(readyRequest);

            client1.get(4);
            client2.get(4);
            client3.get(4);

            session1.close();

            var disconnect2 = client2.get();
            var disconnect3 = client3.get();

            new TestClient(
                    String.format("ws://localhost:%d/game?user=%s&player=%s", port, "user1", playerId1),
                    messageMatcher,
                    TIMEOUT_MS
            ).handshakeWithCatch().join();

            var reconnect2 = client2.get();
            var reconnect3 = client3.get();

            // then
            var expectedDisconnect = """
                    {
                        "messageType": "SERVER_MESSAGE",
                        "body": {
                            "bodyType": "DISCONNECT",
                            "username": "user1"
                        }
                    }
                    """;
            var expectedReconnect = """
                    {
                        "messageType": "SERVER_MESSAGE",
                        "body": {
                            "bodyType": "RECONNECT",
                            "username": "user1"
                        }
                    }
                    """;
            JSONAssert.assertEquals(expectedDisconnect, disconnect2, JSONCompareMode.STRICT);
            JSONAssert.assertEquals(expectedDisconnect, disconnect3, JSONCompareMode.STRICT);
            JSONAssert.assertEquals(expectedReconnect, reconnect2, JSONCompareMode.STRICT);
            JSONAssert.assertEquals(expectedReconnect, reconnect3, JSONCompareMode.STRICT);
        } finally {
            if (session1 != null)
                session1.close();
            if (session2 != null)
                session2.close();
            if (session3 != null)
                session3.close();
        }

    }

    @SneakyThrows
    @Test
    public void whenPlayerReconnects_thenShouldReceiveMultipleActions() {
        Predicate<String> messageMatcher = m -> m.contains("READY") ||
                m.contains("DISCONNECT") ||
                m.contains("RECONNECT") ||
                m.contains("START_GAME") ||
                m.contains("playerId");
        var client1 = new TestClient(TestClient.createConnectionUri(port, "user1"), messageMatcher, TIMEOUT_MS);
        var client2 = new TestClient(TestClient.createConnectionUri(port, "user2"), messageMatcher, TIMEOUT_MS);
        var client3 = new TestClient(TestClient.createConnectionUri(port, "user3"), messageMatcher, TIMEOUT_MS);

        WebSocketSession session1 = null;
        WebSocketSession session2 = null;
        WebSocketSession session3 = null;
        WebSocketSession sessionReconnect = null;

        var readyRequest = new TextMessage(createReadyRequest());
        try {
            session1 = client1.handshake().join();
            session2 = client2.handshake().join();
            session3 = client3.handshake().join();

            var playerId1 = JsonPath.<String>read(client1.get(), "$.action.playerDto.playerId");
            client2.get();
            client3.get();

            session1.sendMessage(readyRequest);
            session2.sendMessage(readyRequest);
            session3.sendMessage(readyRequest);

            // 3x ready and start game
            client1.get(4);
            client2.get(4);
            client3.get(4);

            session1.close();

            // disconnect message
            client2.get();
            client3.get();

            var clientReconnect = new TestClient(
                    String.format("ws://localhost:%d/game?user=%s&player=%s", port, "user1", playerId1),
                    m -> m.contains("ACTION"),
                    TIMEOUT_MS
            );
            sessionReconnect = clientReconnect.handshake().join();
            var reconnectedMessages = clientReconnect.get(7);

            // then
            var drawOptions = Set.of("HIDDEN_DRAW", "DRAW");
            assertThat(JsonPath.<String>read(reconnectedMessages.get(0), "$.action.type")).isEqualTo("START_GAME");
            assertThat(JsonPath.<String>read(reconnectedMessages.get(1), "$.action.type")).isEqualTo("START_PILE");
            assertThat(JsonPath.<String>read(reconnectedMessages.get(2), "$.action.type")).isIn(drawOptions);
            assertThat(JsonPath.<String>read(reconnectedMessages.get(3), "$.action.type")).isIn(drawOptions);
            assertThat(JsonPath.<String>read(reconnectedMessages.get(4), "$.action.type")).isIn(drawOptions);
            assertThat(JsonPath.<String>read(reconnectedMessages.get(5), "$.action.type")).isEqualTo("PLAYER_SHIFT");
            assertThat(JsonPath.<String>read(reconnectedMessages.get(6), "$.action.type")).isEqualTo("PLAYER_RANK");
        } finally {
            var it = Stream.of(session1, session2, session3, sessionReconnect)
                    .filter(Objects::nonNull).iterator();
            while (it.hasNext()) {
                it.next().close();
            }
        }
    }
}
