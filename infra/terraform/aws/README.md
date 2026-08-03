# Terraform — AWS stack

Provisions a **prod-shaped** environment for this Spring Boot API template:

| Resource | Purpose |
|----------|---------|
| VPC + public/private subnets | Network isolation |
| RDS PostgreSQL 16 | App database (private) |
| ECR | Container images |
| Secrets Manager | `JWT_SECRET`, `DB_PASSWORD` |
| ALB + ECS Fargate | HTTP API (optional via `create_ecs_service`) |
| CloudWatch Logs | Container logs |

## Prerequisites

- Terraform ≥ 1.5
- AWS credentials (`aws configure` or env vars)
- Docker (to build/push the app image)

## Apply

```powershell
cd infra/terraform/aws
copy terraform.tfvars.example terraform.tfvars
# edit jwt_secret (≥ 32 chars)

terraform init
terraform plan
terraform apply
```

## Push image & run

```powershell
# Portable outputs (same names on Azure/GCP stacks later)
$ECR = terraform output -raw container_registry_url
$REGION = "us-east-1"   # match aws_region
$REG = $ECR.Split('/')[0]

aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $REG
docker build -t springboot-api-template:local ../../../
docker tag springboot-api-template:local "${ECR}:latest"
docker push "${ECR}:latest"

# Or run: terraform output -raw redeploy_hint
aws ecs update-service `
  --cluster (terraform output -raw provider_ecs_cluster_name) `
  --service (terraform output -raw provider_ecs_service_name) `
  --force-new-deployment `
  --region $REGION
```

Smoke test:

```powershell
terraform output -raw app_base_url
# GET {app_base_url}/actuator/health
```

## App env contract

ECS task injects:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`, `DB_USER` (plain env)
- `DB_PASSWORD`, `JWT_SECRET` (from Secrets Manager)
- `CORS_ALLOWED_ORIGINS`, `SEED_DEMO_USERS`, `ALLOW_REGISTRATION`

Matches `application.yml` prod profile and `.env.example`.

## Staged bootstrap

1. `create_ecs_service = false` → network + RDS + ECR only  
2. Push first image  
3. `create_ecs_service = true` → `terraform apply` again  

## Destroy

```powershell
terraform destroy
```

RDS `skip_final_snapshot` defaults to `true` for template/dev. Set `db_deletion_protection = true` and `db_skip_final_snapshot = false` before real data.

## Security notes (template vs hardened prod)

| Template default | Hardened fork |
|------------------|---------------|
| ALB HTTP :80 | ACM HTTPS + redirect |
| Tasks in public subnets + public IP | Private + NAT + no public IP |
| Open `/actuator/health` for ALB | Same (liveness only); never open metrics |
| `db.t4g.micro` single-AZ | Multi-AZ, larger class, backups policy |
| Mutable ECR tags | Immutable digests + CI promote |

## Cost

Resources create **billable** AWS spend (RDS, ALB, Fargate, NAT if you add it). Destroy when unused.
