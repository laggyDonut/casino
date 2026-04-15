package de.edvschuleplattling.irgendwieanders.rest.dto;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditActionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditLog;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AuditLogResponseDto {

    private Long actor;
    private Long target;
    private AuditActionType actionType;
    private String actionDetails;
    private LocalDateTime createdAt;
    private String actorUsername;
    private String targetUsername;

    public static AuditLogResponseDto fromEntity(AuditLog auditLog, Map<Long, String> usernamesById) {
        AuditLogResponseDto dto = new AuditLogResponseDto();
        dto.setActor(auditLog.getActorId());
        dto.setTarget(auditLog.getTargetId());
        dto.setActionType(auditLog.getActionType());
        dto.setActionDetails(auditLog.getActionDetails());
        dto.setCreatedAt(auditLog.getCreatedAt());
        dto.setActorUsername(usernamesById.get(auditLog.getActorId()));
        dto.setTargetUsername(auditLog.getTargetId() == null ? null : usernamesById.get(auditLog.getTargetId()));
        return dto;
    }
}
