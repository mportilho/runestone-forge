# Modules JPA, Tarefas de Implementação

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Pré-requisitos

- [ ] 🟢 `core` implementado e testado.
- [ ] 🟢 Spring Data JPA, Spring MVC, Jakarta Persistence e runestone-toolkit disponíveis.
- [ ] 🟢 Fixtures JPA/H2 disponíveis para testes de integração.

## Tarefas

- [ ] T-JPA-01, Implementar `SpecificationFilterOperationService` e registrar todas as operações `Specification*`.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/modules/jpa/operation/SpecificationFilterOperationService.java`
  - Critério de pronto: operações declarativas do core são resolvidas para implementações JPA.
  - Confiança: 🟢

- [ ] T-JPA-02, Implementar predicates `SpecificationEquals`, `Like`, `StartsWith`, `EndsWith`, comparações, `Between`, `IsIn` e `IsNull`.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/*.java`
  - Critério de pronto: cada operação converte valores para tipo real do path e cria predicate Criteria correto.
  - Confiança: 🟢

- [ ] T-JPA-03, Implementar `JpaPredicateUtils` para paths, joins e join types.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/JpaPredicateUtils.java`
  - Critério de pronto: paths simples/aninhados funcionam, joins são reutilizados e modifiers `LEFT`/`RIGHT` são respeitados.
  - Confiança: 🟢

- [ ] T-JPA-04, Implementar analyser/resolver de statements para `Specification`.
  - Origem no legado: `SpecificationDynamicFilterResolver.java`, `SpecificationStatementAnalyser.java`
  - Critério de pronto: todos os statement types do core são traduzidos e decorator opcional é aplicado.
  - Confiança: 🟢

- [ ] T-JPA-05, Implementar `FetchingFilterDecorator` e annotations `@Fetching`/`@Fetches`.
  - Origem no legado: `modules/jpa/resolver/FetchingFilterDecorator.java`, `Fetching.java`, `Fetches.java`
  - Critério de pronto: fetch join é aplicado em query normal, ignorado em count query, deduplicado e validado.
  - Confiança: 🟢

- [ ] T-JPA-06, Implementar argument resolver MVC e WebMvcConfigurer.
  - Origem no legado: `SpecificationDynamicFilterArgumentResolver.java`, `SpecificationDynamicFilterWebMvcConfigurer.java`
  - Critério de pronto: resolve `ConditionalStatement`, `Specification` e proxy; query/path variables são mescladas conforme legado.
  - Confiança: 🟢

- [ ] T-JPA-07, Implementar `SpringFilterDecoratorFactory`.
  - Origem no legado: `SpringFilterDecoratorFactory.java`
  - Critério de pronto: decorators customizados são encontrados/registrados no context, cacheados e compostos com fetching por último.
  - Confiança: 🟢

- [ ] T-JPA-08, Implementar auto-configuração e warmup de filtros.
  - Origem no legado: `DynamicFilterServletAutoConfiguration.java`, `EnableDynamicFilterServletConfiguration.java`, `FilterConfigurationAnalyserBeanPostProcessor.java`
  - Critério de pronto: beans default são registrados quando ausentes; filtros inválidos em controllers falham na inicialização.
  - Confiança: 🟢

- [ ] T-JPA-09, Implementar repository dinâmico e sort translation.
  - Origem no legado: `DynamicFilterJpaRepository.java`, `DynamicFilterJpaRepositoryImpl.java`, `DynamicFilterJpaRepositoryBeanPostProcessor.java`
  - Critério de pronto: métodos com `ConditionalStatement` delegam para `SimpleJpaRepository`; sort externo é traduzido pelo primeiro parâmetro de filtro; uso sem resolver injetado falha com erro explícito.
  - Confiança: 🟢

## Tarefas de Teste

- [ ] TT-JPA-01, Testar `JpaPredicateUtils` com paths simples, aninhados e join types. 🟢
- [ ] TT-JPA-02, Testar cada operação `Specification*`. 🟢
- [ ] TT-JPA-03, Testar `IN` sobre `@ElementCollection` com `distinct`. 🟢
- [ ] TT-JPA-04, Testar analyser/resolver de statements. 🟢
- [ ] TT-JPA-05, Testar fetching decorator em query normal e count query. 🟢
- [ ] TT-JPA-06, Testar argument resolver MVC com query params, arrays e URI variables. 🟢
- [ ] TT-JPA-07, Testar decorators Spring e cache. 🟢
- [ ] TT-JPA-08, Testar auto-configuração e repository dinâmico. 🟢
- [ ] TT-JPA-09, Testar sort translation e preservar método `convertoToSpecification`. 🟢
- [ ] TT-JPA-10, Testar que o contrato de proxy customizado cobre `toPredicate` e não promete default methods/Object methods. 🟢

## Ordem Sugerida

1. Operações e path utilities.
2. Resolver/analyser.
3. Fetch decorators.
4. MVC e Spring decorators.
5. Auto-configuração e repositories.
6. Testes de integração e benchmarks de regressão.

## Lacunas Pendentes

- 🟢 Contrato de proxy customizado definido pelo usuário: suporte essencial a `toPredicate`; default methods/Object methods fora do escopo obrigatório.
- 🟢 Uso de repository impl sem resolver injetado deve falhar com erro explícito.
