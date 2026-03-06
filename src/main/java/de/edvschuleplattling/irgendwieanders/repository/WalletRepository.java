package de.edvschuleplattling.irgendwieanders.repository;

import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {


    List<Wallet> findAll();

    Optional<Wallet> findById(long id);

    Optional<Wallet> findByUseraccountId(long useraccountId);

    @Modifying
    @Query("UPDATE Wallet w SET w.depositLimitMonthlyCounter = 0")
    void resetAllDepositLimitMonthlyCounter();






}
