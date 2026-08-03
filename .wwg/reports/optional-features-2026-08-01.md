# Optional features completion — 2026-08-01 (v0.3)

## Delivered

| Feature | Implementation |
|---------|----------------|
| MapStruct | `ItemMapper` + ItemService; unit + Spring tests |
| Persisted users | Flyway V2 `app_users`, `DatabaseUserDetailsService`, `DemoUserSeeder` |
| Refresh tokens | Issue/rotate/revoke; `/auth/refresh`, `/auth/logout` |
| ECS JSON logging | `logback-spring.xml` prod profile |
| Rate limiting | `RateLimitFilter` + `RateLimitService` |

## Validation

`.\mvnw.cmd spotless:apply test` → **PASS**

## Version

`0.3.0-SNAPSHOT`
