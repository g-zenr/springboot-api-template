# Open Questions

## Product / specialization (open)

- When specializing: JWT vs session auth? multi-tenant model?
- Prefer Gradle later? (Maven is current baseline)
- Add Testcontainers PostgreSQL for CI parity?

## Resolved (complete-template pass)

| ID | Resolution |
|----|------------|
| Q1 | Default profile is `local` with H2 console; `prod` disables it |
| Q3 | GitHub Actions CI added |
| Q5 | MIT LICENSE added |

## Still open

| ID | Question / decision needed | Blocking complete template? |
|----|----------------------------|------------------------------|
| Q2 | Preferred auth starter for first product fork? | No — product specialization |
| Q4 | Align unique `items.name` as case-insensitive at DB layer? | No — edge case |
| Q6 | Pin Spring Boot 4.0.0 or track latest patch line? | No — maintenance |

Resolved items should move to ADRs or project-truth and be removed from this list.
