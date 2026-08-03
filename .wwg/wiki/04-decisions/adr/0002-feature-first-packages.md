# ADR 0002: Feature-first package layout

## Status

Accepted

## Context

Layer-only packages (`controller`, `service`, `repository` at root) scatter a single feature across the tree.

## Decision

Group by feature:

```text
com.template.api.feature.item
  ItemController
  ItemService
  ItemRepository
  Item
  ItemRequest / ItemResponse
```

Shared concerns stay under `common` and `config`.

## Consequences

- New capabilities are additive feature packages.
- Skills and reviews can reason about one vertical slice at a time.
