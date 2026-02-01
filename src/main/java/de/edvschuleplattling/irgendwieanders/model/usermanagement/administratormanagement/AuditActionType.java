package de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement;

public enum AuditActionType {
        LOCK_USER,      // Spieler sperren
        UNLOCK_USER,    // Spieler entsperren
        GRANT_ADMIN,    // Zum Admin befördern
        REVOKE_ADMIN,   // Admin-Rechte entziehen
        VIEW_DETAILS,    // Hat Detaildaten angesehen (Datenschutz!)
        MAKE_NOTE,       // Hat Notiz zu Spieler erstellt
        COIN_ADJUST    // Hat Coins angepasst
}