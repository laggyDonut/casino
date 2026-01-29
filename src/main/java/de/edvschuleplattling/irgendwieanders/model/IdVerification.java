package de.edvschuleplattling.irgendwieanders.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")

public class IdVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(optional = false)
    private Useraccount useraccount;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 150)
    private String surname;

    @Column(nullable = false)
    private LocalDate birthdate;

    @Column(nullable = false, length = 150)
    private String birthplace;

    @Column(nullable = false)
    private EyeColor eyeColor;

    @Column(nullable = false)
    private int height;

    @Column(nullable = false)
    private int houseNumber;

    @Column(nullable = false, length = 150)
    private String street;

    @Column(nullable = false)
    private int zip;

    @Column(nullable = false, unique = true, length = 9) //Ausweis ist 9-stellig genormt
    private String idNumber;

    @Column(nullable = false)
    private LocalDate validUntil;

    public IdVerification(int id, Useraccount useraccount, String name, String surname, LocalDate birthdate, String birthplace, EyeColor eyeColor, int height, int houseNumber, String street, int zip, String idNumber, LocalDate validUntil) {
        this.id = id;
        this.useraccount = useraccount;
        this.name = name;
        this.surname = surname;
        this.birthdate = birthdate;
        this.birthplace = birthplace;
        this.eyeColor = eyeColor;
        this.height = height;
        this.houseNumber = houseNumber;
        this.street = street;
        this.zip = zip;
        this.idNumber = idNumber;
        this.validUntil = validUntil;
    }

    @Override
    public String toString() {
        return "IdVerification{" +
                "id=" + id +
                ", useraccount=" + useraccount +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", birthdate=" + birthdate +
                ", birthplace='" + birthplace + '\'' +
                ", eyeColor='" + eyeColor + '\'' +
                ", height=" + height +
                ", houseNumber=" + houseNumber +
                ", street='" + street + '\'' +
                ", zip=" + zip +
                ", idNumber='" + idNumber + '\'' +
                ", validUntil=" + validUntil +
                '}';
    }
}
