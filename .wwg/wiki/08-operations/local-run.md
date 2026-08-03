# Local Run

## Prerequisites

- JDK 21+  
- Maven Wrapper (`mvnw` / `mvnw.cmd`)  

## Default (local profile — H2)

```powershell
.\mvnw.cmd spring-boot:run
```

| URL | Purpose |
|-----|---------|
| http://localhost:8080/actuator/health | Health (public) |
| http://localhost:8080/swagger-ui.html | OpenAPI UI — use Authorize with JWT |
| http://localhost:8080/api/v1/auth/login | Login for Bearer token |
| http://localhost:8080/api/v1/items | Sample API (**JWT required**) |
| http://localhost:8080/h2-console | H2 (JDBC URL from logs / application.yml) |

Demo login: `admin` / `admin123`.

## PostgreSQL via Compose

```powershell
docker compose up -d
$env:DB_URL="jdbc:postgresql://localhost:5432/template"
$env:DB_USER="template"
$env:DB_PASSWORD="template"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Copy `.env.example` for variable names.

## Tests

```powershell
.\mvnw.cmd test
```

Mandatory minima: `testing-policy.md` (feature ≥ 10, function ≥ 5, updates refresh tests).

## CI

Pushes/PRs to `main`/`master` run `.github/workflows/ci.yml`.
