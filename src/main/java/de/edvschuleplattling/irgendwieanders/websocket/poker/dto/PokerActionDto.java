package de.edvschuleplattling.irgendwieanders.websocket.poker.dto;

import de.simonaltschaeffl.poker.model.ActionType;

public record PokerActionDto(
                String gameId,
                String playerId,
                String displayName,
                ActionType actionType,
                long amount) {
}