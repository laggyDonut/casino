package de.edvschuleplattling.irgendwieanders.model.games.Roulette;

public final class RouletteMath {

    private RouletteMath() {}

    public static long calculateWin(RouletteBet bet, int number) {

        return switch (bet.getType()) {
            case NUMBER -> bet.getValue() == number ? bet.getAmount() * 36 : 0;
            case EVEN -> number != 0 && number % 2 == 0 ? bet.getAmount() * 2 : 0;
            case ODD -> number % 2 == 1 ? bet.getAmount() * 2 : 0;
            case DOZEN -> {
                int dozen = (number - 1) / 12 + 1;
                yield bet.getValue() == dozen ? bet.getAmount() * 3 : 0;
            }
            case COLOR -> 0; // Erweiterbar
        };
    }
}
