package de.edvschuleplattling.irgendwieanders.model.games.Roulette;

import java.util.concurrent.ThreadLocalRandom;

public class RouletteWheel {

    public int spin() {
        return ThreadLocalRandom.current().nextInt(0, 37);
    }

    public static String colorOf(int number) {
        if (number == 0) return "GREEN";
        return number % 2 == 0 ? "BLACK" : "RED";
    }
}
