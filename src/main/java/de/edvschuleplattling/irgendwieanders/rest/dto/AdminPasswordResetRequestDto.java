package de.edvschuleplattling.irgendwieanders.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminPasswordResetRequestDto {

    @NotBlank(message = "Temporäres Passwort darf nicht leer sein.")
    private String temporaryPassword;

    @NotBlank(message = "Ticket/Grund darf nicht leer sein.")
    @Size(max = 70, message = "Ticket/Grund darf maximal 70 Zeichen enthalten.")
    private String ticketOrReason;
}
