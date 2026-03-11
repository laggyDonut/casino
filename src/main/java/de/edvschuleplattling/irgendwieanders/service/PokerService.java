package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.websocket.poker.dto.PokerActionDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PokerService {

    // WICHTIG: Damit sendest du aktiv Nachrichten vom Server an die Clients!
    private final SimpMessagingTemplate messagingTemplate;

    public PokerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void processPlayerAction(PokerActionDto action) {
        // TODO: Hier kommt die eigentliche Logik hin, z.B.:

        // 1. Logik ausführen (z.B. prüfen, ob der Spieler genug Geld für einen Raise hat)
        // walletService.deductFunds(action.getPlayerId(), action.getAmount());

        // 2. Den neuen Spielstatus berechnen (Wer ist als nächstes dran? Wie groß ist der Pot?)
        String gameState = "Der Pot ist jetzt bei 500, Spieler 2 ist dran."; // Das wäre in echt ein Objekt

        // 3. Das Update an ALLE Spieler an diesem bestimmten Tisch (gameId) senden
        messagingTemplate.convertAndSend("/topic/poker/" + action.gameId(), gameState);
    }
}