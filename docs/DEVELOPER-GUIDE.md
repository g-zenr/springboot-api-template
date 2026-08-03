# Developer Guide

**New here?** Read [WHAT-IS-THIS.md](WHAT-IS-THIS.md) and [FOLDER-STRUCTURE.md](FOLDER-STRUCTURE.md) first.

## Add a new feature package

1. Create `com.template.api.feature.<name>/`
2. Add Flyway `V{n}__....sql` if schema changes
3. Entity → Repository (`JpaRepository` + `JpaSpecificationExecutor` if listing) → Service → Controller (+ MapStruct mapper if useful)
4. For list GET: reuse `PageQuery` / `PageResponse` / `Pageables` from `common.paging` (do not invent a one-off paging DTO)
5. Secure by default (JWT already required for `/api/**` except auth public routes)
5. **Tests (mandatory):**
   - New feature → **≥ 10** `@Test` methods  
   - New function → **≥ 5** `@Test` methods  
   - Updates → update existing tests  
   - Extend `OpenApiContractTest` for new paths  
   Policy: `.wwg/wiki/08-operations/testing-policy.md`
6. **Swagger / OpenAPI (mandatory)** — same task:
   - `@Tag`, `@Operation`, `@ApiResponse` on every endpoint  
   - `@Schema` on public DTOs  
   - Public routes: empty security requirements  
   - Policy: [OPENAPI-SWAGGER.md](OPENAPI-SWAGGER.md)
7. **Documentation (mandatory)** — same task:
   - `docs/API-REFERENCE.md`
   - `docs/diagrams/*` affected
   - `.wwg/wiki/05-architecture/api-map.md` (+ data-model if schema)
   - `CHANGELOG.md` / `README.md` if user-facing  
   Policy: [DOCUMENTATION-MAINTENANCE.md](DOCUMENTATION-MAINTENANCE.md)

## Run quality gates

```powershell
.\mvnw.cmd spotless:apply
.\mvnw.cmd spotless:check
.\mvnw.cmd compile spotbugs:check
.\mvnw.cmd test
```

Optional IDE: install **SonarLint** for live findings (CI uses SpotBugs, not a SonarQube server).

## Agent entry

Read `AGENTS.md` first (Meta-Prompt v2 + testing + **documentation sync**).

## Feature examples in this repo

| Package | Pattern |
|---------|---------|
| `feature.item` | Shared resource CRUD + MapStruct + paged list + **soft-delete** + **@Cacheable** sample |
| `feature.note` | **Owner-scoped** CRUD + paged list |
| `feature.user` | Identity APIs + admin paged list |
| `feature.auth` | Login / refresh / logout / register |
| `common.paging` | Reusable `PageQuery` / `PageResponse` / `Pageables` / `PagedReads` |
| `common.select` | Sparse + deep `select` (`FieldProjector`, `FieldSelection`) |
| `common.include` | Optional extras (`IncludeOptions`, `IncludeKey`) — `count`, `countOnly`, … |

**List query deep-dive:** [LIST-QUERY.md](LIST-QUERY.md)  
**Agent truth (WWG):** `.wwg/wiki/05-architecture/list-query-contract.md`  
**Best practices map (12 + arsy786 checklist):** `.wwg/wiki/principles/spring-boot-best-practices.md`

### Soft delete (copy from items)

1. Flyway: `deleted_at` + partial unique index for active natural keys.
2. Entity: `@SQLRestriction("deleted_at IS NULL")` + `softDelete()`.
3. Service DELETE: set timestamp, `saveAndFlush`, `entityManager.detach` (L1 safety).
4. Do not expose `deleted_at` on public DTOs unless product needs it.

### Caching (copy from items)

1. `spring-boot-starter-cache` + `CacheConfig` (`app.cache.enabled`).
2. `@Cacheable` on hot single-id reads; `@CacheEvict` on writes.
3. Use Spring proxy / `ObjectProvider` for self-invocation if one method calls another.

## Add a paged list to a new feature

1. Put search logic in a small `*Specs` class (`Specification`).
2. Extend the repository with `JpaSpecificationExecutor<Entity>`.
3. In the service:

```java
Pageable pageable = Pageables.toPageable(query, Set.of("name", "createdAt"), Sort.by("name"));
Specification<MyEntity> spec = query.normalizedSearch()
    .map(MySpecs::matchesSearch)
    .orElseGet(Specs::unrestricted);
if (query.includes().wantsCountOnly()) {
  return pagedReads.countOnly(MyEntity.class, spec); // { "count": N }
}
Pageable pageable = Pageables.toPageable(query, Set.of("name", "createdAt"), Sort.by("name"));
PageResponse<MyResponse> page = pagedReads.find(MyEntity.class, spec, pageable, query, mapper::toResponse);
return FieldSelection.apply(page, query, Set.of("id", "name", "createdAt"));
```

4. Optional extras: `query.includes().has(IncludeKey.COUNT)` / `wantsCountOnly()` (or new keys). Never parse `include` in controllers.
5. For nested populate (deep select): build an enriched map with nested objects, then `FieldSelection.applyEnriched(...)`.
6. In the controller: `list(@ParameterObject PageQuery query)` → `PageResponse<?>`.
7. Document sort/search/**select**/**include** on `@Operation` and in `docs/API-REFERENCE.md`.

## Authorization (RBAC / ABAC switch)

```yaml
app.security.authorization.mode: rbac   # or abac
# env: AUTHZ_MODE=rbac|abac
```

Use the shared facade (do not hardcode only `hasRole` for new admin checks):

```java
@PreAuthorize("@authz.allow(authentication, 'user:list')")
// service:
authorizationService.require(auth, AuthzActions.NOTE_READ, note);
```

Add actions in `AuthzActions`; extend `RbacEvaluator` and/or `AbacEvaluator` together.  
WWG: `.wwg/wiki/05-architecture/security-model.md`.

## Anti AI-slop (keep code clean)

**WWG:** `.wwg/wiki/principles/anti-ai-slop.md` · **AGENTS.md** section of the same name.

- Match sibling `feature.item` / `feature.note` style; reuse `common.*`  
- Smallest complete change; no drive-by refactors  
- No noise comments / abstraction theater / junk dependencies  
- Handoff fails if slop scan fails (even when tests pass)  

## Spring Boot best practices (guardrails)

**WWG:** `.wwg/wiki/principles/spring-boot-best-practices.md`  
Inspired by common 2025 speed/stability practice lists (e.g. [Plain English / Medium article](https://javascript.plainenglish.io/12-spring-boot-best-practices-you-cant-ignore-in-2025-if-you-care-about-speed-stability-892f383804dd)).

Must follow: DTOs not entities, thin controllers, constructor DI, global exception handler, secure Actuator, profiles, Flyway, starters, test minima.

## Do not

- Use `ddl-auto=update`
- Introduce Prisma
- Ship endpoints without tests + docs + **Swagger annotations**
- Leave Swagger showing wrong auth (e.g. Bearer required on `/auth/login`)
- Claim production readiness with default JWT secret or demo seed on
- Ship inconsistent “AI slop” next to clean template code
