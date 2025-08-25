package dev.cerios.maugame.websocket;

import com.jayway.jsonpath.JsonPath;
import dev.cerios.maugame.websocket.clientutils.TestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

import static dev.cerios.maugame.websocket.clientutils.JsonFactory.createReadyRequest;
import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest {

    @LocalServerPort
    private int port;
    private TestClient client;

    static final String connectionUriTemplate = "ws://localhost:%d/game?user=%s";

    @Autowired
    private LobbyHandler lobbyHandler;

    @BeforeEach
    void setUp() {
        client = new TestClient(createConnectionUri("testUser"));
    }

    @Test
    void testShouldGetReadyLobbyResponse() throws IOException, InterruptedException {
        var client2 = new TestClient(createConnectionUri("anotherUser"));
        try (var session = client.handshake().join();
             var session2 = client2.handshake().join()) {

            client.get(2).forEach(out::println);
            session.sendMessage(new TextMessage(createReadyRequest()));
            assertThat(JsonPath.parse(client.get()).read("$['body']['message']").toString()).isEqualTo("ready");
            assertThat(JsonPath.parse(client2.get(3).getLast()).read("$['body']['message']").toString()).isEqualTo("ready");

        }
    }

    private String createConnectionUri(String username) {
        return String.format("ws://localhost:%d/game?user=%s", port, username);
    }
}
