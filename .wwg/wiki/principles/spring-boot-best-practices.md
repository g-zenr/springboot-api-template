# Spring Boot Best Practices Guardrails (Speed & Stability)

**Status:** ACCEPTED_TRUTH — mandatory for agents and contributors  
**Inspired by:** [12 Spring Boot Best Practices You Can’t Ignore in 2025 (speed & stability)](https://javascript.plainenglish.io/12-spring-boot-best-practices-you-cant-ignore-in-2025-if-you-care-about-speed-stability-892f383804dd)  
**Also see:** `anti-ai-slop.md`, feature packages (ADR 0002), `AGENTS.md`

These rules keep the template **fast, stable, and consistent**. Handoff fails if new code violates them without an explicit ADR.

---

## Practice map (12)

| # | Practice | This template | Agent rule |
|---|----------|---------------|------------|
| 1 | **API uses DTOs, not entities** | `*Request` / `*Response` records; entities stay in feature package | Never return JPA entities from controllers |
| 2 | **Database access is disciplined** | Flyway owns schema; `open-in-view: false`; list endpoints paginate via `PageQuery` | No `ddl-auto=update`; no unbounded `findAll` for public lists; no N+1 without reason |
| 3 | **Secure Actuators** | Only `health`, `info` exposed by default | Do not expose `env`, `beans`, `heapdump`, `metrics` publicly without auth + ADR |
| 4 | **DevTools never in prod** | Not on the classpath | Do not add `spring-boot-devtools` to main runtime for prod images |
| 5 | **Global exception handling** | `common.error.ApiExceptionHandler` + ProblemDetail | No try/catch in controllers for expected errors; use handler + domain exceptions |
| 6 | **Observe before guessing** | Actuator health/info; **metrics + Prometheus** (auth required); correlation id; ECS logs | Don’t expose metrics publicly; scrape `/actuator/prometheus` with a token |
| 7 | **Environment-specific config** | `application.yml` multi-doc profiles: `local` / `postgres` / `prod` | Prefer profile blocks or env vars; no secrets in repo |
| 8 | **Thin controllers** | Controller → Service → Repository | Controllers: map HTTP, `@Valid`, auth annotations only — business rules in services |
| 9 | **Fast tests when possible** | Slice samples: `ItemControllerWebMvcTest`, `ItemRepositoryDataJpaTest` + full `@SpringBootTest` contracts | Prefer slices for pure web/JPA units; keep full stack for security/auth contracts; never drop coverage minima |
| 10 | **Constructor injection** | Final fields + constructors everywhere | No `@Autowired` on fields; no field injection |
| 11 | **Async only when needed** | `AsyncConfig` optional via `app.async.enabled=false` (default) | Enable only for non-request-critical jobs; keep API path synchronous |
| 12 | **Use Boot starters** | security, validation, data-jpa, flyway, actuator, webmvc | Prefer official starters over hand-rolled stacks / random libs |

---

## Expanded rules (enforceable)

### 1. DTOs at the edge

- Controllers accept/return request/response types (or `PageResponse` / `CountResponse` / sparse maps).
- Entities (`Item`, `Note`, `AppUser`) are **not** public API bodies.

### 2. Stable data access

- Schema: Flyway only.
- Lists: shared paging (`common.paging`); default page size limits.
- Prefer existing repositories + specs; document new queries.

### 3. Actuator surface

Allowed public (typical): `/actuator/health`, `/actuator/info`.  
If exposing more, require security + update `security-model.md` and OPERATIONS.

### 4. DevTools

Forbidden in production packaging. Local-only optional dependency must not be required for CI.

### 5. Errors

Use `ResourceNotFoundException`, `ConflictException`, validation annotations, auth exceptions — all through `ApiExceptionHandler` → ProblemDetail.

### 6. Observability

Keep `X-Correlation-Id` / MDC. Prod JSON logs via existing logback profile.  
Public: `/actuator/health`, `/actuator/info`.  
Authenticated: `/actuator/metrics`, `/actuator/prometheus` (Micrometer Prometheus registry).

### 7. Profiles

- `local` — H2, demo-friendly  
- `postgres` — real DB  
- `prod` — strict secrets, no open Swagger, no demo seed defaults  

Never hardcode prod secrets in YAML.

### 8. Layering inside features

```text
feature.<name>/
  *Controller  → HTTP only
  *Service     → use-cases, @Transactional
  *Repository  → data access
```

### 9. Testing

- Minima: ≥10 tests/feature, ≥5/function (testing-policy).
- Full `@SpringBootTest` + MockMvc for auth/security contracts.
- Slice samples present: `@WebMvcTest` (ItemController), `@DataJpaTest` (ItemRepository).

### 10. DI style

Constructor injection only (Lombok `@RequiredArgsConstructor` is fine if the project adopts it later; today: explicit constructors).

### 11. Concurrency

Default path stays synchronous. Optional pool: `app.async.enabled=true` + `AsyncConfig`.

### 12. Starters & dependencies

Add libraries only when a starter/capability is insufficient (anti-ai-slop + this rule).

---

## Self-check (before handoff)

- [ ] No entities in controller signatures  
- [ ] Controllers stay thin  
- [ ] Constructor injection  
- [ ] Errors go through global handler  
- [ ] Actuator exposure not widened casually  
- [ ] Profile/secret hygiene  
- [ ] Lists paginated where public  
- [ ] Tests meet minima; Spotless clean  
- [ ] WWG/docs updated if contract/ops changed  

---

## Related guardrails

| Doc | Role |
|-----|------|
| `principles/anti-ai-slop.md` | Style, no noise, no drive-by refactors |
| `principles/README.md` | Principle index |
| `governance/quality-gates.md` | Build gates |
| `governance/drift-guard.md` | Drift flags |
| `08-operations/testing-policy.md` | Test counts |
| ADR 0002 | Feature-first packages |
