package de.edvschuleplattling.irgendwieanders.rest.dto;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Role;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserActionResponseDto {

    private Long userId;
    private Role role;
    private boolean locked;
    private LocalDateTime lockedAt;
    private LocalDateTime passwordResetDate;

    public static AdminUserActionResponseDto fromEntity(Useraccount useraccount) {
        AdminUserActionResponseDto dto = new AdminUserActionResponseDto();
        dto.setUserId(useraccount.getId());
        dto.setRole(useraccount.getRole());
        dto.setLocked(useraccount.isLocked());
        dto.setLockedAt(useraccount.getLockedAt());
        dto.setPasswordResetDate(useraccount.getPasswordResetDate());
        return dto;
    }
}
