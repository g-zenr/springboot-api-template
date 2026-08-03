# Documentation Sync

## When to use

**Always** on `PLAN_AND_APPLY` / `REPAIR_AND_VALIDATE` that changes endpoints, schema, auth, packages, ops, list query, or agent workflow — **same task as code**.

## Force order (do not skip WWG)

1. Read `docs/DOCUMENTATION-MAINTENANCE.md`
2. Read `.wwg/wiki/12-maintenance/context-maintenance-matrix.md`
3. Read **Mandatory WWG Truth Sync** in root `AGENTS.md`
4. List every **WWG** surface affected (wiki paths first)
5. **Write `.wwg/wiki/**` and `workspace/current-task.md` first** (agent truth)
6. Update `docs/**` and `docs/diagrams/**` as human mirrors
7. If API-related: also run **OpenAPI / Swagger Sync** skill
8. If list query / `include` / `select` / paging: **both**
   - `.wwg/wiki/05-architecture/list-query-contract.md`
   - `docs/LIST-QUERY.md`
9. Touch `CHANGELOG.md` / `README.md` if user-facing
10. Confirm no diagram or wiki contradicts code

## Done when

- [ ] Matching **`.wwg/wiki/**` files written** (listed in handoff)
- [ ] `workspace/current-task.md` updated
- [ ] Maintenance matrix rows satisfied
- [ ] Human `docs/**` + diagrams aligned
- [ ] Swagger + contract test when API changed
- [ ] No “docs only, WWG later”

**Fail handoff** if WWG was required and not updated.
