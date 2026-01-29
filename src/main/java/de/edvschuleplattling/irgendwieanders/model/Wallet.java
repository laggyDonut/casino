package de.edvschuleplattling.irgendwieanders.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")


public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private Useraccount useraccount;
    private double balance;
    private double bonusBalance;
    private int currency;  //muss noch enum werden
    private double depositLimitMonthly;
    private double depositLimitMonthlyCounter;
}
