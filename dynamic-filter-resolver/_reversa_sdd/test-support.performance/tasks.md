# Test Support Performance, Tarefas de Implementação

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Pré-requisitos

- [ ] 🟢 JMH configurado no módulo.
- [ ] 🟢 Produção `core` e `modules.jpa` funcional.
- [ ] 🟢 H2 e Spring Test disponíveis para benchmarks JPA.

## Tarefas

- [ ] T-PERF-01, Implementar runner JMH baseline.
  - Origem no legado: `DynamicFilterResolverBenchmarkRunner.java`
  - Critério de pronto: executa `DynamicFilterResolverBenchmark` e grava JSON no target.
  - Confiança: 🟢

- [ ] T-PERF-02, Implementar baseline de geração/resolução/MVC/cache.
  - Origem no legado: `DynamicFilterResolverBenchmark.java`
  - Critério de pronto: states preparam generator, resolver, input e mocks MVC; benchmarks consomem resultados via Blackhole.
  - Confiança: 🟢

- [ ] T-PERF-03, Implementar benchmark Criteria/fetch/cache.
  - Origem no legado: `DynamicFilterResolverPerf02Benchmark.java`
  - Critério de pronto: Spring/H2 inicia em setup, fecha no teardown e cobre muitos filtros, paths repetidos e cache growth.
  - Confiança: 🟢

- [ ] T-PERF-04, Implementar benchmark de proxy.
  - Origem no legado: `DynamicFilterResolverPerf06ProxyBenchmark.java`
  - Critério de pronto: compara proxy atual e hipótese otimizada, documentando limitação de método privado.
  - Confiança: 🟢

- [ ] T-PERF-05, Implementar benchmark de sort translation.
  - Origem no legado: `DynamicFilterRepositorySortPerfBenchmark.java`
  - Critério de pronto: compara otimizado e legado com muitos orders/filtros e cenário sem tradução.
  - Confiança: 🟢

## Tarefas de Teste

- [ ] TT-PERF-01, Rodar baseline antes/depois de alteração em geração ou resolver. 🟢
- [ ] TT-PERF-02, Rodar Perf02 antes/depois de alteração em Criteria/fetch/cache. 🟢
- [ ] TT-PERF-03, Rodar sort benchmark antes/depois de alterar `updateSortFilterPath`. 🟢
- [ ] TT-PERF-04, Atualizar runner se a intenção for executar todos os benchmarks. 🟡

## Ordem Sugerida

1. Criar fixtures e states.
2. Implementar benchmarks baseline.
3. Implementar benchmarks Spring/H2.
4. Implementar benchmarks comparativos.
5. Documentar resultados no histórico de performance.

## Lacunas Pendentes

- 🟡 Decidir se o runner manual deve incluir todos os benchmarks ou continuar restrito ao baseline.
