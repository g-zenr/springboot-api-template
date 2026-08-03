#Requires -Version 5.1
<#
.SYNOPSIS
  Scaffold a new Terraform provider folder that follows the deploy contract.

.EXAMPLE
  pwsh ./infra/scripts/scaffold-provider.ps1 -Name azure
  pwsh ./infra/scripts/scaffold-provider.ps1 -Name gcp
#>
param(
	[Parameter(Mandatory = $true)]
	[ValidatePattern('^[a-z][a-z0-9-]*$')]
	[string] $Name
)

$ErrorActionPreference = "Stop"
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$target = Join-Path $repoRoot "infra\terraform\$Name"

if (Test-Path $target) {
	Write-Host "Already exists: $target" -ForegroundColor Yellow
	Write-Host "Edit that folder or choose another -Name."
	exit 0
}

New-Item -ItemType Directory -Path $target | Out-Null

$readme = @"
# Terraform — $Name

**Status:** scaffold only — implement resources for your project.

Follow the portable contract:

- [../../CONTRACT.md](../../CONTRACT.md)
- [../../env.contract.env.example](../../env.contract.env.example)
- [../../SETUP.md](../../SETUP.md) Path C

## Must implement

1. Network isolation for Postgres  
2. Managed PostgreSQL 16+  
3. Container registry  
4. Secret store (JWT_SECRET, DB_PASSWORD)  
5. Run container from repo ``Dockerfile`` on port **8080**  
6. Public endpoint (LB / gateway) with health ``/actuator/health``  
7. Standard outputs (see ``outputs.tf``)

## Workflow

``````powershell
cd infra/terraform/$Name
copy terraform.tfvars.example terraform.tfvars
# set jwt_secret and provider-specific values

terraform init
terraform plan
terraform apply

# Build from repo root, push to: terraform output -raw container_registry_url
docker build -t app:local ../../../
``````

## Env injection

Map secrets/config to **exact** names in ``env.contract.env.example``  
(``SPRING_PROFILES_ACTIVE``, ``DB_URL``, ``DB_USER``, ``DB_PASSWORD``, ``JWT_SECRET``, …).  
Do not invent cloud-specific env names the Spring app does not read.
"@

$versions = @"
terraform {
  required_version = ">= 1.5.0"

  required_providers {
    # Add your provider, e.g. azurerm, google, digitalocean
    # azurerm = { source = "hashicorp/azurerm", version = "~> 3.0" }
  }

  # Optional remote state — copy backend.hcl.example
  # backend "azurerm" {}
  # backend "gcs" {}
}

# provider "azurerm" { features {} }
"@

$variables = @"
variable "project_name" {
  type        = string
  description = "Short name for resources"
  default     = "springboot-api-template"
}

variable "environment" {
  type        = string
  description = "dev | staging | prod"
  default     = "dev"
}

variable "jwt_secret" {
  type        = string
  description = "JWT signing secret (≥ 32 chars). Prefer TF_VAR_jwt_secret."
  sensitive   = true

  validation {
    condition     = length(var.jwt_secret) >= 32
    error_message = "jwt_secret must be at least 32 characters."
  }
}

variable "db_name" {
  type    = string
  default = "template"
}

variable "db_username" {
  type    = string
  default = "template"
}

variable "cors_allowed_origins" {
  type    = string
  default = "https://localhost"
}

variable "container_port" {
  type    = number
  default = 8080
}
"@

$outputs = @"
# Portable outputs — required by infra/CONTRACT.md (do not rename)

output "app_base_url" {
  description = "Public base URL of the API"
  value       = null # TODO: e.g. https://...
}

output "container_registry_url" {
  description = "Docker push target"
  value       = null # TODO
}

output "db_jdbc_url" {
  description = "JDBC URL for DB_URL env"
  value       = null # TODO: jdbc:postgresql://...
}

output "redeploy_hint" {
  description = "How to roll a new image"
  value       = "TODO: document CLI for this provider"
}
"@

$tfvars = @"
project_name = "springboot-api-template"
environment  = "dev"

# REQUIRED — or set TF_VAR_jwt_secret
jwt_secret = "change-me-template-jwt-secret-key-32b!"

cors_allowed_origins = "https://localhost"
db_name              = "template"
db_username          = "template"
"@

$backend = @"
# Optional remote state for teams. Fill and:
#   terraform init -backend-config=backend.hcl
#
# Azure example:
#   resource_group_name  = "..."
#   storage_account_name = "..."
#   container_name       = "tfstate"
#   key                  = "springboot-api-template/dev.tfstate"
#
# GCS example:
#   bucket = "..."
#   prefix = "springboot-api-template/dev"
"@

$main = @"
# Implement $Name resources here (or split into network.tf, database.tf, compute.tf).
# Satisfy infra/CONTRACT.md before using in a real environment.
#
# Suggested files:
#   network.tf  database.tf  registry.tf  secrets.tf  compute.tf  loadbalancer.tf
"@

Set-Content -Path (Join-Path $target "README.md") -Value $readme -Encoding utf8
Set-Content -Path (Join-Path $target "versions.tf") -Value $versions -Encoding utf8
Set-Content -Path (Join-Path $target "variables.tf") -Value $variables -Encoding utf8
Set-Content -Path (Join-Path $target "outputs.tf") -Value $outputs -Encoding utf8
Set-Content -Path (Join-Path $target "main.tf") -Value $main -Encoding utf8
Set-Content -Path (Join-Path $target "terraform.tfvars.example") -Value $tfvars -Encoding utf8
Set-Content -Path (Join-Path $target "backend.hcl.example") -Value $backend -Encoding utf8

Write-Host "Created: $target" -ForegroundColor Green
Write-Host "Next:"
Write-Host "  1. Read infra/CONTRACT.md and $Name/README.md"
Write-Host "  2. Implement resources; keep standard outputs"
Write-Host "  3. terraform init && terraform validate"
