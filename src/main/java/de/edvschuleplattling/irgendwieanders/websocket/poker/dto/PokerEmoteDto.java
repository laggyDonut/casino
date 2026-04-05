package de.edvschuleplattling.irgendwieanders.websocket.poker.dto;

public record PokerEmoteDto(
        String gameId,
        String playerId,
        String emote,
        String targetId
) {}
