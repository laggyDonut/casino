package de.edvschuleplattling.irgendwieanders.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditActionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Role;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import de.edvschuleplattling.irgendwieanders.repository.AuditLogRepository;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import de.edvschuleplattling.irgendwieanders.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UseraccountRepository useraccountRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void lockUser_success_changesStateAndWritesAudit() throws Exception {
        Useraccount admin = createUser(Role.ADMIN);
        Useraccount target = createUser(Role.GAMER);

        long before = countAuditEntries(admin.getId(), target.getId(), AuditActionType.LOCK_USER);

        mockMvc.perform(post("/api/admin/users/{id}/lock", target.getId())
                        .header("X-Actor-Id", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "Verdacht"))))
                .andExpect(status().isOk());

        Useraccount updated = useraccountRepository.findById(target.getId()).orElseThrow();
        assertTrue(updated.isLocked());
        assertNotNull(updated.getLockedAt());
        assertEquals(before + 1, countAuditEntries(admin.getId(), target.getId(), AuditActionType.LOCK_USER));
    }

    @Test
    void lockUser_forbidden_noStateChangeAndNoAudit() throws Exception {
        Useraccount gamer = createUser(Role.GAMER);
        Useraccount target = createUser(Role.GAMER);

        long before = countAuditEntries(gamer.getId(), target.getId(), AuditActionType.LOCK_USER);

        mockMvc.perform(post("/api/admin/users/{id}/lock", target.getId())
                        .header("X-Actor-Id", gamer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "no rights"))))
                .andExpect(status().isForbidden());

        Useraccount unchanged = useraccountRepository.findById(target.getId()).orElseThrow();
        assertFalse(unchanged.isLocked());
        assertNull(unchanged.getLockedAt());
        assertEquals(before, countAuditEntries(gamer.getId(), target.getId(), AuditActionType.LOCK_USER));
    }

    @Test
    void unlockUser_success_changesStateAndWritesAudit() throws Exception {
        Useraccount admin = createUser(Role.ADMIN);
        Useraccount target = createUser(Role.GAMER);
        target.setLocked(true);
        target.setLockedAt(java.time.LocalDateTime.now());
        useraccountRepository.save(target);

        long before = countAuditEntries(admin.getId(), target.getId(), AuditActionType.UNLOCK_USER);

        mockMvc.perform(post("/api/admin/users/{id}/unlock", target.getId())
                        .header("X-Actor-Id", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "Entsperrt"))))
                .andExpect(status().isOk());

        Useraccount updated = useraccountRepository.findById(target.getId()).orElseThrow();
        assertFalse(updated.isLocked());
        assertNull(updated.getLockedAt());
        assertEquals(before + 1, countAuditEntries(admin.getId(), target.getId(), AuditActionType.UNLOCK_USER));
    }

    @Test
    void unlockUser_forbidden_noStateChangeAndNoAudit() throws Exception {
        Useraccount gamer = createUser(Role.GAMER);
        Useraccount target = createUser(Role.GAMER);
        target.setLocked(true);
        target.setLockedAt(java.time.LocalDateTime.now());
        useraccountRepository.save(target);

        long before = countAuditEntries(gamer.getId(), target.getId(), AuditActionType.UNLOCK_USER);

        mockMvc.perform(post("/api/admin/users/{id}/unlock", target.getId())
                        .header("X-Actor-Id", gamer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "no rights"))))
                .andExpect(status().isForbidden());

        Useraccount unchanged = useraccountRepository.findById(target.getId()).orElseThrow();
        assertTrue(unchanged.isLocked());
        assertNotNull(unchanged.getLockedAt());
        assertEquals(before, countAuditEntries(gamer.getId(), target.getId(), AuditActionType.UNLOCK_USER));
    }

    @Test
    void grantAdmin_success_changesRoleAndWritesAudit() throws Exception {
        Useraccount admin = createUser(Role.ADMIN);
        Useraccount target = createUser(Role.GAMER);

        long before = countAuditEntries(admin.getId(), target.getId(), AuditActionType.GRANT_ADMIN);

        mockMvc.perform(post("/api/admin/users/{id}/grant-admin", target.getId())
                        .header("X-Actor-Id", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "Team"))))
                .andExpect(status().isOk());

        Useraccount updated = useraccountRepository.findById(target.getId()).orElseThrow();
        assertEquals(Role.ADMIN, updated.getRole());
        assertEquals(before + 1, countAuditEntries(admin.getId(), target.getId(), AuditActionType.GRANT_ADMIN));
    }

    @Test
    void grantAdmin_forbidden_noStateChangeAndNoAudit() throws Exception {
        Useraccount gamer = createUser(Role.GAMER);
        Useraccount target = createUser(Role.GAMER);

        long before = countAuditEntries(gamer.getId(), target.getId(), AuditActionType.GRANT_ADMIN);

        mockMvc.perform(post("/api/admin/users/{id}/grant-admin", target.getId())
                        .header("X-Actor-Id", gamer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "no rights"))))
                .andExpect(status().isForbidden());

        Useraccount unchanged = useraccountRepository.findById(target.getId()).orElseThrow();
        assertEquals(Role.GAMER, unchanged.getRole());
        assertEquals(before, countAuditEntries(gamer.getId(), target.getId(), AuditActionType.GRANT_ADMIN));
    }

    @Test
    void revokeAdmin_success_changesRoleAndWritesAudit() throws Exception {
        Useraccount admin = createUser(Role.ADMIN);
        Useraccount target = createUser(Role.ADMIN);

        long before = countAuditEntries(admin.getId(), target.getId(), AuditActionType.REVOKE_ADMIN);

        mockMvc.perform(post("/api/admin/users/{id}/revoke-admin", target.getId())
                        .header("X-Actor-Id", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "Cleanup"))))
                .andExpect(status().isOk());

        Useraccount updated = useraccountRepository.findById(target.getId()).orElseThrow();
        assertEquals(Role.GAMER, updated.getRole());
        assertEquals(before + 1, countAuditEntries(admin.getId(), target.getId(), AuditActionType.REVOKE_ADMIN));
    }

    @Test
    void revokeAdmin_forbidden_noStateChangeAndNoAudit() throws Exception {
        Useraccount gamer = createUser(Role.GAMER);
        Useraccount target = createUser(Role.ADMIN);

        long before = countAuditEntries(gamer.getId(), target.getId(), AuditActionType.REVOKE_ADMIN);

        mockMvc.perform(post("/api/admin/users/{id}/revoke-admin", target.getId())
                        .header("X-Actor-Id", gamer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "no rights"))))
                .andExpect(status().isForbidden());

        Useraccount unchanged = useraccountRepository.findById(target.getId()).orElseThrow();
        assertEquals(Role.ADMIN, unchanged.getRole());
        assertEquals(before, countAuditEntries(gamer.getId(), target.getId(), AuditActionType.REVOKE_ADMIN));
    }

    @Test
    void revokeAdmin_selfRevokeBlocked_noRoleChangeAndNoAudit() throws Exception {
        Useraccount admin = createUser(Role.ADMIN);
        long before = countAuditEntries(admin.getId(), admin.getId(), AuditActionType.REVOKE_ADMIN);

        mockMvc.perform(post("/api/admin/users/{id}/revoke-admin", admin.getId())
                        .header("X-Actor-Id", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "self"))))
                .andExpect(status().isConflict());

        Useraccount unchanged = useraccountRepository.findById(admin.getId()).orElseThrow();
        assertEquals(Role.ADMIN, unchanged.getRole());
        assertEquals(before, countAuditEntries(admin.getId(), admin.getId(), AuditActionType.REVOKE_ADMIN));
    }

    @Test
    void changePassword_success_hashesPasswordAndWritesAudit() throws Exception {
        Useraccount admin = createUser(Role.ADMIN);
        Useraccount target = createUser(Role.GAMER);
        String oldHash = target.getPasswordHash();
        long before = countAuditEntries(admin.getId(), target.getId(), AuditActionType.CHANGE_PASSWD);

        mockMvc.perform(post("/api/admin/users/{id}/password-reset", target.getId())
                        .header("X-Actor-Id", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "temporaryPassword", "TempPasswort!123",
                                "ticketOrReason", "INC-12345"))))
                .andExpect(status().isOk());

        Useraccount updated = useraccountRepository.findById(target.getId()).orElseThrow();
        assertNotEquals(oldHash, updated.getPasswordHash());
        assertNotEquals("TempPasswort!123", updated.getPasswordHash());
        assertNotNull(updated.getPasswordResetDate());
        assertEquals(before + 1, countAuditEntries(admin.getId(), target.getId(), AuditActionType.CHANGE_PASSWD));
    }

    @Test
    void changePassword_forbidden_noStateChangeAndNoAudit() throws Exception {
        Useraccount gamer = createUser(Role.GAMER);
        Useraccount target = createUser(Role.GAMER);
        String oldHash = target.getPasswordHash();
        long before = countAuditEntries(gamer.getId(), target.getId(), AuditActionType.CHANGE_PASSWD);

        mockMvc.perform(post("/api/admin/users/{id}/password-reset", target.getId())
                        .header("X-Actor-Id", gamer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "temporaryPassword", "TempPasswort!123",
                                "ticketOrReason", "ticket"))))
                .andExpect(status().isForbidden());

        Useraccount unchanged = useraccountRepository.findById(target.getId()).orElseThrow();
        assertEquals(oldHash, unchanged.getPasswordHash());
        assertEquals(before, countAuditEntries(gamer.getId(), target.getId(), AuditActionType.CHANGE_PASSWD));
    }

    @Test
    void coinAdjust_success_changesWalletAndWritesAudit() throws Exception {
        Useraccount admin = createUser(Role.ADMIN);
        Useraccount target = createUserWithWallet(Role.GAMER, 1000L);
        long before = countAuditEntries(admin.getId(), target.getId(), AuditActionType.COIN_ADJUST);

        mockMvc.perform(post("/api/admin/users/{id}/coins/adjust", target.getId())
                        .header("X-Actor-Id", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "amountDelta", -200L,
                                "reason", "Correction"))))
                .andExpect(status().isOk());

        Wallet wallet = walletRepository.findByUseraccountId(target.getId()).orElseThrow();
        assertEquals(800L, wallet.getBalance());
        assertEquals(before + 1, countAuditEntries(admin.getId(), target.getId(), AuditActionType.COIN_ADJUST));
    }

    @Test
    void coinAdjust_forbidden_noStateChangeAndNoAudit() throws Exception {
        Useraccount gamer = createUser(Role.GAMER);
        Useraccount target = createUserWithWallet(Role.GAMER, 1000L);
        long before = countAuditEntries(gamer.getId(), target.getId(), AuditActionType.COIN_ADJUST);

        mockMvc.perform(post("/api/admin/users/{id}/coins/adjust", target.getId())
                        .header("X-Actor-Id", gamer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "amountDelta", 100L,
                                "reason", "no rights"))))
                .andExpect(status().isForbidden());

        Wallet wallet = walletRepository.findByUseraccountId(target.getId()).orElseThrow();
        assertEquals(1000L, wallet.getBalance());
        assertEquals(before, countAuditEntries(gamer.getId(), target.getId(), AuditActionType.COIN_ADJUST));
    }

    private Useraccount createUser(Role role) {
        Useraccount user = new Useraccount("user-" + java.util.UUID.randomUUID() + "@example.com", "hash_dummy");
        user.setRole(role);
        return useraccountRepository.save(user);
    }

    private Useraccount createUserWithWallet(Role role, long balance) {
        Useraccount user = createUser(role);
        Wallet wallet = new Wallet(user, balance, 0L, 0L, 0L);
        wallet = walletRepository.save(wallet);
        user.setWallet(wallet);
        return useraccountRepository.save(user);
    }

    private long countAuditEntries(Long actorId, Long targetId, AuditActionType actionType) {
        return auditLogRepository.findAll().stream()
                .filter(a -> a.getActorId().equals(actorId))
                .filter(a -> a.getTargetId() != null && a.getTargetId().equals(targetId))
                .filter(a -> a.getActionType() == actionType)
                .count();
    }
}
