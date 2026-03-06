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

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    
    private final UseraccountRepository useraccountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction createTransaction (long useraccountID, TransactionType type, long cashAmount)
    {
        //Gibt es User?
        Useraccount u = useraccountRepository.findById(useraccountID).orElseThrow();

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

    @Transactional
    public void deleteTransaction(long id) {

        //Gibt es die Id?
        transactionRepository.findById(id).orElseThrow();

        //Löschen
        transactionRepository.deleteById(id);
    }

}



