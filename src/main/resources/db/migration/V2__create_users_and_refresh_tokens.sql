CREATE TABLE app_users (
    id              UUID         NOT NULL PRIMARY KEY,
    username        VARCHAR(80)  NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    roles           VARCHAR(200) NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_app_users_username ON app_users (username);

CREATE TABLE refresh_tokens (
    id              UUID         NOT NULL PRIMARY KEY,
    user_id         UUID         NOT NULL,
    token_hash      VARCHAR(64)  NOT NULL,
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at      TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES app_users (id)
);

CREATE UNIQUE INDEX uk_refresh_tokens_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
