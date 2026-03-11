package de.edvschuleplattling.irgendwieanders.rest.dto;

import de.edvschuleplattling.irgendwieanders.model.id.EyeColor;
import de.edvschuleplattling.irgendwieanders.model.id.IdVerification;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class IdVerificationCreateDto {

    @NotNull
    private long useraccountId;

    @NotNull
    @Size(max = 30)
    private String name;

    @NotNull
    @Size(max = 30)
    private String surname;

    @NotNull
    @PastOrPresent
    private LocalDate birthdate;

    @NotNull
    @Size(max = 30)
    private String birthplace;

    @NotNull
    private EyeColor eyeColor;

    @NotNull
    @Min(value = 60)
    @Max(value = 250)
    private int height;

    @NotNull
    private int houseNumber;

    @NotNull
    @Size(max = 30)
    private String street;

    @NotNull
    @Size(min = 5, max = 5)
    private String zip;

    @NotNull
    @Size(min = 9, max = 9)
    private String idNumber;

    @NotNull
    private LocalDate validUntil;

    public static IdVerificationCreateDto fromEntity(IdVerification idVerification) {

        IdVerificationCreateDto dto = new IdVerificationCreateDto();

        dto.setUseraccountId(idVerification.getUseraccount().getId());
        dto.setName(idVerification.getName());
        dto.setSurname(idVerification.getSurname());
        dto.setBirthdate(idVerification.getBirthdate());
        dto.setBirthplace(idVerification.getBirthplace());
        dto.setEyeColor(idVerification.getEyeColor());
        dto.setHeight(idVerification.getHeight());
        dto.setHouseNumber(idVerification.getHouseNumber());
        dto.setStreet(idVerification.getStreet());
        dto.setZip(idVerification.getZip());
        dto.setIdNumber(idVerification.getIdNumber());
        dto.setValidUntil(idVerification.getValidUntil());

        return dto;
    }

}
