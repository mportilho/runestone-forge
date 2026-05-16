# Legacy Mapping — Módulo `modules.jpa`

> Gerado pelo Reversa Archaeologist em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Pacotes e Responsabilidades

🟢 CONFIRMADO

| Área | Arquivos | Responsabilidade |
|---|---|---|
| Operações JPA | `src/main/java/com/runestone/dynafilter/modules/jpa/operation/SpecificationFilterOperationService.java`, `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/*.java` | Mapear operações declarativas do `core` para predicados Criteria API em `Specification<?>`. |
| Modificadores JPA | `src/main/java/com/runestone/dynafilter/modules/jpa/operation/modifiers/*.java` | Marcar join type usado por filtros em paths com associação. |
| Resolver | `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/*.java` | Converter statement tree em `Specification`, aplicar fetch decorators e declarar annotations de fetch. |
| Repository | `src/main/java/com/runestone/dynafilter/modules/jpa/repository/*.java` | Expor e implementar repository dinâmico baseado em `ConditionalStatement`. |
| Spring MVC | `src/main/java/com/runestone/dynafilter/modules/jpa/spring/*.java` | Registrar beans, argument resolver, decorators e validação antecipada de filtros em controllers. |

## Arquivos de Produção Analisados

🟢 CONFIRMADO

| Arquivo | Papel |
|---|---|
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/SpecificationFilterOperationService.java` | Registro das operações suportadas. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/JpaPredicateUtils.java` | Parse/cache de paths, criação/reuso de joins, seleção de overload numérico/comparável. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/SpecificationEquals.java` | Predicado de igualdade. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/SpecificationLike.java` | Predicado contains. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/SpecificationStartsWith.java` | Predicado prefixo. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/SpecificationEndsWith.java` | Predicado sufixo. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/SpecificationGreater.java` | Predicado maior que. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/SpecificationGreaterOrEquals.java` | Predicado maior ou igual. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/SpecificationLess.java` | Predicado menor que. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/SpecificationLessOrEquals.java` | Predicado menor ou igual. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/SpecificationBetween.java` | Predicado between. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/SpecificationIsIn.java` | Predicado IN com suporte a collection final. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/SpecificationIsNull.java` | Predicado is null / is not null. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/SpecificationDynamicFilterResolver.java` | Entrada de conversão para `Specification<?>`. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/SpecificationStatementAnalyser.java` | Visitor/analyser de statements. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/FetchingFilterDecorator.java` | Fetch joins via decorator. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/Fetching.java` | Annotation repeatable de fetch. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/Fetches.java` | Container de `@Fetching`. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/repository/DynamicFilterJpaRepository.java` | API pública de repository dinâmico. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/repository/DynamicFilterJpaRepositoryImpl.java` | Implementação de repository dinâmico. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/repository/DynamicFilterJpaRepositoryBeanPostProcessor.java` | Injeção tardia do resolver nos repositories. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/spring/DynamicFilterServletAutoConfiguration.java` | Beans de conversão, resolver e MVC configurer. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/spring/SpecificationDynamicFilterArgumentResolver.java` | Resolução de parâmetros HTTP para filtros. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/spring/SpecificationDynamicFilterWebMvcConfigurer.java` | Registro do argument resolver no MVC. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/spring/SpringFilterDecoratorFactory.java` | Resolução/cache de decorators Spring. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/spring/FilterConfigurationAnalyserBeanPostProcessor.java` | Warmup e validação antecipada de filtros em controllers. |
| `src/main/java/com/runestone/dynafilter/modules/jpa/spring/EnableDynamicFilterServletConfiguration.java` | Annotation de habilitação. |

## Testes Usados Como Evidência

🟢 CONFIRMADO

| Teste | Evidência |
|---|---|
| `src/test/java/com/runestone/dynafilter/modules/jpa/operation/specification/TestJpaPredicateUtils.java` | Paths simples e com joins. |
| `src/test/java/com/runestone/dynafilter/modules/jpa/operation/specification/TestSpecificationIsInIntegration.java` | `IN` em `@ElementCollection` com enum e `distinct`. |
| `src/test/java/com/runestone/dynafilter/modules/jpa/resolver/TestFetchingFilterDecoratorIntegration.java` | Fetch joins reais, count query, deduplicação e path inválido. |
| `src/test/java/com/runestone/dynafilter/modules/jpa/spring/TestSpecDynaFilterArgumentResolver.java` | Mapa de parâmetros HTTP e resolução de `Specification`. |
| `src/test/java/com/runestone/dynafilter/modules/jpa/spring/TestSpringFilterDecoratorFactory.java` | Decorators via Spring, fetching e cache. |
| `src/test/java/com/runestone/dynafilter/modules/jpa/repository/TestDynamicFilterJpaRepositorySortTranslation.java` | Tradução de sort por parâmetro de filtro. |
