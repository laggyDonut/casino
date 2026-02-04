package de.edvschuleplattling.irgendwieanders.model.games.poker;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Userprofile;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PokerPlayerConnection {

    // --- IDENTITÄT ---
    @NotNull
    private Userprofile user;

    // User Chips on Table
    @Min(0)
    private long tableChips;  // Die Chips, die er aktuell auf dem Tisch hat (können sich ändern)

    // --- SPIEL-STATUS (Live) ---
    @NotNull
    @Size(max = 2)
    private List<Card> holeCards;     // Die 2 geheimen Karten auf der Hand
    @Min(0)
    private long currentRoundBet; // Was er in der aktuellen Wettrunde gesetzt hat

    private boolean hasFolded;        // Hat er aufgegeben?
    private boolean isAllIn;          // Hat er alles gesetzt?

}
