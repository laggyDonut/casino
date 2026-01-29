package de.edvschuleplattling.irgendwieanders.model.games.Bandit;

import java.util.List;

public class BanditGame {

    private final List<Reel> reels;
    private final List<Payline> paylines;

    public BanditGame(List<Reel> reels, List<Payline> paylines) {
        this.reels = reels;
        this.paylines = paylines;
    }

    public BanditSpinResult spin(BanditPlayerSession session, long betAmount) {
        if (session.isFinished()) {
            throw new IllegalStateException("Bandit session already finished");
        }
        if (betAmount <= 0) {
            throw new IllegalArgumentException("Bet must be greater than zero");
        }

        session.setBetAmount(betAmount);

        List<Symbol> result = reels.stream()
                .map(Reel::spin)
                .toList();

        long win = BanditMath.calculateWin(result, paylines, betAmount);

        session.setReels(result);
        session.setWinAmount(win);
        session.setFinished(true);

        return new BanditSpinResult(
                result,
                win - betAmount,
                win >= betAmount * 50
        );
    }
}
