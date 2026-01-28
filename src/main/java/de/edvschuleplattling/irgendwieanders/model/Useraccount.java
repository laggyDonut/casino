package de.edvschuleplattling.irgendwieanders.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Useraccount implements Serializable {

    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Login
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = true)
    private LocalDateTime email_verified_at;

    // Passwort
    @Column(nullable = false, length = 255)
    private transient String password_hash;

    @Column(nullable = false)
    private boolean is_locked;

    @Column(nullable = true)
    private LocalDateTime locked_at;

    // Wenn Account gelöscht wird
    @Column(nullable = true)
    private LocalDateTime deleted_at;

    // Wenn Account erstellt wird
    @Column(nullable = false)
    private LocalDateTime created_at;

    // Wenn Account geupdatet wird
    @Column(nullable = false)
    private LocalDateTime updated_at;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime password_reset_date;

    //@Column(nullable = false)
    //@OneToOne(mappedBy = "id")
    //private Wallet wallet;

    // Konstruktor für neue User
    public Useraccount(String email, String password_hash) {
        this.email = email;
        this.password_hash = password_hash;
        this.is_locked = false;
        this.created_at = LocalDateTime.now();
        this.updated_at = this.created_at;
        this.role = de.edvschuleplattling.irgendwieanders.model.Role.GAMER;
    }

    @Override
    public String toString() {
        return "Useraccount{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", is_locked=" + is_locked +
                '}';
    }
}