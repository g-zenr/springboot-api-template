# List query reference (page, search, sort, select, include)

**Agent / WWG accepted truth:** `.wwg/wiki/05-architecture/list-query-contract.md`  
This file is the **human-facing mirror**. When list query behavior changes, update **both** (agents load WWG first).

Shared contract for all list GET endpoints in this template:

- `GET /api/v1/items`
- `GET /api/v1/notes` (owner-scoped)
- `GET /api/v1/users` (admin)

Implementation lives under:

| Package | Types |
|---------|--------|
| `common.paging` | `PageQuery`, `PageResponse`, `Pageables`, `PagedReads`, `CountResponse` |
| `common.select` | `SelectPaths`, `FieldProjector`, `FieldSelection` |
| `common.include` | `IncludeKey`, `IncludeOptions` |

Controllers only accept `@ParameterObject PageQuery` — **no** parsing logic in controllers.

---

## Query parameters

| Param | Default | Description |
|-------|---------|-------------|
| `page` | `0` | Zero-based page index |
| `limit` | `20` | Page size **1–100** |
| `sort` | per resource | `field`, `field,asc`, `field,desc`, `-field`, multi `a,asc;b,desc` |
| `search` | _(none)_ | Case-insensitive text search (fields vary) |
| `select` | _(none)_ | Sparse fields; dots for depth (`owner.username`) |
| `include` | _(none)_ | Optional extras (`count`, `countOnly`, …) |

---

## Default list response (`PageResponse`)

When `include` is **omitted** (or does not request count-only):

```json
{
  "content": [ { "...": "..." } ],
  "page": 0,
  "limit": 20,
  "totalElements": 42,
  "totalPages": 3,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false,
  "sort": null,
  "search": null,
  "select": null,
  "include": null
}
```

---

## `include` options

### Available values (catalog)

Use any of these as comma-separated tokens on `?include=...`.  
Source of truth in code: `com.template.api.common.include.IncludeKey`.

| Token | Status | What it does | Response |
|-------|--------|--------------|----------|
| _(omit `include`)_ | **Default** | Load documents + run total count | Full `PageResponse` (same as before) |
| `count` | **Usable** | Load documents + run total count (explicit) | `PageResponse` with real `totalElements` / `totalPages` |
| `countOnly` | **Usable** | Run total count only — **no document rows** | `{ "count": N }` only |
| `pagination` | Reserved (no-op) | Accepted by parser; not implemented yet | Same as list without this flag’s effect |
| `summary` | Reserved (no-op) | Accepted by parser; not implemented yet | If used **alone** (no `count`/`countOnly`): list **without** total count (`totalElements`/`totalPages` = `-1`) |
| `statistics` | Reserved (no-op) | Accepted by parser; not implemented yet | Same as `summary` regarding count skip when no `count`/`countOnly` |
| `relations` | Reserved (no-op) | Accepted by parser; not implemented yet | Same as above |
| `permissions` | Reserved (no-op) | Accepted by parser; not implemented yet | Same as above |
| `analytics` | Reserved (no-op) | Accepted by parser; not implemented yet | Same as above |

**Usable today for real behavior:** `count`, `countOnly` (or omit `include` for the default list).

**Reserved** means the token is recognized (not treated as “unknown”), but no feature payload is added yet. Clients can start sending them later without parser changes once services implement them.

Any **other** string (typos, custom junk) is **ignored** — not an error.

### Parser rules

| Rule | Behavior |
|------|----------|
| Format | Comma-separated, e.g. `count,summary` |
| Case | Insensitive (`COUNTONLY` = `countOnly`) |
| Whitespace | Trimmed |
| Duplicates | Removed |
| Unknown tokens | Ignored (HTTP 200, no 400) |
| Multiple tokens | Combined; `countOnly` still returns count-only body if present |

### Behavior matrix (lists)

| Client `include` | Content rows | Count query | Response shape |
|------------------|--------------|-------------|----------------|
| Omitted | Yes | Yes | Full `PageResponse` |
| `count` | Yes | Yes | `PageResponse` + totals |
| `countOnly` | **No** | Yes | `{ "count": N }` |
| Only reserved token(s), e.g. `summary` | Yes | **No** | `PageResponse`, totals = `-1` |
| `count` + reserved, e.g. `count,summary` | Yes | Yes | `PageResponse` + totals |
| Unknown only, e.g. `foo` | Yes | Yes | Full `PageResponse` (unknown dropped) |

### Examples for each usable option

**Default (omit) — documents + totals**

```http
GET /api/v1/items
GET /api/v1/notes
GET /api/v1/users
```

**`count` — documents + totals (explicit)**

```http
GET /api/v1/items?include=count
GET /api/v1/items?include=count&limit=10&search=widget
GET /api/v1/notes?include=count
GET /api/v1/users?include=count
```

**`countOnly` — number only**

```http
GET /api/v1/items?include=countOnly
GET /api/v1/items?search=widget&include=countOnly
GET /api/v1/notes?include=countOnly
GET /api/v1/users?include=countOnly
GET /api/v1/items?include=COUNTONLY
```

```json
{ "count": 42 }
```

Notes for `countOnly`:

- Still applies `search` and ownership (notes = **your** notes only).
- Ignores `page` / `limit` / `sort` / `select` for the body (no rows).
- Does **not** return `content`.

---

## `select` (sparse / deep fields)

```http
GET /api/v1/items?select=id,name
GET /api/v1/notes?select=id,title,owner.username,owner.roles
GET /api/v1/users/me?select=username,roles
```

- Omit `select` → full DTO per row.  
- Invalid root field → **400**.  
- Notes: `owner` / `owner.*` **populates** nested user.  
- Works with paging; combine with `include=count` if you need totals.

`select` + `include=countOnly`: count-only wins (no rows to select).

---

## Per-resource allow-lists

### Items

| | Fields |
|--|--------|
| Search | `name`, `description` |
| Sort | `name`, `description`, `createdAt`, `updatedAt` (default `name,asc`) |
| Select | `id`, `name`, `description`, `createdAt`, `updatedAt` |

### Notes (mine only)

| | Fields |
|--|--------|
| Search | `title`, `body` |
| Sort | `title`, `body`, `createdAt`, `updatedAt` (default `-updatedAt`) |
| Select | `id`, `ownerUsername`, `title`, `body`, `createdAt`, `updatedAt`, **`owner`** |

### Users (admin)

| | Fields |
|--|--------|
| Search | `username` |
| Sort | `username`, `enabled`, `createdAt`, `updatedAt` (default `username,asc`) |
| Select | `id`, `username`, `roles`, `enabled`, `createdAt` |

---

## Errors

| Situation | Status |
|-----------|--------|
| Bad `page` / `limit` | 400 |
| Bad `sort` field/direction | 400 |
| Bad `select` root | 400 |
| Unknown `include` token | **200** (ignored) |
| Unauthenticated list | 401 |
| Non-admin `GET /users` | 403 |

---

## Adding a new list feature

```java
if (query.includes().wantsCountOnly()) {
  return pagedReads.countOnly(MyEntity.class, spec);
}
Pageable pageable = Pageables.toPageable(query, SORT_FIELDS, DEFAULT_SORT);
PageResponse<MyDto> page = pagedReads.find(MyEntity.class, spec, pageable, query, mapper::toResponse);
return FieldSelection.apply(page, query, SELECT_ROOTS);
```

New `include` token: add `IncludeKey` → check with `query.includes().has(...)`.

---

## Tests (keep green)

| Area | Test class | Asserts |
|------|------------|---------|
| Parser / full catalog | `IncludeOptionsTest` | All `IncludeKey` tokens parse; reserved skip count; countOnly flags; unknown ignored |
| Projector / select paths | `FieldProjectorTest` | Sparse + deep paths; invalid select |
| Sort / page bounds | `PageablesTest` | page/limit/sort validation |
| Items list query | `ItemControllerTest` | page, search, sort, select, `count`, `countOnly`, reserved tokens (content, totals=-1), unknown include |
| Notes list query | `NoteControllerTest` | search, select+owner, `countOnly` (mine only), `count` |
| Users list query | `UserControllerTest` | search, select, `countOnly`, `count`, me+select |

Run:

```powershell
.\mvnw.cmd test
```

Also see: [API-REFERENCE.md](API-REFERENCE.md), [USER-GUIDE.md](USER-GUIDE.md), [DEVELOPER-GUIDE.md](DEVELOPER-GUIDE.md).
