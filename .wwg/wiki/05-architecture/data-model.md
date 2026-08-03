# Data Model

## items (V1 + V4 soft delete) — sample catalog

| Column | Type | Notes |
|--------|------|--------|
| id | UUID PK | |
| name | VARCHAR(120) | Active uniqueness via service (`existsByNameIgnoreCase` + `@SQLRestriction`); index `idx_items_name` |
| description | VARCHAR(500) | Optional |
| created_at / updated_at | TIMESTAMPTZ | |
| deleted_at | TIMESTAMPTZ | Null = active; set = soft-deleted (V4). Hibernate `@SQLRestriction` hides deleted rows |

Public DELETE sets `deleted_at` (soft). Same name can be reused after soft-delete.  
Note: partial unique index is Postgres-only; not used so H2 `local` stays portable.

## app_users + refresh_tokens (V2) — identity
## notes (V3) — owner-scoped domain sample

| notes column | Type |
|--------------|------|
| id | UUID PK |
| owner_username | VARCHAR(80) indexed |
| title | VARCHAR(200) |
| body | VARCHAR(4000) optional |
| created_at / updated_at | TIMESTAMPTZ |
