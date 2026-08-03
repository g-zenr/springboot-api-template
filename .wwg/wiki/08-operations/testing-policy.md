# Testing Policy (Mandatory)

This policy is **accepted truth** for the Spring Boot API template. Agents and humans must follow it for every code change that adds or modifies behavior.

## Minimum counts

| Change type | Minimum automated tests | What counts |
|-------------|-------------------------|-------------|
| **New feature** (new `feature.*` package, new public resource, or new major capability) | **≥ 10** distinct `@Test` methods for that feature | Prefer API/MockMvc or integration tests; unit tests may contribute if they prove real behavior |
| **New function / method / endpoint** (new service method, controller action, or public use-case) | **≥ 5** distinct `@Test` methods covering that function | Happy path + validation/errors + edge cases as applicable |
| **Update / bugfix / behavior change** | **Update existing tests** that assert the old behavior; add tests if coverage falls below the minima above | Never ship a behavior change with only production code updated |

## Counting rules

1. **One `@Test` method = one test case** for counting toward 10 / 5.
2. Multiple assertions inside one `@Test` still count as **one** test case.
3. A mega-test that walks create→list→update→delete does **not** replace ten focused cases (it may exist as an extra integration flow).
4. Context-load-only tests (`contextLoads`) do **not** count toward feature/function minima.
5. Tests must be **meaningful**: assert status, body fields, error titles, side effects, or invariants—not empty methods.
6. Prefer isolation: `@Transactional` on web/integration tests or explicit cleanup so tests do not pollute each other.

## Required coverage dimensions (feature ≥ 10)

When adding a new feature, distribute the ≥ 10 cases across dimensions as applicable:

| Dimension | Examples |
|-----------|----------|
| Happy path create | 201 + body shape |
| Happy path read list | 200 + collection |
| Happy path read one | 200 + fields |
| Happy path update | 200 + changed fields |
| Happy path delete | 204 + subsequent 404 |
| Validation failure | 400 ProblemDetail |
| Not found | 404 |
| Conflict / duplicate | 409 when relevant |
| Boundary input | max length, optional null fields |
| Negative / authz (when security exists) | 401/403 |

If a dimension does not apply, replace with another real risk for that feature—do not invent dummy tests.

## Required coverage dimensions (new function ≥ 5)

| # | Typical set |
|---|-------------|
| 1 | Success path |
| 2 | Invalid input / validation |
| 3 | Not found or missing dependency |
| 4 | Conflict / forbidden / business rule fail |
| 5 | Edge case (null optional, boundary, idempotent delete, etc.) |

## Update rule

When production code is updated:

1. Identify tests that encode previous behavior.
2. **Update** those tests to the new accepted behavior (or delete only if the behavior was removed and no longer required).
3. Ensure the feature still has **≥ 10** cases and each changed public function still has **≥ 5** cases touching it.
4. Re-run `.\mvnw.cmd test` after the final edit.

## Handoff gate

Work that adds a feature or function is **incomplete** if:

- Feature has fewer than 10 tests, or
- New function has fewer than 5 tests, or
- Behavior was updated but tests still assert the old contract, or
- `.\mvnw.cmd test` fails

Record test counts in the Intent Compliance Table / validation evidence (e.g. “Item feature: 12 @Test methods”).

## Sample baseline

The sample `feature.item` suite must itself satisfy the feature minimum (≥ 10) so the template demonstrates the standard.
