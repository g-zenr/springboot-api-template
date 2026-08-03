# Diagram: Request pipeline

> **Living diagram** — update when filters, security, or error handling change.

```mermaid
flowchart LR
  Client[Client] --> Corr[CorrelationIdFilter<br/>X-Correlation-Id + MDC]
  Corr --> Rate[RateLimitFilter<br/>login /api limits]
  Rate -->|429| TooMany[429 Too Many Requests]
  Rate --> Sec[SecurityFilterChain]
  Sec -->|no/invalid JWT| Unauth[401 Unauthorized]
  Sec -->|authenticated| Ctrl[Controller]
  Ctrl --> Svc[Service @Transactional]
  Svc --> Repo[Repository]
  Repo --> DB[(Database<br/>Flyway schema)]
  Svc --> Map[MapStruct DTO]
  Map --> Ctrl
  Ctrl --> Client
  Svc -->|domain error| Err[ApiExceptionHandler<br/>ProblemDetail]
  Err --> Client
```

## Filter order (approx)

1. `CorrelationIdFilter` (highest precedence)  
2. `RateLimitFilter`  
3. Spring Security + `JwtAuthenticationFilter`  
4. DispatcherServlet → Controller  
