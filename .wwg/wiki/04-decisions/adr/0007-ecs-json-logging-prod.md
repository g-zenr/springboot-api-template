# ADR 0007: ECS-style JSON logging on prod

## Status

Accepted

## Decision

- Profile **`local`/`postgres`**: plain console pattern with correlation id
- Profile **`prod`**: Logstash composite JSON encoder with ECS field names (`@timestamp`, `log.level`, `service.name`, `ecs.version`, MDC `correlationId`)

## Consequences

- Log aggregation friendly in prod without changing local DX
