package de.edvschuleplattling.irgendwieanders.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BEZIEHUNG: Der Admin, der die Aktion ausführt
    @ManyToOne(fetch = FetchType.LAZY)
    private Useraccount actor;

    // BEZIEHUNG: Der User, der bearbeitet wurde (optional, da manche Aktionen global sein könnten)
    @ManyToOne(fetch = FetchType.LAZY)
    private Useraccount target;

    @Enumerated(EnumType.STRING)
    @Column( nullable = false, length = 50)
    private AuditActionType actionType;

    @Column(length = 70)
    private String actionDetails; // z.B. "Grund: Betrugsverdacht"

    // Optional: IP-Adresse zur Sicherheit
    //@Column(name = "ip_address", length = 45)
    //private String ipAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public AuditLog(Useraccount actor, Useraccount target, AuditActionType actionType, String actionDetails) {
        this.actor = actor;
        this.target = target;
        this.actionType = actionType;
        this.actionDetails = actionDetails;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", actor=" + actor +
                ", target=" + target +
                ", actionType=" + actionType +
                ", actionDetails='" + actionDetails + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}