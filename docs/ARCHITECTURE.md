# Architecture

## Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| Web | Spring MVC |
| Security | Spring Security + JWT (jjwt) + refresh tokens |
| Persistence | Spring Data JPA + Flyway |
| Mapping | MapStruct |
| Docs runtime | springdoc OpenAPI |
| Logs | Text (local); ECS-style JSON (prod) |
| Containers | Dockerfile (Temurin 21), Compose Postgres |
| IaC | Terraform under `infra/terraform/aws` (VPC, RDS, ECR, ALB, ECS) |

## Package layout

See live diagram: [diagrams/package-structure.md](diagrams/package-structure.md)

```
com.template.api
  feature.auth / feature.user / feature.item / feature.note
  config.security / config (CORS, OpenAPI)
  common.error / common.web (correlation, rate limit)
```

## Key diagrams (living)

| Diagram | File |
|---------|------|
| Request pipeline | [diagrams/request-pipeline.md](diagrams/request-pipeline.md) |
| Auth flow | [diagrams/auth-flow.md](diagrams/auth-flow.md) |
| Data model | [diagrams/data-model.md](diagrams/data-model.md) |
| API surface | [diagrams/api-surface.md](diagrams/api-surface.md) |
| Package structure | [diagrams/package-structure.md](diagrams/package-structure.md) |
| Deploy profiles | [diagrams/deploy-profiles.md](diagrams/deploy-profiles.md) |

## Design rules

1. **Feature packages** own controller → service → repository → entity  
2. **Flyway** owns schema; JPA `ddl-auto=validate`  
3. **No Prisma**  
4. Public API under `/api/v1/**`  
5. Cross-cutting filters: correlation id → rate limit → security JWT  

## WWG companion

- `.wwg/wiki/05-architecture/*`  
- ADRs under `.wwg/wiki/04-decisions/adr/`  

When architecture changes, update **this file + diagrams + WWG** in the same task.
