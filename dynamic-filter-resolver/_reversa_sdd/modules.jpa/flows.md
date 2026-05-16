# Modules JPA, Fluxos Operacionais

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Fluxo 1 — Statement para Specification

```mermaid
flowchart TD
    A[StatementWrapper] --> B[SpecificationDynamicFilterResolver]
    B --> C[statement.acceptAnalyser]
    C --> D{Tipo de statement}
    D -->|NoOp| E[Specification.unrestricted]
    D -->|Logical| F[FilterOperationService.createFilter]
    D -->|Compound| G[left.and/right.or]
    D -->|Negated| H[Specification.not]
    E --> I[Decorator opcional]
    F --> I
    G --> I
    H --> I
    I --> J[Specification final]
```

## Fluxo 2 — Predicate Criteria

```mermaid
flowchart TD
    A[FilterData] --> B[JpaPredicateUtils.computeAttributePath]
    B --> C[Converte valores pelo DataConversionService]
    C --> D{Operação}
    D -->|Equals| E[criteriaBuilder.equal]
    D -->|Like/SW/EW| F[criteriaBuilder.like]
    D -->|Comparação| G[gt/ge/lt/le ou Comparable]
    D -->|Between| H[criteriaBuilder.between]
    D -->|IsNull| I[isNull/isNotNull]
    D -->|IsIn| J[expression.in]
```

## Fluxo 3 — Argument Resolver MVC

```mermaid
flowchart TD
    A[MethodParameter suportado] --> B[Extrai query parameters]
    B --> C[Mescla URI variables]
    C --> D[AnnotationStatementGenerator]
    D --> E[SpringFilterDecoratorFactory]
    E --> F{Tipo do parâmetro}
    F -->|ConditionalStatement| G[Retorna ConditionalStatement]
    F -->|Specification| H[Cria Specification]
    F -->|Interface customizada| I[Cria proxy]
```

## Fluxo 4 — Fetch Decorator

```mermaid
flowchart TD
    A[Specification base] --> B{Result type Long/long?}
    B -->|Sim| C[Não cria fetch]
    B -->|Não| D[query.distinct true]
    D --> E[Cria/reusa fetch paths]
    C --> F[Executa predicate base]
    E --> F
```

## Fluxo 5 — Repository Dinâmico

```mermaid
flowchart TD
    A[ConditionalStatement] --> B[convertoToSpecification]
    B --> C[DynamicFilterResolver]
    C --> D[Specification T]
    D --> E[updateSortFilterPath se houver Sort]
    E --> F[Delegação ao SimpleJpaRepository]
```

## Casos de Erro

- 🟢 Sem `HttpServletRequest` no `NativeWebRequest` gera `IllegalStateException`.
- 🟢 Path inválido em Criteria ou fetching falha explicitamente.
- 🟢 Operation sem implementação é rejeitada no serviço do core.
- 🟢 Proxy customizado tem contrato limitado a `toPredicate`; default methods/Object methods ficam fora do escopo obrigatório conforme decisão do usuário.
