package dev.cerios.maugame.websocket.mapper;

import dev.cerios.maugame.mauengine.game.action.DrawAction;
import dev.cerios.maugame.websocket.dto.HiddenDrawAction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DrawActionMapper {

    @Mapping(target = "count", expression = "java(source.cardsDrawn().size())")
    HiddenDrawAction toHidden(DrawAction source);
}
