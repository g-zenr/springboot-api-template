---
name: skill-router
description: Discover and apply the best WWG and runtime skills for Spring Boot API template tasks.
user-invocable: true
disable-model-invocation: false
metadata:
  short-description: "Route task to WWG + runtime skills"
---

# Skill Router — Spring Boot API Template

## Steps

1. Classify the task (feature, bug, architecture, docs, review, WWG maintenance).
2. Read `.wwg/wiki/10-skills/skill-index.md`.
3. Load matching skill specs under `.wwg/wiki/10-skills/skill-specs/`.
4. Select runtime skills as needed (`/implement`, `/review`, `/design`, `/check-work`).
5. For any feature/function add or behavior update, **always** load `test-coverage-minimums` + `testing-policy.md` (≥10 / ≥5 / update tests).
6. For any endpoint/schema/auth/ops/package change, **always** load `documentation-sync` + `docs/DOCUMENTATION-MAINTENANCE.md` (docs + diagrams + WWG).
7. For any REST/DTO/auth contract change, **always** load `openapi-swagger-sync` + `docs/OPENAPI-SWAGGER.md` (annotations + Swagger UI + OpenApiContractTest).
8. Execute domain workflow skill first, then runtime skill.
9. Report: Checked / Used / N/A — include test counts, doc files, and Swagger/OpenAPI updates when code changed.
