CREATE TABLE items (
    id              UUID         NOT NULL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    description     VARCHAR(500),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_items_name ON items (name);
