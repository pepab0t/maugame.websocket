package dev.cerios.maugame.websocket.mapper;

import dev.cerios.maugame.websocket.message.ErrorMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExceptionMapper {

    ErrorMessage toErrorResponse(Exception exception);
}
