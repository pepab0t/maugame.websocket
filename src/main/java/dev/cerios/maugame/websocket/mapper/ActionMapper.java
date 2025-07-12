package dev.cerios.maugame.websocket.mapper;

import dev.cerios.maugame.mauengine.game.action.*;
import dev.cerios.maugame.websocket.dto.action.*;
import dev.cerios.maugame.websocket.dto.player.PlayerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public abstract class ActionMapper {
    @Autowired
    protected PlayerMapper playerMapper;

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    abstract PlayerActionDto toDto(ActivateAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    abstract PlayerActionDto toDto(DeactivateAction action);

    abstract DrawActionDto toDto(DrawAction action);

    abstract ActionDto toDto(EndAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    abstract HiddenDrawDto toDto(HiddenDrawAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    abstract PlayerActionDto toDto(LoseAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    abstract PlayerActionDto toDto(PassAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    abstract PlayCardActionDto toDto(PlayCardAction action);

    @Mapping(target = "players", expression = "java(action.players().stream().map(p -> p.getUsername()).toList())")
    abstract ShowPlayersActionDto toDto(PlayersAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    abstract PlayerActionDto toDto(PlayerShiftAction action);

    @Mapping(target = "playerDto", expression = "java(mapRegisterPlayer(action))")
    abstract PlayerActionDto toDto(RegisterAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    abstract PlayerActionDto toDto(RemovePlayerAction action);

    @Mapping(target = "players", expression = "java(action.playerRank().stream().toList())")
    abstract ShowPlayersActionDto toDto(SendRankAction action);

    abstract GameIdActionDto toDto(StartAction action);

    abstract CardActionDto toDto(StartPileAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    abstract PlayerActionDto toDto(WinAction action);

    protected PlayerDto mapRegisterPlayer(RegisterAction action) {
        return action.isMe() ? playerMapper.toPrivateDto(action.player()) : playerMapper.toPublicDto(action.player());
    }
}
