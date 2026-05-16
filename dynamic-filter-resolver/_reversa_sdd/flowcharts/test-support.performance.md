# Fluxogramas — Módulo `test-support.performance`

> Gerado pelo Reversa Archaeologist em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Runner JMH

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[main] --> B[OptionsBuilder]
    B --> C[include DynamicFilterResolverBenchmark]
    C --> D[resultFormat JSON]
    D --> E[result target/jmh-result.json]
    E --> F[new Runner options]
    F --> G[run]
```

## Benchmark Geral do Resolver

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[Setup Trial] --> B[Criar AnnotationStatementGenerator]
    B --> C[Criar SpecificationDynamicFilterResolver]
    C --> D[Criar AnnotationStatementInput]
    D --> E[Montar parametros de filtro]
    E --> F[Gerar statement precomputado]
    F --> G{Benchmark}
    G -->|statementGenerator| H[generateStatements]
    G -->|specificationResolver| I[createFilter statement]
    G -->|argumentResolver interface| J[resolveArgument SearchState]
    G -->|argumentResolver fetching| K[resolveArgument SearchMultiDataEmployees]
    G -->|annotation cache reused| L[findAnnotationData sharedInput]
    G -->|annotation cache new| M[findAnnotationData new equivalent input]
```

## Perf02 Criteria e Cache

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[Setup JpaPredicateState] --> B[Subir SpringApplicationBuilder sem web]
    B --> C[Obter EntityManager]
    C --> D[Criar SpecificationStatementAnalyser]
    D --> E[Criar conjunction de 50 filtros variados]
    D --> F[Criar conjunction de 50 paths aninhados repetidos]
    D --> G[Criar FetchingFilterDecorator deep paths]
    D --> H[Criar FetchingFilterDecorator overlapping paths]
    E --> I[toPredicate manyFilters]
    F --> J[toPredicate repeatedNestedPath]
    G --> K[toPredicate deepFetch]
    H --> L[toPredicate overlappingFetch]
    I --> M[TearDown fecha EntityManager e contexto]
    J --> M
    K --> M
    L --> M
```

## Cache Growth Benchmarks

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[clearCaches] --> B{Estado}
    B -->|CacheGrowthState| C[Inserir 20.000 SyntheticAnnotation]
    B -->|BoundedCacheHitState| D[Inserir 50.000 SyntheticAnnotation]
    C --> E[Reaquecer reusedInput]
    D --> F[Reaquecer hotInput]
    E --> G[Benchmark findAnnotationData]
    F --> G
    G --> H[clearCaches no TearDown]
```

## Sort Translation Benchmark

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[Setup estado] --> B{Cenario}
    B -->|HeavyMappings| C[50 orders + 600 filters com traducoes]
    B -->|NoTranslation| D[50 orders + 600 filters sem traducao]
    C --> E[Executar updateSortFilterPath otimizado]
    C --> F[Executar legacySortTranslation]
    D --> E
    D --> F
    E --> G[Blackhole consume]
    F --> G
```
