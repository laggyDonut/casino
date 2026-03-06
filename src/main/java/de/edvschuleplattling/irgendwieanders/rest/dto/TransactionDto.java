package de.edvschuleplattling.irgendwieanders.rest.dto;

import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TransactionDto {

    private long id;

    private long useraccountId;

    private TransactionType type;

    private long cashAmount;

    private TransactionStatus status;

    private LocalDateTime dateTimeCreated;

    private LocalDateTime dateTimeLastUpdate;


    public static TransactionDto fromEntity(Transaction transaction) {
        TransactionDto dto = new TransactionDto();

        dto.setId(transaction.getId());
        dto.setUseraccountId(transaction.getUseraccount().getId());
        dto.setType(transaction.getType());
        dto.setCashAmount(transaction.getCashAmount());
        dto.setStatus(transaction.getStatus());
        dto.setDateTimeCreated(transaction.getDateTimeCreated());
        dto.setDateTimeLastUpdate(transaction.getDateTimeLastUpdate());

        return dto;
    }

}
