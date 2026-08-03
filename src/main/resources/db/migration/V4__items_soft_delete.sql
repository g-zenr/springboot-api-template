-- Soft delete for sample catalog (items). Active rows: deleted_at IS NULL.
-- Uniqueness among active names is enforced in the service layer (existsByNameIgnoreCase
-- + Hibernate @SQLRestriction). Partial unique indexes are Postgres-only and break H2 local.
-- Postgres production forks may add:
--   CREATE UNIQUE INDEX uk_items_name_active ON items (name) WHERE deleted_at IS NULL;

ALTER TABLE items ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

DROP INDEX IF EXISTS uk_items_name;

CREATE INDEX idx_items_name ON items (name);

CREATE INDEX idx_items_deleted_at ON items (deleted_at);
