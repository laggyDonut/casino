package de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Repräsentiert einen einzelnen Protokolleintrag für administrative Tätigkeiten.
 * Dient der Nachvollziehbarkeit (Auditing) von Änderungen an Benutzerkonten.
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
        @Index(name = "idx_audit_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Der Akteur (z. B. Administrator), der die Aktion initiiert hat.
     * Kann null sein, falls es sich um eine systemseitige Aktion handelt.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Useraccount actor;

    /**
     * Der betroffene Benutzeraccount, auf den sich die Aktion bezieht.
     * Optional, da manche Aktionen globaler Natur sein könnten.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Useraccount target;

    /**
     * Die Art der durchgeführten Aktion (z. B. SPERREN, LÖSCHEN).
     */
    @Enumerated(EnumType.STRING)
    @Column( nullable = false, length = 50)
    private AuditActionType actionType;

    /**
     * Zusätzliche Details oder Begründungen zur Aktion (z. B. "Grund: Verstoß gegen AGB").
     */
    @Column(length = 70)
    private String actionDetails;

    /** Zeitstempel der Erstellung des Log-Eintrags. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
 
    /**
     * Konstruktor für die Erstellung eines AuditLog-Eintrags.
     *
     * @param actor Der Akteur, der die Aktion durchgeführt hat.
     * @param target Der Benutzeraccount, auf den sich die Aktion bezieht.
     * @param actionType Die Art der durchgeführten Aktion.
     * @param actionDetails Zusätzliche Details zur Aktion.
     */
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
                ", actorId=" + (actor != null ? actor.getId() : "null") + // Vermeidung von Zyklen/LazyLoading Problemen
                ", targetId=" + (target != null ? target.getId() : "null") + // Vermeidung von Zyklen/LazyLoading Problemen
                ", actionType=" + actionType +
                ", actionDetails='" + actionDetails + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}