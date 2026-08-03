# OpenAPI / Swagger Sync

## When to use

Any change to REST controllers, DTOs, auth, status codes, or public API shape.

## Steps

1. Read `docs/OPENAPI-SWAGGER.md`
2. For each touched endpoint:
   - Ensure `@Tag` on controller
   - Ensure `@Operation(summary=...)`
   - Add/update `@ApiResponse` for success + key errors
   - Public routes: empty `@SecurityRequirements`
   - Admin routes: document 403 + role in description
3. Update `@Schema` on public request/response types
4. Update `OpenApiConfig` if title/version/description/security scheme changed
5. Extend `OpenApiContractTest` with new paths
6. Update `docs/API-REFERENCE.md` + `api-map.md` + `api-surface` diagram
7. Run tests including `OpenApiContractTest`

## Done when

- Swagger annotations match runtime behavior  
- Contract test passes  
- Companion human docs updated  
