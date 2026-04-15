package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditActionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Role;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import de.edvschuleplattling.irgendwieanders.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminActionService {

    private static final int MAX_AUDIT_DETAIL_LENGTH = 70;

    private final UseraccountRepository useraccountRepository;
    private final WalletRepository walletRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    public Useraccount lockUser(Long actorId, long targetUserId, String reason) {
        Useraccount actor = requireAdmin(actorId);
        Useraccount target = getTargetUser(targetUserId);

        if (target.isLocked()) {
            throw new IllegalStateException("User ist bereits gesperrt.");
        }

        target.setLocked(true);
        target.setLockedAt(LocalDateTime.now());
        useraccountRepository.save(target);

        auditService.log(actor.getId(), target.getId(), AuditActionType.LOCK_USER, reason);
        return target;
    }

    public Useraccount unlockUser(Long actorId, long targetUserId, String reason) {
        Useraccount actor = requireAdmin(actorId);
        Useraccount target = getTargetUser(targetUserId);

        if (!target.isLocked()) {
            throw new IllegalStateException("User ist nicht gesperrt.");
        }

        target.setLocked(false);
        target.setLockedAt(null);
        useraccountRepository.save(target);

        auditService.log(actor.getId(), target.getId(), AuditActionType.UNLOCK_USER, reason);
        return target;
    }

    public Useraccount grantAdmin(Long actorId, long targetUserId, String reason) {
        Useraccount actor = requireAdmin(actorId);
        Useraccount target = getTargetUser(targetUserId);

        if (target.getRole() == Role.ADMIN) {
            throw new IllegalStateException("User hat bereits Admin-Rechte.");
        }
        if (target.getRole() != Role.GAMER) {
            throw new IllegalStateException("Rollenwechsel nur für GAMER <-> ADMIN erlaubt.");
        }

        target.setRole(Role.ADMIN);
        useraccountRepository.save(target);

        auditService.log(actor.getId(), target.getId(), AuditActionType.GRANT_ADMIN, reason);
        return target;
    }

    public Useraccount revokeAdmin(Long actorId, long targetUserId, String reason) {
        Useraccount actor = requireAdmin(actorId);
        Useraccount target = getTargetUser(targetUserId);

        if (actor.getId().equals(target.getId())) {
            throw new IllegalStateException("Admin darf sich selbst die Admin-Rechte nicht entziehen.");
        }
        if (target.getRole() != Role.ADMIN) {
            throw new IllegalStateException("User besitzt keine Admin-Rechte.");
        }

        target.setRole(Role.GAMER);
        useraccountRepository.save(target);

        auditService.log(actor.getId(), target.getId(), AuditActionType.REVOKE_ADMIN, reason);
        return target;
    }

    public Useraccount changePassword(Long actorId, long targetUserId, String temporaryPassword, String ticketOrReason) {
        Useraccount actor = requireAdmin(actorId);
        Useraccount target = getTargetUser(targetUserId);

        validatePasswordPolicy(temporaryPassword);

        target.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        target.setPasswordResetDate(LocalDateTime.now());
        useraccountRepository.save(target);

        auditService.log(actor.getId(), target.getId(), AuditActionType.CHANGE_PASSWD, ticketOrReason);
        return target;
    }

    public Wallet adjustCoins(Long actorId, long targetUserId, long amountDelta, String reason) {
        Useraccount actor = requireAdmin(actorId);
        Useraccount target = getTargetUser(targetUserId);

        if (amountDelta == 0) {
            throw new IllegalArgumentException("Der Anpassungsbetrag darf nicht 0 sein.");
        }
        if (target.getWallet() == null) {
            throw new IllegalStateException("Der Ziel-User besitzt kein Wallet.");
        }

        Wallet wallet = target.getWallet();
        long newBalance;
        try {
            newBalance = Math.addExact(wallet.getBalance(), amountDelta);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Die Coin-Anpassung ist außerhalb des gültigen Wertebereichs.");
        }
        if (newBalance < 0) {
            throw new IllegalStateException("Die Coin-Anpassung würde zu negativem Guthaben führen.");
        }

        String details = amountDelta + "|" + reason;
        if (details.length() > MAX_AUDIT_DETAIL_LENGTH) {
            throw new IllegalArgumentException("Die kombinierte Audit-Meldung (Betrag und Grund) darf maximal 70 Zeichen enthalten.");
        }

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        auditService.log(actor.getId(), target.getId(), AuditActionType.COIN_ADJUST, details);
        return wallet;
    }

    public Useraccount getUserDetails(Long actorId, long targetUserId) {
        Useraccount actor = requireAdmin(actorId);
        Useraccount target = getTargetUser(targetUserId);
        auditService.log(actor.getId(), target.getId(), AuditActionType.VIEW_DETAILS, "Sensitive user details viewed");
        return target;
    }

    private Useraccount requireAdmin(Long actorId) {
        if (actorId == null) {
            throw new AccessDeniedException("Admin-Rechte erforderlich.");
        }

        Useraccount actor = useraccountRepository.findById(actorId)
                .orElseThrow(() -> new AccessDeniedException("Admin-Rechte erforderlich."));

        if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.SUPERADMIN) {
            throw new AccessDeniedException("Admin-Rechte erforderlich.");
        }
        return actor;
    }

    private Useraccount getTargetUser(long targetUserId) {
        return useraccountRepository.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Es wurde kein User mit der ID " + targetUserId + " gefunden."));
    }

    private void validatePasswordPolicy(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Passwort darf nicht leer sein.");
        }
        if (password.length() < 12) {
            throw new IllegalArgumentException("Passwort muss mindestens 12 Zeichen lang sein.");
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        if (!hasUpper) {
            throw new IllegalArgumentException("Passwort muss mindestens einen Großbuchstaben enthalten.");
        }
        if (!hasLower) {
            throw new IllegalArgumentException("Passwort muss mindestens einen Kleinbuchstaben enthalten.");
        }
        if (!hasDigit) {
            throw new IllegalArgumentException("Passwort muss mindestens eine Zahl enthalten.");
        }
        if (!hasSpecial) {
            throw new IllegalArgumentException("Passwort muss mindestens ein Sonderzeichen enthalten.");
        }
    }
}
