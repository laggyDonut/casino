package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditActionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditLog;
import de.edvschuleplattling.irgendwieanders.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Service zum paginierten Lesen von Audit-Einträgen mit optionalen Filtern.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditReadService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Lädt Audit-Einträge paginiert, optional gefiltert und standardmäßig nach Erstellungszeit absteigend sortiert.
     *
     * @param page Seitennummer (0-basiert)
     * @param size Seitengröße
     * @param actorId optionaler Filter auf Akteur-ID
     * @param targetId optionaler Filter auf Ziel-ID
     * @param actionType optionaler Filter auf Aktionstyp
     * @return Seite mit Audit-Einträgen
     */
    public Page<AuditLog> getAuditLogs(
            int page,
            int size,
            Long actorId,
            Long targetId,
            AuditActionType actionType,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String detailsQuery
    ) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom darf nicht nach dateTo liegen.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<AuditLog> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (actorId != null) {
                predicates.add(cb.equal(root.get("actorId"), actorId));
            }
            if (targetId != null) {
                predicates.add(cb.equal(root.get("targetId"), targetId));
            }
            if (actionType != null) {
                predicates.add(cb.equal(root.get("actionType"), actionType));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
            }
            if (detailsQuery != null && !detailsQuery.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("actionDetails")),
                        "%" + detailsQuery.trim().toLowerCase() + "%"
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        return auditLogRepository.findAll(specification, pageable);
    }
}
