package de.edvschuleplattling.irgendwieanders.rest.dto;

import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionExecuteRequestDto {

    @NotNull
    private long useraccountId;

    @NotNull
    private TransactionType type;

    @NotNull
    private long amount;

    public static TransactionExecuteRequestDto fromEntity(Transaction transaction) {
        TransactionExecuteRequestDto dto = new TransactionExecuteRequestDto();

        dto.setUseraccountId(transaction.getUseraccount().getId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());

        return dto;
    }

}
