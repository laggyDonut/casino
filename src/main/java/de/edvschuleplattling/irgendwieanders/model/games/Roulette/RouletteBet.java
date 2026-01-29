package de.edvschuleplattling.irgendwieanders.model.games.Roulette;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RouletteBet {

    private RouletteBetType type;
    private int value;
    private long amount;
}
