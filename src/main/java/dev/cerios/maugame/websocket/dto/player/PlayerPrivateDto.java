package dev.cerios.maugame.websocket.dto.player;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class PlayerPrivateDto extends PlayerDto {
    private final String playerId;

    public PlayerPrivateDto(String playerId, String username, boolean active) {
        super(username, active);
        this.playerId = playerId;
    }
}
