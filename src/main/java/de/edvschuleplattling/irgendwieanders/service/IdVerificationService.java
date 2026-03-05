package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.Exceptions.ExpiredIdException;
import de.edvschuleplattling.irgendwieanders.Exceptions.UnderageException;
import de.edvschuleplattling.irgendwieanders.model.id.EyeColor;
import de.edvschuleplattling.irgendwieanders.model.id.IdVerification;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.repository.IdVerificationRepository;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class IdVerificationService {

    private final IdVerificationRepository idVerificationRepository;
    private final UseraccountRepository useraccountRepository;

    @Transactional
    public IdVerification createIdVerification (long useraccountID, String name, String surname, LocalDate birthdate,
                                 String birthplace, EyeColor eyeColor, int height, int houseNumber, String street,
                                 String zip, String idNumber, LocalDate validUntil)
    {
        //Gibt es User?
        Useraccount u = useraccountRepository.findById(useraccountID).orElseThrow();

        //Ist User 18+?
        if (birthdate.plusYears(18).isAfter(LocalDate.now())) {
            throw new UnderageException("Der User muss mindestens 18 Jahre alt sein.");
        }

        //Ist Ausweis heute noch gültig?
        if (validUntil.isBefore(LocalDate.now())) {
            throw new ExpiredIdException("Der Ausweis ist bereits abgelaufen.");
        }

        //Validierung durch Attributseinschränkung: idNumber=unique, height: min=60 max=250

        //Objekt anlegen
        IdVerification i = new IdVerification(u, name, surname, birthdate, birthplace, eyeColor, height, houseNumber, street, zip, idNumber, validUntil);
        idVerificationRepository.save(i);

        return i;
    }

    /**
     * Diese Methode aktualisiert ein bestehendes IdVerification-Objekt.
     * Es werden nur Felder aktualisiert, die nicht null sind.
     */
    @Transactional
                                                //Long/Integer damit null gesetzt werden kann
    public IdVerification updateIdVerification(long id, Long useraccountID, String name, String surname, LocalDate birthdate,
                                               String birthplace, EyeColor eyeColor, Integer height, Integer houseNumber, String street,
                                               String zip, String idNumber, LocalDate validUntil) {

        IdVerification i = idVerificationRepository.findById(id).orElseThrow();
        
        //Alter muss geprüft werden. Falls nicht, könnte der User nachträglich sein Alter < 18 setzen
        if (birthdate != null) {
            if (birthdate.plusYears(18).isAfter(LocalDate.now())) {
                throw new UnderageException("Der User muss mindestens 18 Jahre alt sein.");
            }
            i.setBirthdate(birthdate);
        }

        //Ist Ausweis heute noch gültig?
        if (validUntil != null) {
            if (validUntil.isBefore(LocalDate.now())) {
                throw new ExpiredIdException("Der Ausweis ist bereits abgelaufen.");
            }
            i.setValidUntil(validUntil);
        }
        
        if (useraccountID != null) {
            Useraccount u = useraccountRepository.findById(useraccountID).orElseThrow();
            i.setUseraccount(u);
        }

        if (name != null) i.setName(name);
        if (surname != null) i.setSurname(surname);
        if (birthplace != null) i.setBirthplace(birthplace);
        if (eyeColor != null) i.setEyeColor(eyeColor);
        if (height != null) i.setHeight(height);
        if (houseNumber != null) i.setHouseNumber(houseNumber);
        if (street != null) i.setStreet(street);
        if (zip != null) i.setZip(zip);
        if (idNumber != null) i.setIdNumber(idNumber);

        idVerificationRepository.save(i);

        return i;
    }

    public void deleteIdVerification(long id) {

        //Gibt es die Id?
        idVerificationRepository.findById(id).orElseThrow();

        //Löschen
        idVerificationRepository.deleteById(id);
    }

}


