package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditActionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditLog;
import de.edvschuleplattling.irgendwieanders.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service zum Schreiben unveränderlicher Audit-Einträge.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {

    private static final int MAX_ACTION_DETAILS_LENGTH = 70;

    private final AuditLogRepository auditLogRepository;

    /**
     * Schreibt einen neuen Audit-Eintrag.
     *
     * @param actorId ID des ausführenden Nutzers
     * @param targetId ID des betroffenen Nutzers (optional)
     * @param actionType Aktionstyp
     * @param details Aktionsdetails (max. 70 Zeichen)
     * @return persistierter Audit-Eintrag
     */
    public AuditLog log(Long actorId, Long targetId, AuditActionType actionType, String details) {
        if (actorId == null) {
            throw new IllegalArgumentException("actorId darf nicht null sein.");
        }
        if (actionType == null) {
            throw new IllegalArgumentException("actionType darf nicht null sein.");
        }
        if (details == null || details.isBlank()) {
            throw new IllegalArgumentException("details darf nicht leer sein.");
        }
        if (details.length() > MAX_ACTION_DETAILS_LENGTH) {
            throw new IllegalArgumentException("details darf maximal 70 Zeichen enthalten.");
        }

        AuditLog auditLog = new AuditLog(actorId, targetId, actionType, details);
        return auditLogRepository.save(auditLog);
    }
}
