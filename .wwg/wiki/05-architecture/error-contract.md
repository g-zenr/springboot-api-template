# Error Contract

## Format

HTTP errors from application handlers use Spring **`ProblemDetail`** (RFC 7807-style JSON).

Typical fields:

| Field | Usage |
|-------|--------|
| `type` | Currently `about:blank` (placeholder) |
| `title` | Short category string |
| `status` | HTTP status code |
| `detail` | Human-readable explanation |

## Status mapping (template)

| Situation | Status | Title (current) | Source |
|-----------|--------|-----------------|--------|
| Bean Validation failure | 400 | Validation Failed | `MethodArgumentNotValidException` |
| Missing resource | 404 | Resource Not Found | `ResourceNotFoundException` |
| Business conflict (e.g. duplicate name) | 409 | Conflict | `ConflictException` |
| Unhandled exception | 500 | Internal Server Error | catch-all (opaque detail) |

## Rules

1. Controllers stay thin; domain failures throw typed exceptions from services.  
2. Do not return JPA entities as error bodies.  
3. 500 `detail` must remain non-sensitive (no stack traces to clients).  
4. When extending: prefer stable `type` URIs per error class if clients will branch on them.  
5. OpenAPI should document error responses when specializing contracts.

## Known gap (candidate)

- Catch-all handler does not log the exception (audit **G6**).
