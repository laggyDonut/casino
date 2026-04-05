package de.edvschuleplattling.irgendwieanders.websocket.poker.dto;

import java.util.List;
import java.util.Map;

public record WinnerDto(
    List<String> winnerNames,
    List<String> winnerIds,
    Map<String, Integer> payouts,
    int potTotal
) {}
