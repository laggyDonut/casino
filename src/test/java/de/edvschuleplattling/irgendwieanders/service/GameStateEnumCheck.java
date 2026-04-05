package de.edvschuleplattling.irgendwieanders.service;

import de.simonaltschaeffl.poker.model.GameState;
import org.junit.jupiter.api.Test;

public class GameStateEnumCheck {
    @Test
    public void checkEnum() {
        System.out.println("Checking GamePhase enum values:");
        GameState.GamePhase phase = GameState.GamePhase.PRE_GAME;
        System.out.println("Found PRE_GAME: " + phase);

        for (GameState.GamePhase p : GameState.GamePhase.values()) {
            System.out.println(p.name());
        }
    }
}
