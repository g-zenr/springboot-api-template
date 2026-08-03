# ADR 0006: Persisted users + refresh tokens

## Status

Accepted

## Decision

- Store users in `app_users` (Flyway V2)
- Seed demo users via `DemoUserSeeder` (admin/user)
- Issue opaque **refresh tokens** hashed (SHA-256) in `refresh_tokens`
- Rotate refresh tokens on `/api/v1/auth/refresh`; revoke on `/logout`

## Consequences

- No longer in-memory-only Spring users
- Still demo passwords — replace seeder for real products
