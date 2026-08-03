# Start Docker Postgres and run the API with the postgres profile.
# Host port 5433 maps to container 5432 (avoids local Postgres on 5432).

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "Starting Postgres container..." -ForegroundColor Cyan
docker compose up -d
if ($LASTEXITCODE -ne 0) { throw "docker compose up failed" }

Write-Host "Waiting for Postgres..." -ForegroundColor Cyan
$ready = $false
for ($i = 0; $i -lt 30; $i++) {
	docker exec api-template-postgres pg_isready -U template -d template 2>$null | Out-Null
	if ($LASTEXITCODE -eq 0) { $ready = $true; break }
	Start-Sleep -Seconds 1
}
if (-not $ready) { throw "Postgres did not become ready in time" }

$env:DB_URL = "jdbc:postgresql://localhost:5433/template"
$env:DB_USER = "template"
$env:DB_PASSWORD = "template"

Write-Host "Starting Spring Boot (profile=postgres)..." -ForegroundColor Cyan
Write-Host "  DB_URL=$env:DB_URL" -ForegroundColor DarkGray
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
