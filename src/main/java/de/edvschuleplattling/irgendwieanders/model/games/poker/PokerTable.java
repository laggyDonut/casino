package de.edvschuleplattling.irgendwieanders.model.games.poker;


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
    private String tableId;           // e.g. "Table-HighRoller-1"
    private long smallBlind;    // Rule: Forced bet 1 (e.g. 5€)
    private long bigBlind;      // Rule: Forced bet 2 (e.g. 10€)

    // --- STATE ---
    private PokerStage stage;         // Which phase are we in?
    private CardStack deck;          // The card stack (Server-internal!)
    private List<Card> communityCards = new ArrayList<>(); // The open cards in the middle (Board)

    private long pot;           // The current pot
    private long currentHighestBet; // The amount one has to pay to stay in ("To Call")

    // --- PLAYER MANAGEMENT ---
    private List<PokerPlayerConnection> players = new ArrayList<>(); // List of seats

    // --- POSITIONS ---
    private int currentTurnIndex;     // Pointer to player list: Who has to act now?
    private int dealerButtonIndex;    // Who has the Dealer Button?

    public PokerTable(String tableId, long smallBlind, long bigBlind) {
        this.tableId = tableId;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.stage = PokerStage.WAITING;
        this.deck = new CardStack();
        this.dealerButtonIndex = 0;
    }

}