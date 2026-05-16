# Test Support Performance, Estratégia de Testes

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Benchmarks Disponíveis

| Benchmark | Hotspot | Confiança |
|---|---|---|
| `DynamicFilterResolverBenchmark.statementGenerator_searchPeopleAndGames` | Geração de statements. | 🟢 |
| `DynamicFilterResolverBenchmark.specificationResolver_createFilter` | Conversão para `Specification`. | 🟢 |
| `DynamicFilterResolverBenchmark.argumentResolver_interfaceProxy` | Resolver MVC com proxy. | 🟢 |
| `DynamicFilterResolverBenchmark.argumentResolver_fetchingDecorator` | Resolver MVC com fetching/decorator. | 🟢 |
| `DynamicFilterResolverBenchmark.annotationUtils_*` | Cache de annotations. | 🟢 |
| `DynamicFilterResolverPerf02Benchmark.perf02_*` | Criteria, fetch e cache sob carga. | 🟢 |
| `DynamicFilterResolverPerf06ProxyBenchmark.perf06_*` | Custo de proxy. | 🟢 |
| `DynamicFilterRepositorySortPerfBenchmark.perf05_*` | Sort translation. | 🟢 |

## Comandos de Verificação

```shell
mvn test -pl dynamic-filter-resolver -Dtest=DynamicFilterResolverBenchmarkRunner
```

🟡 **INFERIDO** — Para JMH completo, usar includes específicos de benchmark via Maven/JMH conforme configuração local do projeto.

## Critérios de Uso

- 🟢 Alterações em `TypeAnnotationUtils` exigem benchmark de cache.
- 🟢 Alterações em `JpaPredicateUtils` ou `Specification*` exigem Perf02.
- 🟢 Alterações em `DynamicFilterJpaRepositoryImpl.updateSortFilterPath` exigem benchmark de sort.
- 🟡 Alterações em proxy de `Specification` exigem Perf06 e decisão explícita sobre complexidade.
