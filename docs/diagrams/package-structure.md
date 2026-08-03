# Diagram: Package structure

> **Living diagram** — update when packages/features are added.  
> Full folder prose: [FOLDER-STRUCTURE.md](../FOLDER-STRUCTURE.md)

```mermaid
flowchart TB
  subgraph app [com.template.api]
    Main[ApiTemplateApplication]
    subgraph features [feature.*]
      Auth[auth]
      User[user]
      Item[item]
      Note[note]
    end
    subgraph config [config]
      Sec[security JWT SecurityConfig]
      Cors[CorsConfig]
      Oapi[OpenApiConfig]
    end
    subgraph common [common]
      Err[error ProblemDetail]
      Web[web Correlation RateLimit]
    end
  end

  Auth --> Sec
  User --> Sec
  Item --> Err
  Note --> Err
  Main --> features
  Main --> config
  Main --> common
```

## Feature package contents (pattern)

```
feature.<name>/
  *Controller
  *Service
  *Repository
  * entity
  *Request / *Response
  *Mapper (MapStruct, optional)
```
