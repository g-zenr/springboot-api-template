# ---------------------------------------------------------------------------
# Portable outputs (infra/CONTRACT.md) — keep these names on every provider
# ---------------------------------------------------------------------------

output "app_base_url" {
  description = "Public base URL of the API (portable)"
  value       = try("http://${aws_lb.app[0].dns_name}", null)
}

output "container_registry_url" {
  description = "Docker push target (portable)"
  value       = aws_ecr_repository.app.repository_url
}

output "db_jdbc_url" {
  description = "JDBC URL for DB_URL env (portable)"
  value       = "jdbc:postgresql://${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}/${var.db_name}"
}

output "redeploy_hint" {
  description = "How to roll a new image after push (portable)"
  value = var.create_ecs_service ? (
    "aws ecs update-service --cluster ${aws_ecs_cluster.main[0].name} --service ${aws_ecs_service.app[0].name} --force-new-deployment --region ${var.aws_region}"
  ) : "create_ecs_service=false — enable ECS or run container against db_jdbc_url manually"
}

# ---------------------------------------------------------------------------
# AWS-specific extras (optional for other tools)
# ---------------------------------------------------------------------------

output "vpc_id" {
  description = "VPC id"
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "Public subnet ids"
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "Private subnet ids"
  value       = aws_subnet.private[*].id
}

output "ecr_repository_url" {
  description = "Alias of container_registry_url (AWS naming)"
  value       = aws_ecr_repository.app.repository_url
}

output "rds_endpoint" {
  description = "RDS hostname:port"
  value       = aws_db_instance.postgres.endpoint
}

output "rds_jdbc_url" {
  description = "Alias of db_jdbc_url"
  value       = "jdbc:postgresql://${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}/${var.db_name}"
}

output "app_secrets_arn" {
  description = "Secrets Manager secret ARN (JWT + DB password)"
  value       = aws_secretsmanager_secret.app.arn
  sensitive   = true
}

output "alb_dns_name" {
  description = "Public ALB DNS (HTTP)"
  value       = try(aws_lb.app[0].dns_name, null)
}

output "alb_url" {
  description = "Alias of app_base_url"
  value       = try("http://${aws_lb.app[0].dns_name}", null)
}

output "provider_ecs_cluster_name" {
  description = "ECS cluster name (AWS-specific)"
  value       = try(aws_ecs_cluster.main[0].name, null)
}

output "provider_ecs_service_name" {
  description = "ECS service name (AWS-specific)"
  value       = try(aws_ecs_service.app[0].name, null)
}

# Backward-compatible names
output "ecs_cluster_name" {
  description = "Deprecated alias — prefer provider_ecs_cluster_name"
  value       = try(aws_ecs_cluster.main[0].name, null)
}

output "ecs_service_name" {
  description = "Deprecated alias — prefer provider_ecs_service_name"
  value       = try(aws_ecs_service.app[0].name, null)
}

output "app_env_contract" {
  description = "Environment variables the container expects (names only)"
  value = {
    SPRING_PROFILES_ACTIVE = "prod"
    DB_URL                 = "from output db_jdbc_url"
    DB_USER                = var.db_username
    DB_PASSWORD            = "from Secrets Manager"
    JWT_SECRET             = "from Secrets Manager"
    CORS_ALLOWED_ORIGINS   = var.cors_allowed_origins
    SEED_DEMO_USERS        = tostring(var.seed_demo_users)
    ALLOW_REGISTRATION     = tostring(var.allow_registration)
  }
}
