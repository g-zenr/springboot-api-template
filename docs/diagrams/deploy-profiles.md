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
    PG2[(PostgreSQL RDS)]
    Fargate[ECS Fargate]
    ALB[ALB]
    Sec[Secrets Manager]
    NoSwagger[OpenAPI OFF]
    NoSeed[Demo seed OFF]
    Secret[JWT_SECRET required]
  end

  subgraph iac [IaC]
    TF[Terraform aws stack]
  end

  Dev[Developer] --> local_dev
  Dev --> Compose --> pg
  CI[GitHub Actions] --> Tests[mvn test]
  Image[Dockerfile JAR] --> ECR[ECR]
  ECR --> Fargate
  TF --> production
  Image --> production
```

## Artifacts

- `docker-compose.yml` — Postgres 16 (host 5433)  
- `Dockerfile` — multi-stage Temurin 21  
- `infra/terraform/aws/` — VPC, RDS, ECR, Secrets, ALB, ECS  
- `.github/workflows/ci.yml` — Spotless + SpotBugs + tests  

