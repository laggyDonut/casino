package de.edvschuleplattling.irgendwieanders.repository;

import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

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



}
