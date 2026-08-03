# Data Model

## items (V1) — sample catalog
## app_users + refresh_tokens (V2) — identity
## notes (V3) — owner-scoped domain sample

| notes column | Type |
|--------------|------|
| id | UUID PK |
| owner_username | VARCHAR(80) indexed |
| title | VARCHAR(200) |
| body | VARCHAR(4000) optional |
| created_at / updated_at | TIMESTAMPTZ |
