package de.edvschuleplattling.irgendwieanders.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminReasonRequestDto {

    @NotBlank(message = "Grund darf nicht leer sein.")
    @Size(max = 70, message = "Grund darf maximal 70 Zeichen enthalten.")
    private String reason;
}
