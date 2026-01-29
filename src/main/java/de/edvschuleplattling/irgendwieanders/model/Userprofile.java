package de.edvschuleplattling.irgendwieanders.model;


import jakarta.persistence.*;
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
    private boolean verificated = false;

    //Profil erstellt
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    //Profil bearbeitet
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();


    @Override
    public String toString() {
        return "Userprofile{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", birthday=" + birthday +
                ", verificated=" + verificated +
                ", createdAt=" + createdAt +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public boolean isVerificated() {
        return verificated;
    }

    public void setVerificated(boolean verificated) {
        this.verificated = verificated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}