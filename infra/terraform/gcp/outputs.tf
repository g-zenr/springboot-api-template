# Portable outputs â€” required by infra/CONTRACT.md (do not rename)

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
