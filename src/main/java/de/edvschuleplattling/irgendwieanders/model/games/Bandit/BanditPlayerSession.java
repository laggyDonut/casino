package de.edvschuleplattling.irgendwieanders.model.games.Bandit;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Userprofile;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "gameId")
public class BanditPlayerSession {

    private long gameId;
    private Userprofile user;

    private long betAmount;
    private List<Symbol> reels;
    private long winAmount;

    private boolean finished;

    public BanditPlayerSession(long gameId, Userprofile user) {
        this.gameId = gameId;
        this.user = user;
        this.reels = new ArrayList<>();
        this.finished = false;
    }
}
