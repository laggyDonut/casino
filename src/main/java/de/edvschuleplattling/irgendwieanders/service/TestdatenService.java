package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.model.id.EyeColor;
import de.edvschuleplattling.irgendwieanders.model.id.IdVerification;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditActionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditLog;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Role;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import de.edvschuleplattling.irgendwieanders.repository.AuditLogRepository;
import de.edvschuleplattling.irgendwieanders.repository.IdVerificationRepository;
import de.edvschuleplattling.irgendwieanders.repository.TransactionRepository;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import de.edvschuleplattling.irgendwieanders.rest.dto.IdVerificationCreateDto;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class TestdatenService {

    private final AuditLogRepository auditLogRepository;
    private final UseraccountRepository useraccountRepository;
    private final TransactionService transactionService;
    private final WalletService walletService;
    private final IdVerificationService idVerificationService;
    private final IdVerificationRepository idVerificationRepository;

    // Testdaten anlegen
    public void anlegenTestdaten() {
        // Test-User
        Useraccount user = new Useraccount("user@example.com", "hash_dummy");
        //user.setDeleted_at(LocalDateTime.of(1970, 1, 1, 0, 0));
        useraccountRepository.save(user);

        // Admin
        Useraccount admin = new Useraccount("admin@example.com", "hash_dummy");
        //admin.setDeleted_at(LocalDateTime.of(1970, 1, 1, 0, 0));
        admin.setRole(Role.ADMIN);
        useraccountRepository.save(admin);

        // AuditLog: admin führt Aktion an user aus
        AuditLog log = new AuditLog(admin.getId(), user.getId(), AuditActionType.LOCK_USER, "Test: AuditLog für Integrationstests");
        auditLogRepository.save(log);

        // AuditLog: admin führt Aktion an sich selbst aus
        log = new AuditLog(admin.getId(), admin.getId(), AuditActionType.LOCK_USER, "Test: AuditLog für Integrationstests");
        auditLogRepository.save(log);

        // AuditLog: admin führt Aktion an user aus
        log = new AuditLog(admin.getId(), user.getId(), AuditActionType.LOCK_USER, "Test: AuditLog für Integrationstests");
        auditLogRepository.save(log);
    }


        public void anlegenTestdatenYannick(){
        Useraccount test1 = new Useraccount("test1@test.com", "hash_dummy");
        Useraccount test2 = new Useraccount("test2@test.com", "hash_dummy");
        useraccountRepository.save(test1);
        useraccountRepository.save(test2);
        //Wallet anlegen
        Wallet walletTest1 = walletService.createWallet(test1.getId());
        Wallet walletTest2 = walletService.createWallet(test2.getId());
        //IdVerification anlegen
        IdVerification idVerificationTest1 = new IdVerification(test1,"idVerificationTest1", "idVerificationTest1", LocalDate.of(2000, 1, 1),
                "TestOrt", EyeColor.OTHERS, 170, 1, "TestStraße", "12345", "123456789", LocalDate.of(2030, 1, 1));
        IdVerification idVerificationTest2 = new IdVerification(test2,"idVerificationTest2", "idVerificationTest2", LocalDate.of(2000, 1, 1),
                "TestOrt", EyeColor.OTHERS, 170, 1, "TestStraße", "12345", "223456789", LocalDate.of(2030, 1, 1));
        //Speichern von IdVerification, sonst Absturz
            idVerificationRepository.save(idVerificationTest1);
            idVerificationRepository.save(idVerificationTest2);
        //Setzen von Wallet und IdVerification in Useraccount
        test1.setWallet(walletTest1);
        test1.setIdVerification(idVerificationTest1);
        test2.setWallet(walletTest2);
        test2.setIdVerification(idVerificationTest2);
        useraccountRepository.save(test1);
        useraccountRepository.save(test2);
        }
}
