# Diagram: Auth flow

> **Living diagram** — update when login/refresh/register/user APIs change.

```mermaid
sequenceDiagram
  participant C as Client
  participant A as Auth API
  participant U as AppUser DB
  participant R as RefreshToken DB
  participant J as JwtService

  Note over C,J: Register (optional)
  C->>A: POST /auth/register
  A->>U: insert USER
  A-->>C: 201 UserResponse

  Note over C,J: Login
  C->>A: POST /auth/login
  A->>U: verify password
  A->>J: access JWT
  A->>R: store SHA-256 refresh
  A-->>C: accessToken + refreshToken

  Note over C,J: API call
  C->>A: GET /api/... Bearer access
  A-->>C: 200 or 401

  Note over C,J: Refresh (rotate)
  C->>A: POST /auth/refresh
  A->>R: validate + revoke old
  A->>J: new access
  A->>R: store new refresh
  A-->>C: new pair

  Note over C,J: Logout
  C->>A: POST /auth/logout
  A->>R: revoke refresh
  A-->>C: 204
```

## Demo vs product

| Mode | Users |
|------|-------|
| Local seed on | admin/user seeded |
| Register on | Self-service USER |
| Admin | POST /users with roles |
| Prod default | seed off, register off — provision deliberately |
