package de.edvschuleplattling.irgendwieanders.model.games.Roulette;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RouletteSpinResult {

    private int rolledNumber;
    private String color;
    private long valueChange;
}
