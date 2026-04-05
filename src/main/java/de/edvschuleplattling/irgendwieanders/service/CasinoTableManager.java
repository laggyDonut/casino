package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.service.poker.PokerGameListener;
import de.simonaltschaeffl.poker.engine.PokerGame;
import de.simonaltschaeffl.poker.engine.PokerGameConfiguration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import de.simonaltschaeffl.poker.dto.GameStateDTO;
import de.simonaltschaeffl.poker.model.ActionType;
import de.simonaltschaeffl.poker.model.GameState;
import de.simonaltschaeffl.poker.model.Player;

@Service
public class CasinoTableManager {
    // TableID -> PokerGame
    private final Map<String, PokerGame> activeTables = new ConcurrentHashMap<>();
    private final Map<String, PlayerSessionInfo> sessionMap = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public record PlayerSessionInfo(String gameId, String playerId) {}

    public void registerSession(String sessionId, String gameId, String playerId) {
        if (sessionId != null) {
            sessionMap.put(sessionId, new PlayerSessionInfo(gameId, playerId));
        }
    }

    public void handleDisconnect(String sessionId) {
        PlayerSessionInfo info = sessionMap.remove(sessionId);
        if (info != null) {
            PokerGame game = activeTables.get(info.gameId());
            if (game != null) {
                game.getGameState().getPlayers().stream()
                    .filter(p -> p.getId().equals(info.playerId()))
                    .findFirst()
                    .ifPresent(p -> {
                        System.out.println("WebSocket disconnect: Player " + info.playerId() + " disconnected from table " + info.gameId());

                        // If the disconnected player is the active player mid-hand, auto-fold them first
                        try {
                            GameState.GamePhase phase = game.getGameState().getPhase();
                            if (phase != GameState.GamePhase.PRE_GAME && phase != GameState.GamePhase.HAND_ENDED) {
                                int pos = game.getGameState().getCurrentActionPosition();
                                if (pos >= 0 && pos < game.getGameState().getPlayers().size()) {
                                    Player activePlayer = game.getGameState().getPlayers().get(pos);
                                    if (activePlayer.getId().equals(info.playerId())) {
                                        System.out.println("Disconnected player " + info.playerId() + " was active. Auto-folding.");
                                        game.performAction(info.playerId(), ActionType.FOLD, 0);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to auto-fold disconnected player: " + e.getMessage());
                        }

                        game.leave(p);
                        broadcastGameState(info.gameId());
                    });
            }
        }
    }

    public void broadcastGameState(String tableId) {
        PokerGame game = activeTables.get(tableId);
        if (game != null) {
            messagingTemplate.convertAndSend("/topic/poker/" + tableId, GameStateDTO.from(game.getGameState()));
            for (Player activePlayer : game.getGameState().getPlayers()) {
                messagingTemplate.convertAndSend("/topic/poker/" + tableId + "/" + activePlayer.getId(), GameStateDTO.from(game.getGameState(), activePlayer.getId()));
            }
        }
    }

    public void sendTableRedirect(String oldTableId, String playerId, String newTableId) {
        messagingTemplate.convertAndSend(
            "/topic/poker/" + oldTableId + "/" + playerId + "/redirect",
            (Object) java.util.Map.of("newTableId", newTableId)
        );
    }

    public CasinoTableManager(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Sucht einen freien Tisch oder erstellt einen neuen
    public synchronized String findOrCreateTableId() {
        return findOrCreateTableId(null);
    }

    // Overload that excludes a specific table (e.g. one already known to be full)
    public synchronized String findOrCreateTableId(String excludeTableId) {

        // 1. Alle existierenden Tische durchsuchen
        for (Map.Entry<String, PokerGame> entry : activeTables.entrySet()) {
            String tableId = entry.getKey();
            if (tableId.equals(excludeTableId)) continue;
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
