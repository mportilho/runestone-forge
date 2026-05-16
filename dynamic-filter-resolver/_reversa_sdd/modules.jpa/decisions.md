# Modules JPA, Decisões de Design

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

| ID | Decisão | Consequência | Evidência | Confiança |
|---|---|---|---|---|
| DEC-JPA-01 | Usar Spring Data `Specification` como alvo concreto da árvore core. | Integra nativamente com repositories Spring Data. | `SpecificationDynamicFilterResolver.java` | 🟢 |
| DEC-JPA-02 | `NoOpStatement` vira `Specification.unrestricted()`. | Ausência de filtros não quebra consultas. | `SpecificationStatementAnalyser.java` | 🟢 |
| DEC-JPA-03 | Join type padrão é `INNER`. | Paths em associação filtram por existência da associação por padrão. | ADR 003, `JpaPredicateUtils.java` | 🟢 |
| DEC-JPA-04 | `IN` em collection final usa join final e `distinct(true)`. | Evita duplicidade de entidade. | `SpecificationIsIn.java` | 🟢 |
| DEC-JPA-05 | Fetch join declarativo não roda em count query. | Evita incompatibilidade em contagens. | `FetchingFilterDecorator.java` | 🟢 |
| DEC-JPA-06 | Decorators customizados são beans Spring ou registrados dinamicamente. | Extensibilidade via ApplicationContext. | `SpringFilterDecoratorFactory.java` | 🟢 |
| DEC-JPA-07 | Repository impl recebe resolver por BPP. | Integra com lifecycle Spring Data, mas uso manual fica frágil. | `DynamicFilterJpaRepositoryBeanPostProcessor.java` | 🟢 |
| DEC-JPA-08 | Sort externo é traduzido pelo primeiro parâmetro de filtro. | API pode ordenar por nome exposto em vez de path interno. | `DynamicFilterJpaRepositoryImpl.updateSortFilterPath` | 🟢 |

## Decisões Pendentes

- 🟢 Contrato oficial confirmado pelo usuário: proxies customizados de `Specification` suportam o uso essencial de `toPredicate`; default methods e métodos de `Object` ficam fora do escopo obrigatório.
- 🟢 `DynamicFilterJpaRepositoryImpl` deve falhar com mensagem explícita quando usado sem resolver injetado.
