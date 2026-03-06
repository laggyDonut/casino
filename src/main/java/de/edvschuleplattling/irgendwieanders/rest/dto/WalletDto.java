package de.edvschuleplattling.irgendwieanders.rest.dto;

import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WalletDto {

    private long id;

    private long useraccountId;

    private long balance;

    private long bonusBalance;

    private long depositLimitMonthly;

    private long depositLimitMonthlyCounter;

    public static WalletDto fromEntity(Wallet wallet) {

        WalletDto dto = new WalletDto();

        dto.setId(wallet.getId());
        dto.setUseraccountId(wallet.getUseraccount().getId());
        dto.setBalance(wallet.getBalance());
        dto.setBonusBalance(wallet.getBonusBalance());
        dto.setDepositLimitMonthly(wallet.getDepositLimitMonthly());
        dto.setDepositLimitMonthlyCounter(wallet.getDepositLimitMonthlyCounter());

        return dto;
    }

}
