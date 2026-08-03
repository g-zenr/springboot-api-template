# ADR 0008: In-memory rate limiting

## Status

Accepted

## Decision

- Single-node sliding-window limiter (`RateLimitService` + `RateLimitFilter`)
- Separate limits for login vs `/api/**`
- Configurable via `app.rate-limit.*`
- Local profile uses high limits so tests do not trip 429

## Consequences

- Not distributed; replace with Redis/gateway limits for multi-instance production
