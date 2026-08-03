# Security Model (v0.4)

| Concern | Behavior |
|---------|----------|
| Users | Persisted `app_users` |
| Demo seed | `app.security.seed-demo-users` (default true local; **false prod**) |
| Registration | `app.security.allow-registration` (true local; **false prod**) |
| JWT secret | Fail-fast if default secret outside local; min 32 chars |
| Admin APIs | `@PreAuthorize("hasRole('ADMIN')")` on user list/create |
| Notes | Owner-scoped by username |

Demo credentials (when seed enabled): `admin/admin123`, `user/user123`.
