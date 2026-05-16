# Code/Spec Matrix — dynamic-filter-resolver

> Gerado pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Cobertura por Arquivo de Produção

| Arquivo do legado | Unit correspondente | Cobertura |
|---------|---------------------|-----------|
| `src/main/java/com/runestone/dynafilter/core/generator/StatementGenerator.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/DefaultStatementGenerator.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/ValueExpressionResolver.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/StatementWrapper.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/ConditionalStatement.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/ConditionalStatementBuilder.java` | `core/` | n/a |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementGenerator.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementInput.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/Filter.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/Conjunction.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/Disjunction.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/Statement.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/ConjunctionFrom.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/DisjunctionFrom.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/StatementFrom.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/FilterTarget.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/FilterDecorators.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/FilterAnnotationData.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/FilterAnnotationStatement.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/VirtualAnnotationHolder.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/model/FilterData.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/model/FilterRequestData.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/model/FilterModifier.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/model/modifiers/ModIgnoreCase.java` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/model/statement/*` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/operation/*` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/operation/types/*` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/resolver/*` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/exceptions/*` | `core/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/SpecificationFilterOperationService.java` | `modules.jpa/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/*.java` | `modules.jpa/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/modifiers/*.java` | `modules.jpa/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/SpecificationDynamicFilterResolver.java` | `modules.jpa/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/SpecificationStatementAnalyser.java` | `modules.jpa/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/FetchingFilterDecorator.java` | `modules.jpa/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/Fetching.java` | `modules.jpa/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/Fetches.java` | `modules.jpa/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/modules/jpa/repository/*.java` | `modules.jpa/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/modules/jpa/spring/*.java` | `modules.jpa/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/modules/openapi/DynaFilterOperationCustomizer.java` | `modules.openapi/` | 🟢 |
| `src/main/java/com/runestone/dynafilter/modules/openapi/SchemaValidationUtils.java` | `modules.openapi/` | 🟢 |

## Cobertura por Testes e Benchmarks

| Arquivo do legado | Unit correspondente | Cobertura |
|---------|---------------------|-----------|
| `src/test/java/com/runestone/dynafilter/core/**` | `core/` | 🟢 |
| `src/test/java/com/runestone/dynafilter/modules/jpa/**` | `modules.jpa/` | 🟢 |
| `src/test/java/com/runestone/dynafilter/modules/openapi/**` | `modules.openapi/` | 🟡 |
| `src/test/java/com/runestone/dynafilter/performance/**` | `test-support.performance/` | 🟢 |
| `src/test/java/com/runestone/dynafilter/modules/jpa/tools/app/database/jpamodels/**` | `modules.jpa/`, `test-support.performance/` | 🟢 |

## Estimativa de Cobertura

| Categoria | Estimativa | Observação | Confiança |
|---|---:|---|---|
| Produção `core` | 100% | Todos os grupos de arquivo mapeados para specs da unit. | 🟢 |
| Produção `modules.jpa` | 100% | Operações, resolver, Spring e repository cobertos. | 🟢 |
| Produção `modules.openapi` | 100% | Duas classes de produção cobertas; customizer precisa mais testes. | 🟢 |
| Test/performance | 90% | Benchmarks e fixtures principais cobertos; testes auxiliares menores ficam agregados por pacote. | 🟡 |

## Arquivos n/a

| Arquivo | Motivo | Confiança |
|---|---|---|
| `src/main/java/com/runestone/dynafilter/core/generator/ConditionalStatementBuilder.java` | Código comentado sem símbolos ativos; não participa do comportamento atual. | 🟢 |

## Lacunas

- 🔴 Não há schema produtivo, controllers produtivos ou OpenAPI final para mapear.
- 🟡 Testes diretos do `DynaFilterOperationCustomizer` devem ser adicionados para elevar cobertura comportamental de OpenAPI.
