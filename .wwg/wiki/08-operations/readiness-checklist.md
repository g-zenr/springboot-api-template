# Operational Readiness Checklist

## A. Local template clone — PASS

- [x] JDK 21+  
- [x] `.\mvnw.cmd test` passes  
- [x] Default profile `local` (H2 + console + OpenAPI)  
- [x] Sample CRUD `/api/v1/items`  
- [x] Swagger + health  
- [x] WWG + AGENTS.md + testing policy  

## B. Hardened / complete template — PASS (2026-08-01)

- [x] `prod` profile disables H2 console + OpenAPI  
- [x] Unhandled exceptions logged  
- [x] CI (GitHub Actions) runs tests  
- [x] Test isolation (`@Transactional` on API tests)  
- [x] LICENSE  
- [x] `.env.example`  
- [x] Docker Compose Postgres  
- [x] Dockerfile  
- [x] Configurable CORS  

## C. Product specialization (fork) — open

- [ ] AuthN/AuthZ designed + ADR  
- [ ] Domain entities + Flyway  
- [ ] Security model updated from “none for product”  
- [ ] Deployment target runbook  
- [ ] Domain regression suite per testing policy  

## D. Production product deploy — open

- [ ] Secrets externalized and rotated  
- [ ] Auth proven  
- [ ] Backups / migration strategy  
- [ ] Observability as required  
- [ ] Rollback path  
