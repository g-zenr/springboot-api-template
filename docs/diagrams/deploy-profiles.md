# Diagram: Deploy profiles

> **Living diagram** — update when profiles or deploy artifacts change.

```mermaid
flowchart LR
  subgraph local_dev [local]
    H2[(H2 memory)]
    Seed[Demo seed ON]
    TextLog[Text logs]
    Swagger[OpenAPI ON]
  end

  subgraph pg [postgres]
    PG[(PostgreSQL)]
    Compose[docker-compose]
  end

  subgraph production [prod]
    PG2[(PostgreSQL)]
    ECS[ECS JSON logs]
    NoSwagger[OpenAPI OFF]
    NoSeed[Demo seed OFF]
    Secret[JWT_SECRET required]
  end

  Dev[Developer] --> local_dev
  Dev --> Compose --> pg
  CI[GitHub Actions] --> Tests[mvn test]
  Image[Dockerfile JAR] --> production
```

## Artifacts

- `docker-compose.yml` — Postgres 16  
- `Dockerfile` — multi-stage Temurin 21  
- `.github/workflows/ci.yml` — Spotless + tests  
