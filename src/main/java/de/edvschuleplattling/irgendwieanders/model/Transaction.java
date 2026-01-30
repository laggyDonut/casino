package de.edvschuleplattling.irgendwieanders.model;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")


public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    private Useraccount useraccount;

    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private double cashAmount;

    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    public Transaction(int id, Useraccount useraccount, TransactionType type, double cashAmount, TransactionStatus status) {
        this.id = id;
        this.useraccount = useraccount;
        this.type = type;
        this.cashAmount = cashAmount;
        this.status = status;
        this.dateTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", useraccount=" + useraccount +
                ", type=" + type +
                ", cashAmount=" + cashAmount +
                ", status=" + status +
                ", dateTime=" + dateTime +
                '}';
    }
}
