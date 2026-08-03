# System Overview

## Shape

```text
HTTP
  → CorrelationIdFilter (X-Correlation-Id + MDC)
  → SecurityFilterChain (JWT)
  → Controller → Service → Repository → DB (Flyway)
```

## Major components

| Component | Role |
|-----------|------|
| `feature.auth` | Login + JWT issue |
| `feature.item` | Sample CRUD |
| `config.security` | SecurityConfig, JwtService, filter |
| `common.web.CorrelationIdFilter` | Request correlation |
| `common.error` | ProblemDetail + auth failure mapping |
| `config.CorsConfig` | CORS |
| Actuator / OpenAPI | Ops + docs (profile-dependent) |

## Demo credentials

- `admin` / `admin123`  
- `user` / `user123`  
