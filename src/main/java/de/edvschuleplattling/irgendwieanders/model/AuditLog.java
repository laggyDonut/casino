package de.edvschuleplattling.irgendwieanders.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "admin_audit_logs",
        indexes = {
                @Index(name = "idx_audit_actor", columnList = "actor_user_id"),
                @Index(name = "idx_audit_target", columnList = "target_user_id"),
                @Index(name = "idx_audit_action", columnList = "action_type"),
                @Index(name = "idx_audit_created", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Pflicht für JPA
@AllArgsConstructor
@Builder
@ToString
public class AuditLog {

    // === Primärschlüssel ===============================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Wer hat gehandelt? ============================================
    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    // === Ziel der Aktion (optional) ====================================
    @Column(name = "target_user_id")
    private Long targetUserId;

    // === Aktionstyp =====================================================
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 50, nullable = false)
    private AuditActionType actionType;


    // === Detailinformationen (JSON/Text) ===============================
    @Column(name = "action_details", columnDefinition = "TEXT")
    private String actionDetails;

    // === Metadaten ======================================================
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    // === Zeitstempel ===================================================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // === Lifecycle Callback ===========================================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
