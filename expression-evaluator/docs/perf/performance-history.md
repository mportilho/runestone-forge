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
