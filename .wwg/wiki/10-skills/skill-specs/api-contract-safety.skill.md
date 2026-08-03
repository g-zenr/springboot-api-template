# API Contract Safety

## When to use

Any change to public HTTP paths, status codes, request/response JSON, ProblemDetail shape, or OpenAPI.

## Steps

1. Read `.wwg/wiki/05-architecture/api-map.md` and `error-contract.md`
2. If the change touches **list GET** params (`page`, `limit`, `sort`, `search`, `select`, `include`) or list response shapes: also read and update **`list-query-contract.md`**
3. Preserve `/api/v1` versioning conventions unless an ADR changes versioning
4. Keep controllers returning DTOs / ProblemDetail — not entities for writes (list may return `PageResponse` or `CountResponse`)
5. Update OpenAPI annotations when user-visible contract changes
6. Add/adjust MockMvc tests for new statuses and validation
7. Update `api-map.md` (and `list-query-contract.md` when list query changes) in the same task
8. Mirror human docs: `docs/LIST-QUERY.md` / `docs/API-REFERENCE.md` when list contract changes

## Done when

- Documented map matches code  
- List-query WWG truth matches code when list contract changed  
- Tests cover happy path + key error codes  
- No silent breaking change without ADR / truth update  
