package de.edvschuleplattling.irgendwieanders.config;

public interface GlobalConstants {
    // Allgemeine Konstanten
    String APP_NAME = "IrgendwieAnders";
    String APP_VERSION = "1.0.0";

    // Sicherheitskonstanten
    int PASSWORD_MIN_LENGTH = 12;
    int PASSWORD_MAX_LENGTH = 255;

    // Konstanten
    String CURR_KEY = "EUR";
    long CURR_TO_COINS_FACTOR = 100; // 1 EUR = 100 Coins

    static long currToCoins(long amount) {
        return CURR_TO_COINS_FACTOR*amount;
    }
}
