package de.edvschuleplattling.irgendwieanders.rest.dto;

import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalletUpdateDepositLimitMonthlyDto {

    @NotNull
    private long id;

    @NotNull
    private long depositLimitMonthly;

    public static WalletUpdateDepositLimitMonthlyDto fromEntity(Wallet wallet) {

        WalletUpdateDepositLimitMonthlyDto dto = new WalletUpdateDepositLimitMonthlyDto();

        dto.setId(wallet.getId());
        dto.setDepositLimitMonthly(wallet.getDepositLimitMonthly());

        return dto;
    }

}
