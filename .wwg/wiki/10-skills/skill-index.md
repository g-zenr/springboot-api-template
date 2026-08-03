# Skill Index

## Repo workflow skills

| Skill | Spec | Purpose | When To Use |
|-------|------|---------|-------------|
| Controller–Service–Repository Sync | `skill-specs/controller-service-repository-sync.skill.md` | Keep HTTP, use-case, and persistence aligned | Any endpoint or entity change |
| API Contract Safety | `skill-specs/api-contract-safety.skill.md` | Preserve paths, status codes, ProblemDetail, OpenAPI | Public API changes |
| Migration Safety | `skill-specs/migration-safety.skill.md` | Flyway-first schema changes matching JPA entities | DB schema work |
| Test Coverage Minimums | `skill-specs/test-coverage-minimums.skill.md` | Enforce ≥10 tests/feature, ≥5/function, update-on-change | Any feature/function add or behavior update |
| Documentation Sync | `skill-specs/documentation-sync.skill.md` | Keep docs/ + diagrams/ + WWG aligned with code | Any contract, schema, auth, ops, or feature change |
| OpenAPI / Swagger Sync | `skill-specs/openapi-swagger-sync.skill.md` | Keep springdoc annotations + Swagger UI + `/api-docs` accurate | Any endpoint, DTO, status, or auth change |

## Bundled / generic skills still relevant

- `wiki-ingest` / wiki updates on accepted truth changes
- `wiki-lint` when cleaning WWG consistency
- `context-refresh` after architecture shifts
- `task-ticket-writer` for multi-step work
- Runtime: `/implement`, `/review`, `/design`, `/check-work` as appropriate
- Meta-prompt: mandatory Task Contract from `AGENTS.md` (v2)

## Principles (always on)

| Principle | Path | When |
|-----------|------|------|
| Anti AI-slop | `../principles/anti-ai-slop.md` | **Every** code edit — consistency, no noise, no drive-by refactors |

## Maintenance

Add a skill row here when a workflow becomes recurring and high-risk.
