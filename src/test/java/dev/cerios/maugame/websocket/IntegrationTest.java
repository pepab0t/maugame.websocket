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
import java.util.List;

import static dev.cerios.maugame.websocket.clientutils.JsonFactory.createReadyRequest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest {

    @LocalServerPort
    private int port;
    private TestClient client;

    static final String connectionUriTemplate = "ws://localhost:%d/game?user=%s";
    static final long TIMEOUT_MS = 5_000;

    @Autowired
    private LobbyHandler lobbyHandler;

    @BeforeEach
    void setUp() {
        client = new TestClient(createConnectionUri("testUser"), TIMEOUT_MS);
    }

    @Test
    void shouldReceiveRegisterActions() throws IOException, InterruptedException {
        // setup
        var client2 = new TestClient(createConnectionUri("anotherUser"), TIMEOUT_MS);
        List<String> messages1;
        List<String> messages2;

        // when
        try (var ignore1 = client.handshake().join();
             var ignore2 = client2.handshake().join()) {
            messages1 = client.get(3);
            messages2 = client2.get(2);
        }

        // then
        var m1_1 = messages1.getFirst();
        List<String> t1 = JsonPath.read(m1_1, "$[?(@.messageType == 'ACTION')].action[?(@.type == 'REGISTER_PLAYER')].playerDto.username");
        assertThat(t1).containsExactly("testUser");

        var m1_2 = messages1.get(1);
        List<String> t2 = JsonPath.read(m1_2, "$[?(@.messageType == 'ACTION')].action[?(@.type == 'PLAYERS')].players[*]");
        assertThat(t2).containsExactly("testUser");

        var m1_3 = messages1.get(2);
        List<String> t3 = JsonPath.read(m1_3, "$[?(@.messageType == 'ACTION')].action[?(@.type == 'REGISTER_PLAYER')].playerDto.username");
        assertThat(t3).containsExactly("anotherUser");

        var m2_1 = messages2.getFirst();
        List<String> t4 = JsonPath.read(m2_1, "$[?(@.messageType == 'ACTION')].action[?(@.type == 'REGISTER_PLAYER')].playerDto.username");
        assertThat(t4).containsExactly("anotherUser");

        var m2_2 = messages2.get(1);
        List<String> t5 = JsonPath.read(m2_2, "$[?(@.messageType == 'ACTION')].action[?(@.type == 'PLAYERS')].players[*]");
        assertThat(t5).containsExactly("testUser", "anotherUser");
    }

    @Test
    void testShouldGetReadyLobbyResponse() throws IOException, InterruptedException {
        var client2 = new TestClient(createConnectionUri("anotherUser"), TIMEOUT_MS);
        try (var session = client.handshake().join();
             var session2 = client2.handshake().join()) {

            client.get(3);
            session.sendMessage(new TextMessage(createReadyRequest()));
            client.get();

            client2.get(3);

            assertThat(JsonPath.parse(client.getReceivedMessages().getLast()).read("$['body']['bodyType']", String.class)).isEqualTo("READY");
            assertThat(JsonPath.parse(client.getReceivedMessages().getLast()).read("$['body']['username']", String.class)).isEqualTo("testUser");
            assertThat(JsonPath.parse(client2.getReceivedMessages().getLast()).read("$['body']['bodyType']", String.class)).isEqualTo("READY");
            assertThat(JsonPath.parse(client2.getReceivedMessages().getLast()).read("$['body']['username']", String.class)).isEqualTo("testUser");
        }
    }

    private String createConnectionUri(String username) {
        return String.format("ws://localhost:%d/game?user=%s", port, username);
    }
}
