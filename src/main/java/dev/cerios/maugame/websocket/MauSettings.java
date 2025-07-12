package dev.cerios.maugame.websocket;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("maumaugame")
@Component
@Data
public class MauSettings {
    @Min(2)
    private int maxPlayers = 2;
}
