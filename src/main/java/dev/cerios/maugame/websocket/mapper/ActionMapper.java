package dev.cerios.maugame.websocket.mapper;

import dev.cerios.maugame.mauengine.card.CardType;
import dev.cerios.maugame.mauengine.game.Player;
import dev.cerios.maugame.mauengine.game.action.*;
import dev.cerios.maugame.websocket.dto.action.*;
import dev.cerios.maugame.websocket.dto.player.PlayerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, imports = CardType.class)
public abstract class ActionMapper {
    @Autowired
    protected PlayerMapper playerMapper;

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    public abstract PlayerActionDto toDto(ActivateAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    public abstract PlayerActionDto toDto(DeactivateAction action);

    @Mapping(target = "cards", source = "action.cardsDrawn")
    public abstract DrawActionDto toDto(DrawAction action);

    public abstract ActionDto toDto(EndAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    public abstract HiddenDrawDto toDto(HiddenDrawAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    public abstract PlayerActionDto toDto(LoseAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    public abstract PlayerActionDto toDto(PassAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    @Mapping(target = "type", source = "action", qualifiedByName = "typeGetter")
    @Mapping(target = "nextColor", expression = "java(action.card().type() == CardType.QUEEN ? action.nextColor() : null)")
    public abstract PlayCardActionDto toDto(PlayCardAction action);

    @Mapping(target = "players", expression = "java(action.players().stream().map(p -> p.getUsername()).toList())")
    public abstract ShowPlayersActionDto toDto(PlayersAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    public abstract PlayerActionDto toDto(PlayerShiftAction action);

    @Mapping(target = "playerDto", expression = "java(mapRegisterPlayer(action))")
    public abstract PlayerActionDto toDto(RegisterAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    public abstract PlayerActionDto toDto(RemovePlayerAction action);

    @Mapping(target = "players", expression = "java(action.playerRank().stream().toList())")
    public abstract ShowPlayersActionDto toDto(SendRankAction action);

    public abstract GameIdActionDto toDto(StartAction action);

    public abstract CardActionDto toDto(StartPileAction action);

    @Mapping(target = "playerDto", expression = "java(playerMapper.toPublicDto(action.player()))")
    public abstract PlayerActionDto toDto(WinAction action);

    protected PlayerDto mapRegisterPlayer(RegisterAction action) {
        return action.isMe() ? playerMapper.toPrivateDto(action.player()) : playerMapper.toPublicDto(action.player());
    }

    @Named("typeGetter")
    protected Action.ActionType getType(Action action) {
        return action.getType();
    }
}
