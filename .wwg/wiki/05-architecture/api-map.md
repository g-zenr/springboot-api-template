# API Map

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/api/v1/auth/login` | Public | Access + refresh tokens |
| POST | `/api/v1/auth/refresh` | Public | Rotate refresh |
| POST | `/api/v1/auth/logout` | Public | Revoke refresh |
| POST | `/api/v1/auth/register` | Public* | Self-register USER (*if allow-registration) |
| GET | `/api/v1/users/me` | Bearer | Current user (`select` optional) |
| PUT | `/api/v1/users/me/password` | Bearer | Change password |
| GET | `/api/v1/users` | ADMIN | List users (paged + list query) |
| POST | `/api/v1/users` | ADMIN | Create user |
| GET/POST/PUT/DELETE | `/api/v1/items` | Bearer | Sample catalog CRUD (list uses list query); DELETE is **soft-delete** (`deleted_at`) |
| GET/POST/PUT/DELETE | `/api/v1/notes` | Bearer | Owner-scoped notes (list uses list query) |
| GET | `/actuator/health` | Public | Health |

Notes are isolated per authenticated username (other users get 404).

---

## Shared list query

**Canonical agent truth:** [`list-query-contract.md`](list-query-contract.md)  
**Human mirror:** `docs/LIST-QUERY.md`

### Quick params

| Param | Default | Role |
|-------|---------|------|
| `page` | `0` | Zero-based page |
| `limit` | `20` (max 100) | Page size |
| `sort` | resource default | Allow-listed fields |
| `search` | — | Text filter |
| `select` | — | Sparse / deep fields |
| `include` | — | Optional extras |

### `include` available values (summary)

| Token | Status | Effect |
|-------|--------|--------|
| _(omit)_ | Default | Documents + totals |
| `count` | Usable | Documents + totals |
| `countOnly` | Usable | `{ "count": N }` only |
| `pagination` | Reserved | Recognized, no-op |
| `summary` | Reserved | Recognized, no-op |
| `statistics` | Reserved | Recognized, no-op |
| `relations` | Reserved | Recognized, no-op |
| `permissions` | Reserved | Recognized, no-op |
| `analytics` | Reserved | Recognized, no-op |

Unknown tokens ignored. Full rules, allow-lists, and agent implementation pattern → **list-query-contract.md**.

### Code

`PageQuery`, `PageResponse`, `PagedReads`, `CountResponse`, `IncludeKey` / `IncludeOptions`, `FieldSelection`.
