package de.edvschuleplattling.irgendwieanders.model;

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
    private Userprofiles user;

    // User Chips on Table
    private long tableChips;  // Die Chips, die er aktuell auf dem Tisch hat (können sich ändern)

    // --- SPIEL-STATUS (Live) ---
    private long stack;         // Die Chips, die er vor sich liegen hat (Buy-In)
    private List<Card> holeCards;     // Die 2 geheimen Karten auf der Hand
    private long currentRoundBet; // Was er in der aktuellen Wettrunde gesetzt hat

    private boolean hasFolded;        // Hat er aufgegeben?
    private boolean isAllIn;          // Hat er alles gesetzt?

    public PokerPlayerConnection(Userprofiles user, long initialStack) {
        this.user = user;
        this.stack = initialStack;
        this.tableChips = initialStack; // Assuming table chips = stack initially
        this.holeCards = new ArrayList<>();
    }

    public void addCard(Card card) {
        if (holeCards == null) {
            holeCards = new ArrayList<>();
        }
        holeCards.add(card);
    }

    public void resetForNewRound() {
        this.holeCards = new ArrayList<>();
        this.currentRoundBet = 0;
        this.hasFolded = false;
        this.isAllIn = false;
    }
}
