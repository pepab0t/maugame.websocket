package dev.cerios.maugame.websocket.mapper;

import dev.cerios.maugame.websocket.response.ErrorResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExceptionMapper {

    ErrorResponse toErrorResponse(Exception exception);
}
