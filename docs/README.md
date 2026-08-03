# Documentation Index

Human-facing docs for **springboot-api-template** (v0.4).  
Canonical agent truth remains under `.wwg/`; these docs must stay **in sync** on every meaningful change.

## Start here (orientation)

| Doc | Purpose |
|-----|---------|
| **[WHAT-IS-THIS.md](WHAT-IS-THIS.md)** | **What this project is for**, what it is not, how to use it |
| **[FOLDER-STRUCTURE.md](FOLDER-STRUCTURE.md)** | **Every folder explained** (src, docs, .wwg, feature packages) |
| [USER-GUIDE.md](USER-GUIDE.md) | Run the API, login, call endpoints |

## Reference

| Doc | Purpose |
|-----|---------|
| [API-REFERENCE.md](API-REFERENCE.md) | Full HTTP API catalog |
| **[LIST-QUERY.md](LIST-QUERY.md)** | **Shared list params** (human). Agent truth: `.wwg/wiki/05-architecture/list-query-contract.md` |
| [ARCHITECTURE.md](ARCHITECTURE.md) | System design + **diagrams** |
| [DEVELOPER-GUIDE.md](DEVELOPER-GUIDE.md) | Add features, tests, migrations |
| [OPERATIONS.md](OPERATIONS.md) | Profiles, secrets, Docker, CI, prod |
| [diagrams/](diagrams/) | Living Mermaid diagrams (update with code) |
| [OPENAPI-SWAGGER.md](OPENAPI-SWAGGER.md) | **Swagger/OpenAPI must stay in sync** |
| [DOCUMENTATION-MAINTENANCE.md](DOCUMENTATION-MAINTENANCE.md) | What must update when code changes |

## Readiness (honest)

| Use case | Ready? |
|----------|--------|
| Local development / learning / fork base | **YES** |
| CI + Docker Compose Postgres | **YES** |
| Production product with real customers | **Only after** you set secrets, disable demo seed, and own your domain |

## Mandatory update rule

When code or contracts change, agents and humans **must** update the matching docs, diagrams, **and Swagger** listed in:

- [DOCUMENTATION-MAINTENANCE.md](DOCUMENTATION-MAINTENANCE.md)
- [OPENAPI-SWAGGER.md](OPENAPI-SWAGGER.md) ← **Swagger / `/api-docs` annotations**
- `.wwg/wiki/12-maintenance/context-maintenance-matrix.md`
- `AGENTS.md` → Documentation Sync + OpenAPI / Swagger Sync

**Incomplete work** = code without required doc/diagram/**Swagger** updates.
