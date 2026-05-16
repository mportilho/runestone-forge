# Fluxogramas — Módulo `modules.jpa`

> Gerado pelo Reversa Archaeologist em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Resolução MVC para Specification

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[Controller chama endpoint] --> B{Parametro suportado?}
    B -->|ConditionalStatement| C[Coletar query params e path variables]
    B -->|Interface Specification| C
    B -->|Outro tipo| Z[Ignorar resolver]
    C --> D[Criar AnnotationStatementInput]
    D --> E[Resolver decorators via SpringFilterDecoratorFactory]
    D --> F[Gerar StatementWrapper via AnnotationStatementGenerator]
    F --> G{Tipo do parametro}
    G -->|ConditionalStatement| H[Retornar ConditionalStatement wrapper + decorator]
    G -->|Specification/interface| I[Criar Specification via DynamicFilterResolver]
    I --> J{Tipo alvo assignable?}
    J -->|Sim| K[Retornar specification]
    J -->|Nao| L[Retornar proxy da interface]
```

## StatementWrapper para Specification JPA

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[StatementWrapper] --> B[statement.acceptAnalyser]
    B --> C{Tipo de statement}
    C -->|LogicalStatement| D[FilterOperationService.createFilter FilterData]
    C -->|CompoundStatement| E[Analisar esquerda e direita]
    E --> F{LogicOperator}
    F -->|CONJUNCTION| G[left.and right]
    F -->|DISJUNCTION| H[left.or right]
    C -->|NegatedStatement| I[Analisar statement interno]
    I --> J[Specification.not]
    C -->|NoOpStatement| K[Specification.unrestricted]
    D --> L{Decorator existe?}
    G --> L
    H --> L
    J --> L
    K --> L
    L -->|Sim| M[decorator.decorate specification]
    L -->|Nao| N[Retornar specification]
```

## Criação de Path e Join no Criteria API

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[FilterData.path] --> B[trim e validar nulo]
    B --> C[Chave: rootType + path]
    C --> D{ParsedPath no cache?}
    D -->|Nao| E[split por ponto e validar segmentos]
    E --> F[Cachear ParsedPath]
    D -->|Sim| G[Usar ParsedPath]
    F --> H[Resolver JoinType por modifiers]
    G --> H
    H --> I[Comecar em Root]
    I --> J[Para cada associationSegment]
    J --> K{Join existente mesmo atributo e tipo?}
    K -->|Sim| L[Reusar join]
    K -->|Nao| M[Criar join]
    L --> J
    M --> J
    J --> N[root/join.get attributeSegment]
```

## FetchingFilterDecorator

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[Collection Fetching] --> B{Vazia?}
    B -->|Sim| C[Lancar IllegalArgumentException]
    B -->|Nao| D[Parsear e deduplicar paths]
    D --> E[decorate filter]
    E --> F[Specification decorada executa toPredicate]
    F --> G{Query de count?}
    G -->|Sim| H[Nao criar fetches]
    G -->|Nao| I[query.distinct true]
    I --> J[Para cada ResolvedFetchPath]
    J --> K[Criar/reusar fetch por segmento]
    K --> J
    H --> L[Retornar null do decorator]
    J --> L
    L --> M{Filtro base existe?}
    M -->|Sim| N[decorated.and filter]
    M -->|Nao| O[decorated]
```

## Repository Dinâmico

🟢 CONFIRMADO

```mermaid
flowchart TD
    A[BeanPostProcessor] --> B{Bean implementa DynamicFilterJpaRepository?}
    B -->|Sim| C[setDynamicFilterResolver]
    B -->|Nao| D[Retornar bean sem alteracao]
    C --> E[Repository recebe ConditionalStatement]
    E --> F[dynamicFilterResolver.createFilter]
    F --> G{Metodo com Sort/Pageable?}
    G -->|Sim| H[Traduzir propriedades de Sort por FilterRequestData]
    G -->|Nao| I[Usar query sem sort traduzido]
    H --> J[Delegar para SimpleJpaRepository]
    I --> J
    J --> K{EntityGraph informado?}
    K -->|Sim| L[Aplicar query hint do EntityGraph]
    K -->|Nao| M[Executar consulta padrao]
    L --> M
```
