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

   /* //HIER GEHTS WEITER --> Methode darf nicht erstellt werden, da User Balance ändern könnte in API
    // --> nur das kommt in den controller was der user von aussen steuern darf
    @PatchMapping("/updateWalletBalance/{id}")
    public ResponseEntity<SchuelerDto> update(@PathVariable long id, @RequestBody @Valid UpdateSchuelerRequest dto) {

        Optional<Schueler> schuelerOpt = schuelerRepository.findById(id);

        // existiert Schüler mit der geg. ID?
        if (schuelerOpt.isEmpty()) {
            //return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            return ResponseEntity.notFound().build();
        }

        Schueler s = schuelerOpt.get();

        // nur die übergebenen Werte übernehmen
        if (dto.getName() != null) {
            s.setName(dto.getName());
        }
        if (dto.getGeburtsdatum() != null) {
            s.setGeburtsdatum(dto.getGeburtsdatum());
        }
        if (dto.getGuthabenInCent() != null) {
            s.setGuthabenInCent(dto.getGuthabenInCent());
        }
        if (dto.getEignungstestPunkte() != null) {
            s.setEignungstestPunkte(dto.getEignungstestPunkte());
        }

        // speichern in DB
        Schueler saved = schuelerRepository.save(s);

        // nach DTO konvertieren und zurück geben
        SchuelerDto schuelerDto = SchuelerDto.fromEntity(saved);
        //return new ResponseEntity<>(schuelerDto, HttpStatus.OK);
        return ResponseEntity.ok(schuelerDto);
    } */





















































  /*  @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable long id) {

        transactionService.deleteTransaction(id);

        return ResponseEntity.noContent().build();
    }*/

}
