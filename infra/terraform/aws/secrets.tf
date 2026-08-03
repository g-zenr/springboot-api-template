resource "aws_secretsmanager_secret" "app" {
  name_prefix = "${local.name_prefix}-app-"
  description = "Spring Boot API template secrets (JWT + DB)"

  tags = {
    Name = "${local.name_prefix}-app-secrets"
  }
}

resource "aws_secretsmanager_secret_version" "app" {
  secret_id = aws_secretsmanager_secret.app.id
  secret_string = jsonencode({
    JWT_SECRET  = var.jwt_secret
    DB_PASSWORD = random_password.db.result
    DB_USER     = var.db_username
    DB_URL      = "jdbc:postgresql://${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}/${var.db_name}"
  })
}
