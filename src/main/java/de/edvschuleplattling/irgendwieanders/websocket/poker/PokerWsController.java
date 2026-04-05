package de.edvschuleplattling.irgendwieanders.websocket.poker;

import de.edvschuleplattling.irgendwieanders.service.PokerService;
import de.edvschuleplattling.irgendwieanders.service.CasinoTableManager;
import de.edvschuleplattling.irgendwieanders.websocket.poker.dto.PokerActionDto;
import de.edvschuleplattling.irgendwieanders.websocket.poker.dto.PokerEmoteDto;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
public class PokerWsController {

    private final PokerService pokerService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CasinoTableManager casinoTableManager;

    public PokerWsController(PokerService pokerService, SimpMessagingTemplate messagingTemplate, CasinoTableManager casinoTableManager) {
        this.pokerService = pokerService;
        this.messagingTemplate = messagingTemplate;
        this.casinoTableManager = casinoTableManager;
    }

    // Wenn ein Client die Runde über "/app/poker/join" beitritt
    @MessageMapping("/poker/join")
    public void joinPoker(@Payload PokerActionDto pokerActionDto, SimpMessageHeaderAccessor headerAccessor) {
        pokerService.addPlayerToTable(pokerActionDto);
        if (headerAccessor != null && headerAccessor.getSessionId() != null) {
            casinoTableManager.registerSession(headerAccessor.getSessionId(), pokerActionDto.gameId(), pokerActionDto.playerId());
        }
    }

    @EventListener
    public void onDisconnectEvent(SessionDisconnectEvent event) {
        casinoTableManager.handleDisconnect(event.getSessionId());
    }

    // Wenn ein Client etwas an "/app/poker/action" sendet, landet es hier
    @MessageMapping("/poker/action")
    public void handlePokerAction(@Payload PokerActionDto actionDto) {
        try {
            // Der Controller macht keine Logik! Er gibt nur an den Service weiter.
            pokerService.processPlayerAction(actionDto);
        } catch (Exception e) {
            System.err.println("Error processing poker action: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Wenn ein Client ein Emote sendet
    @MessageMapping("/poker/emote")
    public void handleEmote(@Payload PokerEmoteDto emoteDto) {
        messagingTemplate.convertAndSend("/topic/poker/" + emoteDto.gameId() + "/emotes", emoteDto);
    }
}