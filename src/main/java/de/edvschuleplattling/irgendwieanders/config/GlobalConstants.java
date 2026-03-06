package de.edvschuleplattling.irgendwieanders.config;

public interface GlobalConstants {
    // Allgemeine Konstanten
    String APP_NAME = "IrgendwieAnders";
    String APP_VERSION = "1.0.0";

    // Sicherheitskonstanten
    int PASSWORD_MIN_LENGTH = 12;
    int PASSWORD_MAX_LENGTH = 255;

    //Konstanten
    long CASH_TO_POINTS_FACTOR = 1; // 1 Cent = 1 Point

    public static long cashToPoints(long amount) {
        return amount * CASH_TO_POINTS_FACTOR;
    }
    
    public static long pointsToCash(long pointsAmount){ return pointsAmount / CASH_TO_POINTS_FACTOR; }
}


