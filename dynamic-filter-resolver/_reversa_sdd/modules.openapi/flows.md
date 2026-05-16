# Modules OpenAPI, Fluxos Operacionais

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Fluxo 1 — Customização da Operação

```mermaid
flowchart TD
    A[Operation + HandlerMethod] --> B[Percorre method parameters]
    B --> C{Annotation dinâmica reconhecida?}
    C -->|Não| D[Ignora]
    C -->|Sim| E[Resolve nome técnico]
    E --> F[Lista FilterRequestData]
    F --> G[Remove parâmetro técnico]
    G --> H[Customiza parâmetro por filtro]
    H --> I[Retorna Operation]
```

## Fluxo 2 — Customização por Filtro

```mermaid
flowchart TD
    A[FilterRequestData] --> B{constantValues?}
    B -->|Sim| C[Omitir parâmetro]
    B -->|Não| D{Operação}
    D -->|Dynamic| E[Array string minItems 2]
    D -->|IsIn| F[ArraySchema]
    D -->|IsNull| G[BooleanSchema]
    D -->|Outra| H[Schema pelo field alvo]
    H --> I[Aplica Bean Validation]
    E --> J[Cria/atualiza Parameter]
    F --> J
    G --> J
    I --> J
```

## Fluxo 3 — Bean Validation

```mermaid
flowchart TD
    A[Schema + AnnotatedElement] --> B{Tipo do schema}
    B -->|integer/number| C[Min Max DecimalMin DecimalMax PositiveOrZero]
    B -->|string| D[Size Pattern]
    B -->|array| E[Size]
    B -->|outro/nulo| F[Sem alteração]
```

## Casos de Falha

- 🟢 `Dynamic` com mais de um parâmetro gera `IllegalStateException`.
- 🟢 Falha na customização é encapsulada com informação do handler method.
- 🟢 `@DisjunctionFrom` isolado não entra no fluxo do customizer no legado porque a condição inicial não verifica `DisjunctionFrom.class` (`DynaFilterOperationCustomizer.java:63-64`).
