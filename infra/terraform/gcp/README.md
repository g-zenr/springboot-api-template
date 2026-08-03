# Terraform â€” gcp

**Status:** scaffold only â€” implement resources for your project.

Follow the portable contract:

- [../../CONTRACT.md](../../CONTRACT.md)
- [../../env.contract.env.example](../../env.contract.env.example)
- [../../SETUP.md](../../SETUP.md) Path C

## Must implement

1. Network isolation for Postgres  
2. Managed PostgreSQL 16+  
3. Container registry  
4. Secret store (JWT_SECRET, DB_PASSWORD)  
5. Run container from repo `Dockerfile` on port **8080**  
6. Public endpoint (LB / gateway) with health `/actuator/health`  
7. Standard outputs (see `outputs.tf`)

## Workflow

```powershell
cd infra/terraform/gcp
copy terraform.tfvars.example terraform.tfvars
# set jwt_secret and provider-specific values

terraform init
terraform plan
terraform apply

# Build from repo root, push to: terraform output -raw container_registry_url
docker build -t app:local ../../../
```

## Env injection

Map secrets/config to **exact** names in `env.contract.env.example`  
(`SPRING_PROFILES_ACTIVE`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, â€¦).  
Do not invent cloud-specific env names the Spring app does not read.
