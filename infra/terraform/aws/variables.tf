variable "aws_region" {
  type        = string
  description = "AWS region for all resources"
  default     = "us-east-1"
}

variable "project_name" {
  type        = string
  description = "Short name used in resource names (lowercase, hyphen ok)"
  default     = "springboot-api-template"

  validation {
    condition     = can(regex("^[a-z0-9-]+$", var.project_name))
    error_message = "project_name must be lowercase alphanumeric and hyphens."
  }
}

variable "environment" {
  type        = string
  description = "Environment label (dev, staging, prod)"
  default     = "dev"
}

variable "extra_tags" {
  type        = map(string)
  description = "Additional tags applied to all resources"
  default     = {}
}

# --- Network ---

variable "vpc_cidr" {
  type        = string
  description = "VPC CIDR"
  default     = "10.40.0.0/16"
}

variable "az_count" {
  type        = number
  description = "Number of AZs (2 recommended for ALB)"
  default     = 2

  validation {
    condition     = var.az_count >= 2 && var.az_count <= 3
    error_message = "az_count must be 2 or 3."
  }
}

# --- Database ---

variable "db_name" {
  type        = string
  description = "PostgreSQL database name"
  default     = "template"
}

variable "db_username" {
  type        = string
  description = "Master username for RDS"
  default     = "template"
}

variable "db_instance_class" {
  type        = string
  description = "RDS instance class"
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  type        = number
  description = "RDS allocated storage (GiB)"
  default     = 20
}

variable "db_multi_az" {
  type        = bool
  description = "Enable Multi-AZ RDS (higher cost)"
  default     = false
}

variable "db_skip_final_snapshot" {
  type        = bool
  description = "Skip final snapshot on destroy (true for template/dev)"
  default     = true
}

variable "db_deletion_protection" {
  type        = bool
  description = "RDS deletion protection"
  default     = false
}

# --- App / ECS ---

variable "container_port" {
  type        = number
  description = "Container listen port (Spring Boot default 8080)"
  default     = 8080
}

variable "desired_count" {
  type        = number
  description = "ECS service desired task count"
  default     = 1
}

variable "task_cpu" {
  type        = number
  description = "Fargate task CPU units (256 = 0.25 vCPU)"
  default     = 256
}

variable "task_memory" {
  type        = number
  description = "Fargate task memory (MiB)"
  default     = 512
}

variable "image_tag" {
  type        = string
  description = "ECR image tag to run"
  default     = "latest"
}

variable "assign_public_ip" {
  type        = bool
  description = "Assign public IP to Fargate tasks (simpler without NAT; use private + NAT in hardened prod)"
  default     = true
}

# --- Secrets / app config ---

variable "jwt_secret" {
  type        = string
  description = "JWT signing secret (≥ 32 characters). Prefer TF_VAR_jwt_secret or Secrets Manager injection in CI."
  sensitive   = true

  validation {
    condition     = length(var.jwt_secret) >= 32
    error_message = "jwt_secret must be at least 32 characters."
  }
}

variable "cors_allowed_origins" {
  type        = string
  description = "Comma-separated CORS origins for prod"
  default     = "https://localhost"
}

variable "seed_demo_users" {
  type        = bool
  description = "app.security.seed-demo-users"
  default     = false
}

variable "allow_registration" {
  type        = bool
  description = "app.security.allow-registration"
  default     = false
}

variable "create_ecs_service" {
  type        = bool
  description = "Create ECS service/ALB. Set false to provision only network+RDS+ECR first."
  default     = true
}
