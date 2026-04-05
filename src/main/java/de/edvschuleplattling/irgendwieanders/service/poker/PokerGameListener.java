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
    private java.util.concurrent.ScheduledFuture<?> turnTimer;

    private static final int TURN_TIMEOUT_SECONDS = 10;

    public PokerGameListener(String tableId, SimpMessagingTemplate messagingTemplate) {
        this.tableId = tableId;
        this.messagingTemplate = messagingTemplate;
    }

    public void setGame(PokerGame game) {
        this.game = game;
    }

    private synchronized void cancelTurnTimer() {
        if (turnTimer != null && !turnTimer.isDone()) {
            turnTimer.cancel(false);
            turnTimer = null;
        }
    }

    private synchronized void startTurnTimer(Player player, Set<ActionType> allowedActions) {
        cancelTurnTimer();

        // Broadcast the deadline timestamp so the frontend can show a countdown
        long deadlineMs = System.currentTimeMillis() + (TURN_TIMEOUT_SECONDS * 1000L);
        messagingTemplate.convertAndSend(
            "/topic/poker/" + tableId + "/timer",
            (Object) java.util.Map.of("playerId", player.getId(), "deadlineMs", deadlineMs, "seconds", TURN_TIMEOUT_SECONDS)
        );

        turnTimer = scheduler.schedule(() -> {
            try {
                if (game == null) return;

                // Determine the best auto-action: CHECK if allowed, otherwise FOLD
                ActionType autoAction = allowedActions.contains(ActionType.CHECK) ? ActionType.CHECK : ActionType.FOLD;
                System.out.println("TURN TIMER: Player " + player.getId() + " timed out on table " + tableId + ". Auto-" + autoAction);

                game.performAction(player.getId(), autoAction, 0);
            } catch (Exception e) {
                System.err.println("TURN TIMER: Failed to auto-act for " + player.getId() + ": " + e.getMessage());
            }
        }, TURN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
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
            GameStateDTO personalState = GameStateDTO.from(gameState, p.getId());
            messagingTemplate.convertAndSend("/topic/poker/" + tableId + "/" + p.getId(), personalState);
        }

        // 2. Also broadcast public state (no hole cards) to main topic for observers/new joiners
        GameStateDTO publicState = GameStateDTO.from(gameState);
        messagingTemplate.convertAndSend("/topic/poker/" + tableId, publicState);
    }

    @Override
    public void onPlayerTurn(Player player, Set<ActionType> allowedActions) {
        if (this.game != null) {
            onGameStateChanged(this.game.getGameState());
        }
        // Start the turn timer for the active player
        startTurnTimer(player, allowedActions);
    }

    @Override
    public void onPlayerAction(Player player, ActionType action, int amount, int chipBalanceBefore, int chipBalanceAfter) {
        // Player acted, cancel the turn timer
        cancelTurnTimer();
    }

    @Override
    public void onPotUpdate(int potTotal) {
    }

    @Override
    public void onHandEnded(List<Player> winners, Map<String, Integer> payoutMap) {
        System.out.println("Hand ended on table " + tableId + ". Winners: " + winners.size());

        if (game != null && winners != null && !winners.isEmpty()) {
            java.util.List<String> winnerNames = winners.stream().map(Player::getName).toList();
            java.util.List<String> winnerIds = winners.stream().map(Player::getId).toList();
            int currentPot = payoutMap.values().stream().mapToInt(Integer::intValue).sum();
            
            de.edvschuleplattling.irgendwieanders.websocket.poker.dto.WinnerDto winnerDto = 
                new de.edvschuleplattling.irgendwieanders.websocket.poker.dto.WinnerDto(winnerNames, winnerIds, payoutMap, currentPot);
            
            messagingTemplate.convertAndSend("/topic/poker/" + tableId + "/winners", winnerDto);
        }

        // Auto-start next hand after a delay
        if (game != null) {
            scheduler.schedule(() -> {
                System.out.println("Starting NEXT hand on table " + tableId);
                try {
                    // Kick bankrupt players so we don't hit "Not enough chips" error
                    java.util.List<Player> currentPlayers = new java.util.ArrayList<>(game.getGameState().getPlayers());
                    for (Player p : currentPlayers) {
                        if (p.getChips() <= 0) {
                            System.out.println("Player " + p.getId() + " is bankrupt! Removing from table " + tableId);
                            game.leave(p);
                        }
                    }
                    
                    if (game.getGameState().getPlayers().size() >= 2) {
                        game.startHand();
                    } else {
                        System.out.println("Not enough players left on table " + tableId + ". Transitioning to waiting lobby.");
                        onGameStateChanged(game.getGameState());
                    }
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
