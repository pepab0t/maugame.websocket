package dev.cerios.maugame.websocket.clientutils;

public class JsonFactory {

    public static String createReadyRequest() {
        return """
                {
                    "requestType": "CONTROL",
                    "control": {
                        "controlType": "READY"
                    }
                }
                """;
    }
}
