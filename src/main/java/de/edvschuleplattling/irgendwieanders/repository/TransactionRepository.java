package de.edvschuleplattling.irgendwieanders.repository;

import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAll();

    Optional<Transaction> findById(long id);

    List<Transaction> findAllByUseraccount(Useraccount useraccount);

    List<Transaction> findAllByType(TransactionType type);

    List<Transaction> findAllByStatus(TransactionStatus status);

    List<Transaction> findAllByDateTime(LocalDateTime dateTime);



}
