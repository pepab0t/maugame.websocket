package dev.cerios.maugame.websocket.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.cerios.maugame.mauengine.card.Card;
import dev.cerios.maugame.mauengine.card.Color;
import dev.cerios.maugame.websocket.request.MoveRequest;
import dev.cerios.maugame.websocket.request.RequestType;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PlayRequestDto extends MoveRequest {

    private final Card card;
    private final Color nextColor;

    public PlayRequestDto(
            RequestType.MoveType moveType,
            Card card,
            @JsonProperty(defaultValue = "null") Color nextColor
    ) {
        super(moveType);
        this.card = card;
        this.nextColor = nextColor;
    }

}
