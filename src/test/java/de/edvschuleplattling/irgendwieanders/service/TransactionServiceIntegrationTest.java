package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.Exceptions.DepositLimitMonthlyCounterException;
import de.edvschuleplattling.irgendwieanders.Exceptions.NegativeValueException;
import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Role;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import de.edvschuleplattling.irgendwieanders.repository.TransactionRepository;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import de.edvschuleplattling.irgendwieanders.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UseraccountRepository useraccountRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void executeTransaction_depositLimitExceeded_rollsBackTransactionAndWalletState() {
        Useraccount user = createUserWithWallet(1_000L, 100L, 90L);
        long beforeCount = transactionRepository.count();

        assertThrows(DepositLimitMonthlyCounterException.class,
                () -> transactionService.executeTransaction(user.getId(), TransactionType.DEPOSIT, 20L));

        assertEquals(beforeCount, transactionRepository.count());
        Wallet walletAfter = walletRepository.findById(user.getWallet().getId()).orElseThrow();
        assertEquals(1_000L, walletAfter.getBalance());
        assertEquals(90L, walletAfter.getDepositLimitMonthlyCounter());
    }

    @Test
    void executeTransaction_payoutInsufficientFunds_rollsBackTransactionAndWalletState() {
        Useraccount user = createUserWithWallet(50L, 0L, 0L);
        long beforeCount = transactionRepository.count();

        assertThrows(NegativeValueException.class,
                () -> transactionService.executeTransaction(user.getId(), TransactionType.PAY_OUT, 200L));

        assertEquals(beforeCount, transactionRepository.count());
        Wallet walletAfter = walletRepository.findById(user.getWallet().getId()).orElseThrow();
        assertEquals(50L, walletAfter.getBalance());
    }

    @Test
    void executeTransaction_success_persistsCompletedTransactionAndWalletUpdate() {
        Useraccount user = createUserWithWallet(500L, 0L, 0L);
        long beforeCount = transactionRepository.count();

        transactionService.executeTransaction(user.getId(), TransactionType.DEPOSIT, 100L);

        assertEquals(beforeCount + 1, transactionRepository.count());
        List<Transaction> transactions = transactionRepository.findAllByUseraccountId(user.getId());
        assertEquals(1, transactions.size());
        assertEquals(600L, walletRepository.findById(user.getWallet().getId()).orElseThrow().getBalance());
    }

    private Useraccount createUserWithWallet(long balance, long monthlyLimit, long monthlyCounter) {
        Useraccount user = new Useraccount("txn-" + java.util.UUID.randomUUID() + "@example.com", "hash_dummy");
        user.setRole(Role.GAMER);
        user = useraccountRepository.save(user);

        Wallet wallet = new Wallet(user, balance, 0L, monthlyLimit, monthlyCounter);
        wallet = walletRepository.save(wallet);
        user.setWallet(wallet);
        return useraccountRepository.save(user);
    }
}
