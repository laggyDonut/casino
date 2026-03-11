package de.edvschuleplattling.irgendwieanders.rest;

import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.rest.dto.TransactionCreateDto;
import de.edvschuleplattling.irgendwieanders.rest.dto.TransactionDto;
import de.edvschuleplattling.irgendwieanders.rest.dto.TransactionExecuteDto;
import de.edvschuleplattling.irgendwieanders.rest.dto.TransactionExecuteRequestDto;
import de.edvschuleplattling.irgendwieanders.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionRestController {

    //Aktuelle Methoden für User:

    private final TransactionService transactionService;

    @GetMapping("/getById/{id}")
    public ResponseEntity<TransactionDto> getById(@PathVariable long id) {

        Transaction transaction = transactionService.getById(id);

        TransactionDto dto = TransactionDto.fromEntity(transaction);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/getAllByUseraccountId/{useraccountId}")
    public ResponseEntity<List<TransactionDto>> getAllByUseraccountId(@PathVariable long useraccountId) {

        List<TransactionDto> dtos = transactionService
                .getAllByUseraccountId(useraccountId)
                .stream()
                .map(t -> TransactionDto.fromEntity(t))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/create")
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody @Valid TransactionCreateDto dto) {

        Transaction t = transactionService.createTransaction(dto.getUseraccountId(), dto.getType(), dto.getAmount());

        return ResponseEntity.ok(TransactionDto.fromEntity(t));
    }

    //Rückgabe-DTO zeigt für Entwicklungszwecke aktuell mehr Spalten an als übergeben werden
    //Wird später entfernt, damit keine unberechtigten Spalten ausgelesen werden können
    @PostMapping("/execute")
    public ResponseEntity<TransactionExecuteDto> executeTransaction(@RequestBody @Valid TransactionExecuteRequestDto dto) {

        TransactionExecuteDto dtoReturn = transactionService.executeTransaction(dto.getUseraccountId(), dto.getType(), dto.getAmount());

        return ResponseEntity.ok(dtoReturn);

    }

}
