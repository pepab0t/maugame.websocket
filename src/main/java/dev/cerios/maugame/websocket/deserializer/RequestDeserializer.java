package dev.cerios.maugame.websocket.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import dev.cerios.maugame.websocket.request.Request;

import java.io.IOException;

public class RequestDeserializer extends JsonDeserializer<Request> {
    @Override
    public Request deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return null;
    }
}
