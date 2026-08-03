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

## Containers (local / image)

| Artifact | Path | Notes |
|----------|------|-------|
| Compose Postgres | `docker-compose.yml` | Host **5433**→5432, user/db/password `template` |
| App image | `Dockerfile` | Multi-stage Temurin 21; runs JAR as non-root |

```powershell
docker compose up -d
$env:DB_URL="jdbc:postgresql://localhost:5433/template"
$env:DB_USER="template"
$env:DB_PASSWORD="template"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

```powershell
docker build -t springboot-api-template:local .
docker run --rm -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e DB_URL=... -e DB_USER=... -e DB_PASSWORD=... -e JWT_SECRET=... `
  springboot-api-template:local
```

## Infrastructure as Code (Terraform)

| Path | Role |
|------|------|
| `infra/SETUP.md` | Easy local / AWS / switch-provider paths |
| `infra/CONTRACT.md` | Provider-agnostic deploy contract |
| `infra/env.contract.env.example` | Exact prod env keys for any cloud |
| `infra/terraform/aws/` | Working AWS example |
| `infra/terraform/azure/`, `gcp/` | Stubs; scaffold more via `scripts/scaffold-provider.ps1` |

**Env contract** (prod container — fixed names):

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `JWT_SECRET` (≥ 32)
- `CORS_ALLOWED_ORIGINS`, optional `SEED_DEMO_USERS`, `ALLOW_REGISTRATION`

**Portable Terraform outputs:** `app_base_url`, `container_registry_url`, `db_jdbc_url`, `redeploy_hint`.

Local state for learning; optional remote backend via `backend.hcl.example`.  
Secrets: `terraform.tfvars` / `TF_VAR_*` (gitignored).

```powershell
powershell -File ./infra/scripts/setup-check.ps1
cd infra/terraform/aws
copy terraform.tfvars.example terraform.tfvars
terraform init
terraform plan
```

## CI

GitHub Actions: `.github/workflows/ci.yml` — Spotless + SpotBugs + tests (excludes `*IT`).

## Explicit non-claims

- `prod` profile is **hardened defaults**, not a full multi-tenant product.  
- Terraform AWS stack is a **template example** (HTTP ALB, optional public tasks) — harden HTTPS/NAT/Multi-AZ in product forks.  
- Azure/GCP modules are left to forks; same container + Postgres + secrets contract applies.  
