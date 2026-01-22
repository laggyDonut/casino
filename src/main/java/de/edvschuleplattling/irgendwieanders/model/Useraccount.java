package de.edvschuleplattling.irgendwieanders.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

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

    private Timestamp email_verified_at;

    // Passwort (BCrypt Hash)
    @Column(nullable = false, length = 255)
    private String password_hash;

    // Status
    @Column(nullable = false)
    private boolean is_enabled;

    @Column(nullable = false)
    private boolean is_locked;

    private Timestamp locked_at;

    @Column(length = 200)
    private String lock_reason;

    // Telemetrie
    @Column(nullable = false)
    private int failed_logins;

    private Timestamp last_login_at;

    // Soft Delete
    private Timestamp deleted_at;

    // Meta
    @Column(nullable = false)
    private Timestamp created_at;

    @Column(nullable = false)
    private Timestamp updated_at;

    // Konstruktor für neue User
    public Useraccount(String email, String password_hash) {
        this.email = email;
        this.password_hash = password_hash;
        this.is_enabled = true;
        this.is_locked = false;
        this.failed_logins = 0;
        this.created_at = new Timestamp(System.currentTimeMillis());
        this.updated_at = this.created_at;
    }

    @Override
    public String toString() {
        return "Useraccount{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", is_enabled=" + is_enabled +
                ", is_locked=" + is_locked +
                '}';
    }
}