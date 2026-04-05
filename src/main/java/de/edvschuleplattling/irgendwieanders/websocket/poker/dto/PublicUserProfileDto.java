package de.edvschuleplattling.irgendwieanders.websocket.poker.dto;

import java.io.Serializable;

public record PublicUserProfileDto(
        Long userId,
        String displayName
) implements Serializable {}

