package de.edvschuleplattling.irgendwieanders.repository;

import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAll();

    Optional<Transaction> findById(long id);

    List<Transaction> findAllByUseraccountId(long useraccountId);

    List<Transaction> findAllByType(TransactionType type);

    List<Transaction> findAllByStatus(TransactionStatus status);

    List<Transaction> findAllByDateTimeCreated(LocalDateTime dateTimeCreated);

    List<Transaction> findAllByDateTimeLastUpdate(LocalDateTime dateTimeLastUpdate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.useraccount.id = :useraccountId " +
            "AND t.type = 'DEPOSIT' AND t.status = 'COMPLETED'")
    long findCountSuccessfulDeposits(@Param("useraccountId")long useraccountId);

}
