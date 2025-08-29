package dev.cerios.maugame.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class WebsocketApplicationTests {

    @Autowired
    RequestProcessor requestProcessor;

    @MockitoBean
    PlayerSessionStorage storage;

    @Test
    void contextLoads() {
    }

    @Test
    void testParseRequest() {
        String json = """
                {
                    "requestType": "MOVE",
                    "move": {
                        "moveType": "PLAY"
                    }
                }
                """;
        var session = mock(StandardWebSocketSession.class);
        when(storage.getPlayer(any())).thenReturn("123");
        when(session.getId()).thenReturn("session1");

        requestProcessor.process(session.getId(), json);
    }
}
