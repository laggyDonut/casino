package de.edvschuleplattling.irgendwieanders.model;


import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
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

    @Column(nullable = false) //false weil Standard = 0
    private double depositLimitMonthly;

    @Column(nullable = false) //false weil Standard = 0
    private double depositLimitMonthlyCounter;

    public Wallet(int id, Useraccount useraccount, double balance, double bonusBalance, double depositLimitMonthly, double depositLimitMonthlyCounter) {
        this.id = id;
        this.useraccount = useraccount;
        this.balance = balance;
        this.bonusBalance = bonusBalance;
        this.depositLimitMonthly = depositLimitMonthly;
        this.depositLimitMonthlyCounter = depositLimitMonthlyCounter;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "id=" + id +
                ", useraccount=" + useraccount +
                ", balance=" + balance +
                ", bonusBalance=" + bonusBalance +
                ", depositLimitMonthly=" + depositLimitMonthly +
                ", depositLimitMonthlyCounter=" + depositLimitMonthlyCounter +
                '}';
    }
}
