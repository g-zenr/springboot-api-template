# Context Maintenance Matrix

**Force rule:** On `PLAN_AND_APPLY` / `REPAIR_AND_VALIDATE`, every row that matches the change type is **mandatory in the same task**. Skipping WWG while updating only `docs/` is **incomplete**.

| Change type | Update (**WWG required**) |
|-------------|---------------------------|
| New/changed endpoint | **Swagger** + `OpenApiContractTest` + `docs/OPENAPI-SWAGGER.md` + `docs/API-REFERENCE.md` + `docs/diagrams/api-surface.md` + **`.wwg/wiki/05-architecture/api-map.md`** + tests |
| List query / page / sort / search / select / include | **`.wwg/wiki/05-architecture/list-query-contract.md`** + api-map + `docs/LIST-QUERY.md` + API-REFERENCE + tests (`IncludeOptionsTest`, list controllers) |
| Schema / entity | Flyway + diagrams + **`.wwg/wiki/05-architecture/data-model.md`** |
| Auth / security / roles | USER-GUIDE, OPERATIONS, auth-flow diagram + **`.wwg/wiki/05-architecture/security-model.md`** |
| Request pipeline / filters | request-pipeline diagram, OPERATIONS + error-contract WWG if needed |
| Package/layout | FOLDER-STRUCTURE, package-structure diagram, DEVELOPER-GUIDE, ADRs + **system-overview WWG** |
| Project purpose / positioning | WHAT-IS-THIS, README + **project-truth WWG** |
| New feature package | docs API + architecture + diagrams + **api-map WWG** + domain notes if any + **≥ 10 tests**; list-query-contract if list GET; skill-index if new pattern |
| New function / endpoint | **≥ 5 tests** + **api-map WWG** + API-REFERENCE |
| Behavior update | **Update tests** + human docs that stated old behavior + **matching WWG truth always** |
| Profiles / Docker / CI / env | OPERATIONS, deploy-profiles diagram, `.env.example`, README + **deployment-model / local-run WWG** as needed |
| Logging / correlation / rate limit | OPERATIONS + request-pipeline + WWG ops/logging if contract changes |
| Agent workflow / testing policy | `AGENTS.md`, testing-policy WWG, DEVELOPER-GUIDE |
| **Any of the above** | **`workspace/current-task.md`** + this matrix + `docs/DOCUMENTATION-MAINTENANCE.md` checklist |

## Every apply task — minimum WWG writes

1. `.wwg/workspace/current-task.md` (status, files, evidence)
2. At least one architecture/ops/truth file from the matching row(s)
3. `terminology.md` if a new cross-cutting term was introduced
4. `11-synthesis/current-state.md` if a capability’s state changed

Always refresh `workspace/current-task.md` when starting **and** finishing meaningful work.

**Rule:** code change without required **WWG +** doc/diagram update = incomplete.  
**Rule:** human docs updated without matching WWG = drift (see `governance/drift-guard.md`).
