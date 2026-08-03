# Handoff: arsy786 checklist + soft-delete + cache + SpotBugs

**Date:** 2026-08-03  
**Mode:** PLAN_AND_APPLY  
**Validation:** `spotless:apply` + `compile spotbugs:check` + `test -Dsurefire.excludes=**/*IT.java` → **BUILD SUCCESS** (185 tests, SpotBugs 0)

## Work completed

1. Soft-delete sample on **items** (V4, Hibernate filter, service soft-delete)
2. In-process **cache** on item `findById` with eviction on writes
3. **SpotBugs** Maven plugin + CI gate; SonarLint documented as IDE optional
4. Full **arsy786** practice map in WWG best-practices doc (intentional divergences: no Lombok, no APIResponse envelope)

## Key paths

- `src/main/resources/db/migration/V4__items_soft_delete.sql`
- `feature/item/Item.java`, `ItemService.java`, `ItemController.java`
- `config/CacheConfig.java`
- `pom.xml`, `spotbugs-exclude.xml`, `.github/workflows/ci.yml`
- `.wwg/wiki/principles/spring-boot-best-practices.md`

## Intent compliance (high level)

| Req | Status |
|-----|--------|
| Docs map arsy786 | MET |
| Soft delete | MET (items sample) |
| Caching sample | MET |
| SpotBugs/Sonar path | MET (SpotBugs CI; SonarLint IDE) |
| Tests minima | MET (new soft-delete/cache tests ≥5 each area) |
| Docs/WWG sync | MET |

## Delivery

- Git: **not committed** (NO_GIT_WRITES default unless user authorizes)
