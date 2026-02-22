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
