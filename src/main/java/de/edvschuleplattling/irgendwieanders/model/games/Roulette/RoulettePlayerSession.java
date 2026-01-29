package de.edvschuleplattling.irgendwieanders.model.games.Roulette;

import de.edvschuleplattling.irgendwieanders.model.Userprofiles;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "gameId")
public class RoulettePlayerSession {

    private long gameId;
    private Userprofiles user;

    private RouletteBet bet;
    private Integer rolledNumber;
    private String rolledColor;
    private long winAmount;

    private boolean finished;

    public RoulettePlayerSession(long gameId, Userprofiles user) {
        this.gameId = gameId;
        this.user = user;
        this.finished = false;
    }
}
