# ADR 0005: MapStruct for DTO mapping

## Status

Accepted

## Decision

Use **MapStruct** (`ItemMapper`) for entity ↔ response and request → entity mapping with Spring component model.

## Consequences

- Compile-time mappers; no reflection at runtime
- Annotation processor configured in `maven-compiler-plugin`
