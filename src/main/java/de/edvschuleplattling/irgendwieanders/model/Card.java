package de.edvschuleplattling.irgendwieanders.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Card {
    private final Suit suit;
    private final Rank rank;
}
