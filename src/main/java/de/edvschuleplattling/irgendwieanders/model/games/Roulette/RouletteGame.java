package de.edvschuleplattling.irgendwieanders.model.games.Roulette;

public class RouletteGame {

    private final RouletteWheel wheel = new RouletteWheel();

    public RouletteSpinResult spin(RoulettePlayerSession session, RouletteBet bet) {
        if (session.isFinished()) {
            throw new IllegalStateException("Roulette session already finished");
        }
        if (bet == null || bet.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid bet");
        }

        session.setBet(bet);

        int number = wheel.spin();
        long win = RouletteMath.calculateWin(bet, number);

        session.setRolledNumber(number);
        session.setRolledColor(RouletteWheel.colorOf(number));
        session.setWinAmount(win);
        session.setFinished(true);

        return new RouletteSpinResult(
                number,
                session.getRolledColor(),
                win - bet.getAmount()
        );
    }
}
