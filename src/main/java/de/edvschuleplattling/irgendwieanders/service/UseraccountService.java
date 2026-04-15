package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UseraccountService {

    private final UseraccountRepository useraccountRepository;

    public Useraccount anlegen(String email, String role) {
        Useraccount useraccount = new Useraccount();
        useraccount.setEmail(email);
        useraccount.setRole(role);
        return useraccountRepository.save(useraccount);

    }

}
