package de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Userprofile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Vorname Person
    @Column(nullable = false, length = 30)
    private String firstName;

    //Nachname Person
    @Column(nullable = false, length = 30)
    private String lastName;

    //Anzeigename des Accounts
    @Column(nullable = false, unique = true, length = 30)
    private String displayName;

    //Geburtstag der Person
    @Column(nullable = false)
    private LocalDate birthday;

    //Ausweis verifiziert
    @Column(nullable = false)
    private boolean verified = false;

    //Profil erstellt
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    //Profil bearbeitet
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Userprofile{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", birthday=" + birthday +
                ", verified=" + verified +
                ", createdAt=" + createdAt +
                '}';
    }
}