package de.edvschuleplattling.irgendwieanders.model.games.Roulette;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Userprofile;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "gameId")
public class RoulettePlayerSession {

    private long gameId;
    private Userprofile user;

    private RouletteBet bet;
    private Integer rolledNumber;
    private String rolledColor;
    private long winAmount;

    private boolean finished;

    public RoulettePlayerSession(long gameId, Userprofile user) {
        this.gameId = gameId;
        this.user = user;
        this.finished = false;
    }
}
