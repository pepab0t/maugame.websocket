package dev.cerios.maugame.websocket;

import com.jayway.jsonpath.JsonPath;
import dev.cerios.maugame.websocket.clientutils.TestClient;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.function.Predicate;

import static dev.cerios.maugame.websocket.clientutils.JsonFactory.createReadyRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationGameTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MauSettings settings;

    static final long TIMEOUT_MS = 2000;

    @BeforeEach
    public void setup() {
        settings.setMaxPlayers(3);
    }

    @Test
    public void whenPlayerDisconnectsFromRunningGameAndProvidesPlayerIdInNewConnection_thenShouldBeReconnected() throws IOException, JSONException {
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
}
