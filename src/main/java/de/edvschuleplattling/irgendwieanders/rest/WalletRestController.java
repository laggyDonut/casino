package de.edvschuleplattling.irgendwieanders.rest;

import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import de.edvschuleplattling.irgendwieanders.rest.dto.WalletCreateDto;
import de.edvschuleplattling.irgendwieanders.rest.dto.WalletDto;
import de.edvschuleplattling.irgendwieanders.rest.dto.WalletUpdateDepositLimitMonthlyDto;
import de.edvschuleplattling.irgendwieanders.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletRestController {

    //Aktuelle Methoden für User:

    private final WalletService walletService;

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

    @PatchMapping("/updateWalletDepositLimitMonthly")
    public ResponseEntity<WalletUpdateDepositLimitMonthlyDto> updateWalletDepositLimitMonthly(
            @RequestBody @Valid WalletUpdateDepositLimitMonthlyDto dto) {

        Wallet w = walletService.updateWalletDepositLimitMonthly(dto.getId(), dto.getDepositLimitMonthly());

        WalletUpdateDepositLimitMonthlyDto dtoReturn = WalletUpdateDepositLimitMonthlyDto.fromEntity(w);

        return ResponseEntity.ok(dtoReturn);

    }

}

