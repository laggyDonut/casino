package de.edvschuleplattling.irgendwieanders.rest;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import de.edvschuleplattling.irgendwieanders.config.UserPrincipal;
import de.edvschuleplattling.irgendwieanders.rest.dto.*;
import de.edvschuleplattling.irgendwieanders.service.AdminActionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN','SUPERADMIN')")
public class AdminRestController {

    private final AdminActionService adminActionService;

    @GetMapping("/{targetUserId}")
    @Operation(summary = "Liest sensible User-Details und schreibt verpflichtend ein VIEW_DETAILS Audit-Log")
    public ResponseEntity<AdminUserActionResponseDto> getUserDetails(
            @RequestHeader(value = "X-Actor-Id", required = false) Long actorIdHeader,
            Authentication authentication,
            @PathVariable long targetUserId) {
        Long actorId = resolveActorId(actorIdHeader, authentication);
        Useraccount useraccount = adminActionService.getUserDetails(actorId, targetUserId);
        return ResponseEntity.ok(AdminUserActionResponseDto.fromEntity(useraccount));
    }

    @PostMapping("/{targetUserId}/lock")
    public ResponseEntity<AdminUserActionResponseDto> lockUser(
            @RequestHeader(value = "X-Actor-Id", required = false) Long actorIdHeader,
            Authentication authentication,
            @PathVariable long targetUserId,
            @RequestBody @Valid AdminReasonRequestDto dto) {
        Long actorId = resolveActorId(actorIdHeader, authentication);
        Useraccount useraccount = adminActionService.lockUser(actorId, targetUserId, dto.getReason());
        return ResponseEntity.ok(AdminUserActionResponseDto.fromEntity(useraccount));
    }

    @PostMapping("/{targetUserId}/unlock")
    public ResponseEntity<AdminUserActionResponseDto> unlockUser(
            @RequestHeader(value = "X-Actor-Id", required = false) Long actorIdHeader,
            Authentication authentication,
            @PathVariable long targetUserId,
            @RequestBody @Valid AdminReasonRequestDto dto) {
        Long actorId = resolveActorId(actorIdHeader, authentication);
        Useraccount useraccount = adminActionService.unlockUser(actorId, targetUserId, dto.getReason());
        return ResponseEntity.ok(AdminUserActionResponseDto.fromEntity(useraccount));
    }

    @PostMapping("/{targetUserId}/grant-admin")
    public ResponseEntity<AdminUserActionResponseDto> grantAdmin(
            @RequestHeader(value = "X-Actor-Id", required = false) Long actorIdHeader,
            Authentication authentication,
            @PathVariable long targetUserId,
            @RequestBody @Valid AdminReasonRequestDto dto) {
        Long actorId = resolveActorId(actorIdHeader, authentication);
        Useraccount useraccount = adminActionService.grantAdmin(actorId, targetUserId, dto.getReason());
        return ResponseEntity.ok(AdminUserActionResponseDto.fromEntity(useraccount));
    }

    @PostMapping("/{targetUserId}/revoke-admin")
    public ResponseEntity<AdminUserActionResponseDto> revokeAdmin(
            @RequestHeader(value = "X-Actor-Id", required = false) Long actorIdHeader,
            Authentication authentication,
            @PathVariable long targetUserId,
            @RequestBody @Valid AdminReasonRequestDto dto) {
        Long actorId = resolveActorId(actorIdHeader, authentication);
        Useraccount useraccount = adminActionService.revokeAdmin(actorId, targetUserId, dto.getReason());
        return ResponseEntity.ok(AdminUserActionResponseDto.fromEntity(useraccount));
    }

    @PostMapping("/{targetUserId}/password-reset")
    public ResponseEntity<AdminUserActionResponseDto> changePassword(
            @RequestHeader(value = "X-Actor-Id", required = false) Long actorIdHeader,
            Authentication authentication,
            @PathVariable long targetUserId,
            @RequestBody @Valid AdminPasswordResetRequestDto dto) {
        Long actorId = resolveActorId(actorIdHeader, authentication);
        Useraccount useraccount = adminActionService.changePassword(
                actorId, targetUserId, dto.getTemporaryPassword(), dto.getTicketOrReason());
        return ResponseEntity.ok(AdminUserActionResponseDto.fromEntity(useraccount));
    }

    @PostMapping("/{targetUserId}/coins/adjust")
    public ResponseEntity<AdminCoinAdjustResponseDto> adjustCoins(
            @RequestHeader(value = "X-Actor-Id", required = false) Long actorIdHeader,
            Authentication authentication,
            @PathVariable long targetUserId,
            @RequestBody @Valid AdminCoinAdjustRequestDto dto) {
        Long actorId = resolveActorId(actorIdHeader, authentication);
        Wallet wallet = adminActionService.adjustCoins(actorId, targetUserId, dto.getAmountDelta(), dto.getReason());
        return ResponseEntity.ok(AdminCoinAdjustResponseDto.fromEntity(targetUserId, wallet, dto.getAmountDelta()));
    }

    private Long resolveActorId(Long actorIdHeader, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Admin-Rechte erforderlich.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            Long principalActorId = userPrincipal.getAccount().getId();
            if (actorIdHeader != null && !actorIdHeader.equals(principalActorId)) {
                throw new AccessDeniedException("X-Actor-Id stimmt nicht mit dem angemeldeten Benutzer überein.");
            }
            return principalActorId;
        }

        if (actorIdHeader == null) {
            throw new AccessDeniedException("X-Actor-Id Header fehlt.");
        }
        return actorIdHeader;
    }
}
