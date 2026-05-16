# Fluxogramas — Módulo `core`

> Gerado pelo Reversa Archaeologist em 2026-05-16.

## `AnnotationStatementGenerator.generateStatements`

```mermaid
flowchart TD
    A[Recebe AnnotationStatementInput e parâmetros] --> B[Normaliza parâmetros nulos para emptyMap]
    B --> C[TypeAnnotationUtils.findAnnotationData]
    C --> D{Para cada FilterAnnotationData}
    D --> E[createStatements data, parameters]
    E --> F{Statement criado?}
    F -- Sim --> G[Adiciona em statementList]
    F -- Não --> D
    G --> D
    D --> H[Cria decoratedFilters para operation Decorated]
    H --> I[Lista allFilters]
    I --> J{statementList vazia?}
    J -- Sim --> K[Retorna StatementWrapper com NoOpStatement]
    J -- Não --> L{Existe só 1 statement?}
    L -- Sim --> M[Retorna StatementWrapper com statement único]
    L -- Não --> N[Combina statements por CONJUNCTION]
    N --> O[Retorna StatementWrapper final]
```

## `DefaultStatementGenerator.createFilterData`

```mermaid
flowchart TD
    A[Recebe path, parameters, operation, negateParameter e values] --> B{operation == Dynamic?}
    B -- Não --> C[computeNegatingParameter]
    C --> D[comparisonValue = values]
    D --> Z[Cria FilterData]
    B -- Sim --> E{values[0] é Object[]?}
    E -- Não --> X1[Lança StatementGenerationException]
    E -- Sim --> F[Extrai primeiro item como código da operação]
    F --> G{primeiro item é String?}
    G -- Não --> X2[Lança StatementGenerationException]
    G -- Sim --> H{código tem tamanho 3?}
    H -- Sim --> I{primeiro char é N/n?}
    I -- Não --> X3[Lança StatementGenerationException]
    I -- Sim --> J[negate=true; resolve operação pelo restante]
    H -- Não --> K{código tem tamanho 2?}
    K -- Sim --> L[negate=false; resolve operação]
    K -- Não --> X4[Lança StatementGenerationException]
    J --> M[comparisonValue = valores restantes]
    L --> M
    M --> N{operação IN e primeiro valor não é array?}
    N -- Sim --> O[Empacota valores em array único]
    N -- Não --> P{operação BT?}
    O --> Z
    P -- Sim --> Q{há exatamente 2 valores?}
    Q -- Não --> X5[Lança StatementGenerationException]
    Q -- Sim --> R[Renomeia parâmetros para From/To]
    R --> Z
    P -- Não --> Z
```

## `TypeAnnotationUtils.findCachedMetadata`

```mermaid
flowchart TD
    A[Recebe AnnotationStatementInput] --> B[Valida input não nulo]
    B --> C{Existe metadata no cache?}
    C -- Sim --> D[Retorna metadata cacheada]
    C -- Não --> E[buildMetadata]
    E --> F[findStatementAnnotations]
    F --> G[findAnnotationDataInternal]
    G --> H[validateAnnotationData]
    H --> I[findFilterDecoratorsInternal]
    I --> J[listAllFilterRequestDataInternal]
    J --> K[Cria AnnotationMetadata imutável]
    K --> L[computeIfAbsent no cache]
    L --> M[Retorna metadata]
```

## `TypeAnnotationUtils.findFilterField`

```mermaid
flowchart TD
    A[Recebe classe e path] --> B[Divide path por ponto]
    B --> C{Há navegação aninhada?}
    C -- Sim --> D[Resolve primeiro campo recursivamente]
    D --> E{Tipo do campo é Collection?}
    E -- Sim --> F[Usa primeiro argumento genérico]
    E -- Não --> G[Usa tipo do campo]
    F --> H{Classe real encontrada?}
    G --> H
    H -- Sim --> I[Busca restante do path na classe real]
    H -- Não --> J[Tenta campo direto]
    C -- Não --> J
    J --> K{getDeclaredField encontrou?}
    K -- Sim --> L[Retorna Field]
    K -- Não --> M{Há superclass diferente de Object?}
    M -- Sim --> N[Busca na superclass]
    M -- Não --> O[Lança DynamicFilterConfigurationException]
```
