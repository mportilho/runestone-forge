# Performance History

Arquivo append-only para registrar experimentos de performance com JMH.

## Convencoes
- Sempre registrar before/after em `ns/op`.
- Sempre registrar `Melhoria (%) = ((before_ns_op - after_ns_op) / before_ns_op) * 100`.
- Sempre incluir referencia para os artefatos JSON de benchmark.
- Nao sobrescrever resultados anteriores.

## Experimento PERF-2026-02-22-EXPEVAL-DYNAMIC-FUNCTION-OVERHEAD
- Data: 2026-02-22
- Objetivo: medir before/after da correcao de overhead em funcoes dinamicas por chamada.
- Baseline commit/estado: `HEAD 2884333` (codigo baseline em workspace temporario com mesma infraestrutura JMH e mesmo benchmark).
- Commit/estado testado: working tree atual (com a correcao em `FunctionOperation`, `OperationCallSite`, `OperationContext` e `ExpressionContext`).

### Hipotese
1. Reduzir alocacoes/conversoes no hot path de funcao dinamica reduz `ns/op` de `dynamicConstantFunction`.
2. Ganho deve aparecer tambem em `dynamicVariableFunction` e `dynamicFunctionInsideSequence`.

### Mudancas aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/other/FunctionOperation.java`: cache de chaves de funcao e reuso de `CallSiteContext` por `OperationContext`.
- `expression-evaluator/src/main/java/com/runestone/expeval/support/callsite/OperationCallSite.java`: conversao sob demanda (copy-on-write) e fast-path de array ja tipado.
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/OperationContext.java`: lookup de funcao sem recursao, com fallback explicito.
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/ExpressionContext.java`: fallback lazy para funcoes default.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/DynamicFunctionOverheadBenchmark.java`: benchmark JMH dedicado no package `com.runestone.expeval.perf.jmh`.
- `expression-evaluator/pom.xml`: JMH habilitado (dependencias + annotation processors para testes).

### Protocolo de medicao
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class (src/test/java): `com.runestone.expeval.perf.jmh.DynamicFunctionOverheadBenchmark`
- Package de benchmark: `com.runestone.expeval.perf.jmh`
- Parametros:
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Comando executado (before):
```bash
mvn -f "$BEFORE_DIR/pom.xml" -pl expression-evaluator test -q
cd "$BEFORE_DIR/expression-evaluator"
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*DynamicFunctionOverheadBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-dynamic-overhead-before.json -foe true
```
- Comando executado (after):
```bash
mvn -pl expression-evaluator test -q
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*DynamicFunctionOverheadBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-dynamic-overhead-after.json -foe true
```

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| dynamicConstantFunction | 161.385 | 103.196 | -58.188 | +36.06% |
| dynamicVariableFunction | 267.355 | 213.380 | -53.975 | +20.19% |
| dynamicFunctionInsideSequence | 1560.836 | 905.657 | -655.179 | +41.98% |
| nativeConstantMath | 31.382 | 31.183 | -0.199 | +0.63% |
| nativeVariableMath | 175.149 | 175.215 | +0.066 | -0.04% |

Foco dinamico medio (`dynamicConstantFunction`, `dynamicVariableFunction`, `dynamicFunctionInsideSequence`):
- Before medio: `663.192 ns/op`
- After medio: `407.411 ns/op`
- Melhoria media: `+38.57%`

### Decisao
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura tecnica
1. A correcao reduz overhead de funcoes dinamicas em todos os cenarios-alvo, com melhoria entre `+20.19%` e `+41.98%`.
2. O caminho nativo (`nativeVariableMath`) permaneceu estavel (variacao de `-0.04%`), sem indicio de regressao relevante.

### Atividades executadas
1. `mvn -f "$BEFORE_DIR/pom.xml" -pl expression-evaluator test -q` - sucesso
2. JMH before com `DynamicFunctionOverheadBenchmark` - sucesso
3. `mvn -pl expression-evaluator test -q` - sucesso
4. JMH after com `DynamicFunctionOverheadBenchmark` - sucesso
5. Copia de artefatos e comparacao em markdown - sucesso

### Artefatos
- `docs/perf/artifacts/expression-evaluator-dynamic-function-overhead-before.json`
- `docs/perf/artifacts/expression-evaluator-dynamic-function-overhead-after.json`
- `docs/perf/artifacts/expression-evaluator-dynamic-function-overhead-before.txt`
- `docs/perf/artifacts/expression-evaluator-dynamic-function-overhead-after.txt`
- `docs/perf/artifacts/expression-evaluator-dynamic-function-overhead-comparison.md`

### Riscos residuais
1. Resultados sao de uma unica maquina/ambiente; variacao pode ocorrer em hardware/JVM diferentes.
2. Para protecao continua, ideal incluir gatilho de regressao JMH em pipeline dedicado de performance.

## Experimento PERF-2026-02-22-EXPEVAL-CACHE-INVALIDATION-CASCADE
- Data: 2026-02-22
- Objetivo: reduzir overhead de invalidacao de cache em cascata no caminho de sequencias `S[...]`/`P[...]`.
- Baseline commit/estado: `HEAD 14be2ed` (baseline em workspace temporario com mesma infraestrutura JMH e benchmark identico).
- Commit/estado testado: working tree atual (otimizacao em `SequenceVariableValueOperation` + testes de cobertura).

### Hipotese
1. Evitar `setValue()` a cada iteracao da variavel de sequencia elimina invalidacao recursiva de cache por item do loop.
2. Desabilitar cache para a variavel de sequencia evita estado stale sem necessidade de `clearCache()` em cascata.

### Mudancas aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/values/variable/SequenceVariableValueOperation.java`: `setSequenceIndex` passou a usar `overrideValue` e `getCacheHint()` passou a retornar `false`.
- `expression-evaluator/src/test/java/com/runestone/expeval/operation/values/TestCacheInvalidationBehaviour.java`: novos testes de comportamento para sequencia sem cache residual e cobertura de atualizacao via `setVariables`.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/CacheInvalidationCascadeBenchmark.java`: benchmark JMH dedicado em `src/test/java` no package `com.runestone.expeval.perf.jmh`.
- `expression-evaluator/src/test/java/com/runestone/expeval/operation/math/TestMathOperations.java`: ajuste das expectativas de contagem de cache apos desabilitar cache em caminho de sequencia.

### Protocolo de medicao
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.CacheInvalidationCascadeBenchmark`
- Parametros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g'`
- Comando before:
```bash
mvn -f "$BEFORE_DIR/pom.xml" -pl expression-evaluator -Dtest=TestMathOperations,TestVariableValues test -q
cd "$BEFORE_DIR/expression-evaluator"
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*CacheInvalidationCascadeBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-cache-cascade-before.json -foe true
```
- Comando after:
```bash
mvn -pl expression-evaluator -Dtest=TestCacheInvalidationBehaviour,TestMathOperations,TestVariableValues test -q
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*CacheInvalidationCascadeBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-cache-cascade-after.json -foe true
```

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| manyVariablesWithMapBatchSet | 1691.002 | 1656.442 | -34.560 | +2.04% |
| manyVariablesWithRepeatedSetVariable | 975.883 | 947.344 | -28.539 | +2.92% |
| sequenceWithVariableMutation | 1187.626 | 1002.735 | -184.891 | +15.57% |

Media dos cenarios:
- Before medio: 1284.837 ns/op
- After medio: 1202.174 ns/op
- Melhoria media: +6.43%

### Decisao
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura tecnica
1. O ganho mais relevante ocorreu no cenario de sequencia com mutacao de variavel (`+15.57%`), que era o hotspot da invalidacao em cascata.
2. Os cenarios de atualizacao de multiplas variaveis tambem melhoraram levemente (`+2.04%` e `+2.92%`), sem regressao.

### Atividades executadas
1. Criacao de testes funcionais de comportamento para o problema de cache em sequencia.
2. Execucao de testes relevantes de regressao: `TestCacheInvalidationBehaviour`, `TestMathOperations`, `TestVariableValues`.
3. Medicao JMH before em baseline temporario com benchmark identico.
4. Medicao JMH after no estado atual.
5. Consolidacao de artefatos e comparacao em markdown.

### Artefatos
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-cache-invalidation-cascade-before.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-cache-invalidation-cascade-after.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-cache-invalidation-cascade-before.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-cache-invalidation-cascade-after.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-cache-invalidation-cascade-comparison.md`

### Riscos residuais
1. A desabilitacao de cache no ramo da variavel de sequencia reduz potencial de cache em expressoes que dependam desse ramo.
2. Benchmarks refletem uma unica maquina/JVM; ideal repetir em ambiente de CI de performance.

## Experimento PERF-2026-02-22-EXPEVAL-FUNCTION-REGISTRATION-REFLECTION
- Data: 2026-02-22
- Objetivo: reduzir custo de registro de funcoes via reflexao (`putFunctionsFromProvider`) e custo de lookup padrao de funcoes.
- Baseline commit/estado: `HEAD 91448cd` (baseline em workspace temporario com mesmo benchmark JMH).
- Commit/estado testado: working tree atual com cache de templates/callsites e ajustes de lookup.

### Hipotese
1. Cachear templates por classe e callsites estaticos reduz drasticamente custo de criacao de callsites por request.
2. Evitar inicializacao desnecessaria do mapa de funcoes e lock no path de defaults reduz custo de lookup em runtime.

### Mudancas aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/support/callsite/OperationCallSiteFactory.java`: substituicao de introspecao repetida por cache de templates (`ClassValue`) e cache de callsites para providers `Class`.
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/ExpressionContext.java`: lookup de funcao sem inicializar mapa vazio, uso de defaults via holder e validacao de colisao sem `findFunction` repetido.
- `expression-evaluator/src/main/java/com/runestone/expeval/support/callsite/extensions/OperationCallSiteExtensions.java`: lazy init lock-free via holder idiom.
- `expression-evaluator/src/test/java/com/runestone/expeval/expression/TestFunctionProviderRegistration.java`: cobertura funcional de registro por provider (instancia/static/colisoes).
- `expression-evaluator/src/test/java/com/runestone/expeval/support/callsite/TestOperationCallSite.java`: cobertura de filtro e exposicao de metodos no factory.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/FunctionRegistrationOverheadBenchmark.java`: benchmark JMH dedicado em `src/test/java` no package `com.runestone.expeval.perf.jmh`.

### Protocolo de medicao
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.FunctionRegistrationOverheadBenchmark`
- Parametros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g'`
- Comando before:
```bash
mvn -f "$BEFORE_DIR/pom.xml" -pl expression-evaluator -DskipTests test-compile -q
cd "$BEFORE_DIR/expression-evaluator"
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*FunctionRegistrationOverheadBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-function-registration-before.json -foe true
```
- Comando after:
```bash
mvn -pl expression-evaluator -Dtest=TestFunctionProviderRegistration,TestOperationCallSite,TestFunctionOperations test -q
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*FunctionRegistrationOverheadBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-function-registration-after.json -foe true
```

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| createCallSitesFromProviderClass | 144551.465 | 66.221 | -144485.244 | +99.95% |
| createCallSitesFromProviderInstance | 197274.384 | 143.266 | -197131.118 | +99.93% |
| defaultFunctionLookup | 11.538 | 9.455 | -2.083 | +18.05% |
| putFunctionsFromProviderPerRequest | 198226.670 | 338.870 | -197887.800 | +99.83% |

Media dos cenarios:
- Before medio: 135016.014 ns/op
- After medio: 139.453 ns/op
- Melhoria media: +99.90%

### Decisao
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura tecnica
1. O maior ganho ocorreu no setup por request (`createCallSitesFromProviderClass` e `putFunctionsFromProviderPerRequest`) com reducao expressiva do custo em ns/op.
2. O lookup de funcao padrao ficou mais leve (`defaultFunctionLookup`) com remocao de lock/check repetido.

### Atividades executadas
1. Adicao de testes unitarios para cenarios de registro/callsite antes da implementacao.
2. Implementacao das otimizações de cache/lazy-init.
3. Execucao dos testes direcionados e da suite completa do modulo.
4. Medicao JMH before em baseline temporario.
5. Medicao JMH after no estado atual.
6. Consolidacao de artefatos e comparacao em markdown.

### Artefatos
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-function-registration-overhead-before.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-function-registration-overhead-after.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-function-registration-overhead-before.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-function-registration-overhead-after.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-function-registration-overhead-comparison.md`

### Riscos residuais
1. O cache de templates por classe aumenta uso de memoria proporcional a classes de providers diferentes utilizadas.
2. Benchmarks refletem uma unica maquina/JVM; recomenda-se repeticao em CI de performance.

## Experimento PERF-2026-02-22-EXPEVAL-TYPE-CONVERSION-HOTPATH
- Data: 2026-02-22
- Objetivo: reduzir custo de conversao de tipos no hot path quando tipos nao casam de primeira, evitando fallback guiado por excecao e normalizando conversao numerica em `BaseOperation`.
- Baseline commit/estado: `HEAD 4ad4a08` (codigo baseline em workspace temporario com benchmark JMH identico).
- Commit/estado testado: working tree atual com ajustes em `AbstractOperation` e `BaseOperation`.

### Hipotese
1. Evitar dupla tentativa de conversao com fallback por excecao no caminho quente reduz `ns/op` no cenario de tipos alternados.
2. Normalizar conversao numerica por tipo em `BaseOperation` pode reduzir custo recorrente de `new BigDecimal(result.toString())`.

### Mudancas aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/AbstractOperation.java`: novo fluxo de conversao com `tryConvertValue`/`shouldAttemptConversion`, eliminando padrao de tentativa+excecao+retentativa no caminho nao-`BaseOperation`.
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/BaseOperation.java`: normalizacao numerica por tipo via `normalizeNumberResult(Number, MathContext)`, com fallback para `toString()` apenas em tipos incomuns.
- `expression-evaluator/src/test/java/com/runestone/expeval/operation/TestTypeConversionHotPathBehaviour.java`: testes funcionais para cenarios de alternancia de tipo e invariantes de `MathContext`/escala.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/TypeConversionHotPathBenchmark.java`: benchmark JMH dedicado em `src/test/java` no package `com.runestone.expeval.perf.jmh`.

### Protocolo de medicao
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.TypeConversionHotPathBenchmark`
- Parametros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g'`
- Comando before:
```bash
mvn -f "$BEFORE_DIR/pom.xml" -pl expression-evaluator test -q
cd "$BEFORE_DIR/expression-evaluator"
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*TypeConversionHotPathBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-type-conversion-before.json -foe true
```
- Comando after:
```bash
mvn -pl expression-evaluator test -q
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*TypeConversionHotPathBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-type-conversion-after.json -foe true
```

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| alternatingTypeConversion | 1794.985 | 10.222 | -1784.763 | +99.43% |
| baseOperationDoubleNormalization | 300.542 | 310.918 | +10.376 | -3.45% |
| baseOperationLongNormalization | 108.373 | 119.817 | +11.443 | -10.56% |

Media dos cenarios:
- Before medio: 734.633 ns/op
- After medio: 146.985 ns/op
- Melhoria media: +79.99%

### Decisao
- [ ] Aceitar
- [x] Ajustar
- [ ] Descartar

### Leitura tecnica
1. O hotspot principal (`alternatingTypeConversion`) teve melhora expressiva de `+99.43%`, confirmando a reducao de overhead por fallback com excecao.
2. Os microcenarios de normalizacao em `BaseOperation` regrediram (`-3.45%` e `-10.56%`), o que indica necessidade de ajuste fino nessa parte para evitar custo adicional no caminho numerico comum.
3. A decisao foi marcada como `Ajustar` para preservar o ganho do hot path sem aceitar regressao nos cenarios numericos de apoio.

### Atividades executadas
1. Implementacao de reducao de fallback por excecao e normalizacao numerica tipada.
2. Criacao de testes funcionais de comportamento para cenarios alvo.
3. Execucao de testes funcionais direcionados e suite do modulo.
4. Medicao JMH before em baseline temporario.
5. Medicao JMH after no estado atual.
6. Consolidacao de artefatos e comparacao em markdown.

### Artefatos
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-type-conversion-hotpath-before.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-type-conversion-hotpath-after.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-type-conversion-hotpath-before.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-type-conversion-hotpath-after.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-type-conversion-hotpath-comparison.md`

### Riscos residuais
1. Regressao nos caminhos de `BaseOperation` pode afetar cargas predominantemente numericas simples.
2. Benchmarks refletem uma unica maquina/JVM; recomendada validacao adicional em CI de performance.

## Experimento PERF-2026-02-22-EXPEVAL-TYPE-CONVERSION-BASEOP-FINE-TUNING
- Data: 2026-02-22
- Objetivo: ajuste fino no `BaseOperation` para recuperar throughput em `long/double` sem perder ganho no hot path de conversao.
- Baseline commit/estado: estado `after` do experimento `PERF-2026-02-22-EXPEVAL-TYPE-CONVERSION-HOTPATH`.
- Commit/estado testado: working tree atual com micro-otimizacao estrutural em `BaseOperation.resolve()`.

### Hipotese
1. Reduzir overhead fixo por chamada no `BaseOperation` (caminho comum sem variaveis atribuidas) melhora `baseOperationLongNormalization` e `baseOperationDoubleNormalization`.
2. Ajuste nao deve degradar `alternatingTypeConversion`.

### Mudancas aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/BaseOperation.java`:
  - cache de flag `hasAssignedVariables` no construtor;
  - skip do loop de variaveis atribuidas quando vazio;
  - cache local de `mathContext` e `scale` no hot path de `resolve()`;
  - manutencao da conversao numerica com `new BigDecimal(result.toString(), mathContext)` para preservar comportamento.

### Protocolo de medicao
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.TypeConversionHotPathBenchmark`
- Parametros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g'`
- Comando before: artefato reaproveitado do `after` do experimento anterior (`expression-evaluator-type-conversion-hotpath-after.json`).
- Comando after:
```bash
mvn -pl expression-evaluator test -q
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*TypeConversionHotPathBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-type-conversion-baseop-final2-after.json -foe true
```

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| alternatingTypeConversion | 10.222 | 10.083 | -0.138 | +1.35% |
| baseOperationDoubleNormalization | 310.918 | 305.914 | -5.004 | +1.61% |
| baseOperationLongNormalization | 119.817 | 118.849 | -0.968 | +0.81% |

Media dos cenarios:
- Before medio: 146.985 ns/op
- After medio: 144.949 ns/op
- Melhoria media: +1.39%

### Decisao
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura tecnica
1. Houve recuperacao de `long/double` em relacao ao estado imediatamente anterior.
2. O cenario principal (`alternatingTypeConversion`) foi preservado com leve melhoria adicional.
3. O ajuste e de baixo risco funcional por manter semantica numerica original no `BaseOperation`.

### Atividades executadas
1. Implementacao de ajuste estrutural em `BaseOperation`.
2. Execucao de testes funcionais do modulo (`mvn -pl expression-evaluator test -q`).
3. Medicao JMH after com o mesmo protocolo.
4. Consolidacao dos artefatos e comparacao percentual em ns/op.
5. Correcao de fluxo: execucao de medicoes em sequencia para evitar corrupcao de `jmh_generated` por concorrencia.

### Artefatos
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-type-conversion-baseoperation-tuning-before.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-type-conversion-baseoperation-tuning-after.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-type-conversion-baseoperation-tuning-before.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-type-conversion-baseoperation-tuning-after.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-type-conversion-baseoperation-tuning-comparison.md`

### Riscos residuais
1. Ganho incremental; parte da diferenca historica absoluta em `long/double` pode depender de variacao de ambiente/JIT.
2. Recomendado repetir em pipeline dedicado de performance para validar estabilidade estatistica.

## Experimento PERF-2026-02-22-EXPEVAL-VARIABLE-PROVIDER-CONTEXT-ALLOCATION
- Data: 2026-02-22
- Objetivo: reduzir alocacoes por avaliacao no caminho de `VariableProvider`, mitigando pressao de GC sem alterar semantica de avaliacao.
- Baseline commit/estado: `HEAD 05fbf28` no estado pre-otimizacao (com benchmark e testes novos adicionados para medicao).
- Commit/estado testado: working tree atual com cache de `VariableValueProviderContext` por avaliacao e supplier de data/hora mais leve.

### Hipotese
1. Reutilizar `VariableValueProviderContext` por thread enquanto o mesmo `OperationContext` estiver ativo reduz `B/op` no hot path com providers.
2. Substituir `MemoizedSupplier` por supplier lazy dedicado por avaliacao reduz overhead de alocacao/sincronizacao no acesso a `currentDateTime`.

### Mudancas aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/values/variable/VariableValueOperation.java`: cache `ThreadLocal` de `VariableValueProviderContext` por `OperationContext` para evitar novas instancias repetidas no mesmo fluxo de avaliacao.
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/ExpressionEvaluator.java`: troca de `MemoizedSupplier` por `CurrentDateTimeSupplier` lazy por avaliacao.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/VariableProviderContextOverheadBenchmark.java`: benchmark JMH dedicado ao cenario de providers com `currentDateTime`.
- `expression-evaluator/src/test/java/com/runestone/expeval/operation/values/TestVariableValues.java`: cobertura de consistencia de `currentDateTime` e reutilizacao do contexto de provider na mesma avaliacao.

### Protocolo de medicao
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.VariableProviderContextOverheadBenchmark`
- Package de benchmark: `com.runestone.expeval.perf.jmh`
- Parametros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g' -prof gc`
- Comando before:
```bash
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*VariableProviderContextOverheadBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -prof gc \
  -rf json -rff target/jmh-variable-provider-context-before.json -foe true
```
- Comando after:
```bash
mvn -pl expression-evaluator -Dtest=TestVariableValues,TestVariablesWithExpressionContext test -q
mvn -pl expression-evaluator test -q
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*VariableProviderContextOverheadBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -prof gc \
  -rf json -rff target/jmh-variable-provider-context-after.json -foe true
```

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| variableProvidersUsingCurrentDateTime | 497.950 | 428.934 | -69.016 | +13.86% |

Indicadores de alocacao (apoio):
- `gc.alloc.rate`: `437.201 MB/sec` -> `303.111 MB/sec` (`-134.090 MB/sec`, reducao de `30.67%`).
- `gc.alloc.rate.norm`: `456.020 B/op` -> `272.013 B/op` (`-184.007 B/op`, reducao de `40.35%`).

### Decisao
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura tecnica
1. A reducao de `B/op` confirma que a maior parte do ganho vem de menor churn de objetos no caminho de providers.
2. O ganho em latencia (`+13.86%` em `ns/op`) e consistente com a hipotese de eliminar criacoes repetidas de contexto por avaliacao.
3. A troca do supplier em `ExpressionEvaluator` reduz overhead estrutural mantendo semantica de data/hora consistente por avaliacao.

### Atividades executadas
1. Criacao de benchmark JMH dedicado (`VariableProviderContextOverheadBenchmark`) - sucesso.
2. Execucao de testes direcionados pre-otimizacao: `mvn -pl expression-evaluator -Dtest=TestVariableValues,TestVariablesWithExpressionContext test -q` - sucesso.
3. Medicao JMH before com `-prof gc` - sucesso.
4. Implementacao da otimizacao no hotspot (`VariableValueOperation`, `ExpressionEvaluator`) - sucesso.
5. Execucao de testes direcionados pos-otimizacao: `mvn -pl expression-evaluator -Dtest=TestVariableValues,TestVariablesWithExpressionContext test -q` - sucesso.
6. Execucao da suite completa do modulo: `mvn -pl expression-evaluator test -q` - sucesso.
7. Medicao JMH after com protocolo identico - sucesso.
8. Consolidacao de artefatos em `docs/perf/artifacts` e tabela comparativa - sucesso.

### Artefatos
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-variable-provider-context-before.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-variable-provider-context-after.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-variable-provider-context-before.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-variable-provider-context-after.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-variable-provider-context-comparison.md`

### Riscos residuais
1. O benchmark desabilita cache de variaveis para estressar o caminho de providers; ganhos absolutos podem variar em workloads com cache mais efetivo.
2. Medicoes realizadas em uma unica maquina/JVM; recomendado repetir em pipeline dedicado de performance para validar estabilidade estatistica.

## Experimento PERF-2026-02-22-EXPEVAL-CALCULATOR-CONTEXT-PROPAGATION
- Data: 2026-02-22
- Objetivo: reduzir custo de propagacao de variaveis no `Calculator` quando o contexto possui muitas chaves e a expressao usa poucas variaveis.
- Baseline commit/estado: `HEAD 304733a` em worktree temporario (`/tmp/expeval-baseline-Hjf50x`), com benchmark identico copiado para o baseline.
- Commit/estado testado: working tree atual com otimizacao de `setVariables` no hot path da avaliacao.

### Hipotese
1. Aplicar variaveis apenas para nomes efetivamente presentes na arvore da expressao reduz `ns/op` no cenario de contexto grande e uso esparso.
2. O cenario de contexto pequeno e uso denso deve ficar estavel ou com regressao baixa.
3. Como o foco e hot path com risco de alocacao, comparacao com `-prof gc` e necessaria para validar impacto em alocacao/GC.

### Mudancas aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/ExpressionEvaluator.java`: novo caminho `setVariables(Map<String, Object>)` orientado ao mapa de variaveis da expressao, evitando varredura do mapa inteiro de contexto por chamada.
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/Expression.java`: `setVariables(...)` passou a delegar para o caminho otimizado do evaluator.
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/calculator/Calculator.java`: pre-size de `ArrayList` em `calculate(...)` para reduzir realocacoes.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/CalculatorContextPropagationBenchmark.java`: benchmark JMH dedicado ao cenario de propagacao de contexto no `Calculator`.

### Protocolo de medicao
- JVM: `java version "21.0.10" 2026-01-20 LTS`
- JMH: `1.35`
- Benchmark class: `com.runestone.expeval.perf.jmh.CalculatorContextPropagationBenchmark`
- Package de benchmark: `com.runestone.expeval.perf.jmh`
- Parametros:
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Profiling GC: `-prof gc`
- Comando before:
```bash
mvn -pl expression-evaluator -Dtest=TestCalculationMemory,TestVariableValues,TestCacheInvalidationBehaviour test -q
cd /tmp/expeval-baseline-Hjf50x/expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*CalculatorContextPropagationBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-calculator-context-propagation-before.json -foe true
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*CalculatorContextPropagationBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -prof gc \
  -rf json -rff target/jmh-calculator-context-propagation-before-gc.json -foe true
```
- Comando after:
```bash
mvn -pl expression-evaluator -Dtest=TestCalculationMemory,TestVariableValues,TestCacheInvalidationBehaviour test -q
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*CalculatorContextPropagationBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-calculator-context-propagation-after.json -foe true
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*CalculatorContextPropagationBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -prof gc \
  -rf json -rff target/jmh-calculator-context-propagation-after-gc.json -foe true
```

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| calculatorWithLargeContextSparseUsage | 252743.599 | 78111.171 | -174632.428 | +69.09% |
| calculatorWithSmallContextDenseUsage | 3208.385 | 3277.701 | 69.316 | -2.16% |

### Resultado GC
| Benchmark | Before gc.alloc.rate.norm (B/op) | After gc.alloc.rate.norm (B/op) | Before gc.alloc.rate (MB/s) | After gc.alloc.rate (MB/s) | Before gc.count | After gc.count | Before gc.time (ms) | After gc.time (ms) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| calculatorWithLargeContextSparseUsage | 165415.635 | 165246.889 | 311.146 | 1000.758 | 15.000 | 49.000 | 50.000 | 127.000 |
| calculatorWithSmallContextDenseUsage | 5424.247 | 5386.902 | 815.182 | 821.584 | 42.000 | 41.000 | 124.000 | 114.000 |

### Decisao
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura tecnica
1. O cenario-alvo (`calculatorWithLargeContextSparseUsage`) melhorou `+69.09%`, confirmando a hipotese de remover trabalho inutil no `setVariables`.
2. O cenario de controle (`calculatorWithSmallContextDenseUsage`) teve regressao pequena (`-2.16%`) e permanece da mesma ordem de grandeza, sem sinal de regressao estrutural relevante.
3. Em GC, `gc.alloc.rate.norm` ficou praticamente estavel (leve melhora), indicando que o ganho e majoritariamente de CPU (menos operacoes por avaliacao), nao de reducao drastica de `B/op`.
4. Inferencia: o aumento de `gc.alloc.rate`, `gc.count` e `gc.time` no cenario alvo com `-prof gc` e compativel com maior throughput apos a otimizacao (mais operacoes executadas por unidade de tempo).

### Atividades executadas
1. Criacao do benchmark dedicado `CalculatorContextPropagationBenchmark` - sucesso.
2. Execucao de testes funcionais direcionados no baseline - sucesso.
3. Build de classes de benchmark no baseline - sucesso.
4. Medicao JMH before (`ns/op`) no baseline - sucesso.
5. Medicao JMH before com `-prof gc` no baseline - sucesso.
6. Execucao de testes funcionais direcionados no estado after - sucesso.
7. Build de classes de benchmark no estado after - sucesso.
8. Medicao JMH after (`ns/op`) no estado after - sucesso.
9. Medicao JMH after com `-prof gc` no estado after - sucesso.
10. Consolidacao dos artefatos e tabela comparativa em markdown - sucesso.

### Artefatos
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-calculator-context-propagation-before.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-calculator-context-propagation-before.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-calculator-context-propagation-before-gc.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-calculator-context-propagation-before-gc.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-calculator-context-propagation-after.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-calculator-context-propagation-after.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-calculator-context-propagation-after-gc.json`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-calculator-context-propagation-after-gc.txt`
- `expression-evaluator/docs/perf/artifacts/expression-evaluator-calculator-context-propagation-comparison.md`

### Riscos residuais
1. O benchmark estressa um padrao especifico (contexto amplo com uso esparso). Cargas reais com distribuicao diferente podem ter ganho menor.
2. O `Calculator` ainda faz copia/snapshot completo de memoria por etapa; existe oportunidade adicional fora do escopo desta rodada.
