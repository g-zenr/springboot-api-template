# Drift Guard

Flag drift when:

- Entities diverge from Flyway migrations
- Controllers bypass services for business rules
- `ddl-auto` is changed away from `validate` without an ADR
- Prisma or non-JVM ORMs are introduced without replacing this template's decision
- Product domain is implemented without updating `project-truth.md` and `06-domain/*`
- Public API changes without tests and `api-map.md` updates
- List query / `include` / `select` / paging changed without updating `list-query-contract.md`
- Claiming production readiness while governance gate **G-PROD-DEPLOY** is not PASS
- Enabling public deploy with default H2 console / open Swagger without an accepted security model change
- Skipping Meta-Prompt v2 Task Contract on meaningful implementation work
- Shipping a new feature with fewer than **10** `@Test` methods
- Shipping a new function/endpoint with fewer than **5** dedicated tests
- Changing behavior without updating tests that encoded the old contract
- Changing endpoints/schema/auth/ops without updating `docs/**` and `docs/diagrams/**`
- **Changing product behavior / API / schema / ops without updating the matching `.wwg/wiki/**` surfaces in the same task**
- **Human docs updated while WWG still describes old behavior** (docs-only “sync” is drift)
- Leaving Mermaid diagrams that contradict running code
- Adding/changing REST endpoints without `@Operation` / `@ApiResponse` / `@Tag` updates
- Shipping API changes that break or omit paths in `OpenApiContractTest` / `/api-docs`
- Public auth routes shown in Swagger as requiring Bearer when they are public
- Apply/repair handoff with empty “WWG files changed” while code changed accepted truth
- **AI slop:** drive-by refactors, parallel abstractions next to existing `common.*`, restating comments, unused “framework” layers, dependency adds without need, style that ignores sibling `feature.*` code
- Shipping code that passes tests but fails the **anti-ai-slop** self-check (`.wwg/wiki/principles/anti-ai-slop.md`)

## Force rule

On `PLAN_AND_APPLY` / `REPAIR_AND_VALIDATE`: if code or public contract changed, **at least one** relevant `.wwg/wiki/**` path **and** `workspace/current-task.md` must be written in the same task. Otherwise treat as **incomplete / drift**.

On every code edit: match house style; **no slop**. Fail handoff on slop even if tests are green.
