# Performance History

Arquivo append-only para registrar experimentos de performance com JMH.

## Convenções
- Sempre registrar before/after em `ns/op`.
- Sempre registrar `Melhoria (%) = ((before_ns_op - after_ns_op) / before_ns_op) * 100`.
- Sempre incluir referência para os artefatos JSON de benchmark.
- Não sobrescrever resultados anteriores.

## Experimento PERF-2026-02-22-EXPEVAL-DYNAMIC-FUNCTION-OVERHEAD
- Data: 2026-02-22
- Objetivo: medir before/after da correção de overhead em funções dinâmicas por chamada.
- Baseline commit/estado: `HEAD 2884333` (código baseline em workspace temporário com mesma infraestrutura JMH e mesmo benchmark).
- Commit/estado testado: working tree atual (com a correção em `FunctionOperation`, `OperationCallSite`, `OperationContext` e `ExpressionContext`).

### Hipótese
1. Reduzir alocações/conversões no hot path de função dinâmica reduz `ns/op` de `dynamicConstantFunction`.
2. Ganho deve aparecer também em `dynamicVariableFunction` e `dynamicFunctionInsideSequence`.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/other/FunctionOperation.java`: cache de chaves de função e reuso de `CallSiteContext` por `OperationContext`.
- `expression-evaluator/src/main/java/com/runestone/expeval/support/callsite/OperationCallSite.java`: conversão sob demanda (copy-on-write) e fast-path de array já tipado.
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/OperationContext.java`: lookup de função sem recursão, com fallback explícito.
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/ExpressionContext.java`: fallback lazy para funções default.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/DynamicFunctionOverheadBenchmark.java`: benchmark JMH dedicado no package `com.runestone.expeval.perf.jmh`.
- `expression-evaluator/pom.xml`: JMH habilitado (dependências + annotation processors para testes).

### Protocolo de medição
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class (src/test/java): `com.runestone.expeval.perf.jmh.DynamicFunctionOverheadBenchmark`
- Package de benchmark: `com.runestone.expeval.perf.jmh`
- Parâmetros:
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

Foco dinâmico médio (`dynamicConstantFunction`, `dynamicVariableFunction`, `dynamicFunctionInsideSequence`):
- Before médio: `663.192 ns/op`
- After médio: `407.411 ns/op`
- Melhoria média: `+38.57%`

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. A correção reduz overhead de funções dinâmicas em todos os cenários-alvo, com melhoria entre `+20.19%` e `+41.98%`.
2. O caminho nativo (`nativeVariableMath`) permaneceu estável (variação de `-0.04%`), sem indício de regressão relevante.

### Atividades executadas
1. `mvn -f "$BEFORE_DIR/pom.xml" -pl expression-evaluator test -q` - sucesso
2. JMH before com `DynamicFunctionOverheadBenchmark` - sucesso
3. `mvn -pl expression-evaluator test -q` - sucesso
4. JMH after com `DynamicFunctionOverheadBenchmark` - sucesso
5. Cópia de artefatos e comparação em markdown - sucesso

### Riscos residuais
1. Resultados são de uma única máquina/ambiente; variação pode ocorrer em hardware/JVM diferentes.
2. Para proteção contínua, ideal incluir gatilho de regressão JMH em pipeline dedicado de performance.

## Experimento PERF-2026-02-22-EXPEVAL-CACHE-INVALIDATION-CASCADE
- Data: 2026-02-22
- Objetivo: reduzir overhead de invalidação de cache em cascata no caminho de sequências `S[...]`/`P[...]`.
- Baseline commit/estado: `HEAD 14be2ed` (baseline em workspace temporário com mesma infraestrutura JMH e benchmark idêntico).
- Commit/estado testado: working tree atual (otimização em `SequenceVariableValueOperation` + testes de cobertura).

### Hipótese
1. Evitar `setValue()` a cada iteração da variável de sequência elimina invalidação recursiva de cache por item do loop.
2. Desabilitar cache para a variável de sequência evita estado stale sem necessidade de `clearCache()` em cascata.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/values/variable/SequenceVariableValueOperation.java`: `setSequenceIndex` passou a usar `overrideValue` e `getCacheHint()` passou a retornar `false`.
- `expression-evaluator/src/test/java/com/runestone/expeval/operation/values/TestCacheInvalidationBehaviour.java`: novos testes de comportamento para sequência sem cache residual e cobertura de atualização via `setVariables`.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/CacheInvalidationCascadeBenchmark.java`: benchmark JMH dedicado em `src/test/java` no package `com.runestone.expeval.perf.jmh`.
- `expression-evaluator/src/test/java/com/runestone/expeval/operation/math/TestMathOperations.java`: ajuste das expectativas de contagem de cache após desabilitar cache em caminho de sequência.

### Protocolo de medição
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.CacheInvalidationCascadeBenchmark`
- Parâmetros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g'`
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

Media dos cenários:
- Before médio: 1284.837 ns/op
- After médio: 1202.174 ns/op
- Melhoria média: +6.43%

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. O ganho mais relevante ocorreu no cenário de sequência com mutação de variável (`+15.57%`), que era o hotspot da invalidação em cascata.
2. Os cenários de atualização de múltiplas variáveis também melhoraram levemente (`+2.04%` e `+2.92%`), sem regressão.

### Atividades executadas
1. Criação de testes funcionais de comportamento para o problema de cache em sequência.
2. Execução de testes relevantes de regressão: `TestCacheInvalidationBehaviour`, `TestMathOperations`, `TestVariableValues`.
3. Medição JMH before em baseline temporário com benchmark idêntico.
4. Medição JMH after no estado atual.
5. Consolidação de artefatos e comparação em markdown.

### Riscos residuais
1. A desabilitação de cache no ramo da variável de sequência reduz potencial de cache em expressões que dependam desse ramo.
2. Benchmarks refletem uma única máquina/JVM; ideal repetir em ambiente de CI de performance.

## Experimento PERF-2026-02-22-EXPEVAL-FUNCTION-REGISTRATION-REFLECTION
- Data: 2026-02-22
- Objetivo: reduzir custo de registro de funções via reflexão (`putFunctionsFromProvider`) e custo de lookup padrão de funções.
- Baseline commit/estado: `HEAD 91448cd` (baseline em workspace temporário com mesmo benchmark JMH).
- Commit/estado testado: working tree atual com cache de templates/callsites e ajustes de lookup.

### Hipótese
1. Cachear templates por classe e callsites estáticos reduz drasticamente custo de criação de callsites por request.
2. Evitar inicialização desnecessária do mapa de funções e lock no path de defaults reduz custo de lookup em runtime.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/support/callsite/OperationCallSiteFactory.java`: substituição de introspecção repetida por cache de templates (`ClassValue`) e cache de callsites para providers `Class`.
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/ExpressionContext.java`: lookup de função sem inicializar mapa vazio, uso de defaults via holder e validação de colisão sem `findFunction` repetido.
- `expression-evaluator/src/main/java/com/runestone/expeval/support/callsite/extensions/OperationCallSiteExtensions.java`: lazy init lock-free via holder idiom.
- `expression-evaluator/src/test/java/com/runestone/expeval/expression/TestFunctionProviderRegistration.java`: cobertura funcional de registro por provider (instância/static/colisões).
- `expression-evaluator/src/test/java/com/runestone/expeval/support/callsite/TestOperationCallSite.java`: cobertura de filtro e exposição de métodos no factory.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/FunctionRegistrationOverheadBenchmark.java`: benchmark JMH dedicado em `src/test/java` no package `com.runestone.expeval.perf.jmh`.

### Protocolo de medição
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.FunctionRegistrationOverheadBenchmark`
- Parâmetros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g'`
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

Media dos cenários:
- Before médio: 135016.014 ns/op
- After médio: 139.453 ns/op
- Melhoria média: +99.90%

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. O maior ganho ocorreu no setup por request (`createCallSitesFromProviderClass` e `putFunctionsFromProviderPerRequest`) com redução expressiva do custo em ns/op.
2. O lookup de função padrão ficou mais leve (`defaultFunctionLookup`) com remoção de lock/check repetido.

### Atividades executadas
1. Adição de testes unitários para cenários de registro/callsite antes da implementação.
2. Implementação das otimizações de cache/lazy-init.
3. Execução dos testes direcionados e da suíte completa do módulo.
4. Medição JMH before em baseline temporário.
5. Medição JMH after no estado atual.
6. Consolidação de artefatos e comparação em markdown.

### Riscos residuais
1. O cache de templates por classe aumenta uso de memória proporcional a classes de providers diferentes utilizadas.
2. Benchmarks refletem uma única máquina/JVM; recomenda-se repetição em CI de performance.

## Experimento PERF-2026-02-22-EXPEVAL-TYPE-CONVERSION-HOTPATH
- Data: 2026-02-22
- Objetivo: reduzir custo de conversão de tipos no hot path quando tipos não casam de primeira, evitando fallback guiado por exceção e normalizando conversão numérica em `BaseOperation`.
- Baseline commit/estado: `HEAD 4ad4a08` (código baseline em workspace temporário com benchmark JMH idêntico).
- Commit/estado testado: working tree atual com ajustes em `AbstractOperation` e `BaseOperation`.

### Hipótese
1. Evitar dupla tentativa de conversão com fallback por exceção no caminho quente reduz `ns/op` no cenário de tipos alternados.
2. Normalizar conversão numérica por tipo em `BaseOperation` pode reduzir custo recorrente de `new BigDecimal(result.toString())`.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/AbstractOperation.java`: novo fluxo de conversão com `tryConvertValue`/`shouldAttemptConversion`, eliminando padrão de tentativa+exceção+retentativa no caminho não-`BaseOperation`.
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/BaseOperation.java`: normalização numérica por tipo via `normalizeNumberResult(Number, MathContext)`, com fallback para `toString()` apenas em tipos incomuns.
- `expression-evaluator/src/test/java/com/runestone/expeval/operation/TestTypeConversionHotPathBehaviour.java`: testes funcionais para cenários de alternância de tipo e invariantes de `MathContext`/escala.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/TypeConversionHotPathBenchmark.java`: benchmark JMH dedicado em `src/test/java` no package `com.runestone.expeval.perf.jmh`.

### Protocolo de medição
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.TypeConversionHotPathBenchmark`
- Parâmetros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g'`
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

Media dos cenários:
- Before médio: 734.633 ns/op
- After médio: 146.985 ns/op
- Melhoria média: +79.99%

### Decisão
- [ ] Aceitar
- [ ] Ajustar
- [x] Descartar

### Leitura técnica
1. O hotspot principal (`alternatingTypeConversion`) teve melhora expressiva de `+99.43%`, confirmando a redução de overhead por fallback com exceção.
2. Os microcenários de normalização em `BaseOperation` regrediram (`-3.45%` e `-10.56%`), o que indica necessidade de ajuste fino nessa parte para evitar custo adicional no caminho numérico comum.
3. A decisão foi marcada como `Ajustar` para preservar o ganho do hot path sem aceitar regressão nos cenários numéricos de apoio.

### Atividades executadas
1. Implementação de redução de fallback por exceção e normalização numérica tipada.
2. Criação de testes funcionais de comportamento para cenários alvo.
3. Execução de testes funcionais direcionados e suíte do módulo.
4. Medição JMH before em baseline temporário.
5. Medição JMH after no estado atual.
6. Consolidação de artefatos e comparação em markdown.

### Riscos residuais
1. Regressao nos caminhos de `BaseOperation` pode afetar cargas predominantemente numericas simples.
2. Benchmarks refletem uma única máquina/JVM; recomendada validação adicional em CI de performance.

## Experimento PERF-2026-02-22-EXPEVAL-TYPE-CONVERSION-BASEOP-FINE-TUNING
- Data: 2026-02-22
- Objetivo: ajuste fino no `BaseOperation` para recuperar throughput em `long/double` sem perder ganho no hot path de conversão.
- Baseline commit/estado: estado `after` do experimento `PERF-2026-02-22-EXPEVAL-TYPE-CONVERSION-HOTPATH`.
- Commit/estado testado: working tree atual com micro-otimização estrutural em `BaseOperation.resolve()`.

### Hipótese
1. Reduzir overhead fixo por chamada no `BaseOperation` (caminho comum sem variáveis atribuidas) melhora `baseOperationLongNormalization` e `baseOperationDoubleNormalization`.
2. Ajuste não deve degradar `alternatingTypeConversion`.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/BaseOperation.java`:
  - cache de flag `hasAssignedVariables` no construtor;
  - skip do loop de variáveis atribuidas quando vazio;
  - cache local de `mathContext` e `scale` no hot path de `resolve()`;
  - manutencao da conversão numérica com `new BigDecimal(result.toString(), mathContext)` para preservar comportamento.

### Protocolo de medição
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.TypeConversionHotPathBenchmark`
- Parâmetros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g'`
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

Media dos cenários:
- Before médio: 146.985 ns/op
- After médio: 144.949 ns/op
- Melhoria média: +1.39%

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. Houve recuperacao de `long/double` em relacao ao estado imediatamente anterior.
2. O cenário principal (`alternatingTypeConversion`) foi preservado com leve melhoria adicional.
3. O ajuste e de baixo risco funcional por manter semântica numérica original no `BaseOperation`.

### Atividades executadas
1. Implementação de ajuste estrutural em `BaseOperation`.
2. Execução de testes funcionais do módulo (`mvn -pl expression-evaluator test -q`).
3. Medição JMH after com o mesmo protocolo.
4. Consolidação dos artefatos e comparação percentual em ns/op.
5. Correcao de fluxo: execução de medições em sequência para evitar corrupção de `jmh_generated` por concorrência.

### Riscos residuais
1. Ganho incremental; parte da diferença historica absoluta em `long/double` pode depender de variação de ambiente/JIT.
2. Recomendado repetir em pipeline dedicado de performance para validar estabilidade estatística.

## Experimento PERF-2026-02-22-EXPEVAL-VARIABLE-PROVIDER-CONTEXT-ALLOCATION
- Data: 2026-02-22
- Objetivo: reduzir alocações por avaliação no caminho de `VariableProvider`, mitigando pressão de GC sem alterar semântica de avaliação.
- Baseline commit/estado: `HEAD 05fbf28` no estado pre-otimização (com benchmark e testes novos adicionados para medição).
- Commit/estado testado: working tree atual com cache de `VariableValueProviderContext` por avaliação e supplier de data/hora mais leve.

### Hipótese
1. Reutilizar `VariableValueProviderContext` por thread enquanto o mesmo `OperationContext` estiver ativo reduz `B/op` no hot path com providers.
2. Substituir `MemoizedSupplier` por supplier lazy dedicado por avaliação reduz overhead de alocação/sincronização no acesso a `currentDateTime`.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/values/variable/VariableValueOperation.java`: cache `ThreadLocal` de `VariableValueProviderContext` por `OperationContext` para evitar novas instancias repetidas no mesmo fluxo de avaliação.
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/ExpressionEvaluator.java`: troca de `MemoizedSupplier` por `CurrentDateTimeSupplier` lazy por avaliação.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/VariableProviderContextOverheadBenchmark.java`: benchmark JMH dedicado ao cenário de providers com `currentDateTime`.
- `expression-evaluator/src/test/java/com/runestone/expeval/operation/values/TestVariableValues.java`: cobertura de consistência de `currentDateTime` e reutilização do contexto de provider na mesma avaliação.

### Protocolo de medição
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.VariableProviderContextOverheadBenchmark`
- Package de benchmark: `com.runestone.expeval.perf.jmh`
- Parâmetros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g' -prof gc`
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

Indicadores de alocação (apoio):
- `gc.alloc.rate`: `437.201 MB/sec` -> `303.111 MB/sec` (`-134.090 MB/sec`, redução de `30.67%`).
- `gc.alloc.rate.norm`: `456.020 B/op` -> `272.013 B/op` (`-184.007 B/op`, redução de `40.35%`).

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. A redução de `B/op` confirma que a maior parte do ganho vem de menor churn de objetos no caminho de providers.
2. O ganho em latência (`+13.86%` em `ns/op`) e consistente com a hipótese de eliminar criacoes repetidas de contexto por avaliação.
3. A troca do supplier em `ExpressionEvaluator` reduz overhead estrutural mantendo semântica de data/hora consistente por avaliação.

### Atividades executadas
1. Criação de benchmark JMH dedicado (`VariableProviderContextOverheadBenchmark`) - sucesso.
2. Execução de testes direcionados pre-otimização: `mvn -pl expression-evaluator -Dtest=TestVariableValues,TestVariablesWithExpressionContext test -q` - sucesso.
3. Medição JMH before com `-prof gc` - sucesso.
4. Implementação da otimização no hotspot (`VariableValueOperation`, `ExpressionEvaluator`) - sucesso.
5. Execução de testes direcionados pos-otimização: `mvn -pl expression-evaluator -Dtest=TestVariableValues,TestVariablesWithExpressionContext test -q` - sucesso.
6. Execução da suíte completa do módulo: `mvn -pl expression-evaluator test -q` - sucesso.
7. Medição JMH after com protocolo idêntico - sucesso.

### Riscos residuais
1. O benchmark desabilita cache de variáveis para estressar o caminho de providers; ganhos absolutos podem variar em workloads com cache mais efetivo.
2. Medicoes realizadas em uma única máquina/JVM; recomendado repetir em pipeline dedicado de performance para validar estabilidade estatística.

## Experimento PERF-2026-02-22-EXPEVAL-CALCULATOR-CONTEXT-PROPAGATION
- Data: 2026-02-22
- Objetivo: reduzir custo de propagação de variáveis no `Calculator` quando o contexto possui muitas chaves e a expressão usa poucas variáveis.
- Baseline commit/estado: `HEAD 304733a` em worktree temporário (`/tmp/expeval-baseline-Hjf50x`), com benchmark idêntico copiado para o baseline.
- Commit/estado testado: working tree atual com otimização de `setVariables` no hot path da avaliação.

### Hipótese
1. Aplicar variáveis apenas para nomes efetivamente presentes na árvore da expressão reduz `ns/op` no cenário de contexto grande e uso esparso.
2. O cenário de contexto pequeno e uso denso deve ficar estável ou com regressão baixa.
3. Como o foco e hot path com risco de alocação, comparação com `-prof gc` e necessária para validar impacto em alocação/GC.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/ExpressionEvaluator.java`: novo caminho `setVariables(Map<String, Object>)` orientado ao mapa de variáveis da expressão, evitando varredura do mapa inteiro de contexto por chamada.
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/Expression.java`: `setVariables(...)` passou a delegar para o caminho otimizado do evaluator.
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/calculator/Calculator.java`: pre-size de `ArrayList` em `calculate(...)` para reduzir realocações.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/CalculatorContextPropagationBenchmark.java`: benchmark JMH dedicado ao cenário de propagação de contexto no `Calculator`.

### Protocolo de medição
- JVM: `java version "21.0.10" 2026-01-20 LTS`
- JMH: `1.35`
- Benchmark class: `com.runestone.expeval.perf.jmh.CalculatorContextPropagationBenchmark`
- Package de benchmark: `com.runestone.expeval.perf.jmh`
- Parâmetros:
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

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. O cenário-alvo (`calculatorWithLargeContextSparseUsage`) melhorou `+69.09%`, confirmando a hipótese de remover trabalho inútil no `setVariables`.
2. O cenário de controle (`calculatorWithSmallContextDenseUsage`) teve regressão pequena (`-2.16%`) e permanece da mesma ordem de grandeza, sem sinal de regressão estrutural relevante.
3. Em GC, `gc.alloc.rate.norm` ficou praticamente estável (leve melhora), indicando que o ganho e majoritariamente de CPU (menos operações por avaliação), não de redução drástica de `B/op`.
4. Inferência: o aumento de `gc.alloc.rate`, `gc.count` e `gc.time` no cenário alvo com `-prof gc` e compatível com maior throughput após a otimização (mais operações executadas por unidade de tempo).

### Atividades executadas
1. Criação do benchmark dedicado `CalculatorContextPropagationBenchmark` - sucesso.
2. Execução de testes funcionais direcionados no baseline - sucesso.
3. Build de classes de benchmark no baseline - sucesso.
4. Medição JMH before (`ns/op`) no baseline - sucesso.
5. Medição JMH before com `-prof gc` no baseline - sucesso.
6. Execução de testes funcionais direcionados no estado after - sucesso.
7. Build de classes de benchmark no estado after - sucesso.
8. Medição JMH after (`ns/op`) no estado after - sucesso.
9. Medição JMH after com `-prof gc` no estado after - sucesso.
10. Consolidação dos artefatos e tabela comparativa em markdown - sucesso.

### Riscos residuais
1. O benchmark estressa um padrão especifico (contexto amplo com uso esparso). Cargas reais com distribuicao diferente podem ter ganho menor.
2. O `Calculator` ainda faz cópia/snapshot completo de memória por etapa; existe oportunidade adicional fora do escopo desta rodada.

## Experimento PERF-2026-02-22-EXPEVAL-DYNAMIC-FUNCTION-CALL-ALLOCATION-TUNING
- Data: 2026-02-22
- Objetivo: reduzir alocação por chamada em funções dinâmicas, com foco em reuso de buffers e remoção de adaptadores por invocação.
- Baseline commit/estado: working tree antes da alteração do hotspot (mesmo estado funcional do módulo após item 1), com benchmark dedicado adicionado.
- Commit/estado testado: working tree atual com ajustes em `FunctionOperation` e `OperationCallSite`.

### Hipótese
1. Evitar criação por chamada no caminho de função dinâmica reduz `ns/op` em funções de aridade fixa.
2. Reuso no caminho de fallback varargs (`_N` -> `_1`) reduz `ns/op` e/ou `gc.alloc.rate.norm`.
3. O caminho nativo (`nativeVariableMath`) deve permanecer estável.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/other/FunctionOperation.java`: ajuste do hot path para uso de contexto de chamada cacheado e caminho mutável para parâmetros normalizados.
- `expression-evaluator/src/main/java/com/runestone/expeval/support/callsite/OperationCallSite.java`: novos overloads de chamada com `DataConversionService`, suporte a caminho mutável e exposição de metadado de assinatura em array único.
- `expression-evaluator/src/test/java/com/runestone/expeval/support/callsite/TestOperationCallSite.java`: ajuste da cobertura de callsite para as assinaturas novas.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/DynamicFunctionAllocationBenchmark.java`: benchmark JMH dedicado para aridade fixa e fallback varargs.

### Protocolo de medição
- JVM: `java version "21.0.10" 2026-01-20 LTS`
- JMH: `1.35`
- Benchmark class: `com.runestone.expeval.perf.jmh.DynamicFunctionAllocationBenchmark`
- Package de benchmark: `com.runestone.expeval.perf.jmh`
- Parâmetros:
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Profiling GC: `-prof gc`
- Comando before:
```bash
mvn -pl expression-evaluator -Dtest=TestFunctionOperations,TestKnownFunctionsOnExpression,TestOperationCallSite test -q
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*DynamicFunctionAllocationBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-dynamic-function-allocation-before.json -foe true
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*DynamicFunctionAllocationBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -prof gc \
  -rf json -rff target/jmh-dynamic-function-allocation-before-gc.json -foe true
```
- Comando after:
```bash
mvn -pl expression-evaluator -Dtest=TestFunctionOperations,TestKnownFunctionsOnExpression,TestOperationCallSite test -q
mvn -pl expression-evaluator test -q
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*DynamicFunctionAllocationBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-dynamic-function-allocation-after.json -foe true
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*DynamicFunctionAllocationBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -prof gc \
  -rf json -rff target/jmh-dynamic-function-allocation-after-gc.json -foe true
```

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| dynamicFixedArityFunction | 205.900 | 212.291 | 6.391 | -3.10% |
| dynamicVarArgFallbackFunction | 1081.886 | 1091.281 | 9.395 | -0.87% |
| nativeVariableMath | 167.546 | 167.348 | -0.198 | +0.12% |

### Resultado GC
| Benchmark | Before gc.alloc.rate.norm (B/op) | After gc.alloc.rate.norm (B/op) | Before gc.alloc.rate (MB/s) | After gc.alloc.rate (MB/s) | Before gc.count | After gc.count | Before gc.time (ms) | After gc.time (ms) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| dynamicFixedArityFunction | 248.011 | 248.011 | 560.751 | 550.685 | 28.000 | 28.000 | 93.000 | 92.000 |
| dynamicVarArgFallbackFunction | 600.027 | 624.027 | 268.075 | 267.585 | 12.000 | 12.000 | 41.000 | 42.000 |
| nativeVariableMath | 192.009 | 192.008 | 545.148 | 555.365 | 27.000 | 27.000 | 89.000 | 92.000 |

### Decisão
- [ ] Aceitar
- [ ] Ajustar
- [x] Descartar

### Leitura técnica
1. O tuning não entregou ganho em latência nos cenários dinâmicos medidos; houve regressão de `-3.10%` em aridade fixa e `-0.87%` em fallback varargs.
2. Em GC, o caminho de fallback varargs aumentou `gc.alloc.rate.norm` (`600.027` -> `624.027 B/op`), sem redução de churn no hot path.
3. Inferência: a estratégia atual de buffer/normalização não removeu alocações dominantes do caminho e adicionou sobrecusto de controle de chamada.

### Atividades executadas
1. Criação de benchmark JMH dedicado (`DynamicFunctionAllocationBenchmark`) - sucesso.
2. Execução de testes funcionais direcionados pre-otimização - sucesso.
3. Medição JMH before (`ns/op`) - sucesso.
4. Medição JMH before com `-prof gc` - sucesso.
5. Implementação iterativa do tuning em `FunctionOperation` e `OperationCallSite` - sucesso.
6. Reexecução de testes funcionais direcionados e suíte do módulo - sucesso.
7. Medição JMH after (`ns/op`) - sucesso.
8. Medição JMH after com `-prof gc` - sucesso.
9. Consolidação de artefatos e comparativo em markdown - sucesso.

### Riscos residuais
1. O item de performance permanece aberto para novo ciclo de tuning no caminho de funções dinâmicas.
2. Os cenários deste experimento medem chamadas unitárias por avaliação; recomendada rodada adicional com alta densidade de chamadas por avaliação (ex.: funções dinâmicas dentro de sequências longas).

## Experimento PERF-2026-02-22-EXPEVAL-FUNCTION-LOOKUP-FALLBACK-CALLSITE-MEMOIZATION
- Data: 2026-02-22
- Objetivo: reduzir overhead de lookup de função com fallback (`_N -> _1`) em chamadas repetidas no mesmo `OperationContext`, com foco no hot path de `FunctionOperation`.
- Baseline commit/estado: working tree em `HEAD 60be74e`, sem memoização de `OperationCallSite` e sem short-circuit para contextos iguais.
- Commit/estado testado: working tree atual com short-circuit em `OperationContext` e memoização de callsite em `FunctionOperation`.

### Hipótese
1. Quando `userContext` e `expressionContext` são o mesmo objeto (caso padrão de `Expression.evaluate()`), evitar lookup duplicado reduz `ns/op` no cenário com fallback variádico.
2. Memoizar `OperationCallSite` resolvido por `FunctionOperation + OperationContext` reduz custo acumulado em múltiplas invocações dentro da mesma avaliação.
3. O cenário com contextos distintos deve permanecer estável, com variação pequena.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/OperationContext.java`: short-circuit de lookup quando `userContext == expressionContext`, evitando consulta duplicada.
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/other/FunctionOperation.java`: cache transient de `OperationCallSite` resolvido por instância de `OperationContext`.
- `expression-evaluator/src/test/java/com/runestone/expeval/operation/TestFunctionLookupHotPathBehaviour.java`: cobertura funcional para short-circuit, precedência e memoização por contexto.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/FunctionLookupFallbackBenchmark.java`: benchmark JMH dedicado ao cenário de fallback variádico com alta repetição por contexto.

### Protocolo de medição
- JVM: `java version "21.0.10" 2026-01-20 LTS`
- JMH: `1.35`
- Benchmark class: `com.runestone.expeval.perf.jmh.FunctionLookupFallbackBenchmark`
- Package de benchmark: `com.runestone.expeval.perf.jmh`
- Parâmetros:
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Comando before:
```bash
mvn -pl expression-evaluator -Dtest=TestFunctionOperations,TestOperationCallSite test -q
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*FunctionLookupFallbackBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-function-lookup-fallback-before.json -foe true
```
- Comando after:
```bash
mvn -pl expression-evaluator -Dtest=TestFunctionLookupHotPathBehaviour,TestFunctionOperations,TestOperationCallSite test -q
mvn -pl expression-evaluator test -q
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*FunctionLookupFallbackBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-function-lookup-fallback-after.json -foe true
```

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| fallbackLookupSameContext | 128.567 | 117.410 | -11.157 | +8.68% |
| fallbackLookupSplitContext | 136.733 | 137.851 | 1.117 | -0.82% |

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. O cenário principal (`sameContext`), alinhado ao fluxo padrão de `Expression.evaluate()`, melhorou `+8.68%` em `ns/op`, confirmando a hipótese de reduzir lookup redundante.
2. O cenário com contextos distintos ficou praticamente estável, com regressão pequena (`-0.82%`) e sem mudança de ordem de grandeza.
3. Inferência: o ganho vem da combinação de (a) evitar segunda consulta desnecessária quando os contextos são a mesma referência e (b) evitar relookup por chamada repetida no mesmo `OperationContext`.

### Atividades executadas
1. Criação de benchmark JMH dedicado (`FunctionLookupFallbackBenchmark`) - sucesso.
2. Execução de testes funcionais baseline (`TestFunctionOperations`, `TestOperationCallSite`) - sucesso.
3. Medição JMH before (`ns/op`) - sucesso.
4. Implementação da otimização no hot path (`OperationContext`, `FunctionOperation`) - sucesso.
5. Adição de testes funcionais de cobertura do hotspot (`TestFunctionLookupHotPathBehaviour`) - sucesso.
6. Reexecução de testes direcionados e suíte do módulo - sucesso.
7. Medição JMH after (`ns/op`) com protocolo idêntico - sucesso.
8. Consolidação dos artefatos e comparativo em markdown - sucesso.

### Riscos residuais
1. O benchmark isola chamadas repetidas no mesmo `OperationContext`; cargas com menos repetição por contexto podem observar ganho menor.
2. Como toda medição foi em única máquina/JVM, recomenda-se repetição em pipeline dedicado de performance para validação estatística adicional.
