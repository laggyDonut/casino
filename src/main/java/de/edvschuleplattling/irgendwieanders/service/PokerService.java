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
        PokerGame game = casinoTableManager.getTable(joinAction.gameId());
        if (game == null) {
            // Falls der Tisch noch nicht existiert (kann passieren, wenn er gerade erst erstellt wurde)
            // Aber hier sollte der Controller sicherstellen, dass wir eine gültige ID haben.
            // Für den Fall der Fälle erstellen wir ihn, falls ID passt, oder werfen Fehler.
            throw new IllegalArgumentException("Table not found: " + joinAction.gameId());
        }

        // Parse playerId from DTO (expecting Long)
        long userId;
        try {
            userId = Long.parseLong(joinAction.playerId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid player ID format: " + joinAction.playerId());
        }

        Useraccount user = useraccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Create Public Profile DTO to prevent exposing sensitive data
        Userprofile profile = user.getUserProfile();
        String displayName = (profile != null) ? profile.getDisplayName() : "Player " + user.getId();
        PublicUserProfileDto publicProfile = new PublicUserProfileDto(user.getId(), displayName);

        // Spieler hinzufügen
        // Prüfen ob Spieler schon drin ist
        boolean alreadyIn = game.getGameState().getPlayers().stream()
                .anyMatch(p -> p.getId().equals(String.valueOf(userId)));

        if (!alreadyIn) {
            PokerPlayerImpl newPlayer = new PokerPlayerImpl(publicProfile, 1000);
            game.join(newPlayer);
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

        // Update wird nun automatisch vom Listener gesendet
    }
}