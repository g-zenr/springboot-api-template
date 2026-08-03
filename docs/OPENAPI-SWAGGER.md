# OpenAPI / Swagger Maintenance (Mandatory)

Swagger UI (`/swagger-ui.html`) and `/api-docs` are **generated from code annotations** (springdoc + OpenAPI 3).  
They are a **first-class living contract**. Stale Swagger = incomplete work.

## Surfaces

| Surface | Path / location | Owner |
|---------|-----------------|--------|
| Swagger UI | `/swagger-ui.html` (local; off in prod) | springdoc from annotations |
| OpenAPI JSON | `/api-docs` | springdoc |
| Global config | `OpenApiConfig.java` | Title, version, bearer scheme, description |
| Endpoint docs | `@Tag`, `@Operation`, `@ApiResponse`, `@SecurityRequirements` on controllers | Every public controller method |
| Schema docs | `@Schema` on request/response records | Every public DTO |

## Rules (non-negotiable)

1. **Every** new or changed REST endpoint must update OpenAPI annotations **in the same PR/task**.
2. Controllers **must** have:
   - Class-level `@Tag(name, description)`
   - Method-level `@Operation(summary = "...")` (description recommended for non-obvious ops)
   - `@ApiResponse` for success **and** important error statuses (400/401/403/404/409/429 as applicable)
3. **Public** endpoints (login, register, refresh, logout, health) must declare **empty security** so Swagger does not imply Bearer is required:
   - `@SecurityRequirements` (empty) **or** `@Operation(security = {})`
4. **Protected** endpoints rely on global bearer scheme; document `401` when unauthenticated matters.
5. **ADMIN-only** endpoints must document `403` and mention admin role in `@Operation` description.
6. Request/response bodies used in the public API must have `@Schema` on the type or fields (at least type-level description + required fields).
7. When API version or product description changes, update `OpenApiConfig` `info.version` / `info.description`.
8. After API changes, verify Swagger:
   - Start app (local) → open Swagger UI → confirm path, method, body, responses
   - Or automated: `OpenApiContractTest` must pass (paths present in `/api-docs`)

## Change → Swagger checklist

| Change | Swagger actions |
|--------|-----------------|
| Add endpoint | `@Tag` / `@Operation` / `@ApiResponse` / security requirement |
| Change path or method | Update annotations; ensure OpenAPI JSON shows new path |
| Change request/response fields | Update `@Schema` on DTOs; refresh examples if any |
| Change status codes | Update `@ApiResponse` list |
| Add feature package | New `@Tag`; all methods annotated; contract test paths extended |
| Change auth model | Update `OpenApiConfig` security scheme + public/protected annotations |
| Bump API narrative version | `OpenApiConfig` info.version + CHANGELOG |

## Companion docs (also mandatory)

Swagger alone is not enough. Same task must still update:

- `docs/API-REFERENCE.md`
- `docs/diagrams/api-surface.md` (if surface changed)
- `.wwg/wiki/05-architecture/api-map.md`

## Enforcement

- `AGENTS.md` → Mandatory OpenAPI / Swagger Sync  
- `docs/DOCUMENTATION-MAINTENANCE.md`  
- Skill: `openapi-swagger-sync`  
- Quality gate + drift-guard  
- Automated: `OpenApiContractTest` (fails if expected paths missing from `/api-docs`)

## Done when

- [ ] Annotations complete for every touched endpoint  
- [ ] DTOs have `@Schema` where public  
- [ ] Public endpoints not incorrectly marked as requiring Bearer  
- [ ] `OpenApiContractTest` green  
- [ ] Swagger UI manual spot-check for the changed path (or note if UI N/A in CI)  
