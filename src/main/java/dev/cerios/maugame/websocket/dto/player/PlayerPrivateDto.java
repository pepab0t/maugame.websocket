package dev.cerios.maugame.websocket.dto.player;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class PlayerPrivateDto extends PlayerDto {
    private final String playerId;

    public PlayerPrivateDto(String playerId, String username) {
        super(username);
        this.playerId = playerId;
    }
}
