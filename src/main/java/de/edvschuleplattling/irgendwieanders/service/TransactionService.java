package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.Exceptions.ZeroOrNegativeValueException;
import de.edvschuleplattling.irgendwieanders.Exceptions.StatusIsNullException;
import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import de.edvschuleplattling.irgendwieanders.repository.TransactionRepository;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import de.edvschuleplattling.irgendwieanders.rest.dto.TransactionExecuteDto;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
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
    private final WalletService walletService;

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

    @Transactional
    public TransactionExecuteDto executeTransaction(long useraccountId, TransactionType type, long cashAmount){

        //Gibt es Useraccount? Ja: Useraccount speichern und walletId verwenden
        Useraccount u  = useraccountRepository.findById(useraccountId).orElseThrow();

        //Erstellen und Speichern des Transaction-Objekts
        Transaction t = createTransaction(useraccountId, type, cashAmount);

        //Update von WalletBalance
        Wallet w = walletService.updateWalletBalance(u.getWallet().getId(), cashAmount);

        TransactionExecuteDto dto = TransactionExecuteDto.fromEntity(t, w);

                return dto;
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



