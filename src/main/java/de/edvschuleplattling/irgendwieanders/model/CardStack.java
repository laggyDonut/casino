package de.edvschuleplattling.irgendwieanders.model;

import de.edvschuleplattling.irgendwieanders.Exceptions.CardStackStateException;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class CardStack {
    Stack<Card> cardStack = new Stack<>();

    public CardStack() {
        initializeDeck(); // Initialize and shuffle the deck when a CardStack is created
    }

    private void initializeDeck() {

        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cardStack.push(new Card(suit, rank));
            }
        }

        Collections.shuffle(cardStack);
    }

    public Card drawCard() {
        if (cardStack.isEmpty()) {
            throw new CardStackStateException("No cards left in the stack");
        }
        return cardStack.pop();
    }

    public List<Card> drawCards(int count) {
        if (count > cardStack.size()) {
            throw new CardStackStateException("Not enough cards left in the stack");
        }
        List<Card> drawnCards = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            drawnCards.add(cardStack.pop());
        }
        return drawnCards;
    }

}
