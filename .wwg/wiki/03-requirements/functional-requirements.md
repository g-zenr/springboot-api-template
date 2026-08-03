# Functional Requirements (Template Baseline)

These are **template** functional requirements, not a business product.

| ID | Requirement | Acceptance evidence |
|----|-------------|---------------------|
| FR-1 | Application context starts with default H2 configuration | `ApiTemplateApplicationTests` / `mvn test` |
| FR-2 | Flyway applies schema on startup | Logs / successful JPA validate |
| FR-3 | Create item via `POST /api/v1/items` with valid body returns 201 + body | `ItemControllerTest` |
| FR-4 | List items via `GET /api/v1/items` | `ItemControllerTest` |
| FR-5 | Get item by id; missing id returns 404 ProblemDetail | `ItemControllerTest` |
| FR-6 | Update item via `PUT /api/v1/items/{id}` | `ItemControllerTest` |
| FR-7 | Delete item returns 204; subsequent get 404 | `ItemControllerTest` |
| FR-8 | Invalid/blank name returns 400 Validation Failed | `ItemControllerTest` |
| FR-9 | Duplicate name returns 409 Conflict | `ItemControllerTest` |
| FR-10 | OpenAPI UI and `/api-docs` available in default profile | Manual / springdoc auto-config |
| FR-11 | Actuator health available | Manual / actuator config |
| FR-12 | Agents operate under Meta-Prompt v2 via `AGENTS.md` | Process / AGENTS.md |
| FR-13 | New features ship with ≥ 10 automated `@Test` methods | Feature test class count |
| FR-14 | New functions/endpoints ship with ≥ 5 automated tests covering them | Test methods mapped to function |
| FR-15 | Behavior updates refresh related tests; suite stays green | Diff + `mvn test` |
| FR-16 | Login issues JWT for valid demo credentials | AuthControllerTest |
| FR-17 | Invalid login returns 401 | AuthControllerTest |
| FR-18 | Protected APIs reject missing/invalid Bearer | AuthControllerTest |
| FR-19 | Correlation id generated or echoed on responses | CorrelationIdFilterTest |

Out of scope for FR baseline: multi-tenancy, external IdP, pagination, file upload, messaging.
