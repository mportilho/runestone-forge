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
