# WWG Resolver — Spring Boot API Template

Entry point for full `.wwg` utilization in `springboot-api-template`.

## Read first

1. `wiki/index.md`
2. `wiki/12-maintenance/context-maintenance-matrix.md`
3. `wiki/10-skills/skill-index.md`
4. `workspace/current-task.md`
5. `governance/drift-guard.md`

## Layer inventory

### Wiki

Truth, terminology, project brief, requirements, ADRs, architecture (system, API map, **list-query contract**, data, **security**, **error contract**, **deployment**), domain (template has no product domain), operations (**readiness checklist**, local run, logging), agent context, skills, synthesis (**current-state**, roadmap, open questions), maintenance, principles.

### List / paging / include / select (high traffic)

1. `wiki/05-architecture/list-query-contract.md` — full catalog and rules  
2. `wiki/05-architecture/api-map.md` — endpoints + summary  
3. Human mirror: `docs/LIST-QUERY.md`

### Latest readiness evidence

`.wwg/reports/readiness-standards-audit-2026-08-01.md`

### Workspace

`current-task.md`, testing notes when present.

### Governance

`drift-guard.md`, `quality-gates.md`, `truth-capture.md`

### Reports

Handoffs and validation evidence as generated.

### Config

`config/wwg.project.yaml`

## Routing rule

Use `wiki/12-maintenance/context-maintenance-matrix.md` and close out with a short utilization note when behavior or architecture changes.

## Code quality (anti-slop)

Before coding: read `wiki/principles/anti-ai-slop.md`.  
Match existing `feature.*` / `common.*` style. No drive-by refactors, comment noise, or parallel abstractions.  
Handoff fails quality gates if slop is present even when tests pass.

## Force WWG update (apply / repair)

On every `PLAN_AND_APPLY` or `REPAIR_AND_VALIDATE` that changes product behavior:

1. Set WWG Truth-Write Mode to **`UPDATE_ACCEPTED_TRUTH_AUTHORIZED`** for touched surfaces.
2. Write matching `wiki/**` paths **in the same task** (never “docs only”).
3. Always write `workspace/current-task.md`.
4. List every `.wwg/**` path in the handoff.

Policy: root `AGENTS.md` → **Mandatory WWG Truth Sync**, `governance/drift-guard.md`, `governance/quality-gates.md`, `governance/truth-capture.md`.
