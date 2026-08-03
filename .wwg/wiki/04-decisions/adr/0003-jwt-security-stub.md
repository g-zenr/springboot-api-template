# ADR 0003: JWT security stub (demo in-memory users)

## Status

Accepted

## Context

A complete API template should demonstrate authentication without becoming a full IAM product.

## Decision

- Use **Spring Security** + **stateless JWT** (jjwt).
- Demo **in-memory** users: `admin/admin123`, `user/user123`.
- Protect `/api/**` except `POST /api/v1/auth/login`.
- Leave actuator health/info, OpenAPI (local), and H2 console public.
- Configure secret via `app.security.jwt.secret` / `JWT_SECRET` (min 32 chars).

## Consequences

- Callers must login before using sample Item API.
- Not a production identity system — replace with real user store when specializing.
- Tests use `AuthTestSupport` for bearer tokens.
