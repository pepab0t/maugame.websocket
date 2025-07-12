package dev.cerios.maugame.websocket.mapper;

import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.websocket.dto.player.PlayerDto;
import dev.cerios.maugame.websocket.dto.player.PlayerPrivateDto;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface PlayerMapper {
    PlayerDto toPublicDto(Player player);
    PlayerPrivateDto toPrivateDto(Player player);
}
