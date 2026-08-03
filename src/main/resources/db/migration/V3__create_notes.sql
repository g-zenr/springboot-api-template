CREATE TABLE notes (
    id              UUID         NOT NULL PRIMARY KEY,
    owner_username  VARCHAR(80)  NOT NULL,
    title           VARCHAR(200) NOT NULL,
    body            VARCHAR(4000),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_notes_owner ON notes (owner_username);
