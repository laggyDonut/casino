package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditActionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditLog;
import de.edvschuleplattling.irgendwieanders.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void log_validInput_persistsAuditEntry() {
        AuditLog persisted = new AuditLog(1L, 2L, AuditActionType.LOCK_USER, "Reason");
        when(auditLogRepository.save(org.mockito.ArgumentMatchers.any(AuditLog.class))).thenReturn(persisted);

        AuditLog result = auditService.log(1L, 2L, AuditActionType.LOCK_USER, "Reason");

        assertEquals(persisted, result);
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getActorId());
        assertEquals(2L, captor.getValue().getTargetId());
        assertEquals(AuditActionType.LOCK_USER, captor.getValue().getActionType());
        assertEquals("Reason", captor.getValue().getActionDetails());
    }

    @Test
    void log_detailsTooLong_throwsIllegalArgumentException() {
        String tooLong = "x".repeat(71);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> auditService.log(1L, 2L, AuditActionType.LOCK_USER, tooLong));

        assertEquals("details darf maximal 70 Zeichen enthalten.", exception.getMessage());
    }
}
