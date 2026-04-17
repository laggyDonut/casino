# Admin & Audit Runbook (Phase 4)

## a) Liste aller Verbesserungen

1. **Security-Hardening (Admin/Audit)**
   - `@PreAuthorize("hasAnyAuthority('ADMIN','SUPERADMIN')")` für:
     - `/api/admin/users/**`
     - `/api/admin/audit/**`
   - Admin-Aktor wird serverseitig abgesichert:
     - Wenn `UserPrincipal` vorhanden ist, wird die Actor-ID aus dem eingeloggten User gezogen.
     - Ein abweichender `X-Actor-Id` Header wird abgelehnt.
   - Zusätzlicher Guard gegen **Self-lockout**:
     - Admin darf sich nicht selbst sperren.

2. **Konsistentes Error-Handling**
   - Globales Fehlerformat vereinheitlicht auf:
     - `timestamp`
     - `code`
     - `message`
     - `path`
   - Gilt für fachliche Exceptions im `GlobalExceptionHandler`.
   - Für Security-Fehler auf `/api/**` ebenfalls vereinheitlicht:
     - 401 Authentication Entry Point
     - 403 Access Denied Handler

3. **Transaction-Hardening / Konsistenz**
   - Kritische Integrations-Tests ergänzt, die Rollback-Verhalten absichern:
     - Bei Fehlern (z. B. Limitüberschreitung, unzureichendes Guthaben) bleibt kein halbfertiger Zustand bestehen.

4. **Abgabe-/Admin-Doku**
   - Dieses Runbook dokumentiert Endpunkte, Rollenmodell, Audit-Matrix, Testabdeckung und Demo-Ablauf.

---

## Endpunkte (Admin & Audit)

### Admin User Actions (`/api/admin/users`)

- `GET /api/admin/users/{targetUserId}` – sensible User-Details lesen (auditpflichtig)
- `POST /api/admin/users/{targetUserId}/lock`
- `POST /api/admin/users/{targetUserId}/unlock`
- `POST /api/admin/users/{targetUserId}/grant-admin`
- `POST /api/admin/users/{targetUserId}/revoke-admin`
- `POST /api/admin/users/{targetUserId}/password-reset`
- `POST /api/admin/users/{targetUserId}/coins/adjust`

### Audit Read (`/api/admin/audit`)

- `GET /api/admin/audit` – paginierte Audit-Abfrage mit Filtern
- `GET /api/admin/audit/export` – CSV-Export

---

## Rollenmodell (relevant für Admin/Audit)

- `SUPERADMIN` – Admin-Rechte inkl. übergeordneter Verwaltung
- `ADMIN` – Admin-Funktionen für User/Audit
- `GAMER` – keine Admin/Audit-Zugriffe
- `SYSTEM` / `DOEDL` – keine Admin/Audit-Endpunkte

**Durchsetzung:**
- API-Guard per Spring Security Authority (`ADMIN`/`SUPERADMIN`)
- Zusätzliche Service-Validierung (`requireAdmin`)
- Zusätzliche Edge-Case-Guards (z. B. Self-lockout)

---

## AuditActionType-Matrix

| AuditActionType | Auslöser |
|---|---|
| `VIEW_DETAILS` | `GET /api/admin/users/{targetUserId}` |
| `LOCK_USER` | `POST /api/admin/users/{targetUserId}/lock` |
| `UNLOCK_USER` | `POST /api/admin/users/{targetUserId}/unlock` |
| `GRANT_ADMIN` | `POST /api/admin/users/{targetUserId}/grant-admin` |
| `REVOKE_ADMIN` | `POST /api/admin/users/{targetUserId}/revoke-admin` |
| `CHANGE_PASSWD` | `POST /api/admin/users/{targetUserId}/password-reset` |
| `COIN_ADJUST` | `POST /api/admin/users/{targetUserId}/coins/adjust` |
| `DELETE_USER` | aktuell vorbereitet, noch nicht an Endpunkt gebunden |

---

## b) Testübersicht (neu abgesicherte Fälle)

### Integration (REST / Security)

- `AdminRestControllerIntegrationTest`
  - Admin erlaubt, Gamer verboten (durch Security + Service)
  - **Self-lockout blockiert** (`lock`)
  - Vereinheitlichte Fehlerstruktur bei Validierungsfehler (`timestamp/code/message/path`)
- `AuditReadRestControllerSecurityIntegrationTest`
  - Audit-Read nur mit `ADMIN`
  - `GAMER` erhält `403`
  - Export-Endpunkt ebenfalls abgesichert

### Integration (Transaktionen)

- `TransactionServiceIntegrationTest`
  - Deposit-Limit überschritten: **Rollback**, keine persistierte Teiltransaktion
  - Payout ohne Guthaben: **Rollback**, Wallet bleibt unverändert
  - Erfolgsfall: Transaktion + Wallet-Update werden gemeinsam persistiert

### DataJpa / Unit (bereits vorhanden + weiter genutzt)

- `AuditReadServiceIntegrationTest` (`@DataJpaTest`) für Filter/Pagination/Sortierung
- `AuditServiceTest` für Audit-Validierung

---

## c) Konkrete Demo-Schritte (5–10 Minuten)

1. **Security-Check Admin vs Gamer**
   - Als `GAMER` `GET /api/admin/audit` aufrufen → `403`
   - Als `ADMIN` denselben Endpunkt aufrufen → `200`

2. **Admin-User-Action + Audit-Nachweis**
   - `POST /api/admin/users/{id}/lock` mit Grund ausführen
   - Danach `GET /api/admin/audit?actionType=LOCK_USER` aufrufen und Eintrag zeigen

3. **Self-lockout-Guard zeigen**
   - Admin versucht eigenes Konto zu sperren → `409` mit konsistentem Fehlerbody

4. **Konsistentes Error-Format zeigen**
   - Admin-`lock` mit ungültigem Body (leerer Grund) → `400`
   - Response enthält `timestamp`, `code`, `message`, `path`

5. **Transaktionskonsistenz demonstrieren**
   - Fehlerfall (z. B. Payout > Guthaben) ausführen
   - Danach zeigen: kein unerwarteter Zwischenzustand im Wallet / keine halbfertige Buchung

---

## Bekannte Grenzen / offene Punkte

- In Tests wird teils mit `MockUser` + `X-Actor-Id` gearbeitet; produktiv wird bei `UserPrincipal` die Actor-ID aus dem eingeloggten User erzwungen.
- Laufzeit in dieser Umgebung benötigt Java 21; lokale CI-/Runtime-Version muss dazu passen.
