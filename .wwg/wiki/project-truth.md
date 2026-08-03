# Project Truth — Spring Boot API Template

## Identity

- **Name:** Spring Boot API Template (`springboot-api-template`)
- **Purpose:** Reusable backend starter for new Spring Boot HTTP APIs
- **Not:** A Mediatrix LMS, UNLAD product, or Node/Express port

## Accepted scope (v0)

| In scope | Out of scope (until requested) |
|----------|--------------------------------|
| Health / Actuator | Multi-tenant product IAM / external IdP |
| JWT **stub** (demo in-memory users) | Production-grade user directory |
| Global RFC 7807-style errors (`ProblemDetail`) | Full business domains |
| Spring Data JPA + Flyway schema ownership | Prisma / Node ORMs |
| OpenAPI (springdoc) | GraphQL |
| Sample `Item` CRUD feature | LMS/TNA/certificates/etc. |
| Correlation IDs (`X-Correlation-Id`) | Full distributed tracing backend |
| H2 default (local/test); Postgres profile | Production cloud deploy as product |

## Runtime defaults

- **Java:** 21
- **Spring Boot:** 4.x
- **Default profile:** `local` (H2 mem, H2 console on, OpenAPI on)
- **Postgres profile:** `postgres` (`DB_URL` / `DB_USER` / `DB_PASSWORD`)
- **Prod profile:** `prod` (Postgres required via `DB_*`; OpenAPI off; H2 console off)
- **Schema:** Flyway migrations under `src/main/resources/db/migration`
- **JPA:** `ddl-auto=validate`
- **API prefix for sample:** `/api/v1/items`
- **Docs (local):** `/swagger-ui.html`, `/api-docs`
- **Health:** `/actuator/health`
- **CORS:** `app.cors.*` (wildcard local; env-driven in prod)
- **JWT:** access + refresh (`JWT_SECRET`, expiration + refresh expiration)
- **Users:** persisted `app_users` + `DemoUserSeeder` (`admin/admin123`, `user/user123`)
- **MapStruct:** `ItemMapper` for DTOs
- **Rate limit:** `app.rate-limit.*` (login + API)
- **Logging:** text local; **ECS JSON on prod**
- **Correlation:** `X-Correlation-Id` + MDC
- **Ops:** Docker, Compose, CI, Spotless, `.env.example`

## Layering truth

Controller → Service → Repository → Entity, grouped by `feature.<name>`.  
Cross-cutting code lives in `common.*` and `config.*`.

## Persistence truth

- Flyway is the source of schema truth.
- Entities must match migrations.
- Prefer UUID primary keys for new tables in this template.

## Agent / meta-prompt truth

- Every meaningful agent prompt must run under **Agent Meta-Prompt Template v2.0**.
- The template is **embedded in full** in root `AGENTS.md` (sections 0–17).
- Standalone copy: `agent-meta-prompt-template-v2.md` (must stay in sync with `AGENTS.md`).
- Per prompt: fill **Section 0 Task Contract** (mode, scope, authorization, WWG truth-write, validation) before planning or editing.
- Default delivery authorization is **no git writes** unless the user explicitly authorizes commit/push/PR/release.

## Testing policy (accepted)

| Change | Minimum automated tests |
|--------|-------------------------|
| New feature | **≥ 10** `@Test` methods |
| New function / endpoint | **≥ 5** `@Test` methods |
| Update | Update existing tests; keep minima |

Canonical: `.wwg/wiki/08-operations/testing-policy.md`  
Skill: `.wwg/wiki/10-skills/skill-specs/test-coverage-minimums.skill.md`  
Sample `feature.item` must demonstrate ≥ 10 cases.

## Readiness status (accepted as of 2026-08-01 complete-template pass)

| Gate | Status |
|------|--------|
| Local template use | **READY** |
| Hardened defaults + CI (**G-HARDENED**) | **PASS** |
| **Complete template** (**G-COMPLETE-TEMPLATE**) | **PASS / READY** |
| Production product API (**G-PROD-DEPLOY**) | **NOT_READY** (no auth — specialize first) |

Canonical evidence: `.wwg/reports/complete-template-audit-2026-08-01.md`  
Prior audit: `.wwg/reports/readiness-standards-audit-2026-08-01.md`  
Operational checklist: `.wwg/wiki/08-operations/readiness-checklist.md`  
Governance gates: `.wwg/governance/project-readiness-checklist.md`

Agents **may** call this a complete reusable Spring Boot API template.  
Agents **must not** call it a production-secured product without security specialization.

## Evolution rule

When this template becomes a real product, replace sample domain docs and grow `06-domain/*` and `project-truth.md` — do not import unrelated product WWG by default.
