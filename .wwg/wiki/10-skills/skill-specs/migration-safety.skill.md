# Migration Safety

## When to use

Any database schema change, new table/column/index, or entity field mapping change.

## Steps

1. Add a new Flyway version `V{n}__*.sql` — never rewrite applied migrations in shared history  
2. Align JPA entity with the migration (`ddl-auto` remains `validate`)  
3. Prefer UUID PKs and explicit nullability  
4. Consider Postgres vs H2 dialect differences (template uses H2 PostgreSQL mode)  
5. Run `.\mvnw.cmd test`  
6. Update `.wwg/wiki/05-architecture/data-model.md`  
7. Record data strategy in Task Contract (`PRESERVE_AND_MIGRATE` vs authorized reset)

## Done when

- App starts with validate  
- Tests pass  
- Data model wiki matches schema  
- No use of `ddl-auto=update`  
