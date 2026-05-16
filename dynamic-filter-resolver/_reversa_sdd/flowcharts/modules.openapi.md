# Fluxogramas — Módulo `modules.openapi`

> Gerado pelo Reversa Archaeologist em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Customização de Operação OpenAPI

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[SpringDoc chama OperationCustomizer] --> B[Iterar MethodParameters]
    B --> C{Parametro tem annotation dinamica?}
    C -->|Nao| B
    C -->|Sim| D[Descobrir nome do parametro tecnico]
    D --> E[Listar FilterRequestData via TypeAnnotationUtils]
    E --> F{Ha filtros?}
    F -->|Nao| B
    F -->|Sim| G[Remover parametro tecnico da Operation]
    G --> H[Para cada FilterRequestData]
    H --> I[customizeParameter]
    I --> H
    H --> B
    B --> J[Retornar Operation]
```

## Criação de Parâmetro por Filtro

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[FilterRequestData] --> B{constantValues presente?}
    B -->|Sim| C[Omitir parametro]
    B -->|Nao| D{Dynamic com mais de um parametro?}
    D -->|Sim| E[Lancar IllegalStateException]
    D -->|Nao| F[Iterar filter.parameters]
    F --> G{Parametro ja existe na Operation?}
    G -->|Sim| H[Reusar parametro]
    G -->|Nao| I[Criar parametro novo]
    H --> J[Definir nome]
    I --> J
    J --> K{Operacao}
    K -->|Dynamic| L[ArraySchema string minItems 2]
    K -->|IsIn| M[ArraySchema com item existente ou string]
    K -->|Outra| N[Schema comum por field alvo]
    L --> O[Aplicar required]
    M --> O
    N --> O
    O --> P{Parametro existente?}
    P -->|Nao| Q[in=query e adicionar na Operation]
    P -->|Sim, in null/default| R[in=query]
    P -->|Sim, in path| S[required=true]
```

## Schema Comum e Validações

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[createCommonSchema] --> B{Field encontrado?}
    B -->|Sim| C[Resolver schema pelo tipo e JsonView]
    B -->|Nao| D[StringSchema]
    C --> E{Operacao IsNull?}
    D --> E
    E -->|Sim| F[BooleanSchema]
    E -->|Nao| G{Parametro ja tinha schema?}
    G -->|Sim| H[Novo Schema copiando type e enum]
    G -->|Nao| I[Usar schema resolvido]
    F --> J[Set schema no parametro]
    H --> J
    I --> J
    J --> K[Set description]
    K --> L{defaultValues tem tamanho 1?}
    L -->|Sim| M[Set default]
    L -->|Nao| N[Sem default]
    M --> O[Aplicar Bean Validation]
    N --> O
```

## Bean Validation para OpenAPI Schema

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[applyValidations] --> B{AnnotatedElement nulo?}
    B -->|Sim| C[Retornar sem alterar]
    B -->|Nao| D{Schema number/integer?}
    D -->|Sim| E[Aplicar PositiveOrZero Min Max DecimalMin DecimalMax]
    D -->|Nao| F{Schema string?}
    E --> F
    F -->|Sim| G[Aplicar Size e Pattern]
    F -->|Nao| H{Schema array?}
    G --> H
    H -->|Sim| I[Aplicar Size como minItems/maxItems]
    H -->|Nao| J[Fim]
    I --> J
```
