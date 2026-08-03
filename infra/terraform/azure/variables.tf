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
  description = "JWT signing secret (â‰¥ 32 chars). Prefer TF_VAR_jwt_secret."
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
