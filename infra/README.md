# Infrastructure (`infra/`)

**Goal:** ship this API on any cloud with the same app contract.  
**Local DB:** Docker Compose (repo root). **Cloud:** Terraform under `terraform/<provider>/`.

| Start here | When |
|------------|------|
| **[SETUP.md](SETUP.md)** | First time / next project — step-by-step |
| **[CONTRACT.md](CONTRACT.md)** | What every provider must implement |
| **[env.contract.env.example](env.contract.env.example)** | Exact env names for containers |
| **[terraform/README.md](terraform/README.md)** | Provider layout + switch guide |
| **[terraform/aws/](terraform/aws/)** | Working AWS example (copy pattern) |

```text
infra/
  SETUP.md                 ← easy path A/B/C
  CONTRACT.md              ← provider-agnostic rules
  env.contract.env.example ← prod env keys
  scripts/
    scaffold-provider.ps1  ← new azure/gcp/… folder
    setup-check.ps1        ← tool + layout sanity check
  terraform/
    README.md
    aws/                   ← full example (VPC, RDS, ECR, ECS, ALB)
    azure/                 ← scaffold stub (implement when you pick Azure)
    gcp/                   ← scaffold stub (implement when you pick GCP)
```

## 30-second mental model

```text
  Dockerfile (app)  ──push──►  registry
                                │
  CONTRACT env vars ──────────► compute (ECS / ACA / Cloud Run / …)
                                │
  Postgres (managed) ◄──────────┘
```

Change **only** the middle (Terraform provider). Do not rename env vars per cloud.

## Quick commands

```powershell
# Sanity check tools + folders (Windows PowerShell or PowerShell 7)
powershell -File ./infra/scripts/setup-check.ps1

# Local Postgres
docker compose up -d

# AWS example
cd infra/terraform/aws
copy terraform.tfvars.example terraform.tfvars
terraform init; terraform plan

# Start a new provider for your real project
powershell -File ./infra/scripts/scaffold-provider.ps1 -Name azure
```

## Docs elsewhere

- `docs/OPERATIONS.md` — ops + IaC pointer  
- `.wwg/wiki/05-architecture/deployment-model.md` — agent truth  
