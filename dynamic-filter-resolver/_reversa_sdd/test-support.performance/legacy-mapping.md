# Legacy Mapping — Módulo `test-support.performance`

> Gerado pelo Reversa Archaeologist em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Arquivos de Benchmark

🟢 CONFIRMADO

| Arquivo | Papel |
|---|---|
| `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverBenchmarkRunner.java` | Runner manual JMH para `DynamicFilterResolverBenchmark`. |
| `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverBenchmark.java` | Baseline de geração, resolução, argument resolver e cache de annotation metadata. |
| `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverPerf02Benchmark.java` | Benchmarks de Criteria API, fetch decorator e cache sob crescimento/eviction. |
| `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverPerf06ProxyBenchmark.java` | Benchmark de invocação de proxy `Specification`. |
| `src/test/java/com/runestone/dynafilter/modules/jpa/repository/DynamicFilterRepositorySortPerfBenchmark.java` | Benchmark de tradução de sort otimizada versus legado. |

## Hotspots Medidos

🟢 CONFIRMADO

| Hotspot | Classe |
|---|---|
| Geração de statements por annotations | `DynamicFilterResolverBenchmark` |
| Conversão de statement tree para `Specification` | `DynamicFilterResolverBenchmark` |
| Argument resolver MVC com proxy e fetching | `DynamicFilterResolverBenchmark` |
| Cache de `TypeAnnotationUtils` com input reutilizado e equivalente | `DynamicFilterResolverBenchmark`, `DynamicFilterResolverPerf02Benchmark` |
| `Specification.toPredicate` com muitos filtros | `DynamicFilterResolverPerf02Benchmark` |
| Paths aninhados repetidos em Criteria API | `DynamicFilterResolverPerf02Benchmark` |
| Fetch decorators com paths profundos/sobrepostos | `DynamicFilterResolverPerf02Benchmark` |
| Proxy `Specification` reflexivo | `DynamicFilterResolverPerf06ProxyBenchmark` |
| Tradução de sort | `DynamicFilterRepositorySortPerfBenchmark` |

## Observações de Execução

🟡 INFERIDO

| Observação | Evidência |
|---|---|
| O runner manual não executa todos os benchmarks existentes. | `DynamicFilterResolverBenchmarkRunner.java:16` inclui somente `DynamicFilterResolverBenchmark`. |
| Benchmarks que sobem Spring/H2 são mais caros e devem ser executados isoladamente quando necessário. | `DynamicFilterResolverPerf02Benchmark.JpaPredicateState.setup` cria contexto Spring e `EntityManager`. |
