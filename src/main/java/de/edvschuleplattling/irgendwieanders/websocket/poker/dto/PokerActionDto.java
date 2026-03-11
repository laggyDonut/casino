package de.edvschuleplattling.irgendwieanders.websocket.poker.dto;

import de.simonaltschaeffl.poker.model.Action;
import lombok.Data;
import de.simonaltschaeffl.poker.model.ActionType;

public record PokerActionDto(
        long gameId,
        long playerId,
        ActionType actionType,
        long amount
) {}