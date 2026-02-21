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
