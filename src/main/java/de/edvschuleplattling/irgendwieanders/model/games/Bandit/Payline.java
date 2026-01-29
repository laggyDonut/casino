package de.edvschuleplattling.irgendwieanders.model.games.Bandit;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Payline {

    private List<Integer> positions;

    public boolean matches(List<Symbol> reels) {
        Symbol first = reels.get(positions.get(0));
        return positions.stream()
                .allMatch(i -> reels.get(i) == first || reels.get(i) == Symbol.WILD);
    }
}
