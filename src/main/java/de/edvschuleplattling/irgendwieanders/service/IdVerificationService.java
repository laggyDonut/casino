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
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class IdVerificationService {

    private final IdVerificationRepository idVerificationRepository;
    private final UseraccountRepository useraccountRepository;

    @Transactional
    public List<IdVerification> getAll(){
        return idVerificationRepository.findAll();
    }

    @Transactional
    public IdVerification getById(long id){
        return idVerificationRepository.findById(id).orElseThrow();
    }

    @Transactional
    public IdVerification getByUseraccount(long id){
        return idVerificationRepository.findByUseraccountId(id).orElseThrow();
    }

    @Transactional
    public List<IdVerification> getAllBySurnameAndName(String surname, String name){
        return idVerificationRepository.findAllBySurnameAndName(surname, name);
    }

    @Transactional
    public IdVerification getByIdNumber(String idNumber){
        return idVerificationRepository.findByIdNumber(idNumber).orElseThrow();
    }

    @Transactional
    public List<IdVerification> getAllByValidUntilLessThanEqual(LocalDate date){
        return idVerificationRepository.findAllByValidUntilLessThanEqual(date);
    }

    @Transactional
    public List<IdVerification> getAllExpiredIds(LocalDate date){
        return idVerificationRepository.findAllExpiredIds(date);
    }

    @Transactional
    public List<IdVerification> getAllValidIds(LocalDate date){
        return idVerificationRepository.findAllValidIds(date);
    }

    @Transactional
    public IdVerification createIdVerification (long useraccountId, String name, String surname, LocalDate birthdate,
                                 String birthplace, EyeColor eyeColor, int height, int houseNumber, String street,
                                 String zip, String idNumber, LocalDate validUntil)
    {

        //Sind bereits Ausweisdaten hinterlegt?
        if (idVerificationRepository.findByUseraccountId(useraccountId).isPresent()) {
            throw new NoSuchElementException("Es sind bereits Ausweisdaten des User mit der ID " + useraccountId +
                                                                                                 " hinterlegt.");
        }

        //Gibt es User?
        Useraccount u = useraccountRepository.findById(useraccountId).orElseThrow();

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
    public IdVerification updateIdVerification(long id, Long useraccountId, String name, String surname, LocalDate birthdate,
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
        
        if (useraccountId != null) {
            Useraccount u = useraccountRepository.findById(useraccountId).orElseThrow();
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


