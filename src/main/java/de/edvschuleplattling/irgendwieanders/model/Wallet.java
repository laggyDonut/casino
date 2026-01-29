package de.edvschuleplattling.irgendwieanders.model;


import jakarta.persistence.*;
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

    @OneToOne(optional = false)
    private Useraccount useraccount;

    @Column(nullable = false)
    private double balance;

    @Column(nullable = false)
    private double bonusBalance;

    @Column(nullable = true)
    private double depositLimitMonthly;

    @Column(nullable = true)
    private double depositLimitMonthlyCounter;
}
