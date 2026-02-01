package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditActionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditLog;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Role;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.repository.AuditLogRepository;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class TestdatenService {

    private final AuditLogRepository auditLogRepository;
    private final UseraccountRepository useraccountRepository;

    // Testdaten anlegen
    public void anlegenTestdaten() {
        // Test-User
        Useraccount user = new Useraccount("user@example.com", "hash_dummy");
        user.setDeleted_at(LocalDateTime.of(1970, 1, 1, 0, 0));
        useraccountRepository.save(user);

        // Admin
        Useraccount admin = new Useraccount("admin@example.com", "hash_dummy");
        admin.setDeleted_at(LocalDateTime.of(1970, 1, 1, 0, 0));
        admin.setRole(Role.ADMIN);
        useraccountRepository.save(admin);

        // AuditLog: admin führt Aktion an user aus
        AuditLog log = new AuditLog(admin, user, AuditActionType.MAKE_NOTE, "Test: AuditLog für Integrationstests");
        auditLogRepository.save(log);
    }
}
