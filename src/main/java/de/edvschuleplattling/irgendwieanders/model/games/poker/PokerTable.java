package de.edvschuleplattling.irgendwieanders.model.games.poker;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PokerTable {
    // --- CONFIGURATION ---
    @NotBlank
    private String tableId;           // e.g. "Table-HighRoller-1"
    @Min(0)
    private long smallBlind;    // Rule: Forced bet 1 (e.g. 5€)
    @Min(0)
    private long bigBlind;      // Rule: Forced bet 2 (e.g. 10€)

    // --- STATE ---
    @NotNull
    private PokerStage stage;         // Which phase are we in?
    @NotNull
    private CardStack deck;          // The card stack (Server-internal!)
    @Size(max = 5)
    private List<Card> communityCards = new ArrayList<>(); // The open cards in the middle (Board)

    @Min(0)
    private long pot;           // The current pot
    @Min(0)
    private long currentHighestBet; // The amount one has to pay to stay in ("To Call")

    // --- PLAYER MANAGEMENT ---
    @Size(min = 2, max = 9)
    private List<PokerPlayerConnection> players = new ArrayList<>(); // List of seats

    // --- POSITIONS ---
    private int currentTurnIndex;     // Pointer to player list: Who has to act now?
    private int dealerButtonIndex;    // Who has the Dealer Button?

}