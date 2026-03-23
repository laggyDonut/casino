package de.edvschuleplattling.irgendwieanders.rest.controller;

import de.edvschuleplattling.irgendwieanders.service.CasinoTableManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

record JoinRequest(String playerId, String playerName) {}

@RestController
@RequestMapping("/api/poker")
@CrossOrigin(origins = "*")
public class PokerMatchmakingController {

    private final CasinoTableManager casinoTableManager;

    // Konstruktor-Injektion (Spring fügt den Manager automatisch ein)
    public PokerMatchmakingController(CasinoTableManager casinoTableManager) {
        this.casinoTableManager = casinoTableManager;
    }

    /**
     * Endpunkt: POST /api/poker/join-random
     * Wird von React aufgerufen, wenn ein Spieler auf "Spielen" klickt.
     */
    @PostMapping("/join-random")
    public ResponseEntity<Map<String, String>> joinRandomTable(@RequestBody JoinRequest request) {

        System.out.println("Matchmaking Request für Spieler: " + request.playerId());

        // Der Manager sucht einen freien Tisch oder erstellt einen neuen
        String tableId = casinoTableManager.findOrCreateTableId();

        // Wir bauen eine saubere JSON-Antwort: {"tableId": "table-1234abcd"}
        Map<String, String> responseBody = Map.of(
                "tableId", tableId,
                "message", "Tisch erfolgreich gefunden!"
        );

        // Mit Status 200 (OK) zurück an React schicken
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Optionaler Endpunkt: GET /api/poker/tables
     * Nützlich für eine Lobby-Ansicht in React, um alle offenen Tische anzuzeigen.
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Integer>> getActiveTablesOverview() {
        // Gibt zurück, wie viele Tische gerade laufen
        return ResponseEntity.ok(Map.of("activeTablesCount", casinoTableManager.getAllTables().size()));
    }
}