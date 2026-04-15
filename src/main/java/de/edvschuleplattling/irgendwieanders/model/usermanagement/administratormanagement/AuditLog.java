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
/*
 * @Table definiert den Tabellennamen und Performance-Indizes.
 * Die Indizes sind essenziell, um auch bei vielen Log-Einträgen schnelle Suchabfragen
 * zu gewährleisten (Vermeidung von langsamen "Full Table Scans").
 */
@Table(name = "audit_log", indexes = {
        // Beschleunigt Abfragen nach dem Akteur (z. B. "Zeige alle Aktionen von Admin X")
        @Index(name = "idx_audit_actor", columnList = "actor_id"),
        // Beschleunigt Abfragen nach dem betroffenen User (z. B. "Zeige Historie von User Y")
        @Index(name = "idx_audit_target", columnList = "target_id"),
        // Optimiert die Sortierung und Filterung nach Zeit (z. B. "Logs der letzten 24h")
        @Index(name = "idx_audit_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Der Akteur (z. B. Administrator), der die Aktion initiiert hat.
     * Pflichtfeld.
     */
    @Column(name = "actor_id", nullable = false, updatable = false)
    private Long actorId;

    /**
     * Der betroffene Benutzeraccount, auf den sich die Aktion bezieht.
     * Optional, da manche Aktionen globaler Natur sein können.
     */
    @Column(name = "target_id", updatable = false)
    private Long targetId;

    /**
     * Die Art der durchgeführten Aktion (z. B. SPERREN, LÖSCHEN).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, updatable = false)
    private AuditActionType actionType;

    /**
     * Zusätzliche Details oder Begründungen zur Aktion (maximal 70 Zeichen).
     */
    @Column(name = "action_details", nullable = false, length = 70, updatable = false)
    private String actionDetails;

    /**
     * Zeitstempel der Erstellung des Log-Eintrags.
     * Wird serverseitig beim Persistieren gesetzt.
     */
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
