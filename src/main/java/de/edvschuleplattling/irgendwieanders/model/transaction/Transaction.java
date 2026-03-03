package de.edvschuleplattling.irgendwieanders.model.transaction;


import de.edvschuleplattling.irgendwieanders.config.GlobalConstants;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
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
    private long id;

    @ManyToOne(optional = false)
    private Useraccount useraccount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    private long cashAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @PreUpdate  //bei Update von z.B. PROCESSING auf COMPLETED wird onUpdate() ausgeführt und dateTime wird aktualisiert
    public void onUpdate() {
        this.dateTime = LocalDateTime.now();
    }

    public Transaction(Useraccount useraccount, TransactionType type, long cashAmount, TransactionStatus status) {
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
