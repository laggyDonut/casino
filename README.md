# Casino Backend

## Audit Logging Foundation

- Basis für Admin-Audit mit `audit_log` und `AuditActionType` ist vorhanden.
- Audit-Einträge sind append-only ausgelegt: Es gibt nur Schreib- und Lese-Services, keine Update-/Delete-Operationen.
- `AuditService.log(actorId, targetId, actionType, details)` validiert Pflichtfelder und lehnt `details` über 70 Zeichen mit Fehler ab.
- `AuditReadService.getAuditLogs(page, size, actorId, targetId, actionType)` unterstützt Paging, optionale Filter und standardmäßige Sortierung nach `createdAt` absteigend.
- Eine Flyway-kompatible Migration für `audit_log` inkl. Foreign Keys und Indizes liegt unter `src/main/resources/db/migration/V1__create_audit_log.sql`.
