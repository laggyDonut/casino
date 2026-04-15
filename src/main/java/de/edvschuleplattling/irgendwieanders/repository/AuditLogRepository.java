package de.edvschuleplattling.irgendwieanders.repository;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

}
