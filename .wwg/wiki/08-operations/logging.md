# Logging

| Profile | Format |
|---------|--------|
| `local`, `postgres` | Text: timestamp level `[correlationId]` logger message |
| `prod` | **ECS-style JSON** via logstash-logback-encoder |

Fields in prod JSON include: `@timestamp`, `log.level`, `log.logger`, `message`, `service.name`, `ecs.version`, MDC `correlationId`, stack traces as `error.stack_trace`.

Unhandled exceptions: ERROR + stack in `ApiExceptionHandler`.
