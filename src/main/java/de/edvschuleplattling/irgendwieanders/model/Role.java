package de.edvschuleplattling.irgendwieanders.model;

public enum Role {
    SUPERADMIN, //Darf andere Admins verwalten
    ADMIN, //Darf nur Spieler verwalten
    SYSTEM, //Darf nichts, wird für Systemprozesse verwendet
    GAMER, //Normale User Rolle
    DOEDL //Für Leute die Permagebannt sind und die Möglichkeit haben Dödls zu sammeln (1 Dödl = 10 Spielstunden in Tetris z.B.)

}