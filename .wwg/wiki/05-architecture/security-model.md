# Security Model

## Authentication

| Concern | Behavior |
|---------|----------|
| Mechanism | JWT Bearer (stateless) |
| Users | Persisted `app_users` |
| Demo seed | `app.security.seed-demo-users` (default true local; **false prod**) |
| Registration | `app.security.allow-registration` (true local; **false prod**) |
| JWT secret | Fail-fast if default secret outside local; min 32 chars |

Demo credentials (when seed enabled): `admin/admin123`, `user/user123`.

---

## Authorization: RBAC **or** ABAC (config switch)

Both implementations ship. Select with:

```yaml
app:
  security:
    authorization:
      mode: rbac   # default
      # mode: abac
```

Env: `AUTHZ_MODE=rbac` | `AUTHZ_MODE=abac`

| Mode | Implementation | Entry point |
|------|----------------|-------------|
| **RBAC** | Role checks (`ROLE_ADMIN`, owner match for notes) | `RbacEvaluator` |
| **ABAC** | Subject/action/resource attribute policies | `AbacEvaluator` |

**Facade:** `AuthorizationService` bean name `authz`  

```java
@PreAuthorize("@authz.allow(authentication, 'user:list')")
// or with resource:
authorizationService.allow(auth, "note:read", note);
```

**Actions:** `AuthzActions` (`user:list`, `user:create`, `item:*`, `note:*`).

### Default policy parity (safe switch)

| Action | RBAC | ABAC (default policies) |
|--------|------|-------------------------|
| `user:list` / `user:create` | ADMIN role | subject has role ADMIN |
| `item:*` | Authenticated | Authenticated |
| `note:list` / `note:create` | Authenticated | Authenticated |
| `note:read|update|delete` | Owner only | Owner **or** ADMIN |

Unauthorized note access returns **404** (no existence leak) in both modes.

### Extending ABAC

Edit `AbacEvaluator.evaluatePolicies` (or later externalize policies) — do not fork RBAC paths.  
Keep actions in `AuthzActions`. Controllers stay on `@authz.allow(...)`.

Code package: `com.template.api.config.security.authz`.

---

## HTTP security (both modes)

- Public: auth login/refresh/logout/register, actuator **health/info**, Swagger (non-prod), H2 console (local)
- Authenticated: `/actuator/metrics`, `/actuator/prometheus`, other `/api/**`
- Method security enabled (`@EnableMethodSecurity`)
