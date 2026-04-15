package de.edvschuleplattling.irgendwieanders.rest;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditActionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement.AuditLog;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import de.edvschuleplattling.irgendwieanders.rest.dto.AuditLogResponseDto;
import de.edvschuleplattling.irgendwieanders.service.AuditReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AuditReadRestController {

    private final AuditReadService auditReadService;
    private final UseraccountRepository useraccountRepository;

    @GetMapping
    @Operation(summary = "Audit-Einträge mit Filtern und Pagination abrufen (neueste zuerst)")
    public ResponseEntity<Page<AuditLogResponseDto>> getAuditLogs(
            @Parameter(description = "Seitennummer (0-basiert)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Seitengröße") @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size,
            @Parameter(description = "Filter: actorId") @RequestParam(required = false) Long actorId,
            @Parameter(description = "Filter: targetId") @RequestParam(required = false) Long targetId,
            @Parameter(description = "Filter: actionType") @RequestParam(required = false) AuditActionType actionType,
            @Parameter(description = "Filter: Startzeitpunkt (inklusive), ISO-8601") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "Filter: Endzeitpunkt (inklusive), ISO-8601") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @Parameter(description = "Optional: Textsuche in actionDetails") @RequestParam(required = false) String q
    ) {
        Page<AuditLog> auditLogPage = auditReadService.getAuditLogs(
                page, size, actorId, targetId, actionType, dateFrom, dateTo, q
        );
        Map<Long, String> usernamesById = resolveActorTargetUsernames(auditLogPage);
        Page<AuditLogResponseDto> dtoPage = auditLogPage.map(log -> AuditLogResponseDto.fromEntity(log, usernamesById));
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(summary = "Audit-Einträge als CSV exportieren")
    public ResponseEntity<String> exportAuditLogsCsv(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "500") @Min(1) @Max(2000) int size,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) AuditActionType actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) String q
    ) {
        Page<AuditLog> auditLogPage = auditReadService.getAuditLogs(
                page, size, actorId, targetId, actionType, dateFrom, dateTo, q
        );
        Map<Long, String> usernamesById = resolveActorTargetUsernames(auditLogPage);

        StringBuilder csv = new StringBuilder("actor,target,actorUsername,targetUsername,actionType,actionDetails,createdAt\n");
        for (AuditLog log : auditLogPage.getContent()) {
            csv.append(csvValue(log.getActorId())).append(',')
                    .append(csvValue(log.getTargetId())).append(',')
                    .append(csvValue(usernamesById.get(log.getActorId()))).append(',')
                    .append(csvValue(log.getTargetId() == null ? null : usernamesById.get(log.getTargetId()))).append(',')
                    .append(csvValue(log.getActionType())).append(',')
                    .append(csvValue(log.getActionDetails())).append(',')
                    .append(csvValue(log.getCreatedAt()))
                    .append('\n');
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log.csv\"")
                .contentType(new MediaType("text", "csv"))
                .body(csv.toString());
    }

    private Map<Long, String> resolveActorTargetUsernames(Page<AuditLog> auditLogPage) {
        Set<Long> ids = new HashSet<>();
        for (AuditLog log : auditLogPage.getContent()) {
            ids.add(log.getActorId());
            if (log.getTargetId() != null) {
                ids.add(log.getTargetId());
            }
        }
        if (ids.isEmpty()) {
            return Map.of();
        }
        return useraccountRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Useraccount::getId, Useraccount::getEmail, (a, b) -> a, HashMap::new));
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString().replace("\"", "\"\"");
        if (!text.isEmpty() && ("=+-@".indexOf(text.charAt(0)) >= 0)) {
            text = "'" + text;
        }
        return "\"" + text + "\"";
    }
}
