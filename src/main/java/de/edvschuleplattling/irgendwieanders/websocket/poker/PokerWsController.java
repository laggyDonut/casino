package de.edvschuleplattling.irgendwieanders.websocket.poker;

import de.edvschuleplattling.irgendwieanders.service.PokerService;
import de.edvschuleplattling.irgendwieanders.websocket.poker.dto.PokerActionDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class PokerWsController {

    private final PokerService pokerService;

    public PokerWsController(PokerService pokerService) {
        this.pokerService = pokerService;
    }

    // Wenn ein Client die Runde über "/app/poker/join" beitritt
    @MessageMapping("/poker/join")
    public void joinPoker(@Payload PokerActionDto pokerActionDto) {
        pokerService.addPlayerToTable(pokerActionDto);
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
}