package de.edvschuleplattling.irgendwieanders.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class LoginAttempt implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Useraccount user;

    @Column(length = 150)
    private String emailInput;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 80)
    private String failReason; // "WRONG_PASSWORD", "USER_LOCKED"

    //@Column(name = "ip_address", length = 45)
    //private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime attemptedAt;

    @PrePersist
    protected void onCreate() { this.attemptedAt = LocalDateTime.now(); }

    public LoginAttempt(Useraccount user, String emailInput, boolean success, String failReason) {
        this.user = user;
        this.emailInput = emailInput;
        this.success = success;
        this.failReason = failReason;
    }
}