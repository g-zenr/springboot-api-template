# Operations Guide

## Profiles

| Profile | DB | OpenAPI | Demo seed | Registration | Logs | JWT default secret |
|---------|-----|---------|-----------|--------------|------|--------------------|
| `local` (default) | H2 mem | On | On | On | Text + correlation | Allowed (warn) |
| `postgres` | Postgres `DB_*` | On | Config | Config | Text | Prefer real secret |
| `prod` | Postgres required | **Off** | **Off** | **Off** | **ECS JSON** | **Required; no default** |

## Environment variables

See `.env.example`:

- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `JWT_SECRET` (≥ 32 chars; required prod)
- `JWT_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS`
- `CORS_ALLOWED_ORIGINS`
- `SEED_DEMO_USERS`, `ALLOW_REGISTRATION`
- `RATE_LIMIT_LOGIN_PER_MIN`, `RATE_LIMIT_API_PER_MIN`
- `AUTHZ_MODE` — `rbac` (default) or `abac` (both implementations ship; see security model)

## Docker

```powershell
docker compose up -d          # Postgres on host port 5433
docker build -t api-template .
```

Compose publishes **5433→5432** so a local Windows Postgres on 5432 does not steal connections.

Run app image with `SPRING_PROFILES_ACTIVE=prod` and real `DB_*` + `JWT_SECRET`.

## CI

`.github/workflows/ci.yml` (optimized for fewer downloads / faster runs):

1. JDK 21 + **Maven dependency cache** (`~/.m2/repository` via `setup-java`)
2. **Maven wrapper cache** (`~/.m2/wrapper` — avoids re-downloading the Maven distro)
3. **Single** `./mvnw -B -ntp spotless:check test` (one process, not two)
4. **Excludes `*IT`** (e.g. Testcontainers Postgres) so CI does not pull Docker images every time

Local full suite (includes Postgres IT when Docker is available):

```powershell
.\mvnw.cmd test
```

First CI run after `pom.xml` changes still downloads dependencies; later runs should mostly hit cache.

## Observability

- Header/MDC: `X-Correlation-Id` / `correlationId`
- Actuator **public:** `/actuator/health`, `/actuator/info`
- Actuator **authenticated (Bearer):** `/actuator/metrics`, `/actuator/prometheus` (Prometheus scrape)
- Rate limit: 429 on abuse
- Optional async executor: `app.async.enabled=true` (off by default)

Prometheus scrape example (local):

```http
GET /actuator/prometheus
Authorization: Bearer <accessToken>
```

## Authorization mode (RBAC / ABAC)

| Mode | Config | Behavior (defaults) |
|------|--------|---------------------|
| `rbac` | `app.security.authorization.mode=rbac` or `AUTHZ_MODE=rbac` | Roles (`ADMIN` for user admin APIs); notes owner-only |
| `abac` | `app.security.authorization.mode=abac` or `AUTHZ_MODE=abac` | Attribute policies; notes owner **or** ADMIN |

Code: `config.security.authz` · WWG: `.wwg/wiki/05-architecture/security-model.md`

Switch only — both evaluators are always on the classpath; `AuthorizationService` selects at runtime.

## Production checklist (product fork)

- [ ] Set strong `JWT_SECRET`
- [ ] `seed-demo-users=false`
- [ ] Registration policy decided
- [ ] Choose `AUTHZ_MODE` (`rbac` or `abac`) and review default policies
- [ ] Postgres backups / migrations plan
- [ ] Replace demo passwords; lock down CORS
- [ ] Review rate limits for multi-instance (in-memory is single-node)

Diagram: [diagrams/deploy-profiles.md](diagrams/deploy-profiles.md)
