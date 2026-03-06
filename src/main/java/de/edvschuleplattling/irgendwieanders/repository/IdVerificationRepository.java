package de.edvschuleplattling.irgendwieanders.repository;

import de.edvschuleplattling.irgendwieanders.model.id.IdVerification;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IdVerificationRepository extends JpaRepository<IdVerification, Long> {

    List<IdVerification> findAll();

    Optional<IdVerification> findById(long id);

    Optional<IdVerification> findByUseraccount(Useraccount useraccount);

    List<IdVerification> findAllBySurnameAndName(String surname, String name);

    Optional<IdVerification> findByIdNumber(String idNumber);

    List<IdVerification> findAllByValidUntilLessThanEqual(LocalDate date);

    @Query("SELECT i FROM IdVerification i WHERE i.validUntil < :date")
    List<IdVerification> findAllExpiredIds(@Param("date") LocalDate date);

    @Query("SELECT i FROM IdVerification i WHERE i.validUntil >= :date")
    List<IdVerification> findAllValidIds(@Param("date") LocalDate date);



}
