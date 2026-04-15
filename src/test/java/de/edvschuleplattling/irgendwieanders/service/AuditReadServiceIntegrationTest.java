package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditActionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditLog;
import de.edvschuleplattling.irgendwieanders.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(AuditReadService.class)
class AuditReadServiceIntegrationTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditReadService auditReadService;

    @Test
    void getAuditLogs_returnsEntriesSortedByCreatedAtDesc() throws InterruptedException {
        AuditLog first = auditLogRepository.save(new AuditLog(1L, 2L, AuditActionType.LOCK_USER, "first"));
        Thread.sleep(5L);
        AuditLog second = auditLogRepository.save(new AuditLog(1L, 3L, AuditActionType.UNLOCK_USER, "second"));
        Thread.sleep(5L);
        AuditLog third = auditLogRepository.save(new AuditLog(2L, 3L, AuditActionType.VIEW_DETAILS, "third"));

        Page<AuditLog> page = auditReadService.getAuditLogs(0, 10, null, null, null);

        assertEquals(3, page.getTotalElements());
        assertEquals(third.getId(), page.getContent().get(0).getId());
        assertEquals(second.getId(), page.getContent().get(1).getId());
        assertEquals(first.getId(), page.getContent().get(2).getId());
        assertTrue(page.getContent().get(0).getCreatedAt().isAfter(page.getContent().get(1).getCreatedAt())
                || page.getContent().get(0).getCreatedAt().isEqual(page.getContent().get(1).getCreatedAt()));
    }
}
