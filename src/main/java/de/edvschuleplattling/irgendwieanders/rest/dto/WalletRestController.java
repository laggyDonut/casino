package de.edvschuleplattling.irgendwieanders.rest.dto;

import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import de.edvschuleplattling.irgendwieanders.service.TransactionService;
import de.edvschuleplattling.irgendwieanders.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletRestController {

    private final WalletService walletService;

    @GetMapping("/getAll")
    public ResponseEntity<List<WalletDto>> getAll() {

        List<WalletDto> dtos = walletService
                .getAll()
                .stream()
                .map(t -> WalletDto.fromEntity(t))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<WalletDto> getById(@PathVariable long id) {

        Wallet wallet = walletService.getById(id);

        WalletDto dto = WalletDto.fromEntity(wallet);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/getByUseraccountId/{id}")
    public ResponseEntity<WalletDto> getByUseraccountId(@PathVariable long id) {

        Wallet wallet = walletService.getByUseraccountId(id);

        WalletDto dto = WalletDto.fromEntity(wallet);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/create")
    public ResponseEntity<WalletDto> createWallet(@RequestBody @Valid WalletCreateDto dto) {

        Wallet w = walletService.createWallet(dto.getUseraccountId());

        return ResponseEntity.ok(WalletDto.fromEntity(w));
    }
















































}
