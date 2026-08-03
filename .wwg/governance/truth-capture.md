# Truth Capture

## Force capture (every apply / repair)

When implementation changes accepted behavior, agents **must write WWG in the same task**. Do not wait for a separate “documentation phase.”

Capture into wiki when any of these become accepted (or change):

- New baseline dependency or framework choice
- New or changed public URL / method / status / body
- List query params (`page`, `limit`, `sort`, `search`, `select`, `include`) or list response shapes
- Persistence or migration policy change
- Error contract change
- Auth, multi-tenancy, messaging, or security model change
- Ops / profile / deploy defaults
- Terminology for new cross-cutting concepts

## Always-on paths

| Event | Write |
|-------|--------|
| Any apply task | `workspace/current-task.md` |
| API / endpoint | `05-architecture/api-map.md` |
| List query / include / select | `05-architecture/list-query-contract.md` |
| Schema | `05-architecture/data-model.md` |
| Security | `05-architecture/security-model.md` |
| Errors | `05-architecture/error-contract.md` |
| New capability shipped | `11-synthesis/current-state.md` |
| New term | `terminology.md` |

## Human docs

Mirror user-facing detail under `docs/**` (e.g. `docs/LIST-QUERY.md`) **after or with** WWG — never instead of WWG.

## Done when

- Matching `.wwg/wiki/**` files reflect current code  
- Handoff lists those paths  
- Drift-guard and quality-gates pass  
