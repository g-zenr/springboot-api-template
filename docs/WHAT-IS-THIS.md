# What is this project?

## In one sentence

**A reusable Spring Boot API template** you clone (or copy) to start a new backend service with modern defaults already decided.

## What it is for

| Purpose | Description |
|---------|-------------|
| **Starter / scaffold** | Skip weeks of “which stack?” decisions for a typical REST API |
| **Learning reference** | See a full vertical slice: auth, users, sample domains, tests, docs |
| **Team standard** | Same foldering, testing rules, Swagger, and agent (WWG) workflow across services |
| **Fork base** | Rename packages, add your `feature.*` modules, plug real identity when ready |

## What it is **not**

| Not this | Why |
|----------|-----|
| A finished product SaaS | Demo users and sample Items/Notes are teaching examples |
| A Node/Express or Prisma app | Java 21 + Spring Boot + JPA + Flyway |
| Production-ready “as-is” for customers | You must set secrets, turn off demo seed, and own security/domain |
| Multi-tenant LMS / Mediatrix port | Intentionally domain-light |

## Who should use it

- Backend developers starting a **new Spring Boot REST API**
- Teams that want **feature-first packages** + Flyway + JWT patterns
- AI agents following `AGENTS.md` / WWG with forced tests + docs + Swagger updates

## What you get out of the box

1. **HTTP API** with versioned paths under `/api/v1/...`
2. **Auth** — login, refresh tokens, logout, optional register
3. **Users** — profile, password change, admin create/list
4. **Sample domains** — Items (shared catalog), Notes (per-user)
5. **Ops** — local H2, Postgres profile, prod profile, Docker, CI
6. **Quality** — tests (≥10 per feature / ≥5 per function), Spotless, OpenAPI/Swagger
7. **Docs** — human guides + living diagrams + agent WWG truth

## How to use it (high level)

```text
1. Clone / open this repo
2. Run tests:  .\mvnw.cmd test
3. Run app:    .\mvnw.cmd spring-boot:run
4. Login via Swagger or curl (admin/admin123 on local seed)
5. Add YOUR domain as com.template.api.feature.<yourthing>
6. Keep docs + Swagger + tests updated (enforced by AGENTS.md)
```

Step-by-step: [USER-GUIDE.md](USER-GUIDE.md)  
Folders: [FOLDER-STRUCTURE.md](FOLDER-STRUCTURE.md)  
New features: [DEVELOPER-GUIDE.md](DEVELOPER-GUIDE.md)

## Naming

| Name | Meaning |
|------|---------|
| Project / artifact | `springboot-api-template` |
| Java base package | `com.template.api` |
| When productizing | Rename package/groupId and replace sample features with real ones |

## Related

- Readiness and honesty: [README.md](../README.md) (root)
- Architecture: [ARCHITECTURE.md](ARCHITECTURE.md)
- Operations: [OPERATIONS.md](OPERATIONS.md)
