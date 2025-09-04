package dev.cerios.maugame.websocket.wshandler;

import dev.cerios.maugame.websocket.exception.InvalidHandshakeException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class ParameterParser {

    private final Validator validator;

    public ConnectionParameters parse(Map<String, Object> attributes) throws InvalidHandshakeException {
        var username = safelyConvert(attributes.get("user"), this::mapString, null);
        var playerId = safelyConvert(attributes.get("player"), this::mapString, null);
        var lobbyName = safelyConvert(attributes.get("lobby"), this::mapString, null);
        var isNew = safelyConvert(attributes.get("new"), x -> x.equals("true"), false);
        var isPrivate = safelyConvert(attributes.get("private"), x -> x.equals("true"), false);

        var cp = new ConnectionParameters(
                username,
                Optional.ofNullable(playerId),
                Optional.ofNullable(lobbyName),
                isNew,
                isPrivate
        );

        validateConnectionParams(cp);

        return cp;
    }

    private <T> T safelyConvert(Object obj, Function<Object, T> converter, T defaultValue) {
        if  (obj == null) {
            return defaultValue;
        }
        return converter.apply(obj);
    }

    private String mapString(Object value) {
        var out = value.toString();
        return out.isBlank() ? null : out;
    }

    private void validateConnectionParams(ConnectionParameters cp) throws InvalidHandshakeException {
        var constraints = validator.validate(cp);
        if (!constraints.isEmpty()) {
            throw new InvalidHandshakeException(constraints.stream().map(ConstraintViolation::getMessage).toList());
        }
    }

    public record ConnectionParameters(
            @NotNull @Size(max = 32) String username,
            Optional<@Size(max = 100) String> playerId,
            Optional<@Size(max = 50) String> lobbyName,
            Boolean isNew,
            Boolean isPrivate
    ) {
        public Operation decideOperation() {
            if (playerId.isPresent()) {
                return Operation.RECONNECT;
            }
            if (lobbyName.isPresent()) {
                if (isNew) {
                    if (isPrivate) {
                        return Operation.CREATE;
                    }
                    return Operation.CREATE;
                } else {
                    return Operation.CONNECT_CUSTOM;
                }
            }
            return Operation.CONNECT_RANDOM;
        }
    }

    public enum Operation {
        RECONNECT,
        CONNECT_RANDOM,
        CONNECT_CUSTOM,
        CREATE;
    }
}
