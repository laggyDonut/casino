package de.edvschuleplattling.irgendwieanders.repository;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UseraccountRepository extends JpaRepository<Useraccount, Long> {

    Optional<Useraccount> findById(long id);

}
