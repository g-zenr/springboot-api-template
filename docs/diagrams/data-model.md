# Diagram: Data model

> **Living diagram** — update with every Flyway migration.

```mermaid
erDiagram
  APP_USERS ||--o{ REFRESH_TOKENS : has
  APP_USERS ||--o{ NOTES : owns

  APP_USERS {
    uuid id PK
    string username UK
    string password_hash
    string roles
    boolean enabled
    timestamptz created_at
    timestamptz updated_at
  }

  REFRESH_TOKENS {
    uuid id PK
    uuid user_id FK
    string token_hash UK
    timestamptz expires_at
    timestamptz revoked_at
    timestamptz created_at
  }

  ITEMS {
    uuid id PK
    string name
    string description
    timestamptz created_at
    timestamptz updated_at
    timestamptz deleted_at
  }

  NOTES {
    uuid id PK
    string owner_username
    string title
    string body
    timestamptz created_at
    timestamptz updated_at
  }
```

## Migrations

| Version | File |
|---------|------|
| V1 | `V1__create_items.sql` |
| V2 | `V2__create_users_and_refresh_tokens.sql` |
| V3 | `V3__create_notes.sql` |
