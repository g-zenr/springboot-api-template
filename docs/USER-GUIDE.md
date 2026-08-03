# User Guide

## Prerequisites

- JDK **21+**
- Windows: use `.\mvnw.cmd` (Maven Wrapper; no global Maven required)
- Optional: Docker Desktop for Postgres Compose / Testcontainers

## Start the API (local H2)

```powershell
cd C:\Users\Renz\SpringBoot\api
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Default profile: **`local`**

| URL | Purpose |
|-----|---------|
| http://localhost:8080/actuator/health | Health |
| http://localhost:8080/swagger-ui.html | Interactive API (Authorize with JWT) |
| http://localhost:8080/api-docs | OpenAPI JSON |

## First login (local demo seed)

When `app.security.seed-demo-users=true` (local default):

| User | Password | Roles |
|------|----------|-------|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{ "username": "admin", "password": "admin123" }
```

Save `accessToken` and `refreshToken` from the response.

## Call a protected API

```http
GET http://localhost:8080/api/v1/items
Authorization: Bearer <accessToken>
```

List endpoints support shared query params: `page`, `limit`, `sort`, `search`, `select`, `include`.

### Available `include` values

| Value | Use when | Response |
|-------|----------|----------|
| _(omit)_ | Normal list | Documents + totals (`content`, `totalElements`, …) |
| `count` | Same as normal, but you want totals explicitly | Documents + totals |
| `countOnly` | You only need the number | `{ "count": N }` — **no** documents |
| `pagination`, `summary`, `statistics`, `relations`, `permissions`, `analytics` | Reserved for later | Recognized today but **no extra payload** yet |

Full catalog: [LIST-QUERY.md](LIST-QUERY.md#include-options).

```http
GET http://localhost:8080/api/v1/items?page=0&limit=10&sort=name,asc&search=widget&select=id,name&include=count
Authorization: Bearer <accessToken>
```

Count only (no documents — just the number):

```http
GET http://localhost:8080/api/v1/items?search=widget&include=countOnly
Authorization: Bearer <accessToken>
```

```json
{ "count": 42 }
```

Deep select (notes populate nested owner):

```http
GET http://localhost:8080/api/v1/notes?select=id,title,owner.username
Authorization: Bearer <accessToken>
```

Response is a page envelope (`content`, `page`, `limit`, `totalElements`, …) — not a bare array.  
Count only: `include=countOnly` → `{ "count": N }` with no `content`.  

See [LIST-QUERY.md](LIST-QUERY.md) and [API-REFERENCE.md](API-REFERENCE.md#list-query-shared).

Optional: `X-Correlation-Id: my-trace-1` (echoed on responses; auto-generated if omitted).

## Register a new user (local)

```http
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{ "username": "alice", "password": "password1" }
```

Then login with those credentials.

## Refresh and logout

```http
POST /api/v1/auth/refresh
{ "refreshToken": "<refreshToken>" }

POST /api/v1/auth/logout
{ "refreshToken": "<refreshToken>" }
```

Refresh tokens are **rotated** (old token becomes invalid after refresh).

## Sample domains

### Items (shared catalog)

```http
POST /api/v1/items
Authorization: Bearer <token>
{ "name": "Widget", "description": "Demo" }
```

### Notes (your notes only)

```http
POST /api/v1/notes
Authorization: Bearer <token>
{ "title": "My note", "body": "Only I can see this" }
```

## Postgres locally

```powershell
docker compose up -d
# Host port 5433 → container 5432 (avoids clashing with local Windows Postgres on 5432)
$env:DB_URL="jdbc:postgresql://localhost:5433/template"
$env:DB_USER="template"
$env:DB_PASSWORD="template"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

## Related docs

- [API-REFERENCE.md](API-REFERENCE.md)
- [OPERATIONS.md](OPERATIONS.md)
- Diagrams: [diagrams/auth-flow.md](diagrams/auth-flow.md)
