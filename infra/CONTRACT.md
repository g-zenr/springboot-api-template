# Deploy contract (provider-agnostic)

**This is the only contract the Spring Boot app cares about.**  
Every cloud stack (AWS today, Azure/GCP later) must satisfy it. Terraform providers are swappable; the app is not rewritten.

---

## 1. Runtime shape

| Piece | Requirement |
|-------|-------------|
| App | Container from repo root `Dockerfile` (port **8080**) |
| Profile | `SPRING_PROFILES_ACTIVE=prod` |
| Database | **PostgreSQL 16+** (Flyway migrations run on boot) |
| Secrets | Not baked into the image; injected at runtime |
| Health | `GET /actuator/health` → **200** (ALB / probe target) |
| Metrics | `/actuator/metrics`, `/actuator/prometheus` need **auth** — do not expose publicly |

---

## 2. Environment variables (required injection)

Copy from `env.contract.env.example`. Names are **fixed** (Spring Boot + this template).

| Variable | Required | Example / rule |
|----------|----------|----------------|
| `SPRING_PROFILES_ACTIVE` | **yes** | `prod` |
| `DB_URL` | **yes** | `jdbc:postgresql://HOST:5432/DBNAME` |
| `DB_USER` | **yes** | DB username |
| `DB_PASSWORD` | **yes** | From secret store |
| `JWT_SECRET` | **yes** | ≥ **32** characters |
| `CORS_ALLOWED_ORIGINS` | recommended | Comma-separated HTTPS origins |
| `SEED_DEMO_USERS` | optional | `false` in real envs |
| `ALLOW_REGISTRATION` | optional | `false` in real envs |
| `JWT_EXPIRATION_MS` | optional | default in app |
| `JWT_REFRESH_EXPIRATION_MS` | optional | default in app |
| `RATE_LIMIT_LOGIN_PER_MIN` | optional | prod defaults in yml |
| `RATE_LIMIT_API_PER_MIN` | optional | prod defaults in yml |
| `AUTHZ_MODE` | optional | `rbac` \| `abac` |

Local/dev only: use root `.env.example` + Compose (not this prod contract).

---

## 3. Standard Terraform outputs (every provider stack)

Name these the same so scripts and docs stay portable:

| Output | Description |
|--------|-------------|
| `app_base_url` | Public base URL of the API (no trailing slash) |
| `container_registry_url` | Push target for `docker build` image |
| `db_jdbc_url` | Same value as runtime `DB_URL` |
| `redeploy_hint` | One-line CLI or note to roll a new image |

Provider-specific extras (`ecs_cluster_name`, `resource_group`, …) are optional and may use a `provider_` prefix.

---

## 4. What a provider stack must create

Minimum viable production shape:

1. **Network** — isolate DB from the public internet  
2. **Postgres** — reachable only from the app  
3. **Container registry** — hold the Dockerfile image  
4. **Secret store** — at least `JWT_SECRET` + `DB_PASSWORD`  
5. **Compute** — run the container with the env contract  
6. **Load balancer / public endpoint** — HTTP(S) → container `:8080`  
7. **Health check** — path `/actuator/health`

---

## 5. Switching providers (checklist)

When you start a project on Azure, GCP, etc.:

1. Keep **this file** and `env.contract.env.example` unchanged.  
2. Scaffold: `pwsh ./infra/scripts/scaffold-provider.ps1 -Name azure`  
3. Implement resources until `terraform apply` yields the **standard outputs**.  
4. Wire the same env names into the container (not Azure-specific renames).  
5. Point CI “deploy” at `infra/terraform/<provider>/` only — app build stays `Dockerfile` at repo root.  
6. Do **not** fork `application.yml` per cloud.

---

## 6. Non-goals

- Multi-cloud simultaneous deploy (pick one stack per environment)  
- Embedding secrets in Terraform state without encryption/backend discipline  
- Replacing Flyway with cloud SQL “init scripts” as the schema source of truth  
