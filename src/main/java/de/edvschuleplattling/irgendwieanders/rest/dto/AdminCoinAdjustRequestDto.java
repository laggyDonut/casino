package de.edvschuleplattling.irgendwieanders.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCoinAdjustRequestDto {

    @NotNull(message = "Betrag darf nicht null sein.")
    private Long amountDelta;

    @NotBlank(message = "Grund darf nicht leer sein.")
    @Size(max = 49, message = "Grund darf maximal 49 Zeichen enthalten.")
    private String reason;
}
