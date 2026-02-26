# Dynamic Filter Resolver - Historico de Performance

## Formato adotado
Para este modulo, o formato mais util e um **Registro de Experimentos de Performance (append-only)** com:

1. Contexto e hipotese
2. Mudancas aplicadas (com commit e arquivos)
3. Protocolo de benchmark (comando exato)
4. Resultado quantitativo (antes/depois)
5. Decisao (aceitar, descartar, ajustar)
6. Licoes aprendidas

Motivo: esse formato preserva historico tecnico e evita repetir analises em cenarios ja validados.

---

## Protocolo padrao de medicao (JMH)
- JVM: Java 21.0.10
- JMH: 1.37
- Parametros:
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Benchmarks:
  - `annotationUtils_newInputInstance`
  - `annotationUtils_reusedInput`
  - `argumentResolver_fetchingDecorator`
  - `argumentResolver_interfaceProxy`
  - `specificationResolver_createFilter`
  - `statementGenerator_searchPeopleAndGames`

Comando de referencia:

```bash
java -cp "$CP" org.openjdk.jmh.Main \
'DynamicFilterResolverBenchmark\.(statementGenerator_searchPeopleAndGames|specificationResolver_createFilter|argumentResolver_interfaceProxy|argumentResolver_fetchingDecorator|annotationUtils_reusedInput|annotationUtils_newInputInstance)' \
-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us -jvmArgs '-Xms1g -Xmx1g' -rf json -rff <arquivo.json> -foe true
```

---

## Experimento PERF-001
- Data: 2026-02-21
- Objetivo: otimizar os hotspots do `dynamic-filter-resolver` sem regressao funcional
- Baseline: `e1501414`
- Tentativa 1: `33a2f4df`
- Tentativa 2: commit `3405adb0`

### Hipoteses
1. Corrigir igualdade/hash semantico de `AnnotationStatementInput` melhora cache por equivalencia.
2. Centralizar metadados em cache unico reduz reflexao repetida.
3. Cache no decorator factory reduz custo de resolucao de beans.
4. Ajustes no fetching melhoram custo de composicao.

### Mudancas feitas na tentativa 1 (`33a2f4df`)
Arquivos principais:
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementInput.java`
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java`
- `src/main/java/com/runestone/dynafilter/modules/jpa/spring/SpringFilterDecoratorFactory.java`
- `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/FetchingFilterDecorator.java`

Resumo:
- Igualdade/hash profundo para `AnnotationStatementInput`
- Cache unico (`ConcurrentHashMap`) para metadados em `TypeAnnotationUtils`
- Cache de decorators no factory Spring
- Ajustes de fetching e deduplicacao

### Resultado da tentativa 1 (`33a2f4df`) - comparado ao baseline (`e1501414`)
| Benchmark | `e1501414` (us/op) | `33a2f4df` (us/op) | Delta |
|---|---:|---:|---:|
| annotationUtils_newInputInstance | 4.497 | 0.424 | -90.57% |
| annotationUtils_reusedInput | 0.007 | 0.390 | +5857.67% |
| argumentResolver_fetchingDecorator | 53.383 | 54.368 | +1.85% |
| argumentResolver_interfaceProxy | 54.943 | 54.580 | -0.66% |
| specificationResolver_createFilter | 0.249 | 0.251 | +1.08% |
| statementGenerator_searchPeopleAndGames | 4.689 | 5.655 | +20.60% |

Diagnostico:
- **Acerto**: ganho expressivo em `newInputInstance`.
- **Erro/Regressao**: `reusedInput` e `statementGenerator_searchPeopleAndGames` pioraram significativamente.

Decisao: **nao encerrar na tentativa 1**; aplicar ajuste no commit `3405adb0`.

### Commit `3405adb0`
Arquivos alterados:
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementInput.java`
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java`
- `src/test/java/com/runestone/dynafilter/core/generator/annotation/TestTypeAnnotationUtils.java`

Ajustes:
1. `AnnotationStatementInput` convertido para classe final com:
   - hash pre-computado (`cachedHashCode`)
   - copia defensiva do array de annotations
2. `TypeAnnotationUtils` com fast-path de cache hit (`get` antes de `computeIfAbsent`)
3. Teste de copia defensiva para estabilidade de chave de cache

### Resultado final (commit `3405adb0`)
| Benchmark | `e1501414` (us/op) | `33a2f4df` (us/op) | `3405adb0` (us/op) | Delta `3405adb0` vs `33a2f4df` | Delta `3405adb0` vs `e1501414` |
|---|---:|---:|---:|---:|---:|
| annotationUtils_newInputInstance | 4.497 | 0.424 | 0.439 | +3.66% | -90.23% |
| annotationUtils_reusedInput | 0.007 | 0.390 | 0.003 | -99.23% | -54.24% |
| argumentResolver_fetchingDecorator | 53.383 | 54.368 | 53.193 | -2.16% | -0.36% |
| argumentResolver_interfaceProxy | 54.943 | 54.580 | 53.526 | -1.93% | -2.58% |
| specificationResolver_createFilter | 0.249 | 0.251 | 0.248 | -1.08% | -0.01% |
| statementGenerator_searchPeopleAndGames | 4.689 | 5.655 | 4.682 | -17.20% | -0.14% |

Conclusao:
- O commit `3405adb0` removeu as regresses principais da tentativa 1.
- Resultado final ficou igual ou melhor que baseline em todos os cenarios medidos.
- Ganho estrutural (cache semantico) foi mantido para `newInputInstance`.

---

## Licoes aprendidas
1. Otimizacao de cache sem medir hot-hit pode introduzir regressao severa.
2. Hash profundo de array em chave muito quente precisa de hash pre-computado.
3. Sempre medir com 3 estados quando possivel:
   - baseline original
   - tentativa otimizada
   - commit `3405adb0`
4. Registrar comando JMH e ambiente evita comparacoes inconsistentes.

---

## Artefatos gerados
- Baseline: `/tmp/runestone-bench-before/dynamic-filter-resolver/target/jmh-before.json`
- Tentativa 1: `/tmp/runestone-bench-after/dynamic-filter-resolver/target/jmh-after.json`
- Commit `3405adb0`: `dynamic-filter-resolver/target/jmh-after-patch.json`
- PERF-002: `dynamic-filter-resolver/target/jmh-perf02.json`
- PERF-003 (after): `dynamic-filter-resolver/target/jmh-perf03-after.json`
- PERF-004 (cache): `dynamic-filter-resolver/target/jmh-perf04-cache.json`
- PERF-005 (sort translation): `dynamic-filter-resolver/target/jmh-perf05-sort-translation.json`
- PERF-006 (proxy path): `dynamic-filter-resolver/target/jmh-perf06-proxy.json`
- PERF-006 (proxy invocation): `dynamic-filter-resolver/target/jmh-perf06-proxy-invocation.json`
- PERF-007 (metadata validation warmup): `dynamic-filter-resolver/target/jmh-perf07-validation-warmup.json`

---

## Experimento PERF-002
- Data: 2026-02-21
- Objetivo: cobrir cenarios de custo nao medidos no PERF-001 (path parsing/join tree, fetching profundo e crescimento de cache)
- Baseline commit: `3405adb0` (referencia funcional/performance dos cenarios anteriores)
- Commit testado: working tree atual (benchmark dedicado PERF-002)

### Hipoteses
1. `toPredicate` com muitos filtros escala com custo relevante por `PropertyPath.from(...)` e busca de joins por segmento.
2. `FetchingFilterDecorator` em paths profundos/sobrepostos adiciona custo perceptivel por requisicao.
3. Cache de anotacoes continua estavel em hot-hit mesmo apos crescimento significativo.

### Mudancas aplicadas
- `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverPerf02Benchmark.java`
  - Novo benchmark JMH com cenarios:
    - `perf02_specification_toPredicate_manyFilters`
    - `perf02_fetchingDecorator_deepPaths`
    - `perf02_annotationUtils_reusedInput_afterCacheGrowth`
    - `perf02_annotationUtils_newEquivalentInput_afterCacheGrowth`

### Protocolo de medicao
- JVM: Java 21.0.10
- JMH: 1.37
- Parametros:
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Comando:

```bash
java -cp "$CP" org.openjdk.jmh.Main \
'DynamicFilterResolverPerf02Benchmark\.(perf02_specification_toPredicate_manyFilters|perf02_fetchingDecorator_deepPaths|perf02_annotationUtils_reusedInput_afterCacheGrowth|perf02_annotationUtils_newEquivalentInput_afterCacheGrowth)' \
-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us -jvmArgs '-Xms1g -Xmx1g' -rf json -rff dynamic-filter-resolver/target/jmh-perf02.json -foe true
```

### Resultado
| Benchmark | Score (us/op) | Erro |
|---|---:|---:|
| perf02_annotationUtils_reusedInput_afterCacheGrowth | 0.004 | +- 0.001 |
| perf02_annotationUtils_newEquivalentInput_afterCacheGrowth | 0.074 | +- 0.002 |
| perf02_fetchingDecorator_deepPaths | 7.374 | +- 0.259 |
| perf02_specification_toPredicate_manyFilters | 43.746 | +- 1.352 |

### Decisao
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura tecnica
1. O maior custo medido nesta rodada ficou em `toPredicate_manyFilters` (~43.7 us/op), validando que parsing de path + composicao de joins e o hotspot dominante fora do PERF-001.
2. O custo de `fetchingDecorator_deepPaths` (~7.4 us/op) e menor, mas nao desprezivel em endpoints de alta taxa.
3. Cenarios de cache apos crescimento permaneceram muito baratos (<0.1 us/op), sem sinal de degradacao relevante nos hits.

### Proximos passos sugeridos para PERF-004
1. Avaliar cache de `PropertyPath`/segmentos pre-parseados por `filter.path`.
2. Avaliar cache de mapeamento de sort (`parameter -> path`) para evitar custo O(ordens x filtros) por chamada.
3. Repetir os cenarios de PERF-002 apos cada tentativa para validar ganho sem regressao.

---

## Experimento PERF-003
- Data: 2026-02-21
- Objetivo: reduzir custo no caminho quente de alta taxa em `JpaPredicateUtils` e `FetchingFilterDecorator`
- Baseline commit: estado pos PERF-002 (arquivo `dynamic-filter-resolver/target/jmh-perf02.json`)
- Commit testado: working tree atual (otimizacoes + cenarios adicionais de JMH)

### Hipotese
1. Evitar `PropertyPath.from(...)` por chamada e reutilizar parse de caminho reduz custo de `toPredicate`.
2. Pre-processar fetch paths no construtor e usar lookup por identidade reduz custo de `FetchingFilterDecorator`.
3. Mesmo com as otimizacoes, os cenarios de cache de anotacao devem permanecer estaveis.

### Mudancas aplicadas
- `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/JpaPredicateUtils.java`
  - cache de parse de path (`PARSED_PATH_CACHE`)
  - parser de path sem `PropertyPath.from(...)`
  - calculo de `JoinType` uma unica vez por chamada
- `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/FetchingFilterDecorator.java`
  - pre-processamento de paths no construtor
  - deduplicacao de paths no setup do decorator
  - lookup por `FetchParent` + segmento para evitar varreduras repetidas de `getFetches()`
- `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverPerf02Benchmark.java`
  - cenarios adicionais:
    - `perf02_specification_toPredicate_repeatedNestedPath`
    - `perf02_fetchingDecorator_overlappingPaths`

### Protocolo de medicao
- JVM: Java 21.0.10
- JMH: 1.37
- Parametros:
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Comando:

```bash
java -cp "$CP" org.openjdk.jmh.Main \
'DynamicFilterResolverPerf02Benchmark\.(perf02_specification_toPredicate_manyFilters|perf02_specification_toPredicate_repeatedNestedPath|perf02_fetchingDecorator_deepPaths|perf02_fetchingDecorator_overlappingPaths|perf02_annotationUtils_reusedInput_afterCacheGrowth|perf02_annotationUtils_newEquivalentInput_afterCacheGrowth)' \
-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us -jvmArgs '-Xms1g -Xmx1g' -rf json -rff dynamic-filter-resolver/target/jmh-perf03-after.json -foe true
```

### Resultado (comparavel com PERF-002)
| Benchmark | PERF-002 (us/op) | PERF-003 (us/op) | Delta |
|---|---:|---:|---:|
| perf02_specification_toPredicate_manyFilters | 43.746 | 38.729 | -11.47% |
| perf02_fetchingDecorator_deepPaths | 7.374 | 5.623 | -23.74% |
| perf02_annotationUtils_reusedInput_afterCacheGrowth | 0.004 | 0.004 | +0.00% |
| perf02_annotationUtils_newEquivalentInput_afterCacheGrowth | 0.074 | 0.078 | +5.41% |

### Resultado (novos cenarios de estresse)
| Benchmark | PERF-003 (us/op) | Erro |
|---|---:|---:|
| perf02_fetchingDecorator_overlappingPaths | 4.340 | +- 0.169 |
| perf02_specification_toPredicate_repeatedNestedPath | 51.516 | +- 2.081 |

### Decisao
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura tecnica
1. Houve ganho real nos dois hotspots de alta taxa (predicates e fetching).
2. A variacao nos benchmarks de cache de anotacao foi pequena e continua na faixa sub-microsegundo.
3. Os novos cenarios adicionados aumentam cobertura para caminhos repetidos/sobrepostos, melhorando confianca para carga real.

---

## Experimento PERF-004
- Data: 2026-02-21
- Objetivo: limitar crescimento de memoria do cache de `TypeAnnotationUtils` sem adicionar dependencia externa
- Baseline commit: estado pos PERF-003 (`dynamic-filter-resolver/target/jmh-perf03-after.json`)
- Commit testado: working tree atual (LRU bounded cache + testes de limite/eviccao)

### Hipotese
1. Cache LRU limitado evita crescimento ilimitado de memoria.
2. Hit-latency permanece baixa (sem regressao relevante para uso real).

### Mudancas aplicadas
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java`
  - cache trocado de `ConcurrentHashMap` ilimitado para LRU bounded (`LinkedHashMap` access-order)
  - tamanho maximo configuravel via system property `runestone.dynafilter.annotation.cache.max-size` (default `4096`)
  - metodos de apoio para teste: `cacheSize()` e `cacheMaxSize()`
- `src/test/java/com/runestone/dynafilter/core/generator/annotation/TestTypeAnnotationUtils.java`
  - teste de limite do cache
  - teste de sobrevivencia de entrada recentemente acessada sob pressao de eviccao
- `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverPerf02Benchmark.java`
  - novo benchmark: `perf04_annotationUtils_hitLatency_lruBoundedCache`

### Protocolo de medicao
- JVM: Java 21.0.10
- JMH: 1.37
- Parametros:
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Comando:

```bash
java -cp "$CP" org.openjdk.jmh.Main \
'DynamicFilterResolverPerf02Benchmark\.(perf02_annotationUtils_reusedInput_afterCacheGrowth|perf02_annotationUtils_newEquivalentInput_afterCacheGrowth|perf04_annotationUtils_hitLatency_lruBoundedCache)' \
-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us -jvmArgs '-Xms1g -Xmx1g' -rf json -rff dynamic-filter-resolver/target/jmh-perf04-cache.json -foe true
```

### Resultado
| Benchmark | PERF-003 (us/op) | PERF-004 (us/op) | Delta |
|---|---:|---:|---:|
| perf02_annotationUtils_reusedInput_afterCacheGrowth | 0.004 | 0.022 | +450.00% |
| perf02_annotationUtils_newEquivalentInput_afterCacheGrowth | 0.078 | 0.092 | +17.95% |
| perf04_annotationUtils_hitLatency_lruBoundedCache | n/a | 0.023 | n/a |

### Decisao
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura tecnica
1. Houve regressao percentual na latencia de hit, mas com impacto absoluto muito baixo (ordem de centesimos de microssegundo).
2. O ganho estrutural de seguranca de memoria (cache bounded) compensa para workloads long-lived.
3. Para cenarios ultra sensiveis de latencia, o proximo passo e avaliar um bounded cache concorrente com menor contencao.

---

## Experimento PERF-005
- Data: 2026-02-21
- Objetivo: remover custo O(ordens x filtros) na traducao de sort em `DynamicFilterJpaRepositoryImpl`
- Baseline commit: estado pos PERF-004
- Commit testado: working tree atual (lookup de parametro para path + benchmark comparativo legado/otimizado)

### Hipotese
1. Traduzir sort em duas fases (`mapa parametro->path` + aplicacao nas ordens) reduz custo para O(ordens + filtros).
2. Em cenarios sem traducao efetiva, retorno do mesmo `Sort` reduz alocacao e latencia.

### Mudancas aplicadas
- `src/main/java/com/runestone/dynafilter/modules/jpa/repository/DynamicFilterJpaRepositoryImpl.java`
  - nova estrategia de traducao com mapa de lookup (uma passada nos filtros + uma passada nas ordens)
  - preservacao da semantica original de prioridade (primeiro match valido)
  - fast-path para retornar o mesmo `Sort` quando nada muda
- `src/test/java/com/runestone/dynafilter/modules/jpa/repository/TestDynamicFilterJpaRepositorySortTranslation.java`
  - testes de traducao, preservacao de direcao, prioridade de duplicidade e caso sem alteracao
- `src/test/java/com/runestone/dynafilter/modules/jpa/repository/DynamicFilterRepositorySortPerfBenchmark.java`
  - benchmark comparativo entre algoritmo legado e otimizado:
    - `perf05_sortTranslation_legacy_manyOrdersManyFilters`
    - `perf05_sortTranslation_legacy_noTranslationNeeded`
    - `perf05_sortTranslation_optimized_manyOrdersManyFilters`
    - `perf05_sortTranslation_optimized_noTranslationNeeded`

### Protocolo de medicao
- JVM: Java 21.0.10
- JMH: 1.37
- Parametros:
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Comando:

```bash
java -cp "$CP" org.openjdk.jmh.Main \
'DynamicFilterRepositorySortPerfBenchmark\.(perf05_sortTranslation_optimized_manyOrdersManyFilters|perf05_sortTranslation_legacy_manyOrdersManyFilters|perf05_sortTranslation_optimized_noTranslationNeeded|perf05_sortTranslation_legacy_noTranslationNeeded)' \
-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us -jvmArgs '-Xms1g -Xmx1g' -rf json -rff dynamic-filter-resolver/target/jmh-perf05-sort-translation.json -foe true
```

### Resultado
| Benchmark | Score (us/op) |
|---|---:|
| perf05_sortTranslation_legacy_manyOrdersManyFilters | 79.466 |
| perf05_sortTranslation_optimized_manyOrdersManyFilters | 6.921 |
| perf05_sortTranslation_legacy_noTranslationNeeded | 139.353 |
| perf05_sortTranslation_optimized_noTranslationNeeded | 1.528 |

### Decisao
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura tecnica
1. Cenario pesado teve reducao de ~91.3% (79.466 -> 6.921 us/op).
2. Cenario sem traducao teve reducao de ~98.9% (139.353 -> 1.528 us/op).
3. O risco de crescimento de latencia com mais filtros/ordens foi significativamente reduzido.

---

## Experimento PERF-006
- Data: 2026-02-21
- Objetivo: reduzir overhead de proxy em `SpecificationDynamicFilterArgumentResolver` e remover dependencia de `method.invoke(...)` no caminho quente de `toPredicate`
- Baseline commit: estado pos PERF-005
- Commit testado: working tree atual (invocation handler otimizado + cobertura funcional adicional)

### Hipotese
1. Delegar `toPredicate` diretamente para `Specification` reduz overhead de invocacao reflexiva.
2. Tratar `default methods` via `InvocationHandler.invokeDefault` evita falhas em interfaces customizadas com metodos default.
3. Cache simples do array de interfaces reduz pequena alocacao por request no `newProxyInstance`.

### Mudancas aplicadas
- `src/main/java/com/runestone/dynafilter/modules/jpa/spring/SpecificationDynamicFilterArgumentResolver.java`
  - substituicao do lambda reflexivo por `SpecificationProxyInvocationHandler`
  - dispatch direto para `toPredicate` sem `method.invoke(...)`
  - suporte explicito a metodos default de interface
  - tratamento consistente de `equals/hashCode/toString`
  - cache de array de interfaces para `Proxy.newProxyInstance`
- `src/test/java/com/runestone/dynafilter/modules/jpa/spring/TestSpecDynaFilterArgumentResolver.java`
  - teste para interface `Specification` com metodo default
  - teste para consistencia de metodos `Object` no proxy
  - teste direto de delegacao de `toPredicate` via `createProxy`
- `src/test/java/com/runestone/dynafilter/modules/jpa/spring/tools/SearchStateWithDefaultMethod.java`
  - fixture de teste para interface custom com default method
- `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverPerf06ProxyBenchmark.java`
  - benchmark focado comparando:
    - `perf06_proxyInvocation_legacyReflective_toPredicate`
    - `perf06_proxyInvocation_optimized_toPredicate`

### Protocolo de medicao
- JVM: Java 21.0.10
- JMH: 1.37
- Parametros:
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Comandos:

```bash
java -cp "$CP" org.openjdk.jmh.Main \
'DynamicFilterResolverBenchmark\.(argumentResolver_interfaceProxy|argumentResolver_fetchingDecorator)' \
-wi 5 -i 8 -w 500ms -r 500ms -f 3 -tu us -jvmArgs '-Xms1g -Xmx1g' -rf json -rff dynamic-filter-resolver/target/jmh-perf06-proxy.json -foe true
```

```bash
java -cp "$CP" org.openjdk.jmh.Main \
'DynamicFilterResolverPerf06ProxyBenchmark\.(perf06_proxyInvocation_optimized_toPredicate|perf06_proxyInvocation_legacyReflective_toPredicate)' \
-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us -jvmArgs '-Xms1g -Xmx1g' -rf json -rff dynamic-filter-resolver/target/jmh-perf06-proxy-invocation.json -foe true
```

### Resultado
| Benchmark | Score (us/op) |
|---|---:|
| argumentResolver_interfaceProxy | 55.298 |
| argumentResolver_fetchingDecorator | 53.780 |
| perf06_proxyInvocation_legacyReflective_toPredicate | 6.231 |
| perf06_proxyInvocation_optimized_toPredicate | 5.671 |

### Decisao
- [ ] Aceitar
- [ ] Ajustar
- [x] Descartar

### Leitura tecnica
1. O benchmark amplo de `argumentResolver_interfaceProxy` variou pouco e com ruido alto, sem ganho claro.
2. O benchmark focado no hotspot de invocacao mostrou reducao de ~8.99% em `toPredicate` (6.231 -> 5.671 us/op).
3. Alem do ganho de latencia no hotspot, a mudanca corrige compatibilidade para interfaces `Specification` com default methods.

### Revisao de decisao
1. Em 2026-02-21, a alteracao em `SpecificationDynamicFilterArgumentResolver` foi revertida por relacao custo/beneficio desfavoravel.
2. O ganho medido nao justificou o aumento de complexidade no codigo de producao para o contexto atual.
3. Os testes e benchmarks de PERF-006 foram mantidos como referencia tecnica; os testes que dependiam do comportamento revertido foram marcados com `@Disabled`.

---

## Experimento PERF-007
- Data: 2026-02-21
- Objetivo: remover validacao repetida de metadado por request em `AnnotationStatementGenerator` e realizar warmup de metadado na inicializacao
- Baseline commit: estado pos PERF-006
- Commit testado: working tree atual (pre-validacao de metadado em cache + warmup no BeanPostProcessor)

### Hipotese
1. Validar filtros durante build de metadado cacheado elimina trabalho redundante no caminho quente.
2. Warmup na inicializacao reduz custo de primeira chamada e antecipa falha de configuracao invalida.

### Mudancas aplicadas
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java`
  - validacao de configuracao de filtros movida para `buildMetadata(...)`
  - validacao aplicada a filtros diretos e filtros de statements aninhados
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementGenerator.java`
  - remocao da validacao por request em `processFilterAnnotations(...)`
- `src/main/java/com/runestone/dynafilter/modules/jpa/spring/FilterConfigurationAnalyserBeanPostProcessor.java`
  - warmup de metadado para parametros suportados (`ConditionalStatement` e interfaces `Specification`)
  - manutencao da validacao de paths quando `entityClass` estiver disponivel
- `src/test/java/com/runestone/dynafilter/core/generator/annotation/TestValidationAnnotationStatementGenerator.java`
  - novo teste de fail-fast durante warmup de metadado
- `src/test/java/com/runestone/dynafilter/modules/jpa/spring/TestFilterConfigurationAnalyserBeanPostProcessor.java`
  - testes de warmup para configuracao valida e invalida

### Protocolo de medicao
- JVM: Java 21.0.10
- JMH: 1.37
- Parametros:
  - `-wi 5 -i 8 -w 500ms -r 500ms -f 3 -tu us`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Comando:

```bash
java -cp "$CP" org.openjdk.jmh.Main \
'DynamicFilterResolverBenchmark\.statementGenerator_searchPeopleAndGames' \
-wi 5 -i 8 -w 500ms -r 500ms -f 3 -tu us -jvmArgs '-Xms1g -Xmx1g' -rf json -rff dynamic-filter-resolver/target/jmh-perf07-validation-warmup.json -foe true
```

### Resultado
| Benchmark | Referencia anterior (us/op) | PERF-007 (us/op) | Delta |
|---|---:|---:|---:|
| statementGenerator_searchPeopleAndGames | 4.682 | 4.172 | -10.89% |

### Decisao
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura tecnica
1. A remocao da validacao por request reduziu o custo medio do statement generator no cenario medido.
2. Warmup agora detecta configuracao invalida na inicializacao de controllers REST suportados.
3. O custo de validacao passou para fase de build de metadado cacheado, reduzindo trabalho redundante por requisicao.

---

## Template para proximos experimentos
Copiar e preencher:

```md
## Experimento PERF-XXX
- Data:
- Objetivo:
- Baseline commit:
- Commit testado:

### Hipotese

### Mudancas aplicadas
- Arquivo:
- Arquivo:

### Protocolo de medicao
- Comando:
- Ambiente:

### Resultado
| Benchmark | Antes | Depois | Delta |
|---|---:|---:|---:|

### Decisao
- [ ] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Licoes aprendidas
```
