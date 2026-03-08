package de.edvschuleplattling.irgendwieanders.rest;

import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import de.edvschuleplattling.irgendwieanders.rest.dto.IdVerificationCreateDto;
import de.edvschuleplattling.irgendwieanders.rest.dto.WalletUpdateDepositLimitMonthlyDto;
import de.edvschuleplattling.irgendwieanders.service.IdVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/IdVerification")
@RequiredArgsConstructor
public class IdVerificationRestController {

    private final IdVerificationService idVerificationService;

    //Aktuelle Methoden für User:

    //Es werden keine Ausweisdaten nach aussen gegeben.
    //User besitzt keine Möglichkeit zur Ansicht des Ausweisdaten.

    @PostMapping("/create")
    public void createIdVerification(@RequestBody @Valid IdVerificationCreateDto dto) {

        idVerificationService.createIdVerification(dto);

    }

}

