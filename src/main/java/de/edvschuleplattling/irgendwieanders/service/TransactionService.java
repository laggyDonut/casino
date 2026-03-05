package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.Exceptions.ZeroOrNegativeValueException;
import de.edvschuleplattling.irgendwieanders.Exceptions.StatusIsNullException;
import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.repository.TransactionRepository;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    
    private final UseraccountRepository useraccountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public List<Transaction> getAll(){
        return transactionRepository.findAll();
    }

    @Transactional
    public Transaction getById(long id){
        return transactionRepository.findById(id).orElseThrow();
    }

    @Transactional
    public List<Transaction> getAllByUseraccountId(long useraccountId){
        return transactionRepository.findAllByUseraccountId(useraccountId);
    }

    @Transactional
    public List<Transaction> getAllByType(TransactionType type){
        return transactionRepository.findAllByType(type);
    }

    @Transactional
    public List<Transaction> getAllByStatus(TransactionStatus status){
        return transactionRepository.findAllByStatus(status);
    }

    @Transactional
    public List<Transaction> getAllByDateTimeCreated(LocalDateTime dateTimeCreated){
        return transactionRepository.findAllByDateTimeCreated(dateTimeCreated);
    }

    @Transactional
    public List<Transaction> getAllByDateTimeLastUpdate(LocalDateTime dateTimeLasdtUpdate){
        return transactionRepository.findAllByDateTimeLastUpdate(dateTimeLasdtUpdate);
    }

    @Transactional
    public Transaction createTransaction (long useraccountId, TransactionType type, long cashAmount)
    {
        //Gibt es User?
        Useraccount u = useraccountRepository.findById(useraccountId).orElseThrow();

        //Ist cashAmount positiv?
        if (cashAmount <= 0){
            throw new ZeroOrNegativeValueException("Der Transaktionsbetrag muss positiv sein.");
        }

        //Objekt anlegen
        Transaction t = new Transaction(u, type, cashAmount, TransactionStatus.PROCESSING);
        transactionRepository.save(t);

        return t;
    }

    /**
     * Diese Methode aktualisiert das Attribut status eines bestehenden Wallet-Objekts.
     *
     */
    @Transactional
    public Transaction updateTransactionStatus(long id, TransactionStatus status) {

        //Gibt es User?
        Transaction t = transactionRepository.findById(id).orElseThrow();

        //Ist Status null?
        if (status == null) {
            throw new StatusIsNullException("Status ist null.");
        }

        t.setStatus(status);


        transactionRepository.save(t);

        return t;
    }

}



