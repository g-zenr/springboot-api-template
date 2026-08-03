# ADR 0004: Correlation ID filter

## Status

Accepted

## Context

Distributed debugging needs a request id in logs and response headers.

## Decision

- `CorrelationIdFilter` reads or generates `X-Correlation-Id`.
- Puts value in MDC key `correlationId` for log pattern.
- Echoes header on every response (including 401).

## Consequences

- Console logs include `[%X{correlationId}]`.
- Clients can pass their own id for end-to-end tracing.
