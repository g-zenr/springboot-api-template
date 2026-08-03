# Bootstrap Report — 2026-08-01

## Decision

Create a greenfield **Spring Boot API template** with WWG formatting.  
Do **not** port Mediatrix LMS domain or Node infrastructure.

## Stack chosen

- Java 21
- Spring Boot 4.0.0
- Spring Data JPA + Flyway (not Prisma)
- H2 default, Postgres profile
- springdoc OpenAPI
- Sample `Item` feature

## Delivered

- Application code under `com.template.api`
- WWG layers under `.wwg/`
- `AGENTS.md`, `README.md`, `CHANGELOG.md`
- Grok skills: `.grok/skills/agents-bridge`, `.grok/skills/skill-router`
