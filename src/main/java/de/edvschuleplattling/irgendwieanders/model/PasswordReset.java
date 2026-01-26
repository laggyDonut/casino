package de.edvschuleplattling.irgendwieanders.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Useraccount useraccount;

    @Column(nullable = false)
    private String passwordHashOld;

    @Column(nullable = false)
    private String passwordHashNew;

    @Column(nullable = false)
    private LocalDateTime password_reset_date;

}