package dev.cerios.maugame.websocket;

import dev.cerios.maugame.websocket.clientutils.TestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @LocalServerPort
    private int port;
    private TestClient client;

    @BeforeEach
    void setUp() {
        client = new TestClient(String.format("ws://localhost:%d/game?user=testUser", port));
    }

    @Test
    void name() throws IOException {
        try (var session = client.handshake().join()) {
            System.out.println(session.getRemoteAddress());
        }
    }
}
