# Terminology

| Term | Meaning |
|------|---------|
| Template | This repository as a reusable starter, not a shipped business product |
| Feature package | `com.template.api.feature.<name>` containing controller/service/repo/entity for one vertical slice |
| Item | Sample domain entity demonstrating the preferred layering pattern |
| Flyway migration | Versioned SQL that owns database schema |
| ProblemDetail | RFC 7807 error body returned by the global exception handler |
| Profile `postgres` | Activates PostgreSQL datasource settings |
| WWG | Wiki / Workspace / Governance layers under `.wwg/` for agent-governed work |
| Test case (count) | One JUnit `@Test` method; used for ≥10 feature / ≥5 function minima |
| Feature (testing) | New `feature.*` package, public resource, or major capability requiring ≥ 10 tests |
| Function (testing) | New service method, controller action, or public use-case requiring ≥ 5 tests |
| List query | Shared list GET contract: `page`, `limit`, `sort`, `search`, `select`, `include` — see `05-architecture/list-query-contract.md` |
| `include` | Comma-separated optional list extras; catalog in list-query-contract (`count`, `countOnly`, reserved tokens) |
| `countOnly` | `include` token that returns `{ "count": N }` with no document rows |
| `select` | Sparse fieldset query param; dots for deep paths; allow-listed roots per resource |
| PageQuery | Reusable Spring MVC bind object for list params (`common.paging.PageQuery`) |
| PageResponse | Paged list envelope (`content` + metadata) |
| CountResponse | Count-only body when `include=countOnly` |
| AI slop | Inconsistent, noisy, or over-abstracted agent code; banned — see `principles/anti-ai-slop.md` |
