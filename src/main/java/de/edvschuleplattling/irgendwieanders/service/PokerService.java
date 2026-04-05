package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.websocket.poker.dto.PublicUserProfileDto;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Userprofile;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import de.edvschuleplattling.irgendwieanders.websocket.poker.dto.PokerActionDto;
import de.edvschuleplattling.irgendwieanders.service.poker.PokerPlayerImpl;
import de.simonaltschaeffl.poker.engine.PokerGame;
import de.simonaltschaeffl.poker.model.GameState;
import de.simonaltschaeffl.poker.model.GameState.GamePhase;
import de.simonaltschaeffl.poker.model.Player;
import org.springframework.stereotype.Service;

@Service
public class PokerService {

    // WICHTIG: Damit sendest du aktiv Nachrichten vom Server an die Clients!
    private final CasinoTableManager casinoTableManager;
    private final UseraccountRepository useraccountRepository;

    public PokerService(CasinoTableManager casinoTableManager, UseraccountRepository useraccountRepository) {
        this.casinoTableManager = casinoTableManager;
        this.useraccountRepository = useraccountRepository;
    }

    public void processPlayerAction(PokerActionDto action) {
        PokerGame game = casinoTableManager.getTable(action.gameId());
        if (game == null) {
            throw new IllegalArgumentException("Game not found: " + action.gameId());
        }

        // DEBUG: Check who needs to act
        try {
            int pos = game.getGameState().getCurrentActionPosition();
            if (pos >= 0 && pos < game.getGameState().getPlayers().size()) {
                Player active = game.getGameState().getPlayers().get(pos);
                System.out.println("ACTION DEBUG: Request from '" + action.playerId() + "'. Active: '" + active.getId() + "'. Match: " + active.getId().equals(action.playerId()));
            }
        } catch(Exception e) { e.printStackTrace(); }

        // Action ausführen (trim ID to be safe)
        game.performAction(action.playerId().trim(), action.actionType(), (int) action.amount());

        // Update wird nun automatisch vom Listener gesendet
    }

    public void addPlayerToTable(PokerActionDto joinAction) {
        String gid = joinAction.gameId() != null ? joinAction.gameId().trim() : "";
        PokerGame game = casinoTableManager.getTable(gid);
        
        // Safety retry for race conditions
        if (game == null) {
            try { Thread.sleep(100); } catch(Exception e) {}
            game = casinoTableManager.getTable(gid);
        }

        if (game == null) {
            throw new IllegalArgumentException("Table not found: [" + gid + "]");
        }

        // Parse playerId from DTO (expecting Long)
        long userId;
        try {
            userId = Long.parseLong(joinAction.playerId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid player ID format: " + joinAction.playerId());
        }

        String displayName;
        // Prioritize the custom name sent from the frontend
        if (joinAction.displayName() != null && !joinAction.displayName().isBlank()) {
            displayName = joinAction.displayName().trim();
        } else {
            Useraccount user = useraccountRepository.findById(userId).orElse(null);
            if (user != null) {
                Userprofile profile = user.getUserProfile();
                displayName = (profile != null && profile.getDisplayName() != null && !profile.getDisplayName().isEmpty()) 
                    ? profile.getDisplayName() 
                    : "Player " + user.getId();
            } else {
                displayName = "Player_" + userId;
            }
        }

        PublicUserProfileDto publicProfile = new PublicUserProfileDto(userId, displayName);

        // Spieler hinzufügen
        // Prüfen ob Spieler schon drin ist
        boolean alreadyIn = game.getGameState().getPlayers().stream()
                .anyMatch(p -> p.getId().equals(String.valueOf(userId)));

        if (!alreadyIn) {
            PokerPlayerImpl newPlayer = new PokerPlayerImpl(publicProfile, 1000);
            try {
                game.join(newPlayer);
            } catch (de.simonaltschaeffl.poker.exception.GameFullException e) {
                // Table is full - find/create a NEW table (excluding the current full one) and redirect
                System.out.println("Table " + gid + " is full! Redirecting player " + userId + " to a new table.");
                String newTableId = casinoTableManager.findOrCreateTableId(gid);
                PokerGame newGame = casinoTableManager.getTable(newTableId);
                
                try {
                    newGame.join(newPlayer);
                } catch (Exception ex) {
                    System.err.println("Failed to join new table " + newTableId + ": " + ex.getMessage());
                    return;
                }
                
                // Send redirect message to client so it reconnects to the new table
                casinoTableManager.sendTableRedirect(gid, String.valueOf(userId), newTableId);
                
                // Auto-start on new table if enough players
                if (newGame.getGameState().getPlayers().size() >= 2 &&
                    (newGame.getGameState().getPhase() == GamePhase.PRE_GAME ||
                     newGame.getGameState().getPhase() == GamePhase.HAND_ENDED)) {
                    try {
                        newGame.startHand();
                    } catch (Exception ex) {
                        System.err.println("Failed to start hand on new table: " + ex.getMessage());
                    }
                }
                casinoTableManager.broadcastGameState(newTableId);
                return; // Don't continue with the old table logic
            }
        }

        // Auto-start game if enough players
        if (game.getGameState().getPlayers().size() >= 2 &&
            (game.getGameState().getPhase() == GamePhase.PRE_GAME ||
             game.getGameState().getPhase() == GamePhase.HAND_ENDED)) {

            System.out.println("Starting new hand on table " + joinAction.gameId());
            try {
                game.startHand();
            } catch (Exception e) {
                System.err.println("Failed to start hand: " + e.getMessage());
            }
        }

        // Always force broadcast to update the UI lobby state, whether a hand started or not.
        casinoTableManager.broadcastGameState(joinAction.gameId());
    }
}