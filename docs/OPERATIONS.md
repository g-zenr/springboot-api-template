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

`.github/workflows/ci.yml`:

1. `spotless:check`
2. `./mvnw test`

## Observability

- Header/MDC: `X-Correlation-Id` / `correlationId`
- Actuator: `/actuator/health`, `/actuator/info`
- Rate limit: 429 on abuse

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
