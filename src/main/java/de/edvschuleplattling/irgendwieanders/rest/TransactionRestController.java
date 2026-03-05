package de.edvschuleplattling.irgendwieanders.rest;

import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.rest.dto.TransactionCreateDto;
import de.edvschuleplattling.irgendwieanders.rest.dto.TransactionDto;
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

    private final TransactionService transactionService;

    @GetMapping("/getAll")
    public ResponseEntity<List<TransactionDto>> getAll() {

        List<TransactionDto> dtos = transactionService
                .getAll()
                .stream()
                .map(t -> TransactionDto.fromEntity(t))
                .toList();

        return ResponseEntity.ok(dtos);
    }

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

    @GetMapping("/getAllByType/{type}")
    public ResponseEntity<List<TransactionDto>> getAllByType(@PathVariable TransactionType type) {

        List<TransactionDto> dtos = transactionService
                .getAllByType(type)
                .stream()
                .map(t -> TransactionDto.fromEntity(t))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/getAllByStatus/{status}")
    public ResponseEntity<List<TransactionDto>> getAllByStatus(@PathVariable TransactionStatus status) {

        List<TransactionDto> dtos = transactionService
                .getAllByStatus(status)
                .stream()
                .map(t -> TransactionDto.fromEntity(t))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/getAllByDateTimeCreated/{dateTimeCreated}")
    public ResponseEntity<List<TransactionDto>> getAllByDateTimeCreated(@PathVariable
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTimeCreated) {

        List<TransactionDto> dtos = transactionService
                .getAllByDateTimeCreated(dateTimeCreated)
                .stream()
                .map(t -> TransactionDto.fromEntity(t))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/getAllByDateTimeLastUpdate/{dateTimeLastUpdate}")
    public ResponseEntity<List<TransactionDto>> getAllByDateTimeLastUpdate(@PathVariable
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTimeLastUpdate) {

        List<TransactionDto> dtos = transactionService
                .getAllByDateTimeLastUpdate(dateTimeLastUpdate)
                .stream()
                .map(t -> TransactionDto.fromEntity(t))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/create")
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody @Valid TransactionCreateDto dto) {

        Transaction t = transactionService.createTransaction(dto.getUseraccountId(), dto.getType(), dto.getCashAmount());

        return ResponseEntity.ok(TransactionDto.fromEntity(t));
    }

}
