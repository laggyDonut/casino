package de.edvschuleplattling.irgendwieanders.rest;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import de.edvschuleplattling.irgendwieanders.rest.dto.*;
import de.edvschuleplattling.irgendwieanders.service.AdminActionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminRestController {

    private final AdminActionService adminActionService;

    @GetMapping("/{targetUserId}")
    @Operation(summary = "Liest sensible User-Details und schreibt verpflichtend ein VIEW_DETAILS Audit-Log")
    public ResponseEntity<AdminUserActionResponseDto> getUserDetails(
            @RequestHeader("X-Actor-Id") Long actorId,
            @PathVariable long targetUserId) {
        Useraccount useraccount = adminActionService.getUserDetails(actorId, targetUserId);
        return ResponseEntity.ok(AdminUserActionResponseDto.fromEntity(useraccount));
    }

    @PostMapping("/{targetUserId}/lock")
    public ResponseEntity<AdminUserActionResponseDto> lockUser(
            @RequestHeader("X-Actor-Id") Long actorId,
            @PathVariable long targetUserId,
            @RequestBody @Valid AdminReasonRequestDto dto) {
        Useraccount useraccount = adminActionService.lockUser(actorId, targetUserId, dto.getReason());
        return ResponseEntity.ok(AdminUserActionResponseDto.fromEntity(useraccount));
    }

    @PostMapping("/{targetUserId}/unlock")
    public ResponseEntity<AdminUserActionResponseDto> unlockUser(
            @RequestHeader("X-Actor-Id") Long actorId,
            @PathVariable long targetUserId,
            @RequestBody @Valid AdminReasonRequestDto dto) {
        Useraccount useraccount = adminActionService.unlockUser(actorId, targetUserId, dto.getReason());
        return ResponseEntity.ok(AdminUserActionResponseDto.fromEntity(useraccount));
    }

    @PostMapping("/{targetUserId}/grant-admin")
    public ResponseEntity<AdminUserActionResponseDto> grantAdmin(
            @RequestHeader("X-Actor-Id") Long actorId,
            @PathVariable long targetUserId,
            @RequestBody @Valid AdminReasonRequestDto dto) {
        Useraccount useraccount = adminActionService.grantAdmin(actorId, targetUserId, dto.getReason());
        return ResponseEntity.ok(AdminUserActionResponseDto.fromEntity(useraccount));
    }

    @PostMapping("/{targetUserId}/revoke-admin")
    public ResponseEntity<AdminUserActionResponseDto> revokeAdmin(
            @RequestHeader("X-Actor-Id") Long actorId,
            @PathVariable long targetUserId,
            @RequestBody @Valid AdminReasonRequestDto dto) {
        Useraccount useraccount = adminActionService.revokeAdmin(actorId, targetUserId, dto.getReason());
        return ResponseEntity.ok(AdminUserActionResponseDto.fromEntity(useraccount));
    }

    @PostMapping("/{targetUserId}/password-reset")
    public ResponseEntity<AdminUserActionResponseDto> changePassword(
            @RequestHeader("X-Actor-Id") Long actorId,
            @PathVariable long targetUserId,
            @RequestBody @Valid AdminPasswordResetRequestDto dto) {
        Useraccount useraccount = adminActionService.changePassword(
                actorId, targetUserId, dto.getTemporaryPassword(), dto.getTicketOrReason());
        return ResponseEntity.ok(AdminUserActionResponseDto.fromEntity(useraccount));
    }

    @PostMapping("/{targetUserId}/coins/adjust")
    public ResponseEntity<AdminCoinAdjustResponseDto> adjustCoins(
            @RequestHeader("X-Actor-Id") Long actorId,
            @PathVariable long targetUserId,
            @RequestBody @Valid AdminCoinAdjustRequestDto dto) {
        Wallet wallet = adminActionService.adjustCoins(actorId, targetUserId, dto.getAmountDelta(), dto.getReason());
        return ResponseEntity.ok(AdminCoinAdjustResponseDto.fromEntity(targetUserId, wallet, dto.getAmountDelta()));
    }
}
