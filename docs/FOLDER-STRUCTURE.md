# Folder structure explained

This document is the **canonical map of the repository**.  
Update it whenever you add/remove top-level folders or Java packages  
(see [DOCUMENTATION-MAINTENANCE.md](DOCUMENTATION-MAINTENANCE.md)).

---

## What the repo is for (context)

This tree implements a **Spring Boot API template**, not a single business product.  
Folders separate: **runtime code**, **tests**, **human docs**, **agent governance (WWG)**, and **ops**.

---

## Top-level tree

```text
springboot-api-template/
├── src/                          # Application + test source
├── docs/                         # Human documentation (you are here)
├── .wwg/                         # Agent/wiki governance (WWG)
├── .grok/skills/                 # Grok skill bridges
├── .github/workflows/            # CI (Spotless + tests)
├── pom.xml                       # Maven build + dependencies
├── mvnw / mvnw.cmd               # Maven Wrapper (no global Maven needed)
├── docker-compose.yml            # Local Postgres
├── Dockerfile                    # Multi-stage JAR image
├── .env.example                  # Env var names (never commit real secrets)
├── README.md                     # Front door
├── AGENTS.md                     # Agent operating contract (+ Meta-Prompt v2)
├── CONTRIBUTING.md               # How to contribute
├── CHANGELOG.md                  # Version history
├── LICENSE                       # MIT
└── agent-meta-prompt-template-v2.md  # Standalone copy of meta-prompt
```

### Top-level: what each part is for

| Path | Purpose | When you touch it |
|------|---------|-------------------|
| `src/` | All runtime and test Java/resources | Implementing features |
| `docs/` | How-to guides, API reference, diagrams | Explaining behavior to humans |
| `.wwg/` | Project truth, architecture, skills for AI agents | Keeping agent “memory” accurate |
| `.grok/skills/` | Grok entry skills (agents-bridge, skill-router) | Agent routing only |
| `.github/workflows/` | CI pipeline | Build/test automation |
| `pom.xml` | Dependencies, plugins (MapStruct, Spotless, Boot) | Adding libraries |
| `docker-compose.yml` | Postgres for local/dev | DB without installing Postgres globally |
| `Dockerfile` | Container image for the API | Deploy packaging |
| `AGENTS.md` | Rules agents must follow (tests, docs, Swagger) | Changing team/agent process |
| `README.md` | First 60-second orientation | Quick start changes |

---

## `src/` — application code

```text
src/
├── main/
│   ├── java/com/template/api/
│   │   ├── ApiTemplateApplication.java    # Spring Boot entrypoint
│   │   ├── feature/                       # Business vertical slices
│   │   │   ├── auth/                      # Login, refresh, logout, register
│   │   │   ├── user/                      # Users, demo seeder, refresh tokens entity
│   │   │   ├── item/                      # Sample shared catalog CRUD
│   │   │   └── note/                      # Sample owner-scoped notes CRUD
│   │   ├── config/                        # Framework wiring
│   │   │   ├── security/                  # JWT, SecurityConfig, validators
│   │   │   ├── CorsConfig.java
│   │   │   └── OpenApiConfig.java         # Swagger global metadata
│   │   └── common/                        # Cross-cutting (not a domain)
│   │       ├── error/                     # ProblemDetail handlers, exceptions
│   │       ├── include/                   # Optional include flags (count, …)
│   │       ├── paging/                    # PageQuery / PageResponse / Pageables / PagedReads
│   │       ├── select/                    # Sparse + deep select (FieldProjector)
│   │       └── web/                       # Correlation ID, rate limiting
│   └── resources/
│       ├── application.yml                # Profiles: local / postgres / prod
│       ├── logback-spring.xml             # Text logs vs ECS JSON (prod)
│       └── db/migration/                  # Flyway SQL (source of schema truth)
│           ├── V1__create_items.sql
│           ├── V2__create_users_and_refresh_tokens.sql
│           └── V3__create_notes.sql
└── test/
    └── java/com/template/api/
        ├── feature/...                    # Feature tests (mirror main packages)
        ├── config/...                     # Unit tests (JWT, CORS, secrets)
        ├── common/...                     # Filter/rate-limit tests
        ├── platform/                      # Actuator + OpenApiContractTest
        └── support/                       # Shared test helpers (e.g. AuthTestSupport)
```

### Java packages: what each is for

| Package | Responsibility | Example |
|---------|----------------|---------|
| `feature.auth` | HTTP auth endpoints only (thin) | `AuthController`, `AuthService` |
| `feature.user` | User entity, seeding, user APIs, refresh-token storage | `AppUser`, `UserController` |
| `feature.item` | **Sample** shared resource (any logged-in user) | Items CRUD |
| `feature.note` | **Sample** domain rule: only owner sees their notes | Notes CRUD |
| `config.security` | Security filter chain, JWT issue/validate, DB user details | `SecurityConfig`, `JwtService` |
| `config` | Non-security Spring beans | CORS, OpenAPI |
| `common.error` | Shared exceptions + global error JSON | `ApiExceptionHandler` |
| `common.web` | Request-wide filters | Correlation ID, rate limit |

### Feature package pattern (how to add yours)

Every new capability should look like this:

```text
feature.<name>/
  <Name>Controller.java     # HTTP only
  <Name>Service.java        # Business rules + @Transactional
  <Name>Repository.java     # Spring Data
  <Name>.java               # JPA entity (if persisted)
  <Name>Request.java        # Inbound DTO (+ @Schema for Swagger)
  <Name>Response.java       # Outbound DTO
  <Name>Mapper.java         # Optional MapStruct
```

**Do not** invent parallel trees like `controllers/`, `services/` at the root of the app — keep **feature-first** vertical slices.

Diagram: [diagrams/package-structure.md](diagrams/package-structure.md)

---

## `docs/` — human documentation

```text
docs/
├── README.md                      # Index of all docs
├── WHAT-IS-THIS.md                # Purpose (this project's "why")
├── FOLDER-STRUCTURE.md            # This file
├── USER-GUIDE.md                  # Run & call the API
├── API-REFERENCE.md               # Endpoint catalog
├── ARCHITECTURE.md                # Design overview
├── DEVELOPER-GUIDE.md             # How to extend
├── OPERATIONS.md                  # Profiles, Docker, secrets, CI
├── OPENAPI-SWAGGER.md             # Swagger must stay updated
├── DOCUMENTATION-MAINTENANCE.md   # What to update when code changes
└── diagrams/                      # Living Mermaid diagrams
    ├── request-pipeline.md
    ├── auth-flow.md
    ├── data-model.md
    ├── api-surface.md
    ├── package-structure.md
    └── deploy-profiles.md
```

| Doc | Audience |
|-----|----------|
| WHAT-IS-THIS | Anyone new to the repo |
| FOLDER-STRUCTURE | Developers orienting in the tree |
| USER-GUIDE | Operators / API consumers getting started |
| DEVELOPER-GUIDE | People adding features |
| OPERATIONS | Deploy / env / CI |
| diagrams/* | Visual architecture (must track code) |

---

## `.wwg/` — agent / project governance

```text
.wwg/
├── RESOLVER.md                 # What to read first
├── config/wwg.project.yaml     # Project registry
├── wiki/                       # Canonical product/template truth
│   ├── project-truth.md
│   ├── 05-architecture/        # api-map, data-model, security, ...
│   ├── 08-operations/          # testing-policy, local-run, logging
│   ├── 10-skills/              # Skill index + specs
│   └── 12-maintenance/         # What to update when X changes
├── governance/                 # Quality gates, drift-guard
├── workspace/                  # current-task.md
└── reports/                    # Audits and handoffs
```

| Audience | Use |
|----------|-----|
| AI agents | Required reading order via `AGENTS.md` |
| Humans | Optional deep truth; prefer `docs/` for day-to-day |

**Rule:** Code changes that change accepted behavior must update both `docs/` (humans) and `.wwg/` (agents) when applicable.

---

## Resources vs code (important)

| Concern | Lives in | Not in |
|---------|----------|--------|
| Table schema | `src/main/resources/db/migration/*.sql` | JPA `ddl-auto=update` (forbidden) |
| Runtime config | `application.yml` profiles | Hardcoded secrets in Java |
| API docs UI | Generated from annotations → Swagger | Hand-written only (must match code) |
| Human how-to | `docs/` | Random comments only |

---

## Mental model: request path through folders

```text
HTTP request
  → common/web (correlation, rate limit)
  → config/security (JWT)
  → feature.<x> Controller
  → feature.<x> Service
  → feature.<x> Repository
  → DB (schema from db/migration)
  → DTO / MapStruct
  → JSON response
```

See also: [diagrams/request-pipeline.md](diagrams/request-pipeline.md)

---

## How to use the foldering day to day

| I want to… | Go to… |
|------------|--------|
| Understand purpose | [WHAT-IS-THIS.md](WHAT-IS-THIS.md) |
| Run the API | [USER-GUIDE.md](USER-GUIDE.md) |
| Add a business feature | `src/.../feature/<new>/` + [DEVELOPER-GUIDE.md](DEVELOPER-GUIDE.md) |
| Change tables | New file under `db/migration/` |
| Change security | `config/security/` + security docs |
| Fix CORS / OpenAPI global | `config/CorsConfig`, `OpenApiConfig` |
| Change env/profiles | `application.yml`, `.env.example`, [OPERATIONS.md](OPERATIONS.md) |
| Update Swagger | Annotations on controllers/DTOs + [OPENAPI-SWAGGER.md](OPENAPI-SWAGGER.md) |
| Teach an agent the rules | `AGENTS.md` + `.wwg/` |

---

## Keep this document updated

When you:

- Add a `feature.*` package  
- Add a top-level folder  
- Move auth/user/security responsibilities  

…update **this file** and `docs/diagrams/package-structure.md` in the same task.
