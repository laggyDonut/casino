package de.edvschuleplattling.irgendwieanders.model.games.Bandit;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BanditSpinResult {

    private List<Symbol> reels;
    private long valueChange;
    private boolean jackpot;
}
