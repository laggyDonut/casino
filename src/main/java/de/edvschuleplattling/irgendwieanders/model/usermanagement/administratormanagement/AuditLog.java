package de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Unveränderlicher Audit-Log-Eintrag für administrative Aktionen.
 */
@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_actor", columnList = "actor_id"),
        @Index(name = "idx_audit_target", columnList = "target_id"),
        @Index(name = "idx_audit_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id", nullable = false, updatable = false)
    private Long actorId;

    @Column(name = "target_id", updatable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, updatable = false)
    private AuditActionType actionType;

    @Column(name = "action_details", nullable = false, length = 70, updatable = false)
    private String actionDetails;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Erstellt einen neuen Audit-Log-Eintrag.
     *
     * @param actorId ID des auslösenden Benutzers
     * @param targetId ID des betroffenen Benutzers (optional)
     * @param actionType Aktionstyp
     * @param actionDetails ergänzende Aktionsdetails
     */
    public AuditLog(Long actorId, Long targetId, AuditActionType actionType, String actionDetails) {
        this.actorId = actorId;
        this.targetId = targetId;
        this.actionType = actionType;
        this.actionDetails = actionDetails;
    }
}
