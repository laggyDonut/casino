package de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement;

import de.edvschuleplattling.irgendwieanders.model.id.IdVerification;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import java.io.Serializable;
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
    @Email
    private String email;

    // Email verifiziert
    @Column(nullable = true)
    private LocalDateTime emailVerifiedAt;

    // Passwort, 12 Zeichen, Groß/Klein/Zahl/Sonderzeichen
    @Column(nullable = true, length = 255)
    private String passwordHash;

    // ist Account gesperrt
    @Column(nullable = false)
    private boolean isLocked = false;

    // Wann wurde Account gesperrt
    @Column(nullable = true)
    private LocalDateTime lockedAt;

    // Wann wurde Account gelöscht
    @Column(nullable = true)
    private LocalDateTime deletedAt;

    // Wenn Account erstellt wird
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Wenn Account geupdatet wird
    @Column(nullable = true)
    private LocalDateTime updatedAt;

    //Rolle des Accounts
    @Column(length = 20)
    private String role;

    // Wann Passwort zurückgesetzt
    @Column(nullable = true)
    private LocalDateTime passwordResetDate;

    // Loginversuche des Users
    @Column(nullable = false)
    private int loginAttempts = 0;

    // Geld vom Spieler
    @OneToOne
    private Wallet wallet;

    // Profil vom Spieler
    @OneToOne
    private Userprofile userProfile;

    //Ausweisdaten vom Spieler
    @OneToOne
    private IdVerification idVerification;

    // Konstruktor für neue User
    public Useraccount(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.isLocked = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.role = "Role_GAMER";
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Useraccount{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", isLocked=" + isLocked +
                '}';
    }

    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
    }
}