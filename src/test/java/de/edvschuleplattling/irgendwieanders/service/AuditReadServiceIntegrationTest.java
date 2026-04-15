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

        Page<AuditLog> page = auditReadService.getAuditLogs(0, 10, null, null, null, null, null, null);

        assertEquals(3, page.getTotalElements());
        assertEquals(third.getId(), page.getContent().get(0).getId());
        assertEquals(second.getId(), page.getContent().get(1).getId());
        assertEquals(first.getId(), page.getContent().get(2).getId());
        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), page.getContent().get(0).getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 1, 1, 11, 0), page.getContent().get(1).getCreatedAt());
    }

    @Test
    void getAuditLogs_filtersByCombinationIncludingDetails() {
        AuditLog matching = auditLogRepository.save(new AuditLog(11L, 21L, AuditActionType.LOCK_USER, "Ticket-ABC"));
        AuditLog wrongTarget = auditLogRepository.save(new AuditLog(11L, 22L, AuditActionType.LOCK_USER, "Ticket-ABC"));
        AuditLog wrongType = auditLogRepository.save(new AuditLog(11L, 21L, AuditActionType.UNLOCK_USER, "Ticket-ABC"));
        AuditLog wrongText = auditLogRepository.save(new AuditLog(11L, 21L, AuditActionType.LOCK_USER, "other"));

        setCreatedAt(matching.getId(), LocalDateTime.of(2026, 2, 1, 10, 0));
        setCreatedAt(wrongTarget.getId(), LocalDateTime.of(2026, 2, 1, 10, 1));
        setCreatedAt(wrongType.getId(), LocalDateTime.of(2026, 2, 1, 10, 2));
        setCreatedAt(wrongText.getId(), LocalDateTime.of(2026, 2, 1, 10, 3));
        entityManager.clear();

        Page<AuditLog> page = auditReadService.getAuditLogs(
                0, 10, 11L, 21L, AuditActionType.LOCK_USER, null, null, "ticket"
        );

        assertEquals(1, page.getTotalElements());
        assertEquals(matching.getId(), page.getContent().get(0).getId());
    }

    @Test
    void getAuditLogs_filtersByDateRangeInclusive() {
        AuditLog before = auditLogRepository.save(new AuditLog(1L, 2L, AuditActionType.LOCK_USER, "before"));
        AuditLog fromBoundary = auditLogRepository.save(new AuditLog(1L, 2L, AuditActionType.LOCK_USER, "from"));
        AuditLog toBoundary = auditLogRepository.save(new AuditLog(1L, 2L, AuditActionType.LOCK_USER, "to"));
        AuditLog after = auditLogRepository.save(new AuditLog(1L, 2L, AuditActionType.LOCK_USER, "after"));

        LocalDateTime dateFrom = LocalDateTime.of(2026, 3, 5, 11, 0);
        LocalDateTime dateTo = LocalDateTime.of(2026, 3, 5, 12, 0);

        setCreatedAt(before.getId(), LocalDateTime.of(2026, 3, 5, 10, 59));
        setCreatedAt(fromBoundary.getId(), dateFrom);
        setCreatedAt(toBoundary.getId(), dateTo);
        setCreatedAt(after.getId(), LocalDateTime.of(2026, 3, 5, 12, 1));
        entityManager.clear();

        Page<AuditLog> page = auditReadService.getAuditLogs(
                0, 10, null, null, null, dateFrom, dateTo, null
        );

        assertEquals(2, page.getTotalElements());
        assertEquals(toBoundary.getId(), page.getContent().get(0).getId());
        assertEquals(fromBoundary.getId(), page.getContent().get(1).getId());
    }

    private void setCreatedAt(Long id, LocalDateTime createdAt) {
        entityManager.createNativeQuery("UPDATE audit_log SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", Timestamp.valueOf(createdAt))
                .setParameter("id", id)
                .executeUpdate();
    }
}
