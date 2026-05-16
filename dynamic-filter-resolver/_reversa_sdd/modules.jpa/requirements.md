# Modules JPA

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Visão Geral

🟢 **CONFIRMADO** — A unit `modules.jpa` adapta a árvore lógica do `core` para Spring Data JPA `Specification<?>`, integra filtros dinâmicos ao Spring MVC e fornece repositories que aceitam `ConditionalStatement`.

🟢 **CONFIRMADO** — O módulo não define entidades de produção; ele opera sobre entidades da aplicação consumidora por meio de Criteria API.

## Responsabilidades

- 🟢 Converter `StatementWrapper` em `Specification<?>` por visitor/analyser.
- 🟢 Mapear operações do `core` para predicados JPA Criteria (`Equals`, `Like`, `Between`, `IsIn`, `IsNull`, comparações e prefixo/sufixo).
- 🟢 Resolver paths simples e dot-notation, criando/reutilizando joins conforme necessário.
- 🟢 Suportar modifiers de join type: default `INNER`, `LEFT` e `RIGHT`.
- 🟢 Resolver argumentos Spring MVC como `ConditionalStatement`, `Specification<?>` ou proxy de interface `Specification`.
- 🟢 Criar decorators Spring e `FetchingFilterDecorator`, incluindo fetch joins seguros para count query.
- 🟢 Expor `DynamicFilterJpaRepository` e implementação baseada em `SimpleJpaRepository`.
- 🟢 Traduzir `Sort` externo baseado em parâmetro de filtro para path real da entidade.
- 🟢 Validar configurações de filtros em controllers durante warmup Spring.

## Regras de Negócio Técnico

- 🟢 `NoOpStatement` deve virar `Specification.unrestricted()`.
- 🟢 `LogicalStatement` deve criar filtro concreto via `SpecificationFilterOperationService`.
- 🟢 `CompoundStatement` deve virar `.and(...)` ou `.or(...)` conforme `LogicOperator`.
- 🟢 `NegatedStatement` deve virar `Specification.not(...)`.
- 🟢 Paths compostos criam joins para segmentos intermediários; paths simples usam `root.get(attribute)`.
- 🟢 Join type padrão é `INNER`; modifiers podem escolher `LEFT` ou `RIGHT`.
- 🟢 `SpecificationIsIn` faz join no segmento final quando o path resolve collection e marca `distinct(true)`.
- 🟢 `FetchingFilterDecorator` não cria fetch em count query e aplica `distinct(true)` em query normal.
- 🟢 Query parameters escalares viram `String`; múltiplos valores viram `String[]`; URI variables são mescladas depois e podem sobrescrever query parameters.
- 🟢 Sort usa o primeiro parâmetro de cada `FilterRequestData` para mapear ordenação externa para path real.
- 🟢 Proxies customizados de `Specification` têm contrato suportado limitado ao uso essencial de `Specification`, especialmente `toPredicate`; default methods e métodos de `Object` não são requisito de compatibilidade.
- 🟢 `DynamicFilterJpaRepositoryImpl` depende de injeção tardia do resolver por BeanPostProcessor no uso esperado; se usado sem resolver, deve falhar com erro explícito.
- 🟢 Limitações de múltiplas bags/fetch joins com Hibernate devem ser documentadas como responsabilidade da aplicação consumidora.

## Requisitos Funcionais

| ID | Requisito | Prioridade | Critério de Aceite |
|----|-----------|-----------|-------------------|
| RF-JPA-01 | 🟢 Converter statement tree em `Specification<?>`. | Must | Dado cada tipo de statement do core, quando analisado, então deve gerar `Specification` equivalente. |
| RF-JPA-02 | 🟢 Registrar todas as operações JPA suportadas. | Must | Dado `FilterData.operation`, quando `createFilter` for chamado, então deve escolher a implementação `Specification*` correta. |
| RF-JPA-03 | 🟢 Resolver paths JPA simples e aninhados. | Must | Dado path dot-notation, quando predicate for criado, então joins intermediários devem ser criados/reutilizados. |
| RF-JPA-04 | 🟢 Converter valores para o tipo real do Criteria path. | Must | Dado valor string recebido do request, quando predicate for criado, então valor deve ser convertido antes do CriteriaBuilder. |
| RF-JPA-05 | 🟢 Suportar `IN` sobre collections com `distinct`. | Must | Dado filtro sobre `@ElementCollection`, quando `IN` for aplicado, então entidades duplicadas devem ser evitadas. |
| RF-JPA-06 | 🟢 Resolver argumentos Spring MVC. | Must | Dado parâmetro de controller compatível, quando o resolver executar, então deve retornar `ConditionalStatement`, `Specification` ou proxy. |
| RF-JPA-07 | 🟢 Aplicar fetch decorators. | Should | Dado `@Fetching`, quando query normal executar, então deve criar fetch join; em count query não deve criar fetch. |
| RF-JPA-08 | 🟢 Executar repositories dinâmicos com `ConditionalStatement`. | Must | Dado repository dinâmico, quando método `findAll/count/exists` receber `ConditionalStatement`, então deve delegar como `Specification`. |
| RF-JPA-09 | 🟢 Traduzir sort por parâmetro externo. | Should | Dado `Sort` com propriedade igual ao primeiro parâmetro do filtro, quando repository executar, então deve ordenar pelo path real. |
| RF-JPA-10 | 🟢 Validar filtros em warmup de controllers. | Should | Dado controller com filtros inválidos, quando contexto Spring inicializar, então erro deve ocorrer cedo. |
| RF-JPA-11 | 🟢 Limitar contrato de proxy customizado ao uso essencial de `Specification`. | Could | Dado interface `Specification` customizada, então somente o comportamento essencial de `toPredicate` é contrato suportado; default methods e métodos de `Object` não são requisito de compatibilidade. |

## Requisitos Não Funcionais

| Tipo | Requisito inferido | Evidência no código | Confiança |
|------|--------------------|---------------------|-----------|
| Performance | Cache de paths JPA parseados deve reduzir custo de dot-notation. | `JpaPredicateUtils.java` | 🟢 |
| Performance | Sort translation deve evitar algoritmo legado O(n*m). | `DynamicFilterJpaRepositoryImpl.updateSortFilterPath`, benchmark de sort | 🟢 |
| Robustez | Fetch joins devem ser ignorados em count query. | `FetchingFilterDecorator.java` | 🟢 |
| Extensibilidade | Decorators customizados devem ser resolvidos via Spring context. | `SpringFilterDecoratorFactory.java` | 🟢 |
| Segurança indireta | Paths filtráveis vêm de annotations, não do request arbitrário. | `FilterRequestData.path`, `JpaPredicateUtils` | 🟢 |

## Critérios de Aceitação

```gherkin
Cenário: converter statement lógico para Specification
Dado um StatementWrapper com LogicalStatement
Quando SpecificationDynamicFilterResolver.createFilter for chamado
Então uma Specification correspondente à operação deve ser retornada

Cenário: resolver path aninhado
Dado um filtro com path addresses.location.city
Quando JpaPredicateUtils.computeAttributePath executar
Então joins intermediários devem ser criados ou reutilizados

Cenário: aplicar IN em collection
Dado uma entidade com ElementCollection
Quando SpecificationIsIn executar
Então deve fazer join no segmento final e marcar query distinct

Cenário: resolver argumento MVC
Dado um controller com parâmetro Specification anotado com filtros
Quando o request contiver query parameters válidos
Então o argumento resolvido deve ser uma Specification aplicável

Cenário: evitar fetch em count query
Dado um FetchingFilterDecorator
Quando a query tiver result type Long ou long
Então nenhum fetch join deve ser criado
```

## Rastreabilidade de Código

| Arquivo | Função / Classe | Cobertura |
|---------|-----------------|-----------|
| `modules/jpa/operation/SpecificationFilterOperationService.java` | Registro de operações JPA | 🟢 |
| `modules/jpa/operation/specification/*.java` | Predicados Criteria API | 🟢 |
| `modules/jpa/operation/specification/JpaPredicateUtils.java` | Path, joins e conversão | 🟢 |
| `modules/jpa/resolver/SpecificationDynamicFilterResolver.java` | Entrada de conversão para Specification | 🟢 |
| `modules/jpa/resolver/SpecificationStatementAnalyser.java` | Visitor/analyser | 🟢 |
| `modules/jpa/resolver/FetchingFilterDecorator.java` | Fetch joins | 🟢 |
| `modules/jpa/spring/*` | MVC, decorators e warmup | 🟢 |
| `modules/jpa/repository/*` | Repository dinâmico e sort translation | 🟢 |
