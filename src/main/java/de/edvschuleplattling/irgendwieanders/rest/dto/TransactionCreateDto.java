package de.edvschuleplattling.irgendwieanders.rest.dto;

import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionCreateDto {

    private long useraccountId;

    private TransactionType type;

    private long cashAmount;

    public static TransactionCreateDto fromEntity(Transaction transaction) {
        TransactionCreateDto dto = new TransactionCreateDto();

        dto.setUseraccountId(transaction.getUseraccount().getId());
        dto.setType(transaction.getType());
        dto.setCashAmount(transaction.getCashAmount());

        return dto;
    }

}
