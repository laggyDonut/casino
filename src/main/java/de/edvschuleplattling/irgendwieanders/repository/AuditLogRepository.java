package de.edvschuleplattling.irgendwieanders.repository;

import de.edvschuleplattling.irgendwieanders.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

}

