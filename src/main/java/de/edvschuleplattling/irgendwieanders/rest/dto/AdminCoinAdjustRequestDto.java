package de.edvschuleplattling.irgendwieanders.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCoinAdjustRequestDto {

    private static final int MAX_REASON_LENGTH = 49;

    @NotNull(message = "Betrag darf nicht null sein.")
    private Long amountDelta;

    @NotBlank(message = "Grund darf nicht leer sein.")
    @Size(max = MAX_REASON_LENGTH, message = "Grund darf maximal 49 Zeichen enthalten.")
    private String reason;
}
