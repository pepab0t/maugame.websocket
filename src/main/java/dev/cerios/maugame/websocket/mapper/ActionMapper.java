package dev.cerios.maugame.websocket.mapper;

import dev.cerios.maugame.mauengine.game.action.*;
import dev.cerios.maugame.websocket.dto.action.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActionMapper {
    PlayerActionDto toDto(ActivateAction action);
    PlayerActionDto toDto(DeactivateAction action);
    DrawActionDto toDto(DrawAction action);
    ActionDto toDto(EndAction action);
    HiddenDrawDto toDto(HiddenDrawAction action);
    PlayerActionDto toDto(LoseAction action);
    PlayerActionDto toDto(PassAction action);
    PlayCardActionDto toDto(PlayCardAction action);
    ShowPlayersActionDto toDto(PlayersAction action);
    PlayerActionDto toDto(PlayerShiftAction action);
    PlayerActionDto toDto(RegisterAction action);
    PlayerActionDto toDto(RemovePlayerAction action);
    ShowPlayersActionDto toDto(SendRankAction action);
    GameIdActionDto toDto(StartAction action);
    CardActionDto toDto(StartPileAction action);
    PlayerActionDto toDto(WinAction action);
}
