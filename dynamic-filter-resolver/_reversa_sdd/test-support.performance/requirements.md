# Test Support Performance

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Visão Geral

🟢 **CONFIRMADO** — A unit `test-support.performance` reúne benchmarks JMH e fixtures de medição para validar hotspots do `dynamic-filter-resolver`.

## Responsabilidades

- 🟢 Medir geração de statements por annotations.
- 🟢 Medir conversão de `StatementWrapper` para `Specification`.
- 🟢 Medir argument resolver MVC com proxy e fetching.
- 🟢 Medir cache de annotations com input reutilizado/equivalente e sob crescimento.
- 🟢 Medir custo de Criteria API com muitos filtros e paths aninhados repetidos.
- 🟢 Medir fetch decorators com paths profundos/sobrepostos.
- 🟢 Medir proxy `Specification` reflexivo.
- 🟢 Medir tradução de sort otimizada versus algoritmo legado.

## Requisitos Funcionais

| ID | Requisito | Prioridade | Critério de Aceite |
|----|-----------|-----------|-------------------|
| RF-PERF-01 | 🟢 Fornecer runner JMH manual. | Should | Runner executa benchmark baseline e grava JSON em `target/jmh-result.json`. |
| RF-PERF-02 | 🟢 Medir baseline de geração/resolução/cache/MVC. | Must | Benchmarks em `DynamicFilterResolverBenchmark` executam sem depender de aplicação externa. |
| RF-PERF-03 | 🟢 Medir Criteria/fetch/cache com Spring/H2. | Should | `Perf02` sobe contexto Spring/H2 e fecha recursos no teardown. |
| RF-PERF-04 | 🟢 Comparar proxy reflexivo e hipótese otimizada. | Could | `Perf06` compara proxies com mocks Criteria. |
| RF-PERF-05 | 🟢 Comparar sort translation otimizada e legado. | Should | Benchmark cobre muitos orders/filtros e cenário sem tradução. |

## Requisitos Não Funcionais

| Tipo | Requisito inferido | Evidência | Confiança |
|---|---|---|---|
| Performance | Otimizações devem ser justificadas por benchmark, não hipótese. | ADR 005, `docs/performance-history.md` | 🟢 |
| Reprodutibilidade | Benchmarks devem fixar warmup, measurement, fork e unidade. | Classes JMH documentadas no `data-dictionary.md` | 🟢 |
| Isolamento | Contexto Spring/H2 deve fechar recursos após trial. | `DynamicFilterResolverPerf02Benchmark.JpaPredicateState.tearDown` | 🟢 |

## Critérios de Aceitação

```gherkin
Cenário: executar benchmark baseline
Dado o runner JMH manual
Quando ele for executado
Então deve produzir resultado JSON em dynamic-filter-resolver/target/jmh-result.json

Cenário: medir Criteria com H2
Dado o benchmark Perf02
Quando o trial iniciar
Então contexto Spring e EntityManager devem ser criados
E ao final devem ser fechados

Cenário: comparar sort translation
Dado 50 orders e 600 filtros
Quando benchmark otimizado e legado forem executados
Então ambos devem produzir resultado funcional comparável para análise de performance
```

## Rastreabilidade

| Arquivo | Papel | Cobertura |
|---|---|---|
| `DynamicFilterResolverBenchmarkRunner.java` | Runner manual | 🟢 |
| `DynamicFilterResolverBenchmark.java` | Baseline geral | 🟢 |
| `DynamicFilterResolverPerf02Benchmark.java` | Criteria, fetch e cache | 🟢 |
| `DynamicFilterResolverPerf06ProxyBenchmark.java` | Proxy invocation | 🟢 |
| `DynamicFilterRepositorySortPerfBenchmark.java` | Sort translation | 🟢 |
