package dev.cerios.maugame.websocket.dto.player;

import lombok.Data;

@Data
public class PlayerDto {
    private final String username;
    private final boolean active;
}
