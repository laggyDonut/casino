package de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_actor", columnList = "actor_id"),
        @Index(name = "idx_audit_target", columnList = "target_id"),
        @Index(name = "idx_audit_created", columnList = "createdAt")
})
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
                ", actorId=" + (actor != null ? actor.getId() : "null") + // Nur ID
                ", targetId=" + (target != null ? target.getId() : "null") + // Nur ID
                ", actionType=" + actionType +
                ", actionDetails='" + actionDetails + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}