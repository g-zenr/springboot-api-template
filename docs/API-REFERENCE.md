# API Reference

Base URL (local): `http://localhost:8080`  
Auth header: `Authorization: Bearer <accessToken>`  
Errors: RFC 7807-style `ProblemDetail` JSON (`title`, `status`, `detail`) unless noted.  
Rate limit exceeded: **429** `{ "title": "Too Many Requests", ... }`  
Correlation: request/response header `X-Correlation-Id`

---

## Auth

### POST `/api/v1/auth/login` — Public

**Body**

```json
{ "username": "admin", "password": "admin123" }
```

**200**

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresInMs": 3600000,
  "refreshExpiresInMs": 604800000,
  "username": "admin",
  "roles": ["ADMIN"]
}
```

**401** invalid credentials

### POST `/api/v1/auth/refresh` — Public

**Body:** `{ "refreshToken": "..." }`  
**200** new token pair (old refresh revoked)  
**401** invalid/expired/revoked

### POST `/api/v1/auth/logout` — Public

**Body:** `{ "refreshToken": "..." }`  
**204** (idempotent if unknown)

### POST `/api/v1/auth/register` — Public when enabled

**Body:** `{ "username": "alice", "password": "password1" }`  
Constraints: username 3–80, password ≥ 8  
**201** `UserResponse`  
**409** username taken  
**403** registration disabled

---

## Users

### GET `/api/v1/users/me` — Bearer

**200** `{ id, username, roles, enabled, createdAt }`

### PUT `/api/v1/users/me/password` — Bearer

**Body:** `{ "currentPassword": "...", "newPassword": "..." }`  
**204** / **401** wrong current password

### GET `/api/v1/users` — Bearer + **ADMIN**

**Query** (shared list contract — see [List query](#list-query-shared) / [LIST-QUERY.md](LIST-QUERY.md)): `page`, `limit`, `sort`, `search`, `select`, `include`  
- Search fields: `username`  
- Sort fields: `username`, `enabled`, `createdAt`, `updatedAt` (default `username,asc`)  
- Select roots: `id`, `username`, `roles`, `enabled`, `createdAt`  
- Include: `count`, `countOnly`

**200** `PageResponse<UserResponse>` or `{ "count": N }` when `include=countOnly`  
**400** invalid page/limit/sort/select  
**403** non-admin

### POST `/api/v1/users` — Bearer + **ADMIN**

**Body:** `{ "username": "...", "password": "...", "roles": ["USER"] }`  
Roles allowed: `ADMIN`, `USER`  
**201** / **409**

---

## List query (shared)

**Full guide (humans):** [LIST-QUERY.md](LIST-QUERY.md)  
**Agent / WWG truth:** `.wwg/wiki/05-architecture/list-query-contract.md`

All list endpoints use the reusable query object from `com.template.api.common.paging` (+ `common.select`, `common.include`):

| Param | Default | Description |
|-------|---------|-------------|
| `page` | `0` | Zero-based page index |
| `limit` | `20` | Page size (1–100) |
| `sort` | resource default | `field`, `field,asc`, `field,desc`, `-field`, or multi `a,asc;b,desc` |
| `search` | _(none)_ | Case-insensitive text search (fields vary by resource) |
| `select` | _(none)_ | Sparse fieldset: comma-separated paths; dots for depth (`owner.username`) |
| `include` | _(none)_ | Optional extras (comma-separated). See [Include](#include-shared) |

**200 envelope**

```json
{
  "content": [ /* full DTOs, or sparse maps when select is set */ ],
  "page": 0,
  "limit": 20,
  "totalElements": 42,
  "totalPages": 3,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false,
  "sort": "name,asc",
  "search": "widget",
  "select": "id,name,owner.username",
  "include": "count"
}
```

**400** when `page < 0`, `limit` outside 1–100, sort field/direction is invalid, or `select` path root is not allow-listed.  
`include` never 400s for unknown tokens (they are ignored).

### Include (shared)

Reusable parser: `com.template.api.common.include.IncludeOptions` / `IncludeKey`.  
**Full catalog + examples:** [LIST-QUERY.md](LIST-QUERY.md#include-options).

#### Available `include` tokens

| Token | Status | Effect |
|-------|--------|--------|
| _(omit)_ | Default | Documents + total count (`PageResponse`) |
| `count` | **Usable** | Documents + total count (`totalElements` / `totalPages`) |
| `countOnly` | **Usable** | **Count only** — `{ "count": N }`; no document rows |
| `pagination` | Reserved | Recognized; not implemented yet (no-op) |
| `summary` | Reserved | Recognized; not implemented yet (no-op; alone skips total count) |
| `statistics` | Reserved | Recognized; not implemented yet (no-op; alone skips total count) |
| `relations` | Reserved | Recognized; not implemented yet (no-op) |
| `permissions` | Reserved | Recognized; not implemented yet (no-op) |
| `analytics` | Reserved | Recognized; not implemented yet (no-op) |

**Usable for real behavior today:** `count`, `countOnly`, or omit `include`.

#### Parser rules

| Rule | Behavior |
|------|----------|
| Format | Comma-separated, e.g. `count,summary` |
| Case | Insensitive |
| Whitespace | Trimmed |
| Duplicates | Removed |
| Unknown values | **Ignored** (no error) |
| Omitted | Default list behavior unchanged |

#### Count policy for lists

| Client `include` | Content rows | Count query | Response shape |
|------------------|--------------|-------------|----------------|
| Omitted | **Yes** | **Yes** | `PageResponse` (default) |
| `count` | **Yes** | **Yes** | `PageResponse` + totals |
| `countOnly` | **No** | **Yes** | `{ "count": N }` only |
| Only reserved (e.g. `summary`) | **Yes** | **No** | `PageResponse`, totals = `-1` |

`countOnly` still respects `search` (and owner scope for notes). `page` / `limit` / `sort` / `select` are ignored for the body (no rows).

Add a new option: add `IncludeKey` constant → services call `query.includes().has(IncludeKey.X)` — no controller parsing.

Examples (every usable option):

```http
GET /api/v1/items
GET /api/v1/items?include=count
GET /api/v1/items?include=countOnly
GET /api/v1/items?search=widget&include=countOnly
GET /api/v1/notes?include=count
GET /api/v1/notes?include=countOnly
GET /api/v1/users?include=count
GET /api/v1/users?include=countOnly
```

Count-only response:

```json
{ "count": 42 }
```

### Select & deep populate

- Omit `select` → full default DTO for each row.
- `select=id,name` → only those top-level fields.
- Deep: `select=id,title,owner.username` → nested object under `owner` (when the resource supports populate).
- Deeper paths work the same way: `a.b.c` (max depth 8).
- Unknown root fields → **400** (allow-listed per resource).
- Get-by-id endpoints also accept optional `select`.

Notes support **populate** for nested `owner` (loaded from users when any `owner` / `owner.*` path is selected). Items/users are flat today — deep paths only apply once nested data exists on the projected object.

Apply the same pattern to new features: `PageQuery` + `Pageables` + `FieldSelection.apply(...)` with a root allow-list.

---

## Items (shared sample)

| Method | Path | Auth | Success |
|--------|------|------|---------|
| GET | `/api/v1/items` | Bearer | 200 paged list |
| GET | `/api/v1/items/{id}` | Bearer | 200 / 404 |
| POST | `/api/v1/items` | Bearer | 201 |
| PUT | `/api/v1/items/{id}` | Bearer | 200 / 404 / 409 |
| DELETE | `/api/v1/items/{id}` | Bearer | 204 / 404 |

**List query:** search on `name`, `description`. Sort: `name`, `description`, `createdAt`, `updatedAt` (default `name,asc`).  
**Select roots:** `id`, `name`, `description`, `createdAt`, `updatedAt`.  
**Include:** `count`, `countOnly` (see [LIST-QUERY.md](LIST-QUERY.md)).

**Body (create/update):** `{ "name": "string", "description": "optional" }`  
Name unique (case-insensitive conflict → 409), max 120; description max 500.

Examples:

```http
GET /api/v1/items?page=0&limit=10&sort=name,asc&search=widget&select=id,name
GET /api/v1/items?include=countOnly
GET /api/v1/items?search=widget&include=countOnly
Authorization: Bearer <token>
```

---

## Notes (owner-scoped sample)

| Method | Path | Auth | Success |
|--------|------|------|---------|
| GET | `/api/v1/notes` | Bearer | 200 paged my notes |
| GET | `/api/v1/notes/{id}` | Bearer | 200 / 404 |
| POST | `/api/v1/notes` | Bearer | 201 |
| PUT | `/api/v1/notes/{id}` | Bearer | 200 / 404 |
| DELETE | `/api/v1/notes/{id}` | Bearer | 204 / 404 |

**List query:** owner-scoped; search on `title`, `body`. Sort: `title`, `body`, `createdAt`, `updatedAt` (default `-updatedAt`).  
**Select roots:** `id`, `ownerUsername`, `title`, `body`, `createdAt`, `updatedAt`, **`owner`** (populate).  
**Include:** `count`, `countOnly` (count is **only your** notes).

Examples:

```http
GET /api/v1/notes?select=id,title,owner.username,owner.roles
GET /api/v1/notes?include=countOnly
Authorization: Bearer <token>
```

**Body:** `{ "title": "string", "body": "optional" }`  
Other users’ notes return **404** (no leakage).

---

## Platform

| Path | Auth | Notes |
|------|------|-------|
| GET `/actuator/health` | Public | Liveness/readiness style |
| GET `/actuator/info` | Public | App info |
| GET `/swagger-ui.html` | Public (local) | Disabled on prod |
| GET `/api-docs` | Public (local) | Disabled on prod |

---

## Keep this file updated

See [DOCUMENTATION-MAINTENANCE.md](DOCUMENTATION-MAINTENANCE.md).  
Mirror: `.wwg/wiki/05-architecture/api-map.md`
