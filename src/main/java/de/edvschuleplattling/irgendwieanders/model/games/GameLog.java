package de.edvschuleplattling.irgendwieanders.model.games;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_log")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class GameLog implements Serializable {

    // --- PRIMARY KEY ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Useraccount user;

    // --- ZEITPUNKT DES EVENTS ---
    @PastOrPresent
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // --- SPIELTYP (BANDIT, ROULETTE, POKER) ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GameType game;

    // --- WERTÄNDERUNG (+Gewinn / -Einsatz) ---
    @Column(nullable = false)
    private long valueChange;

    // --- EVENT-TYP (START, BET, RESULT, FINISH, ERROR) ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GameLogType type;

    // --- SPIELRUNDE / SESSION-ID ---
    @Column(nullable = false)
    private long gameId;

    // Optional: Komfort-Konstruktor für neue Logs
    public GameLog(GameType game, GameLogType type, long valueChange, long gameId) {
        this.timestamp = LocalDateTime.now();
        this.game = game;
        this.type = type;
        this.valueChange = valueChange;
        this.gameId = gameId;
    }
}
