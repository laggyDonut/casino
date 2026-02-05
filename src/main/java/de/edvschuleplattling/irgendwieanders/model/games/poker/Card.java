package de.edvschuleplattling.irgendwieanders.model.games.poker;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Card {
    @NotNull
    private final Suit suit;
    @NotNull
    private final Rank rank;
}
