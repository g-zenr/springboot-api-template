# Easy setup guide

Use this when you **clone the template** or **start a real project** on another cloud.

---

## Path A — Local only (no cloud, 2 minutes)

```powershell
# From repo root
docker compose up -d
copy .env.example .env
# defaults already point at localhost:5433

.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Smoke: `http://localhost:8080/actuator/health`  
Swagger (non-prod): `http://localhost:8080/swagger-ui.html`

No Terraform required.

---

## Path B — First cloud deploy (AWS example included)

### 1. One-time tools

| Tool | Check |
|------|--------|
| Terraform ≥ 1.5 | `terraform version` |
| Docker | `docker version` |
| Cloud CLI | `aws --version` (or `az` / `gcloud` later) |
| Cloud credentials | CLI logged in / env vars set |

### 2. Configure secrets (never commit)

```powershell
cd infra/terraform/aws
copy terraform.tfvars.example terraform.tfvars
# edit: jwt_secret (≥ 32 chars), project_name, environment
```

Or: `$env:TF_VAR_jwt_secret = "your-32+-char-secret............"`

### 3. Plan & apply

```powershell
terraform init
terraform plan
terraform apply
```

### 4. Build, push, run image

```powershell
# From infra/terraform/aws after apply
$ECR = terraform output -raw container_registry_url
$REG = $ECR.Split('/')[0]
$REGION = "us-east-1"   # match your tfvars

aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $REG
docker build -t app:local ../../../
docker tag app:local "${ECR}:latest"
docker push "${ECR}:latest"

# Optional force rollout (AWS)
aws ecs update-service `
  --cluster (terraform output -raw provider_ecs_cluster_name) `
  --service (terraform output -raw provider_ecs_service_name) `
  --force-new-deployment --region $REGION
```

### 5. Smoke

```text
GET {app_base_url}/actuator/health
```

```powershell
terraform output -raw app_base_url
```

### 6. Tear down (dev)

```powershell
terraform destroy
```

---

## Path C — New project on another provider (Azure / GCP / …)

The **app does not change**. You only add a sibling stack under `infra/terraform/<name>/`.

```powershell
# From repo root
powershell -File ./infra/scripts/scaffold-provider.ps1 -Name azure
# or: -Name gcp
```

Then:

1. Open `infra/terraform/<name>/README.md` and implement the resource list.  
2. Keep **output names** from [CONTRACT.md](CONTRACT.md) §3.  
3. Inject env vars from [env.contract.env.example](env.contract.env.example) into the container.  
4. `terraform init && plan && apply` inside that folder only.  
5. Same Docker build at **repo root**; push to that provider’s registry output.

| Keep unchanged | Replace per cloud |
|----------------|-------------------|
| `Dockerfile` | `infra/terraform/<provider>/` |
| `CONTRACT.md` | Registry + LB + DB resource types |
| `env.contract.env.example` | Secret store product (Key Vault, GSM, …) |
| Flyway SQL | IAM / networking details |

---

## Decision cheat sheet

| Goal | Use |
|------|-----|
| Code + tests | `local` profile (H2) — no Docker needed |
| Real Postgres on laptop | Path A (Compose) |
| Shared/staging API | Path B (Terraform + image) |
| “We’re on Azure/GCP” | Path C (scaffold + implement) |

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| App won’t start in prod | Missing `JWT_SECRET` or `DB_*` |
| Health check fails | Security group / probe path must be `/actuator/health` |
| Flyway errors | DB user needs DDL rights; URL must hit empty or migratable DB |
| Image pull fails | Push to `container_registry_url` before first task start |
| Wrong port | Container **8080**; LB maps 80/443 → 8080 |

---

## Related

- [CONTRACT.md](CONTRACT.md) — portable rules  
- [terraform/README.md](terraform/README.md) — provider folders  
- [terraform/aws/README.md](terraform/aws/README.md) — AWS details  
- Root `.env.example` — local/postgres only  
