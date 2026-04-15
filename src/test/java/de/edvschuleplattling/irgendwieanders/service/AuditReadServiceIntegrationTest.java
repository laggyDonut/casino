package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditActionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditLog;
import de.edvschuleplattling.irgendwieanders.repository.AuditLogRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(AuditReadService.class)
class AuditReadServiceIntegrationTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditReadService auditReadService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void getAuditLogs_returnsEntriesSortedByCreatedAtDesc() {
        AuditLog first = auditLogRepository.save(new AuditLog(1L, 2L, AuditActionType.LOCK_USER, "first"));
        AuditLog second = auditLogRepository.save(new AuditLog(1L, 3L, AuditActionType.UNLOCK_USER, "second"));
        AuditLog third = auditLogRepository.save(new AuditLog(2L, 3L, AuditActionType.VIEW_DETAILS, "third"));

        setCreatedAt(first.getId(), LocalDateTime.of(2026, 1, 1, 10, 0));
        setCreatedAt(second.getId(), LocalDateTime.of(2026, 1, 1, 11, 0));
        setCreatedAt(third.getId(), LocalDateTime.of(2026, 1, 1, 12, 0));
        entityManager.clear();

        Page<AuditLog> page = auditReadService.getAuditLogs(0, 10, null, null, null);

        assertEquals(3, page.getTotalElements());
        assertEquals(third.getId(), page.getContent().get(0).getId());
        assertEquals(second.getId(), page.getContent().get(1).getId());
        assertEquals(first.getId(), page.getContent().get(2).getId());
        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), page.getContent().get(0).getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 1, 1, 11, 0), page.getContent().get(1).getCreatedAt());
    }

    private void setCreatedAt(Long id, LocalDateTime createdAt) {
        entityManager.createNativeQuery("UPDATE audit_log SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", Timestamp.valueOf(createdAt))
                .setParameter("id", id)
                .executeUpdate();
    }
}
