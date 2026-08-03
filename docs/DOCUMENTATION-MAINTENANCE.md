# Documentation Maintenance Policy (Mandatory)

This policy is **accepted project truth**. Handoffs that skip required doc/diagram updates are incomplete.

## Living surfaces

| Surface | Role | Update when |
|---------|------|-------------|
| `README.md` | Front door quick start | Auth paths, major features, profiles |
| `docs/**` | Human manuals + diagrams | Any user-visible or structural change |
| `docs/WHAT-IS-THIS.md` | Project purpose | Scope/identity of the template changes |
| `docs/FOLDER-STRUCTURE.md` | Folder map | New packages, top-level dirs, responsibility moves |
| `docs/diagrams/*.md` | Architecture diagrams | Request flow, data model, auth, packages |
| `.wwg/wiki/**` | Canonical agent truth | Behavior, contracts, ops, ADRs |
| `CHANGELOG.md` | Release memory | Each shipped capability set |
| OpenAPI / Swagger (springdoc) | Runtime + UI contract | **Every** endpoint/DTO/status/auth change — see [OPENAPI-SWAGGER.md](OPENAPI-SWAGGER.md) |

## Change → required updates

| If you change… | You must update… |
|----------------|------------------|
| Endpoint path/method/status | **Swagger annotations** (`@Operation`, `@ApiResponse`, `@Tag`), `OpenApiContractTest` paths, `docs/API-REFERENCE.md`, `docs/diagrams/api-surface.md`, `.wwg/wiki/05-architecture/api-map.md`, [OPENAPI-SWAGGER.md](OPENAPI-SWAGGER.md) checklist |
| List query (`page`/`limit`/`sort`/`search`/`select`/`include`) | **`.wwg/wiki/05-architecture/list-query-contract.md`** (agent truth) + `docs/LIST-QUERY.md` (human mirror) + api-map + API-REFERENCE + tests |
| Auth / roles / tokens | `docs/USER-GUIDE.md`, `docs/OPERATIONS.md`, `docs/diagrams/auth-flow.md`, security-model WWG |
| Entity / Flyway migration | `docs/diagrams/data-model.md`, `docs/ARCHITECTURE.md`, `.wwg/wiki/05-architecture/data-model.md` |
| New `feature.*` package | Full OpenAPI tags/ops/responses, `OpenApiContractTest`, `docs/FOLDER-STRUCTURE.md`, `docs/DEVELOPER-GUIDE.md`, `docs/diagrams/package-structure.md`, `docs/ARCHITECTURE.md`, api-map, tests ≥10 |
| Top-level or package tree change | `docs/FOLDER-STRUCTURE.md` + package-structure diagram |
| Request/response DTO fields | `@Schema` on DTO, API-REFERENCE, OpenAPI examples if any |
| Profile / env / Docker | `docs/OPERATIONS.md`, `.env.example`, `README.md`, local-run WWG |
| Error contract / rate limit | `docs/API-REFERENCE.md`, `docs/diagrams/request-pipeline.md`, error-contract WWG |
| Logging / correlation | `docs/OPERATIONS.md`, `docs/diagrams/request-pipeline.md` |
| Agent rules / testing policy | `AGENTS.md`, testing-policy WWG, `docs/DEVELOPER-GUIDE.md` |

## Diagrams are source-controlled living assets

Files under `docs/diagrams/` use **Mermaid**. Rules:

1. Never leave a diagram that contradicts current code.
2. Prefer editing Mermaid in place over adding stale PNGs.
3. On architecture change, update the diagram **in the same task** as the code.
4. Link diagrams from `ARCHITECTURE.md` and this index.

## Agent checklist (close-out)

Before handoff of implementation work (**apply/repair**):

### WWG first (mandatory — fail handoff if skipped)

- [ ] **`.wwg/workspace/current-task.md` updated**  
- [ ] **Matching `.wwg/wiki/**` truth paths written** per matrix (api-map, list-query-contract, data-model, security-model, …)  
- [ ] Handoff **lists every `.wwg/**` file changed**  
- [ ] WWG Truth-Write Mode was `UPDATE_ACCEPTED_TRUTH_AUTHORIZED` for product truth changes  

### Human docs + contract

- [ ] `docs/` surfaces for the change type updated  
- [ ] Affected `docs/diagrams/*` updated  
- [ ] **Swagger/OpenAPI annotations updated** (see [OPENAPI-SWAGGER.md](OPENAPI-SWAGGER.md))  
- [ ] `OpenApiContractTest` covers new/changed paths  
- [ ] `CHANGELOG.md` entry if user-visible  
- [ ] `README.md` if quick-start paths changed  
- [ ] Tests pass (includes OpenAPI contract test)  

**Rule:** updating only `docs/` without WWG = **incomplete** (see `AGENTS.md` → Mandatory WWG Truth Sync).

## Skills

- `.wwg/wiki/10-skills/skill-specs/documentation-sync.skill.md`
- `.wwg/wiki/10-skills/skill-specs/openapi-swagger-sync.skill.md`
