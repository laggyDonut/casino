package de.edvschuleplattling.irgendwieanders.model.games.Bandit;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@AllArgsConstructor
public class Reel {

    private List<Symbol> symbols;

    public Symbol spin() {
        int index = ThreadLocalRandom.current().nextInt(symbols.size());
        return symbols.get(index);
    }
}
