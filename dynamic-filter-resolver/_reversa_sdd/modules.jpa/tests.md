# Modules JPA, Estratégia de Testes

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Testes Legados Relevantes

| Teste | Cobertura | Confiança |
|---|---|---|
| `TestJpaPredicateUtils` | Paths simples, join path e múltiplos joins. | 🟢 |
| `TestSpecification*` | Operações JPA individuais. | 🟢 |
| `TestSpecificationIsInIntegration` | `IN` sobre `@ElementCollection` e `distinct`. | 🟢 |
| `TestFetchingFilterDecoratorIntegration` | Fetch joins reais, count query, deduplicação e path inválido. | 🟢 |
| `TestSpecDynaFilterArgumentResolver` | Parâmetros HTTP, URI variables e resolução MVC. | 🟢 |
| `TestSpringFilterDecoratorFactory` | Decorators Spring, fetching e cache. | 🟢 |
| `TestDynamicFilterServletAutoConfiguration` | Auto-configuração MVC. | 🟢 |
| `TestDynamicFilterJpaRepositorySortTranslation` | Tradução de sort. | 🟢 |

## Suíte Mínima

| ID | Cenário | Confiança |
|---|---|---|
| TC-JPA-01 | `NoOp`, `Logical`, `Compound` e `Negated` para `Specification`. | 🟢 |
| TC-JPA-02 | Cada operação `Specification*` com conversão de tipo. | 🟢 |
| TC-JPA-03 | Dot-notation com join reuse e join type modifiers. | 🟢 |
| TC-JPA-04 | `IN` em collection final com distinct. | 🟢 |
| TC-JPA-05 | Fetch decorator em query normal/count query. | 🟢 |
| TC-JPA-06 | Argument resolver para `ConditionalStatement`, `Specification` e proxy. | 🟢 |
| TC-JPA-07 | Decorator customizado via bean e registro dinâmico. | 🟢 |
| TC-JPA-08 | Repository methods e sort translation. | 🟢 |
| TC-JPA-09 | Proxy customizado com `toPredicate` suportado e default methods/Object methods fora do contrato obrigatório. | 🟢 |

## Comandos de Verificação

```shell
mvn clean test -pl dynamic-filter-resolver -Dtest=TestJpaPredicateUtils
mvn clean test -pl dynamic-filter-resolver -Dtest=TestSpecificationIsInIntegration
mvn clean test -pl dynamic-filter-resolver -Dtest=TestFetchingFilterDecoratorIntegration
mvn clean test -pl dynamic-filter-resolver -Dtest=TestSpecDynaFilterArgumentResolver
mvn clean test -pl dynamic-filter-resolver -Dtest=TestSpringFilterDecoratorFactory
mvn clean test -pl dynamic-filter-resolver -Dtest=TestDynamicFilterJpaRepositorySortTranslation
```
