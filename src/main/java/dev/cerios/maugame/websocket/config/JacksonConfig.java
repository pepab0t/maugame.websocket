package dev.cerios.maugame.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.cerios.maugame.websocket.deserializer.RequestDeserializer;
import dev.cerios.maugame.websocket.request.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

//    @Bean
//    public ObjectMapper customizedObjectMapper(ObjectMapper objectMapper) {
//        var module = new SimpleModule();
//
//        module.addDeserializer(Request.class, new RequestDeserializer());
//
//        return objectMapper;
//    }
}
