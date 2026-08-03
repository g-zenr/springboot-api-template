# Changelog

## Unreleased

### Added

- Reusable list/pagination package `common.paging` (`PageQuery`, `PageResponse`, `Pageables`, `Specs`)
- List GET for **items**, **notes**, and **users** supports `page`, `limit`, `sort`, `search` and returns a paged envelope
- Reusable sparse/deep field selection `common.select` (`select` query param, dot paths, allow-lists)
- Notes can **populate** nested `owner` when select includes `owner` / `owner.*` (e.g. `owner.username`)
- Reusable `include` flags (`common.include`): parse once, ignore unknown; **`count`** (totals with content), **`countOnly`** (`{ "count": N }` only — no rows)
- `PagedReads` list helper: default/omit `include` keeps full totals; explicit includes without `count`/`countOnly` skip `COUNT(*)`
- Dedicated list-query doc [docs/LIST-QUERY.md](docs/LIST-QUERY.md) + expanded tests (items/notes/users for include/select/countOnly)
- WWG accepted truth for list query: `.wwg/wiki/05-architecture/list-query-contract.md` (agents load via api-map / RESOLVER / AGENTS.md)
- **Mandatory WWG Truth Sync:** apply/repair tasks must update matching `.wwg/wiki/**` + `workspace/current-task.md` in the same task (enforced in AGENTS.md, drift-guard, quality-gates)
- **Anti AI-slop** principle: `.wwg/wiki/principles/anti-ai-slop.md` + AGENTS.md gate — house style, no noise, no drive-by refactors

### Changed

- List endpoints no longer return a bare JSON array; clients must read `content` (and page metadata)
- When `select` is set, each content row is a sparse map (only requested paths)

## 0.4.0 — 2026-08-01

### Added

- User APIs: register, me, change password, admin list/create
- Optional demo seed (`app.security.seed-demo-users`)
- Optional registration (`app.security.allow-registration`)
- JWT secret fail-fast validator (no default secret outside local)
- **Notes** owner-scoped domain (`feature.note`, Flyway V3)
- Method security (`@PreAuthorize` ADMIN)
- Tests for users, notes, secret validator
- **Complete human docs** under `docs/` + **living Mermaid diagrams**
- Mandatory **documentation sync** policy (AGENTS + WWG + skill)
- Mandatory **OpenAPI/Swagger sync** (`docs/OPENAPI-SWAGGER.md`, full `@ApiResponse`/`@Schema`, `OpenApiContractTest`)

### Changed

- Version `0.4.0-SNAPSHOT`
- Prod defaults: no demo seed, no open registration

## 0.3.0 — MapStruct, refresh tokens, ECS logs, rate limit
## 0.2.0 — JWT, correlation, Testcontainers, Spotless
## 0.1.0 — Profiles, Docker, CI
## 0.0.1 — Bootstrap + WWG
