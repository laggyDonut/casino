package de.edvschuleplattling.irgendwieanders.service.poker;

import de.simonaltschaeffl.poker.api.GameEventListener;
import de.simonaltschaeffl.poker.dto.GameStateDTO;
import de.simonaltschaeffl.poker.model.ActionType;
import de.simonaltschaeffl.poker.model.GameState;
import de.simonaltschaeffl.poker.model.Player;
import de.simonaltschaeffl.poker.engine.PokerGame;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PokerGameListener implements GameEventListener {

    private final String tableId;
    private final SimpMessagingTemplate messagingTemplate;
    private PokerGame game; // Set via setter or constructor if possible
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public PokerGameListener(String tableId, SimpMessagingTemplate messagingTemplate) {
        this.tableId = tableId;
        this.messagingTemplate = messagingTemplate;
    }

    public void setGame(PokerGame game) {
        this.game = game;
    }

    @Override
    public void onGameStarted() {
        System.out.println("DEBUG: Game started on table " + tableId);
    }

    @Override
    public void onRoundStarted(String roundName) {
        System.out.println("DEBUG: Round " + roundName + " started on table " + tableId);
    }

    @Override
    public void onGameStateChanged(GameState gameState) {
        // Debug: Log active player on server side
        int pos = gameState.getCurrentActionPosition();
        if (pos >= 0 && pos < gameState.getPlayers().size()) {
            Player active = gameState.getPlayers().get(pos);
            System.out.println("DEBUG: Server ActionPos: " + pos + " -> " + active.getName() + " (ID: " + active.getId() + ")");
        } else {
            System.out.println("DEBUG: Server ActionPos: " + pos + " (Outside bounds or invalid?) Players: " + gameState.getPlayers().size());
        }

        // 1. Send personalized state to each player (showing their own cards)
        for (Player p : gameState.getPlayers()) {
            // Debugging cards
            if (!p.getHoleCards().isEmpty()) {
                System.out.println("DEBUG: Player " + p.getId() + " has " + p.getHoleCards().size() + " cards.");
            } else {
                // Only log if phase suggests they should have cards
                if (gameState.getPhase() != GameState.GamePhase.PRE_GAME && gameState.getPhase() != GameState.GamePhase.HAND_ENDED) {
                     System.out.println("DEBUG: Player " + p.getId() + " has NO cards, but phase is " + gameState.getPhase());
                }
            }

            // Each player gets their OWN view (with hole cards revealed for THEM)
            // p.getId() corresponds to the UserID text
            GameStateDTO personalState = GameStateDTO.from(gameState, p.getId());

            // Send to user-specific topic: /topic/poker/{tableId}/{userId}
            messagingTemplate.convertAndSend("/topic/poker/" + tableId + "/" + p.getId(), personalState);
        }

        // 2. Also broadcast public state (no hole cards) to main topic for observers/new joiners
        GameStateDTO publicState = GameStateDTO.from(gameState);
        messagingTemplate.convertAndSend("/topic/poker/" + tableId, publicState);
    }

    @Override
    public void onPlayerTurn(Player player, Set<ActionType> allowedActions) {
        // Optional: Send "Your Turn" signal
    }

    @Override
    public void onPlayerAction(Player player, ActionType action, int amount, int chipBalanceBefore, int chipBalanceAfter) {
        // Optional: Send detailed action log
    }

    @Override
    public void onPotUpdate(int potTotal) {
    }

    @Override
    public void onHandEnded(List<Player> winners, Map<String, Integer> payoutMap) {
        System.out.println("Hand ended on table " + tableId + ". Winners: " + winners.size());

        // Auto-start next hand after a delay
        if (game != null) {
            scheduler.schedule(() -> {
                System.out.println("Starting NEXT hand on table " + tableId);
                try {
                    game.startHand();
                } catch (Exception e) {
                    System.err.println("Failed to start next hand: " + e.getMessage());
                }
            }, 5, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onPlayerJoinedWaitingList(Player player) {
    }

    @Override
    public void onRakeCollected(int amount) {
    }
}
