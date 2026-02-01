package de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement;

/**
 * Definiert die Typen von administrativen Aktionen, die im System protokolliert werden.
 * Dient der Kategorisierung von Einträgen im {@link AuditLog}.
 */
public enum AuditActionType {
    /** Sperren eines Benutzerkontos, um den Zugriff zu verhindern. */
    LOCK_USER,

    /** Aufheben einer bestehenden Sperre eines Benutzerkontos. */
    UNLOCK_USER,

    /** Beförderung eines Benutzers zum Administrator. */
    GRANT_ADMIN,

    /** Entzug von Administrator-Rechten. */
    REVOKE_ADMIN,

    /** Protokollierung des Zugriffs auf sensible Detaildaten (relevant für Datenschutz/DSGVO). */
    VIEW_DETAILS,

    /** Manuelle Anpassung des virtuellen Guthabens (Coins) durch einen Admin. */
    COIN_ADJUST,

    /** Manuelle Änderung des Benutzerpassworts durch einen Admin. */
    CHANGE_PASSWD,

    /** Dauerhaftes Löschen oder Deaktivieren eines Benutzers. */
    DELETE_USER
}