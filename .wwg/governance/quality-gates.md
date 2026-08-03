# Quality Gates

Before handoff of meaningful work:

1. `.\mvnw.cmd test` passes
2. **Testing policy minima** (see `.wwg/wiki/08-operations/testing-policy.md`):
   - New **feature** → **≥ 10** `@Test` methods for that feature
   - New **function** → **≥ 5** `@Test` methods covering that function
   - **Update** → existing tests updated; minima still met
3. Schema changes include Flyway migration
4. No secrets committed
5. **WWG force-update (apply/repair):** every touched truth surface under `.wwg/wiki/**` updated **in the same task** (not deferred). Minimum: matrix-mapped paths for the change type + `workspace/current-task.md`
6. Meta-Prompt v2 Task Contract used for meaningful tasks; **WWG Truth-Write Mode = `UPDATE_ACCEPTED_TRUTH_AUTHORIZED`** for apply/repair product changes
7. Do not claim production readiness unless governance **G-PROD-DEPLOY** is PASS with evidence
8. Public API changes update `05-architecture/api-map.md` + error contract if status mapping changes; list query changes also update `list-query-contract.md`
9. Handoff reports actual test counts for features/functions touched
10. **Documentation sync:** update `docs/**` + `docs/diagrams/**` + matching `.wwg` per `docs/DOCUMENTATION-MAINTENANCE.md` and **Mandatory WWG Truth Sync** in `AGENTS.md`
11. No stale diagrams (Mermaid must match current architecture/API)
12. **Swagger/OpenAPI sync:** annotations + `OpenApiConfig` + `OpenApiContractTest` per `docs/OPENAPI-SWAGGER.md`
13. Public endpoints must not incorrectly require Bearer in Swagger; admin endpoints document 403
14. Handoff **lists every `.wwg/**` path written**; if none and code changed accepted behavior → **gate FAIL**
15. **Anti AI-slop:** pass self-check in `.wwg/wiki/principles/anti-ai-slop.md` (style match, no noise comments, no drive-by refactors, no parallel abstractions, no useless deps). Green tests alone do **not** pass this gate.
