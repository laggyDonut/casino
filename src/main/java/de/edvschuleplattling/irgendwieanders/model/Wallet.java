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
    private long id;

    @OneToOne(optional = false)
    private Useraccount useraccount;

    @Column(nullable = false)
    private long balance;

    @Column(nullable = false)
    private long bonusBalance;

    @Column(nullable = false) //false weil Standard = 0
    private long depositLimitMonthly;

    @Column(nullable = false) //false weil Standard = 0
    private long depositLimitMonthlyCounter;

    public Wallet(long id, Useraccount useraccount, long balance, long bonusBalance, long depositLimitMonthly, long depositLimitMonthlyCounter) {
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
