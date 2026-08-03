# Spring Boot API Template

Reusable **Spring Boot REST API starter** for Java 21 — clone it, run it, add your own `feature.*` packages.  
Not a finished business product; production use needs real secrets, your domain, and hardened config.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

| Goal | Ready? |
|------|--------|
| Local learning / fork / build features | **Yes** |
| Production product for real customers | After real secrets, no demo seed, your domain |

---

## Stack

- **Java 21**, Spring Boot 4, Spring Web MVC, Spring Security (JWT)
- **Spring Data JPA** + **Flyway** (schema source of truth; `ddl-auto=validate`)
- **springdoc OpenAPI** / Swagger UI (local)
- Sample domains: **auth**, **users**, **items**, **notes** (owner-scoped)
- Shared list API: `page`, `limit`, `sort`, `search`, `select`, `include` (`count`, `countOnly`)
- Profiles: `local` (H2) · `postgres` · `prod`
- Quality: Spotless, Testcontainers (optional), CI-ready Maven Wrapper

---

## Quick start

### Requirements

- JDK **21+**
- (Optional) Docker — for Postgres via Compose

### Run locally (H2)

```bash
git clone https://github.com/g-zenr/springboot-api-template.git
cd springboot-api-template

# Windows
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw test
./mvnw spring-boot:run
```

### Call the API

```http
POST /api/v1/auth/login
Content-Type: application/json

{ "username": "admin", "password": "admin123" }
```

```http
GET /api/v1/items?page=0&limit=10&sort=name,asc&search=widget
Authorization: Bearer <accessToken>
```

```http
GET /api/v1/items?include=countOnly
Authorization: Bearer <accessToken>
```

```json
{ "count": 42 }
```

| URL | Description |
|-----|-------------|
| http://localhost:8080/actuator/health | Health |
| http://localhost:8080/swagger-ui.html | Swagger UI (local) |
| http://localhost:8080/api-docs | OpenAPI JSON (local) |

**Demo users** (when seed is on): `admin` / `admin123`, `user` / `user123`.

### Postgres (Docker)

```powershell
# Set a real secret (required for postgres profile)
$env:JWT_SECRET = "your-own-dev-secret-at-least-32-chars!!"
.\run-postgres.ps1
```

Host DB: `jdbc:postgresql://localhost:5433/template` (mapped from container 5432).

---

## Project layout

```text
src/main/java/com/template/api/
  feature/auth|user|item|note   ← vertical slices (controller → service → repo)
  config/security               ← JWT, security config
  common/error|web|paging|select|include
src/main/resources/db/migration ← Flyway SQL (schema truth)
docs/                           ← human guides + Mermaid diagrams
.wwg/                           ← agent / wiki project truth
```

Full map: [docs/FOLDER-STRUCTURE.md](docs/FOLDER-STRUCTURE.md)

---

## Documentation

| Doc | Purpose |
|-----|---------|
| [docs/WHAT-IS-THIS.md](docs/WHAT-IS-THIS.md) | What this template is for |
| [docs/USER-GUIDE.md](docs/USER-GUIDE.md) | Run & call the API |
| [docs/API-REFERENCE.md](docs/API-REFERENCE.md) | HTTP catalog |
| [docs/LIST-QUERY.md](docs/LIST-QUERY.md) | List params: page, sort, search, select, **include** |
| [docs/DEVELOPER-GUIDE.md](docs/DEVELOPER-GUIDE.md) | Add features, tests, list patterns |
| [docs/OPERATIONS.md](docs/OPERATIONS.md) | Profiles, secrets, Docker, CI, Terraform |
| [infra/SETUP.md](infra/SETUP.md) | Easy local / cloud / switch-provider setup |
| [infra/CONTRACT.md](infra/CONTRACT.md) | Portable deploy contract (any cloud) |
| [docs/README.md](docs/README.md) | Full docs index |
| [AGENTS.md](AGENTS.md) | Agent operating contract (tests, docs, WWG, anti-slop) |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contribution basics |

**Agent truth (list query):** `.wwg/wiki/05-architecture/list-query-contract.md`

---

## Built-in capabilities

- **Auth:** login, refresh (rotation), logout, register (configurable)
- **Users:** me, password change, admin list/create
- **Samples:** Items (shared catalog), Notes (owner-scoped)
- **List query:** pagination, sort, search, sparse `select`, `include=count` / `countOnly`
- **Ops:** `local` \| `postgres` \| `prod`, Docker Compose, Dockerfile, Terraform (`infra/`), Spotless
- **Authz:** **RBAC or ABAC** via `AUTHZ_MODE` / `app.security.authorization.mode` (both implemented)
- **Quality:** test minima (≥10/feature, ≥5/function), docs + Swagger sync, WWG governance

### Shared list query (summary)

| Param | Role |
|-------|------|
| `page` / `limit` | Pagination (0-based page; limit 1–100) |
| `sort` | e.g. `name,asc` or `-updatedAt` |
| `search` | Case-insensitive text filter |
| `select` | Sparse fields; deep paths with dots |
| `include` | `count` (docs + totals), `countOnly` (`{ "count": N }` only) |

Full catalog: [docs/LIST-QUERY.md](docs/LIST-QUERY.md#include-options)

---

## Profiles

| Profile | Database | Notes |
|---------|----------|--------|
| `local` (default) | H2 in-memory | H2 console + Swagger on; demo seed typical |
| `postgres` | PostgreSQL | Set `DB_*` + **`JWT_SECRET`** (≥ 32 chars) |
| `prod` | PostgreSQL | Strong secrets; no open Swagger; no demo seed defaults |

### Authorization mode

| Mode | How to enable |
|------|----------------|
| **RBAC** (default) | `AUTHZ_MODE=rbac` or omit |
| **ABAC** | `AUTHZ_MODE=abac` |

Both evaluators ship; switch is configuration only. Details: [docs/OPERATIONS.md](docs/OPERATIONS.md) · WWG `security-model.md`.

---

## Contributing & quality

```powershell
.\mvnw.cmd spotless:apply
.\mvnw.cmd compile spotbugs:check
.\mvnw.cmd test
```

- Feature packages under `com.template.api.feature.<name>`
- Schema via **Flyway only** — never `ddl-auto=update`
- Keep style lean: [anti AI-slop](.wwg/wiki/principles/anti-ai-slop.md)
- Agents: read [AGENTS.md](AGENTS.md) and `.wwg/RESOLVER.md`

---

## License

MIT — see [LICENSE](LICENSE)
