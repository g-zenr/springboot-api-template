# ADR 0001: Use Spring Data JPA + Flyway (not Prisma)

## Status

Accepted

## Context

Prisma is excellent for Node/TypeScript APIs but has no first-class Java/Spring client. This template targets Spring Boot.

## Decision

- Use **Spring Data JPA** for persistence mapping and repositories.
- Use **Flyway** for versioned schema migrations.
- Use **H2** for default local/test; optional **PostgreSQL** via `postgres` profile.
- Do **not** integrate Prisma.

## Consequences

- One JVM stack; standard Spring transactions and testing.
- Schema review happens in SQL migrations.
- Developers familiar with Prisma will map concepts to entities + Flyway instead.
