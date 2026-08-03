# Terraform providers

Each **sibling folder** is one cloud. Pick **one** per environment.

| Folder | Status | Notes |
|--------|--------|--------|
| [`aws/`](aws/) | **Implemented example** | VPC, RDS 16, ECR, Secrets Manager, ALB, ECS Fargate |
| [`azure/`](azure/) | Stub | Scaffold README + required outputs — fill when you choose Azure |
| [`gcp/`](gcp/) | Stub | Scaffold README + required outputs — fill when you choose GCP |

## Rules (so the next project is easy)

1. **One root module per provider** — no nested “which cloud?” switch in a single root.  
2. **Same outputs** — see [../CONTRACT.md](../CONTRACT.md) §3 (`app_base_url`, `container_registry_url`, `db_jdbc_url`, `redeploy_hint`).  
3. **Same env injection** — [../env.contract.env.example](../env.contract.env.example).  
4. **App image always** from repo root `Dockerfile`.  
5. **Secrets never committed** — `*.tfvars` gitignored; use `*.tfvars.example` only.  
6. **Commit** `.terraform.lock.hcl` per provider after first successful `init`.

## Add a provider

```powershell
powershell -File ../scripts/scaffold-provider.ps1 -Name digitalocean
# implements infra/terraform/digitalocean/
```

Or copy `azure/` stub and rename.

## Workflow

```text
terraform/<provider>/
  versions.tf              # providers + required_version
  variables.tf             # include contract-related vars
  outputs.tf               # MUST include standard names
  *.tf                     # resources
  terraform.tfvars.example
  backend.hcl.example      # optional remote state
  README.md                # apply + push image steps for THIS cloud
```

## Validate (any provider)

```powershell
cd infra/terraform/<provider>
terraform fmt -recursive
terraform init -backend=false
terraform validate
```
