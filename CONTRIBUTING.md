# Contributing

## Agent / human workflow

1. Read `AGENTS.md` (includes Meta-Prompt Template v2).
2. Follow `.wwg/RESOLVER.md` and project truth.
3. Use feature packages: `com.template.api.feature.<name>`.
4. Schema only via Flyway; JPA `ddl-auto=validate`.
5. **Testing policy** (mandatory):
   - New **feature** → ≥ **10** `@Test` methods
   - New **function** → ≥ **5** `@Test` methods
   - **Updates** → update related tests
6. Run `./mvnw test` (Windows: `.\mvnw.cmd test`) before handoff.
7. Update WWG surfaces when accepted truth or contracts change.

## Profiles

| Profile | Use |
|---------|-----|
| `local` (default) | H2 in-memory, H2 console, OpenAPI on |
| `postgres` | PostgreSQL via `DB_*` env |
| `prod` | PostgreSQL + `JWT_SECRET` required; OpenAPI off; no H2 console |

## Auth (demo)

- Login: `POST /api/v1/auth/login` with `admin/admin123` or `user/user123`
- Protected APIs need `Authorization: Bearer <token>`

## Formatting

```powershell
.\mvnw.cmd spotless:apply
.\mvnw.cmd spotless:check
```

## Code quality (anti AI-slop)

Keep changes small and consistent with existing `feature.*` / `common.*` code.  
Full rules: `.wwg/wiki/principles/anti-ai-slop.md` and `AGENTS.md` → Anti AI-Slop.

- No drive-by refactors or unused abstractions  
- No comments that restate the code / AI banners  
- Prefer extending existing helpers over new parallel patterns  
- Meaningful tests only; Spotless for format  

## Do not

- Introduce Prisma
- Use `ddl-auto=update`
- Commit secrets or `.env`
- Ship “slop” that ignores house style
- Claim production readiness without security specialization
