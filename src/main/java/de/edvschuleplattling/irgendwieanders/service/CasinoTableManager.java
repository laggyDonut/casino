package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.service.poker.PokerGameListener;
import de.simonaltschaeffl.poker.engine.PokerGame;
import de.simonaltschaeffl.poker.engine.PokerGameConfiguration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CasinoTableManager {
    // TableID -> PokerGame
    private final Map<String, PokerGame> activeTables = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public CasinoTableManager(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Sucht einen freien Tisch oder erstellt einen neuen
    public synchronized String findOrCreateTableId() {

        // 1. Alle existierenden Tische durchsuchen
        for (Map.Entry<String, PokerGame> entry : activeTables.entrySet()) {
            String tableId = entry.getKey();
            PokerGame game = entry.getValue();

            int currentPlayers = game.getGameState().getPlayers().size();
            int maxPlayers = game.getConfig().getMaxPlayers();

            // Ist noch Platz an diesem Tisch?
            if (currentPlayers < maxPlayers) {
                System.out.println("Freier Tisch gefunden: " + tableId + " (" + currentPlayers + "/" + maxPlayers + ")");
                return tableId;
            }
        }

        // 2. Wenn wir hier ankommen, waren ALLE Tische voll (oder es gab noch gar keine).
        // Also: Neuen Tisch erstellen!
        String newTableId = "table-" + UUID.randomUUID().toString().substring(0, 8);
        createNewTable(newTableId);

        System.out.println("Alle Tische voll. Neuer Tisch erstellt: " + newTableId);
        return newTableId;
    }

    // Erstellt das eigentliche PokerGame und legt es in die Map
    private PokerGame createNewTable(String tableId) {
        PokerGameConfiguration config = new PokerGameConfiguration.Builder()
                .smallBlind(10)
                .bigBlind(20)
                .maxPlayers(6) // Hier definierst du, wann der Tisch "voll" ist
                .build();

        PokerGame newGame = new PokerGame(config);

        // Listener registrieren, damit Updates automatisch an die Clients gehen
        PokerGameListener listener = new PokerGameListener(tableId, messagingTemplate);
        listener.setGame(newGame);
        newGame.addListener(listener);

        activeTables.put(tableId, newGame);
        return newGame;
    }

    public PokerGame getTable(String tableId) {
        return activeTables.get(tableId);
    }

    public Map<String, PokerGame> getAllTables() {
        return activeTables;
    }
}
