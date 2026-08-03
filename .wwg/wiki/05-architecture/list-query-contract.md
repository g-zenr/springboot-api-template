# List Query Contract (Accepted Truth)

**Status:** ACCEPTED_TRUTH for agents  
**Human mirror:** `docs/LIST-QUERY.md` (keep in sync)  
**Code:** `com.template.api.common.paging` + `common.select` + `common.include`  
**Applies to:** `GET /api/v1/items`, `GET /api/v1/notes`, `GET /api/v1/users`

Agents **must** load this file when changing list endpoints, `PageQuery`, `include`/`select` behavior, or list response shapes.

---

## Shared query parameters

| Param | Default | Description |
|-------|---------|-------------|
| `page` | `0` | Zero-based page index |
| `limit` | `20` | Page size **1–100** |
| `sort` | per resource | `field`, `field,asc`, `field,desc`, `-field`, multi `a,asc;b,desc` |
| `search` | _(none)_ | Case-insensitive text search (fields vary) |
| `select` | _(none)_ | Sparse fields; dots for depth (`owner.username`) |
| `include` | _(none)_ | Optional extras — see catalog below |

**Types**

| Package | Types |
|---------|--------|
| `common.paging` | `PageQuery`, `PageResponse`, `Pageables`, `PagedReads`, `CountResponse` |
| `common.select` | `SelectPaths`, `FieldProjector`, `FieldSelection` |
| `common.include` | `IncludeKey`, `IncludeOptions` |

Controllers bind `@ParameterObject PageQuery` only — **no** include/select parsing in controllers.

---

## `include` catalog (available values)

Source of truth for tokens: `IncludeKey` enum.

| Token | Status | Effect | Response |
|-------|--------|--------|----------|
| _(omit)_ | **Default** | Load documents + run total count | Full `PageResponse` |
| `count` | **Usable** | Documents + total count | `PageResponse` + real totals |
| `countOnly` | **Usable** | Count only — **no document rows** | `{ "count": N }` (`CountResponse`) |
| `pagination` | Reserved | Recognized; not implemented | No extra payload |
| `summary` | Reserved | Recognized; not implemented | Alone → list without total count (`totalElements`/`totalPages` = `-1`) |
| `statistics` | Reserved | Recognized; not implemented | Same count-skip as other reserved-only |
| `relations` | Reserved | Recognized; not implemented | No extra payload |
| `permissions` | Reserved | Recognized; not implemented | No extra payload |
| `analytics` | Reserved | Recognized; not implemented | No extra payload |

**Usable today for real behavior:** `count`, `countOnly`, or omit `include`.

### Parser rules (non-negotiable)

| Rule | Behavior |
|------|----------|
| Format | Comma-separated |
| Case | Insensitive |
| Whitespace | Trimmed |
| Duplicates | Removed |
| Unknown tokens | **Ignored** (never 400) |
| `countOnly` present | Body is count-only even if other tokens present |

### Count policy

| Client `include` | Content | Count query | Shape |
|------------------|---------|-------------|--------|
| Omitted | Yes | Yes | `PageResponse` |
| `count` | Yes | Yes | `PageResponse` |
| `countOnly` | **No** | Yes | `{ "count": N }` |
| Reserved only | Yes | **No** | `PageResponse`, totals = `-1` |

`countOnly` still applies `search` and note ownership. Ignores `page`/`limit`/`sort`/`select` for the body.

---

## Default `PageResponse` envelope

```
content, page, limit, totalElements, totalPages,
first, last, hasNext, hasPrevious,
sort, search, select, include
```

When count not computed: `totalElements` / `totalPages` = `-1`.

---

## `select` (sparse / deep)

- Omit → full DTO  
- Comma-separated paths; dots for depth (`owner.username`)  
- Invalid **root** → **400** (allow-listed per resource)  
- Notes: `owner` / `owner.*` **populates** nested user  
- With `countOnly`: no rows to project (count wins)

---

## Per-resource allow-lists

### Items

| | Fields |
|--|--------|
| Search | `name`, `description` |
| Sort | `name`, `description`, `createdAt`, `updatedAt` (default `name,asc`) |
| Select | `id`, `name`, `description`, `createdAt`, `updatedAt` |

### Notes (owner-scoped)

| | Fields |
|--|--------|
| Search | `title`, `body` |
| Sort | `title`, `body`, `createdAt`, `updatedAt` (default `-updatedAt`) |
| Select | `id`, `ownerUsername`, `title`, `body`, `createdAt`, `updatedAt`, **`owner`** |

### Users (ADMIN)

| | Fields |
|--|--------|
| Search | `username` |
| Sort | `username`, `enabled`, `createdAt`, `updatedAt` (default `username,asc`) |
| Select | `id`, `username`, `roles`, `enabled`, `createdAt` |

---

## Implementation pattern (agents)

```java
if (query.includes().wantsCountOnly()) {
  return pagedReads.countOnly(Entity.class, spec);
}
Pageable pageable = Pageables.toPageable(query, SORT_FIELDS, DEFAULT_SORT);
PageResponse<Dto> page = pagedReads.find(Entity.class, spec, pageable, query, mapper::toResponse);
return FieldSelection.apply(page, query, SELECT_ROOTS);
```

New include token:

1. Add `IncludeKey` constant  
2. Service: `query.includes().has(IncludeKey.X)`  
3. Update **this WWG file** + `docs/LIST-QUERY.md` + tests  
4. Do not put parsing in controllers  

---

## Tests agents must keep green

| Class | Coverage |
|-------|----------|
| `IncludeOptionsTest` | Full catalog parse, reserved skip count, countOnly flags |
| `FieldProjectorTest` | select sparse/deep |
| `PageablesTest` | page/limit/sort |
| `ItemControllerTest` | list query + all include modes + reserved HTTP |
| `NoteControllerTest` | countOnly mine, count, select+owner |
| `UserControllerTest` | countOnly, count, select |

---

## Related surfaces

| Surface | Role |
|---------|------|
| `05-architecture/api-map.md` | Endpoint table + pointer here |
| `docs/LIST-QUERY.md` | Human-readable mirror |
| `docs/API-REFERENCE.md` | Public HTTP catalog |
| `IncludeKey.java` | Runtime token enum |
