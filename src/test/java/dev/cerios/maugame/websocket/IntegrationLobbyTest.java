package dev.cerios.maugame.websocket;

import dev.cerios.maugame.mauengine.exception.MauEngineBaseException;
import dev.cerios.maugame.mauengine.game.Game;
import dev.cerios.maugame.mauengine.game.GameFactory;
import dev.cerios.maugame.websocket.clientutils.TestClient;
import org.json.JSONException;
import org.junit.jupiter.api.*;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static dev.cerios.maugame.websocket.clientutils.JsonFactory.createReadyRequest;
import static java.lang.System.out;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.Random.class)
class IntegrationLobbyTest {

    @LocalServerPort
    private int port;
    private TestClient client;

    static final long TIMEOUT_MS = 3_000;

    @MockitoSpyBean
    private GameFactory gameFactory;

    @Autowired
    private LobbyHandler lobbyHandler;

    @Autowired
    private MauSettings mauSettings;

    @BeforeEach
    void setUp() {
        client = new TestClient(createConnectionUri("user1"), TIMEOUT_MS);
        mauSettings.setMaxPlayers(3);
    }

    @AfterEach
    void tearDown() {
        lobbyHandler.clear();
    }

    @Test
    void shouldReceiveRegisterActions() throws IOException, InterruptedException {
        // setup
        var client2 = new TestClient(createConnectionUri("user2"), TIMEOUT_MS);
        List<String> messages1;
        List<String> messages2;

        // when
        try (var ignore1 = client.handshake().join(); var ignore2 = client2.handshake().join()) {
            messages1 = client.get(3);
            messages2 = client2.get(2);
        }

        messages1.forEach(System.out::println);

        // then
        assertRegisterAction(messages1.getFirst(), "user1");
        assertPlayersAction(messages1.get(1), List.of("user1"));
        assertRegisterAction(messages1.get(2), "user2");

        assertRegisterAction(messages2.getFirst(), "user2");
        assertPlayersAction(messages2.get(1), List.of("user1", "user2"));
    }

    @Test
    void testShouldGetReadyLobbyResponse() throws IOException, InterruptedException {
        var client2 = new TestClient(createConnectionUri("user2"), TIMEOUT_MS);
        try (var session = client.handshake().join(); var ignore = client2.handshake().join()) {

            client.get(3);
            session.sendMessage(new TextMessage(createReadyRequest()));
            client.get();

            client2.get(3);
        }

        assertReadyMessage(client.getReceivedMessages().getLast(), "user1");
        assertReadyMessage(client2.getReceivedMessages().getLast(), "user1");
        out.println("END");
    }

    @Test
    void when2PlayersInLobbyAndFirstDisconnects_thenRemainingGetsUnready() throws IOException, InterruptedException {
        // given
        var client2 = new TestClient(
                createConnectionUri("user2"),
                message -> message.contains("REMOVE") || message.contains("READY"),
                TIMEOUT_MS
        );

        // when
        try (var s1 = client.handshake().join(); var s2 = client2.handshake().join()) {
            s2.sendMessage(new TextMessage(createReadyRequest()));
            client2.get();
            s1.close();
            client2.get(2);
        }

        // then
        var messages = client2.getReceivedMessages();
        messages.forEach(out::println);
        assertReadyMessage(messages.getFirst(), "user2");
        assertUnreadyMessage(messages.get(1), "user2");
        assertRemovePlayerAction(messages.get(2), "user1");
    }

    @Test
    void whenAllPlayerReady_thenGameStartsAndLobbyDisappears() throws IOException, MauEngineBaseException, InterruptedException {
        // given
        var client2 = new TestClient(
                createConnectionUri("user2"),
                TIMEOUT_MS
        );
        var client3 = new TestClient(
                createConnectionUri("user3"),
                message -> message.matches(".*:\\s*\"READY.*"),
                TIMEOUT_MS
        );
        var readyRequest = new TextMessage(createReadyRequest());
        var gameMock = mock(Game.class);
        when(gameFactory.createGame(any(int.class), any(int.class))).thenReturn(gameMock);
        when(gameMock.registerPlayer(any(String.class), any()))
                .thenReturn("id1", "id2", "id3");

        when(gameMock.getUuid()).thenReturn(UUID.randomUUID());

        // when
        try (var s1 = client.handshake().join(); var s2 = client2.handshake().join(); var s3 = client3.handshake().join()) {
            s1.sendMessage(readyRequest);
            client3.get();
            s2.sendMessage(readyRequest);
            client3.get();
            s3.sendMessage(readyRequest);
            client3.get();

            // then
            verify(gameMock, timeout(TIMEOUT_MS)).start();
        }
    }

    @Test
    void whenPlayerDisconnects_thenUpdateReadyOnlyThoseChanged() throws IOException, InterruptedException {
        // given
        Predicate<String> readyMatcher = message -> message.matches(".*:\\s*\"(UN)?READY.*");
        var client2 = new TestClient(
                createConnectionUri("user2"),
                readyMatcher,
                TIMEOUT_MS
        );
        var client3 = new TestClient(
                createConnectionUri("user3"),
                readyMatcher,
                TIMEOUT_MS
        );

        try (var s1 = client.handshake().join(); var s2 = client2.handshake().join(); var s3 = client3.handshake().join()) {
            s2.sendMessage(new TextMessage(createReadyRequest()));
            client2.get();
            client3.get();

            // when
            s1.close();

            client2.get();
            client3.get();
        }

        var messages2 = client2.getReceivedMessages();
        assertReadyMessage(messages2.getFirst(), "user2");
        assertUnreadyMessage(messages2.get(1), "user2");

        var messages3 = client3.getReceivedMessages();
        assertReadyMessage(messages3.getFirst(), "user2");
        assertUnreadyMessage(messages3.get(1), "user2");
    }

    private void assertRegisterAction(String jsonMessage, String expectedUsername) {
        var expectedJson = """
                {
                    "messageType": "ACTION",
                    "action": {
                        "type": "REGISTER_PLAYER",
                        "playerDto": {
                            "username": "%s"
                        }
                    }
                }
                """.formatted(expectedUsername);
        try {
            JSONAssert.assertEquals(expectedJson, jsonMessage, JSONCompareMode.STRICT_ORDER);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertPlayersAction(String jsonMessage, List<String> expectedPlayers) {
        var expectedJson = """
                {
                    "messageType": "ACTION",
                    "action": {
                        "type": "PLAYERS",
                        "players": [%s]
                    }
                }
                """.formatted(expectedPlayers.stream().map(p -> "\"" + p + "\"").collect(Collectors.joining(", ")));
        try {
            JSONAssert.assertEquals(expectedJson, jsonMessage, JSONCompareMode.STRICT);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertRemovePlayerAction(String jsonMessage, String expectedUsername) {
        var expectedJson = """
                {
                    "messageType": "ACTION",
                    "action": {
                        "type": "REMOVE_PLAYER",
                        "playerDto": {
                            "username": "%s"
                        }
                    }
                }
                """.formatted(expectedUsername);
        try {
            JSONAssert.assertEquals(expectedJson, jsonMessage, JSONCompareMode.STRICT_ORDER);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertReadyMessage(String jsonMessage, String expectedUsername) {
        try {
            JSONAssert.assertEquals(
                    """
                            {
                              "messageType": "SERVER_MESSAGE",
                              "body": {
                                "bodyType": "READY",
                                "username": "%s"
                              }
                            }
                            """.formatted(expectedUsername), jsonMessage, JSONCompareMode.NON_EXTENSIBLE
            );
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertUnreadyMessage(String jsonMessage, String expectedUsername) {
        try {
            JSONAssert.assertEquals(
                    """
                            {
                              "messageType": "SERVER_MESSAGE",
                              "body": {
                                "bodyType": "UNREADY",
                                "username": "%s"
                              }
                            }
                            """.formatted(expectedUsername), jsonMessage, JSONCompareMode.NON_EXTENSIBLE
            );
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String createConnectionUri(String username) {
        return String.format("ws://localhost:%d/game?user=%s", port, username);
    }
}
