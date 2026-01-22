package de.edvschuleplattling.irgendwieanders.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

public class Accountrole {

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

    @Entity
    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")


        // Primary Key
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Short id;

        // Rollenname
        @Column(nullable = false, unique = true, length = 30)
        private String name; // z.B. "SUPERADMIN", "ADMIN", "USER"

        // Beschreibung
        @Column(length = 200)
        private String description;

        // Konstruktor für neue Rollen
        public Accountrole(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        public String toString() {
            return "Role{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }

        public void setId(Short id) {
            this.id = id;
        }

        public Short getId() {
            return id;
        }
    }

}
