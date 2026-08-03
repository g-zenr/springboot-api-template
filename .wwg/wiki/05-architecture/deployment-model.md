# Deployment Model

## Profiles

| Profile | Default? | DB | H2 console | OpenAPI | Purpose |
|---------|----------|-----|------------|---------|---------|
| `local` | **Yes** | H2 mem | On | On | Day-to-day dev / tests |
| `postgres` | No | PostgreSQL (`DB_*`) | Off | On (inherited) | Local/shared Postgres |
| `prod` | No | PostgreSQL (`DB_*` required) | Off | **Off** | Hardened runtime defaults |

Activate:

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

## Containers

| Artifact | Path | Notes |
|----------|------|-------|
| Compose Postgres | `docker-compose.yml` | `localhost:5432`, user/db/password `template` |
| App image | `Dockerfile` | Multi-stage Temurin 21; runs JAR as non-root |

```powershell
docker compose up -d
$env:DB_URL="jdbc:postgresql://localhost:5432/template"
$env:DB_USER="template"
$env:DB_PASSWORD="template"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

```powershell
docker build -t springboot-api-template:local .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod -e DB_URL=... -e DB_USER=... -e DB_PASSWORD=... springboot-api-template:local
```

## CI

GitHub Actions: `.github/workflows/ci.yml` — JDK 21 + `./mvnw test` on push/PR to main/master.

## Explicit non-claims

- `prod` profile is **hardened defaults**, not a full production product (no auth).  
- Cloud-specific runbooks (Cloud Run, K8s, etc.) are left to the product fork.  
