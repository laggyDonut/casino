package de.edvschuleplattling.irgendwieanders.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Game_round implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long game_id;

    @Column(nullable = false)
    private Long user_id;

    @Column(nullable = false)
    private Timestamp start_time;

    private Timestamp end_time;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal bet_amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal win_amount;

    @Lob
    private String result_details;

    // -------------------------
    // Konstruktor für neue Spielrunden
    // -------------------------
    public Game_round(Long game_id, Long user_id) {
        this.game_id = game_id;
        this.user_id = user_id;
        this.start_time = new Timestamp(System.currentTimeMillis());
        this.status = "CREATED";
        this.bet_amount = BigDecimal.ZERO;
        this.win_amount = BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "Game_round{" +
                "id=" + id +
                ", game_id=" + game_id +
                ", user_id=" + user_id +
                ", status='" + status + '\'' +
                '}';
    }
}
