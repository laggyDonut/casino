package de.edvschuleplattling.irgendwieanders.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PokerTable {
    // --- KONFIGURATION ---
    private String tableId;           // z.B. "Table-HighRoller-1"
    private long smallBlind;    // Regel: Zwangseinsatz 1 (z.B. 5€)
    private long bigBlind;      // Regel: Zwangseinsatz 2 (z.B. 10€)

    // --- ZUSTAND (State) ---
    private PokerStage stage;         // In welcher Phase sind wir?
    private CardStack deck;          // Der Kartenstapel (Server-intern!)
    private List<Card> communityCards = new ArrayList<>(); // Die offenen Karten in der Mitte (Board)

    private long pot;           // Der aktuelle Gewinn-Topf
    private long currentHighestBet; // Der Betrag, den man zahlen muss, um dabei zu bleiben ("To Call")

    // --- SPIELER-VERWALTUNG ---
    private List<PokerPlayerConnection> players = new ArrayList<>(); // Liste der Sitzplätze

    // --- POSITIONEN ---
    private int currentTurnIndex;     // Zeiger auf player-Liste: Wer muss jetzt handeln?
    private int dealerButtonIndex;    // Wer hat den Dealer Button?

    public PokerTable(String tableId, long smallBlind, long bigBlind) {
        this.tableId = tableId;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.stage = PokerStage.WAITING;
        this.deck = new CardStack();
        this.dealerButtonIndex = 0;
    }

    public void addPlayer(PokerPlayerConnection player) {
        players.add(player);
        startGame();
    }

    public void removePlayer(PokerPlayerConnection player) {
        players.remove(player);
    }

    public void startGame() {
        if (players.size() < 4) {
            return;
        }
        dealerButtonIndex = 0;
        startRound();
    }

    public void startRound() {
        this.stage = PokerStage.PRE_FLOP;
        this.deck = new CardStack(); // Neues Deck
        this.communityCards.clear();
        this.pot = 0;
        this.currentHighestBet = 0;

        // Spieler zurücksetzen
        for (PokerPlayerConnection p : players) {
            p.resetForNewRound();
        }

        // Blinds setzen
        postBlinds();

        // Karten austeilen
        dealHoleCards();

        // Erster Spieler ist nach Big Blind (UTG)
        this.currentTurnIndex = (dealerButtonIndex + 3) % players.size();
    }

    private void postBlinds() {
        int sbIndex = (dealerButtonIndex + 1) % players.size();
        int bbIndex = (dealerButtonIndex + 2) % players.size();

        placeBet(players.get(sbIndex), smallBlind);
        placeBet(players.get(bbIndex), bigBlind);

        this.currentHighestBet = bigBlind;
    }

    private void placeBet(PokerPlayerConnection player, long amount) {
        long actualBet = Math.min(player.getStack(), amount);
        player.setStack(player.getStack() - actualBet);
        player.setTableChips(player.getStack());
        player.setCurrentRoundBet(player.getCurrentRoundBet() + actualBet);
        this.pot += actualBet;
    }

    private void dealHoleCards() {
        for (int i = 0; i < 2; i++) {
            for (PokerPlayerConnection p : players) {
                if (p.getStack() > 0) {
                    p.addCard(deck.drawCard());
                }
            }
        }
    }

    public void nextStage() {
        resetRoundBets();

        switch (stage) {
            case PRE_FLOP:
                performStageTransition(PokerStage.FLOP, 3);
                break;
            case FLOP:
                performStageTransition(PokerStage.TURN, 1);
                break;
            case TURN:
                performStageTransition(PokerStage.RIVER, 1);
                break;
            case RIVER:
                stage = PokerStage.SHOWDOWN;
                evaluateWinner();
                break;
            case SHOWDOWN:
                stage = PokerStage.WAITING;
                dealerButtonIndex = (dealerButtonIndex + 1) % players.size();
                // Hier könnte man automatisch die nächste Runde starten
                break;
            default:
                break;
        }

        // Nach dem Flop/Turn/River beginnt der Spieler links vom Dealer
        if (stage != PokerStage.SHOWDOWN && stage != PokerStage.WAITING) {
            currentTurnIndex = (dealerButtonIndex + 1) % players.size();
            // Falls der Spieler gefoldet hat, zum nächsten
            ensureActivePlayer();
        }
    }

    private void performStageTransition(PokerStage nextStage, int cardsToDeal) {
        this.stage = nextStage;
        dealCommunityCards(cardsToDeal);
    }

    private void dealCommunityCards(int count) {
        communityCards.addAll(deck.drawCards(count));
    }

    private void resetRoundBets() {
        currentHighestBet = 0;
        for (PokerPlayerConnection p : players) {
            p.setCurrentRoundBet(0);
        }
    }

    private void evaluateWinner() {
        // Platzhalter für Gewinner-Ermittlung
        System.out.println("Showdown! Gewinner wird ermittelt...");
        // Einfache Logik: Pot an den letzten verbleibenden Spieler (wenn alle gefoldet haben)
        // oder Handvergleich (komplex)
    }

    // --- Aktionen ---

    public void fold(PokerPlayerConnection player) {
        if (!isTurn(player)) return;
        player.setHasFolded(true);
        nextTurn();
    }

    public void call(PokerPlayerConnection player) {
        if (!isTurn(player)) return;
        long toCall = currentHighestBet - player.getCurrentRoundBet();
        placeBet(player, toCall);
        nextTurn();
    }

    public void raise(PokerPlayerConnection player, long totalAmount) {
        if (!isTurn(player)) return;
        if (totalAmount <= currentHighestBet) return; // Muss höher sein

        long diff = totalAmount - player.getCurrentRoundBet();
        placeBet(player, diff);
        currentHighestBet = totalAmount;

        nextTurn();
    }

    public void check(PokerPlayerConnection player) {
        if (!isTurn(player)) return;
        if (currentHighestBet > player.getCurrentRoundBet()) {
            // Check nicht erlaubt, wenn Einsatz offen
            return;
        }
        nextTurn();
    }

    private boolean isTurn(PokerPlayerConnection player) {
        return players.indexOf(player) == currentTurnIndex;
    }

    private void nextTurn() {
        int start = currentTurnIndex;
        do {
            currentTurnIndex = (currentTurnIndex + 1) % players.size();
            PokerPlayerConnection p = players.get(currentTurnIndex);
            if (!p.isHasFolded() && p.getStack() > 0) {
                return; // Nächster Spieler gefunden
            }
        } while (currentTurnIndex != start);
        
        // Wenn wir hier ankommen, sind wir einmal rum.
        // In einer echten Implementierung müsste hier geprüft werden, ob die Wettrunde vorbei ist.
    }
    
    private void ensureActivePlayer() {
        PokerPlayerConnection p = players.get(currentTurnIndex);
        if (p.isHasFolded() || p.getStack() == 0) {
            nextTurn();
        }
        }
    }
}
