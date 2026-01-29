package de.edvschuleplattling.irgendwieanders.model.games.Bandit;

import java.util.List;

public final class BanditMath {

    private BanditMath() {}

    public static long calculateWin(
            List<Symbol> reels,
            List<Payline> paylines,
            long bet
    ) {
        long totalWin = 0;

        for (Payline payline : paylines) {
            if (payline.matches(reels)) {
                totalWin += bet * getMultiplier(reels);
            }
        }
        return totalWin;
    }

    private static int getMultiplier(List<Symbol> reels) {
        if (reels.stream().allMatch(s -> s == Symbol.SEVEN)) {
            return 100;
        }
        if (reels.contains(Symbol.WILD)) {
            return 10;
        }
        return 5;
    }
}
