# Project Readiness Checklist (Governance)

Linked: `.wwg/wiki/08-operations/readiness-checklist.md`  
Evidence: `.wwg/reports/complete-template-audit-2026-08-01.md`

## Gate definitions

| Gate | Meaning | Status (2026-08-01) |
|------|---------|---------------------|
| **G-LOCAL** | Clone, test, run sample | **PASS** |
| **G-HARDENED** | Profiles, CI, env example, Docker, logging, CORS | **PASS** |
| **G-COMPLETE-TEMPLATE** | Usable as full reusable starter | **PASS** |
| **G-PRODUCT** | Real domain + security truth | **N/A** (template) |
| **G-PROD-DEPLOY** | Safe production product operation | **NOT_READY** (needs product security) |

## Promotion rules

1. Agents may call this a **complete Spring Boot API template** when **G-COMPLETE-TEMPLATE** is PASS.  
2. Agents must **not** call it a production-secured product API until **G-PROD-DEPLOY** is PASS with evidence.  
3. Closing optional roadmap items is not required to keep G-COMPLETE-TEMPLATE.  
