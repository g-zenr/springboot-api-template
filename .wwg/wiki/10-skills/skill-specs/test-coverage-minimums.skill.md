# Test Coverage Minimums

## When to use

Any implementation that adds a feature, adds a function/endpoint, or updates behavior.

## Steps

1. Read `.wwg/wiki/08-operations/testing-policy.md`
2. Classify the change: **new feature** / **new function** / **update**
3. Plan test cases before or with implementation:
   - New feature → list ≥ 10 `@Test` methods
   - New function → list ≥ 5 `@Test` methods for that function
   - Update → name tests to change and gaps to fill
4. Implement tests under `src/test/java` mirroring feature packages
5. Prefer `@SpringBootTest` + `@AutoConfigureMockMvc` for HTTP; add unit tests for pure logic when useful
6. Use `@Transactional` on integration tests (or cleanup) to avoid cross-test pollution
7. Run `.\mvnw.cmd test`
8. Report counts in handoff

## Done when

- Minima met (10 / 5 / updated)
- All tests pass
- No obsolete assertions left after behavior change
