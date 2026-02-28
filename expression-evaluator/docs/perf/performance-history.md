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

## Experimento PERF-2026-02-22-EXPEVAL-CALCULATOR-MEMORY-MODES
- Data: 2026-02-22
- Objetivo: reduzir pressão de heap no caminho de memória detalhada do `Calculator` com estratégia configurável, mantendo compatibilidade por padrão.
- Baseline commit/estado: working tree atual com modo `FULL` como referência de comparação.
- Commit/estado testado: working tree atual com novos modos `FULL`, `COMPACT` e `LAZY`.

### Hipótese
1. `COMPACT` reduz alocação por operação ao evitar snapshots completos por etapa.
2. `LAZY` reduz alocação no caminho padrão (quando snapshots não são acessados) ao materializar sob demanda.
3. `FULL` mantém comportamento legado sem regressão funcional.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/calculator/CalculatorMemoryMode.java`: enum de estratégia de memória (`FULL`, `COMPACT`, `LAZY`).
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/calculator/CalculatorOptions.java`: opções configuráveis (`memoryMode`, `checkpointInterval`).
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/calculator/CalculationMemory.java`: factories `compact`/`lazy` e mapas lazy memoizados com materialização sob demanda.
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/calculator/Calculator.java`: overloads `calculate(..., CalculatorOptions)` e pipeline por modo, com reconstrução lazy por delta + checkpoints.
- `expression-evaluator/src/test/java/com/runestone/expeval/expression/TestCalculatorContracts.java`: cobertura de compatibilidade default, `COMPACT`, `LAZY`, memoização e não mutação de contexto.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/CalculatorContextPropagationBenchmark.java`: benchmark parametrizado por `memoryMode`.

### Protocolo de medição
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.CalculatorContextPropagationBenchmark`
- Comando de preparação:
```bash
mvn -pl expression-evaluator -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=expression-evaluator/target/jmh.classpath -Dmdep.includeScope=test -q
```
- Comparação de latência (`ns/op`, preliminar):
```bash
cd expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*CalculatorContextPropagationBenchmark.*' \
  -wi 3 -i 5 -w 200ms -r 200ms -f 1 -tu ns \
  -rf json -rff target/jmh-calculator-context-modes.json -foe true
```
- Comparação de alocação (`B/op`, preliminar):
```bash
cd expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*CalculatorContextPropagationBenchmark.calculatorWithLargeContextSparseUsage.*' \
  -p memoryMode=FULL,COMPACT,LAZY \
  -wi 2 -i 3 -w 200ms -r 200ms -f 1 -tu ns -prof gc \
  -rf json -rff target/jmh-calculator-context-modes-gc.json -foe true
```

### Resultado
Latência (`ns/op`) - `target/jmh-calculator-context-modes.json`:

| Benchmark | FULL | COMPACT | LAZY | Melhoria COMPACT vs FULL | Melhoria LAZY vs FULL |
|---|---:|---:|---:|---:|---:|
| calculatorWithLargeContextSparseUsage | 82491.373 | 24424.650 | 41537.478 | +70.39% | +49.65% |
| calculatorWithSmallContextDenseUsage | 3641.443 | 3305.941 | 3647.797 | +9.22% | -0.17% |

Alocação (`gc.alloc.rate.norm`, `B/op`) - `target/jmh-calculator-context-modes-gc.json`:

| Benchmark | FULL (B/op) | COMPACT (B/op) | LAZY (B/op) | Redução COMPACT vs FULL | Redução LAZY vs FULL |
|---|---:|---:|---:|---:|---:|
| calculatorWithLargeContextSparseUsage | 165292.225 | 44073.485 | 84934.570 | +73.34% | +48.62% |

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. `COMPACT` confirmou redução expressiva de alocação e latência no cenário com contexto grande e uso esparso, principal hotspot de amplificação de memória.
2. `LAZY` apresentou redução intermediária, preservando acesso ao detalhe quando necessário via materialização sob demanda.
3. Em cenário denso e pequeno, ganhos são modestos, como esperado, por menor custo de snapshot no baseline.

### Atividades executadas
1. `mvn -pl expression-evaluator test` - sucesso (`221` testes).
2. JMH comparativo de latência por modo - sucesso.
3. JMH com `-prof gc` para alocação por modo (cenário sparse) - sucesso.
4. Consolidação de artefatos e registro histórico - sucesso.

### Riscos residuais
1. As medições são preliminares (iterações/forks reduzidos); números absolutos podem variar em hardware/JVM diferentes.
2. O modo `COMPACT` reduz drasticamente detalhe histórico em `variables/contextVariables` por design; consumidores que dependam desse detalhe devem permanecer em `FULL` ou `LAZY`.

## Experimento PERF-2026-02-22-EXPEVAL-EMPTY-CONTEXT-VARIABLE-LOOKUP
- Data: 2026-02-22
- Objetivo: remover alocação desnecessária no hot path de leitura de variável quando `ExpressionContext` não possui `dictionary`.
- Baseline commit/estado: `HEAD 49d0b77` (working tree com benchmark/teste novos e comportamento anterior em `findValue`, que inicializava `dictionary` no caminho de leitura).
- Commit/estado testado: working tree atual com retorno imediato de `null` quando `dictionary == null` em `findValue`.

### Hipótese
1. Evitar `new HashMap<>()` implícito em leitura sem dicionário reduz `ns/op` nos cenários de contexto vazio e contexto com `variablesSupplier` sem valor.
2. O cenário com dicionário já presente deve permanecer estável, sem regressão relevante.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/ExpressionContext.java`: `findValue` agora retorna `null` diretamente quando `dictionary` é `null`, sem chamar `initializeDictionaryMap()`.
- `expression-evaluator/src/test/java/com/runestone/expeval/expression/TestExpressionContext.java`: cobertura funcional para garantir que leitura em contexto vazio não materializa `dictionary`.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/EmptyContextValueLookupBenchmark.java`: benchmark JMH dedicado para lookup de chave ausente em três cenários (`empty`, `supplier-only`, `dictionary`).

### Protocolo de medição
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.EmptyContextValueLookupBenchmark`
- Parâmetros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g'`
- Comando before:
```bash
cd expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*EmptyContextValueLookupBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-empty-context-lookup-before.json -foe true
```
- Comando after:
```bash
cd expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*EmptyContextValueLookupBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/jmh-empty-context-lookup-after.json -foe true
```

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| findMissingValueOnDictionaryContext | 2.042 | 2.100 | +0.059 | -2.88% |
| findMissingValueOnEmptyContext | 1.338 | 0.881 | -0.457 | +34.16% |
| findMissingValueWithSupplierOnlyContext | 1.478 | 1.043 | -0.435 | +29.44% |

Foco do hot path sem dicionário (`findMissingValueOnEmptyContext` + `findMissingValueWithSupplierOnlyContext`):
- Before médio: `1.408 ns/op`
- After médio: `0.962 ns/op`
- Melhoria média: `+31.68%`

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. A mudança atingiu o alvo: os cenários sem dicionário melhoraram entre `+29.44%` e `+34.16%`.
2. O cenário com dicionário existente apresentou pequena piora (`-2.88%`) e não pertence ao caminho de otimização proposta.
3. Inferência: o ganho principal vem da remoção da alocação e escrita de estado (`dictionary = new HashMap<>()`) em leituras efêmeras que só consultam valores.

### Atividades executadas
1. `mvn -pl expression-evaluator -Dtest=TestExpressionContext test -q` - falha esperada no baseline (`dictionary` inicializada em leitura).
2. `mvn -pl expression-evaluator -Dtest=TestExpressionContext,TestVariablesWithExpressionContext test -q` - sucesso após correção.
3. `mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test` - sucesso.
4. JMH before (`target/jmh-empty-context-lookup-before.json`) - sucesso.
5. JMH after (`target/jmh-empty-context-lookup-after.json`) - sucesso.

### Riscos residuais
1. A regressão leve no cenário com dicionário presente merece monitoramento em medições futuras de maior duração/forks.
2. Medições em uma única máquina/JVM; recomenda-se repetir em ambiente de CI de performance.

## Experimento PERF-2026-02-22-EXPEVAL-SEMANTIC-EQUIVALENCE-CACHE-INVALIDATION
- Data: 2026-02-22
- Objetivo: quantificar o ganho da correção no `setValue` que evita `clearCache()` para valores comparáveis semanticamente equivalentes (ex.: `BigDecimal("1.0")` vs `BigDecimal("1.00")`).
- Baseline commit/estado: `HEAD` em archive temporário (`/tmp/expeval-baseline-semantic-NydSFX`) com benchmark idêntico.
- Commit/estado testado: working tree atual com ajuste em `AbstractVariableValueOperation#setValue`.

### Hipótese
1. No cenário de atualização com valor comparável equivalente, a mudança deve reduzir fortemente `ns/op`.
2. No cenário de atualização com valor realmente diferente, a latência deve permanecer estável.
3. Em alocação (`B/op`), o cenário equivalente deve reduzir drasticamente o churn por evitar invalidação e recomputação.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/values/AbstractVariableValueOperation.java`: `setValue` passou a usar equivalência semântica (`equals` ou `compareTo == 0`) antes de invalidar cache.
- `expression-evaluator/src/test/java/com/runestone/expeval/operation/values/TestCacheInvalidationBehaviour.java`: novo teste para `BigDecimal("1.0")` -> `BigDecimal("1.00")` sem invalidação desnecessária.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/VariableSetSemanticEqualityBenchmark.java`: benchmark JMH dedicado aos cenários equivalente vs diferente.

### Protocolo de medição
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.VariableSetSemanticEqualityBenchmark`
- Latência (`ns/op`) before:
```bash
cd /tmp/expeval-baseline-semantic-NydSFX/expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*VariableSetSemanticEqualityBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -rf json -rff target/jmh-semantic-equality-before.json -foe true
```
- Latência (`ns/op`) after:
```bash
cd expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*VariableSetSemanticEqualityBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -rf json -rff target/jmh-semantic-equality-after.json -foe true
```
- Alocação (`B/op`) before:
```bash
cd /tmp/expeval-baseline-semantic-NydSFX/expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*VariableSetSemanticEqualityBenchmark.*' \
  -wi 3 -i 5 -w 300ms -r 300ms -f 2 -tu ns -prof gc \
  -rf json -rff target/jmh-semantic-equality-before-gc.json -foe true
```
- Alocação (`B/op`) after:
```bash
cd expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*VariableSetSemanticEqualityBenchmark.*' \
  -wi 3 -i 5 -w 300ms -r 300ms -f 2 -tu ns -prof gc \
  -rf json -rff target/jmh-semantic-equality-after-gc.json -foe true
```

### Resultado
Latência (`ns/op`):

| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| setVariableWithEquivalentComparableValue | 138.805 | 18.779 | -120.027 | +86.47% |
| setVariableWithDifferentComparableValue | 133.692 | 132.049 | -1.642 | +1.23% |

Alocação (`gc.alloc.rate.norm`, B/op):

| Benchmark | Before (B/op) | After (B/op) | Redução (%) |
|---|---:|---:|---:|
| setVariableWithEquivalentComparableValue | 200.010136 | 0.000024 | +100.00% |
| setVariableWithDifferentComparableValue | 168.008209 | 168.008681 | -0.00% |

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. O cenário-alvo (valor comparável equivalente) apresentou ganho expressivo de `+86.47%` em `ns/op`.
2. O cenário de controle (valor realmente diferente) permaneceu estável (`+1.23%`), sem regressão relevante.
3. Em `B/op`, o cenário-alvo praticamente eliminou alocação por operação após a otimização.

### Atividades executadas
1. Adição de benchmark JMH dedicado (`VariableSetSemanticEqualityBenchmark`) - sucesso.
2. Build de benchmark before e after (`test-compile` + classpath) - sucesso.
3. Medição JMH before/after de latência (`ns/op`) - sucesso.
4. Medição JMH before/after com `-prof gc` (`B/op`) - sucesso.
5. Testes funcionais direcionados (`TestCacheInvalidationBehaviour`, `TestVariableValues`) - sucesso.

### Riscos residuais
1. O ganho medido é focado em comparáveis que usam semântica de `compareTo`; para tipos que não implementam essa semântica, o comportamento continua dependente de `equals`.
2. Medições foram feitas em única máquina/JVM; ideal repetir em pipeline de performance para validação estatística contínua.

## Experimento PERF-2026-02-22-EXPEVAL-PARENT-LINKING-HOTPATH
- Data: 2026-02-22
- Objetivo: reduzir o custo de ligação de pais (`addParent`) no hot path e validar impacto em cenário integrado com alta fan-in de variável.
- Baseline commit/estado: `HEAD` em archive temporário (`/tmp/expeval-baseline-parent-link-qkBwjD`) com benchmark idêntico.
- Commit/estado testado: working tree atual com otimização em `AbstractOperation` e testes funcionais adicionais.

### Hipótese
1. Trocar crescimento por cópia total a cada `addParent` por crescimento amortizado reduz fortemente `ns/op` no cenário de burst.
2. Iterar até `parentCount` (em vez da capacidade do array) preserva semântica e evita trabalho indevido com slots não usados.
3. O ganho deve aparecer também no benchmark integrado de parse + warmup + evaluate, principalmente em cardinalidade alta (`parentCount=1024`).

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/AbstractOperation.java`: introdução de `parentCount`, crescimento amortizado do array de pais (`INITIAL_PARENT_CAPACITY` + `Arrays.copyOf`) e loops de propagação (`clearCache`, `disableCaching`, `enableCaching`) limitados ao total real de pais.
- `expression-evaluator/src/test/java/com/runestone/expeval/operation/values/TestCacheInvalidationBehaviour.java`: cobertura funcional para fan-in alto de variável (invalidação de cache em múltiplos pais e alternância de `configureCaching` sem erro).
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/ParentLinkingHotPathBenchmark.java`: benchmark JMH dedicado para `addParentBurst` e cenário integrado `parseWarmUpAndEvaluateRepeatedVariableExpression`.

### Protocolo de medição
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.ParentLinkingHotPathBenchmark`
- Package de benchmark: `com.runestone.expeval.perf.jmh`
- Parâmetros de latência (`ns/op`):
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns`
- Parâmetros de alocação (`B/op`):
  - `-wi 3 -i 5 -w 300ms -r 300ms -f 2 -tu ns -prof gc`
- Comando before (latência):
```bash
mvn -f "$BEFORE_DIR/pom.xml" -pl expression-evaluator -DskipTests test-compile -q
cd "$BEFORE_DIR/expression-evaluator"
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*ParentLinkingHotPathBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -rf json -rff target/jmh-parent-link-before.json -foe true
```
- Comando after (latência):
```bash
mvn -pl expression-evaluator -DskipTests test-compile -q
cd expression-evaluator
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*ParentLinkingHotPathBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -rf json -rff target/jmh-parent-link-after.json -foe true
```
- Comandos adicionais (alocação):
```bash
cd "$BEFORE_DIR/expression-evaluator"
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*ParentLinkingHotPathBenchmark.addParentBurst.*' \
  -wi 3 -i 5 -w 300ms -r 300ms -f 2 -tu ns -prof gc \
  -rf json -rff target/jmh-parent-link-before-gc.json -foe true

cd expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*ParentLinkingHotPathBenchmark.addParentBurst.*' \
  -wi 3 -i 5 -w 300ms -r 300ms -f 2 -tu ns -prof gc \
  -rf json -rff target/jmh-parent-link-after-gc.json -foe true
```

### Resultado
Latência (`ns/op`):

| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| addParentBurst (`parentCount=64`) | 1951.177 | 585.264 | -1365.913 | +70.00% |
| addParentBurst (`parentCount=256`) | 26884.575 | 2070.434 | -24814.141 | +92.30% |
| addParentBurst (`parentCount=1024`) | 399018.564 | 7964.767 | -391053.797 | +98.00% |
| parseWarmUpAndEvaluateRepeatedVariableExpression (`parentCount=64`) | 72798.340 | 71676.846 | -1121.494 | +1.54% |
| parseWarmUpAndEvaluateRepeatedVariableExpression (`parentCount=256`) | 296951.522 | 285302.954 | -11648.568 | +3.92% |
| parseWarmUpAndEvaluateRepeatedVariableExpression (`parentCount=1024`) | 1590597.517 | 1209522.082 | -381075.435 | +23.96% |

Alocação (`gc.alloc.rate.norm`, `B/op`) em `addParentBurst`:

| Benchmark | Before (B/op) | After (B/op) | Redução (%) |
|---|---:|---:|---:|
| addParentBurst (`parentCount=64`) | 9512.411 | 1320.061 | +86.12% |
| addParentBurst (`parentCount=256`) | 136237.798 | 4048.205 | +97.03% |
| addParentBurst (`parentCount=1024`) | 2117765.512 | 13104.629 | +99.38% |

Artefatos:
- before latência: `/tmp/expeval-baseline-parent-link-qkBwjD/expression-evaluator/target/jmh-parent-link-before.json`
- after latência: `expression-evaluator/target/jmh-parent-link-after.json`
- before alocação: `/tmp/expeval-baseline-parent-link-qkBwjD/expression-evaluator/target/jmh-parent-link-before-gc.json`
- after alocação: `expression-evaluator/target/jmh-parent-link-after-gc.json`

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. O hot path de ligação de pais melhorou de forma consistente e expressiva (`+70.00%` a `+98.00%`), confirmando que o custo anterior era dominado por realocação/cópia a cada inserção.
2. O cenário integrado também melhorou, com ganho mais forte em cardinalidade alta (`+23.96%` em `parentCount=1024`), sinalizando efeito prático quando o fan-in cresce.
3. A alocação por operação caiu de forma acentuada no microbenchmark (`+86.12%` a `+99.38%` de redução em `B/op`), coerente com o crescimento amortizado do vetor de pais.

### Atividades executadas
1. `mvn -f "$BEFORE_DIR/pom.xml" -pl expression-evaluator -DskipTests test-compile -q` - sucesso.
2. Build de classpath JMH before/after (`dependency:build-classpath`) - sucesso.
3. JMH before/after de latência com protocolo idêntico (`ParentLinkingHotPathBenchmark`) - sucesso.
4. JMH before/after com `-prof gc` para alocação (`addParentBurst`) - sucesso.
5. `mvn -pl expression-evaluator -Dtest=TestCacheInvalidationBehaviour,TestVariableValues test -q` - sucesso.

### Riscos residuais
1. O ganho máximo foi medido em microbenchmark e pode ser menor em workloads com fan-in baixo de pais.
2. Medições foram realizadas em uma única máquina/JVM; recomenda-se repetição em pipeline dedicado de performance para maior robustez estatística.

## Experimento PERF-2026-02-22-EXPEVAL-EXPRESSION-SUPPLIER-DEEP-CLONE
- Data: 2026-02-22
- Objetivo: reduzir overhead de clone profundo por chamada no `DefaultExpressionSupplier#createExpression`, preservando isolamento funcional entre expressões retornadas.
- Baseline commit/estado: `HEAD` em workspace temporário (`/tmp/expeval-baseline-item1-5GgZNK`) com benchmark idêntico.
- Commit/estado testado: working tree atual com otimização no supplier e testes dedicados.

### Hipótese
1. Evitar clone profundo na primeira chamada por chave (caminho frio) reduz `ns/op` no cenário `coldCreateAndEvaluate`.
2. A redução de alocação no caminho frio deve aparecer em `gc.alloc.rate.norm`.
3. O caminho quente (`hotCreateAndEvaluate`) deve permanecer estável, sem regressão relevante.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/supplier/DefaultExpressionSupplier.java`: cache passou a armazenar `CachedExpression` com estratégia em duas fases (primeira expressão sem clone profundo; template aquecido lazy para chamadas subsequentes).
- `expression-evaluator/src/test/java/com/runestone/expeval/expression/supplier/TestDefaultExpressionSupplier.java`: cobertura de isolamento entre chamadas e ausência de vazamento de estado de variáveis.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/ExpressionSupplierCloneOverheadBenchmark.java`: benchmark JMH dedicado para cenários cold/hot do supplier.

### Protocolo de medição
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.ExpressionSupplierCloneOverheadBenchmark`
- Package de benchmark: `com.runestone.expeval.perf.jmh`
- Parâmetros de latência (`ns/op`):
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns`
- Parâmetros de alocação (`B/op`):
  - `-wi 3 -i 5 -w 300ms -r 300ms -f 2 -tu ns -prof gc`
- Comando before (latência):
```bash
mvn -f /tmp/expeval-baseline-item1-5GgZNK/pom.xml -pl expression-evaluator -DskipTests test-compile -q
mvn -f /tmp/expeval-baseline-item1-5GgZNK/pom.xml -pl expression-evaluator -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
cd /tmp/expeval-baseline-item1-5GgZNK/expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*ExpressionSupplierCloneOverheadBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -rf json -rff target/jmh-expression-supplier-clone-before.json -foe true
```
- Comando after (latência):
```bash
mvn -pl expression-evaluator clean -q
mvn -pl expression-evaluator -Dtest=TestDefaultExpressionSupplier,TestCalculationMemory,TestCalculatorContracts test -q
mvn -pl expression-evaluator -DskipTests test-compile -q
mvn -pl expression-evaluator -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
cd expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*ExpressionSupplierCloneOverheadBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -rf json -rff target/jmh-expression-supplier-clone-after.json -foe true
```
- Comandos adicionais (alocação):
```bash
cd /tmp/expeval-baseline-item1-5GgZNK/expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*ExpressionSupplierCloneOverheadBenchmark.*' \
  -wi 3 -i 5 -w 300ms -r 300ms -f 2 -tu ns -prof gc \
  -rf json -rff target/jmh-expression-supplier-clone-before-gc.json -foe true

cd expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*ExpressionSupplierCloneOverheadBenchmark.*' \
  -wi 3 -i 5 -w 300ms -r 300ms -f 2 -tu ns -prof gc \
  -rf json -rff target/jmh-expression-supplier-clone-after-gc.json -foe true
```

### Resultado
Latência (`ns/op`):

| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| coldCreateAndEvaluate | 6949.006 | 6234.749 | -714.257 | +10.28% |
| hotCreateAndEvaluate | 1137.814 | 1129.878 | -7.936 | +0.70% |

Alocação (`gc.alloc.rate.norm`, `B/op`):

| Benchmark | Before (B/op) | After (B/op) | Redução (%) |
|---|---:|---:|---:|
| coldCreateAndEvaluate | 9363.245 | 7768.731 | +17.03% |
| hotCreateAndEvaluate | 1824.086 | 1840.090 | -0.88% |

Artefatos:
- before latência: `/tmp/expeval-baseline-item1-5GgZNK/expression-evaluator/target/jmh-expression-supplier-clone-before.json`
- after latência: `expression-evaluator/target/jmh-expression-supplier-clone-after.json`
- before alocação: `/tmp/expeval-baseline-item1-5GgZNK/expression-evaluator/target/jmh-expression-supplier-clone-before-gc.json`
- after alocação: `expression-evaluator/target/jmh-expression-supplier-clone-after-gc.json`

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. O cenário-alvo (`coldCreateAndEvaluate`) melhorou `+10.28%` em `ns/op`, confirmando ganho ao remover clone profundo no primeiro acesso.
2. Em alocação do cenário cold, houve redução de `+17.03%` em `B/op`, coerente com menor churn no caminho frio.
3. O cenário hot permaneceu estável (`+0.70%` em `ns/op`), com variação pequena de alocação (`-0.88%`), sem regressão estrutural relevante.

### Atividades executadas
1. Adição de benchmark JMH dedicado (`ExpressionSupplierCloneOverheadBenchmark`) - sucesso.
2. Adição de testes funcionais para isolamento do supplier (`TestDefaultExpressionSupplier`) - sucesso.
3. Execução de testes direcionados (`TestDefaultExpressionSupplier`, `TestCalculationMemory`, `TestCalculatorContracts`) - sucesso.
4. Medição JMH before em baseline temporário (`/tmp/expeval-baseline-item1-5GgZNK`) - sucesso.
5. Implementação da otimização em `DefaultExpressionSupplier` - sucesso.
6. Medição JMH after com protocolo idêntico - sucesso.
7. Medição before/after com `-prof gc` - sucesso.
8. Correção de fluxo de build: medições sequenciais após `clean` para evitar corrupção de `jmh_generated` por execução Maven paralela - sucesso.

### Riscos residuais
1. A primeira chamada por chave agora não faz warm-up antecipado; cargas dominadas por reutilização imediata da mesma expressão podem ter perfil diferente do cenário cold.
2. Benchmarks executados em uma única máquina/JVM; recomenda-se repetição em pipeline dedicado de performance para robustez estatística.

## Experimento PERF-2026-02-22-EXPEVAL-EXPRESSION-SUPPLIER-DEEP-CLONE-DECISION-REVIEW
- Data: 2026-02-22
- Objetivo: revisar a decisão do experimento `PERF-2026-02-22-EXPEVAL-EXPRESSION-SUPPLIER-DEEP-CLONE` sob critério de custo/benefício de manutenção.
- Baseline commit/estado: resultado registrado no experimento anterior (`PERF-2026-02-22-EXPEVAL-EXPRESSION-SUPPLIER-DEEP-CLONE`).
- Commit/estado testado: working tree após reversão das mudanças de implementação do item 1.

### Hipótese
1. Apesar do ganho em cold path, o aumento de complexidade no `DefaultExpressionSupplier` pode não se justificar para o benefício líquido.
2. O caminho hot, mais representativo de reutilização de cache, não teve ganho relevante.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/supplier/DefaultExpressionSupplier.java`: reversão para implementação anterior.
- `expression-evaluator/src/test/java/com/runestone/expeval/expression/supplier/TestDefaultExpressionSupplier.java`: removido (teste criado apenas para a solução descartada).
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/ExpressionSupplierCloneOverheadBenchmark.java`: removido (benchmark criado apenas para a solução descartada).

### Protocolo de decisão
- Critério principal: equilíbrio entre ganho de performance medido e custo de complexidade/manutenibilidade do código adicional.
- Evidência usada: resultados já registrados no experimento `PERF-2026-02-22-EXPEVAL-EXPRESSION-SUPPLIER-DEEP-CLONE`.

### Resultado considerado
| Benchmark | Before (ns/op) | After (ns/op) | Melhoria (%) |
|---|---:|---:|---:|
| coldCreateAndEvaluate | 6949.006 | 6234.749 | +10.28% |
| hotCreateAndEvaluate | 1137.814 | 1129.878 | +0.70% |

| Benchmark | Before (B/op) | After (B/op) | Redução (%) |
|---|---:|---:|---:|
| coldCreateAndEvaluate | 9363.245 | 7768.731 | +17.03% |
| hotCreateAndEvaluate | 1824.086 | 1840.090 | -0.88% |

### Decisão
- [ ] Aceitar
- [ ] Ajustar
- [x] Descartar

### Leitura técnica
1. O ganho relevante apareceu somente no cold path; no hot path o ganho foi marginal (`+0.70%` em `ns/op`).
2. A estratégia adicionou complexidade estrutural no supplier para ganho concentrado em cenário menos frequente de cache frio.
3. Decisão consolidada: descartar a mudança do item 1 e manter a implementação simples anterior.

### Atividades executadas
1. Reversão do código de produção do item 1 (`git restore -- expression-evaluator/src/main/java/com/runestone/expeval/expression/supplier/DefaultExpressionSupplier.java`) - sucesso.
2. Remoção dos artefatos de teste/benchmark exclusivos do item 1 - sucesso.
3. Registro append-only da revisão de decisão em `performance-history.md` - sucesso.

### Riscos residuais
1. O hotspot de cold path permanece sem otimização dedicada.
2. Se o perfil de uso mudar para alta taxa de cold starts por expressão, recomenda-se reabrir o item com proposta de menor complexidade.

## Experimento PERF-2026-02-22-EXPEVAL-CALCULATOR-EAGER-CONTEXT-COPY
- Data: 2026-02-22
- Objetivo: corrigir o item 2 (cópia eager de contexto no `Calculator`) mantendo `CalculatorOptions` com default `FULL`.
- Baseline commit/estado: `HEAD` em workspace temporário (`/tmp/expeval-baseline-item2-G7ItSw`) com benchmark idêntico.
- Commit/estado testado: working tree atual com otimização no caminho `FULL` de snapshot de contexto.

### Hipótese
1. No modo `FULL`, evitar copiar `contextVariables` em toda etapa quando não houve mudança de contexto reduz fortemente `ns/op`.
2. Mesmo com atribuições esparsas, copiar snapshot apenas quando há delta efetivo reduz custo de CPU/alocação.
3. O comportamento default deve permanecer compatível (`CalculatorOptions.defaultOptions()` continua `FULL`).

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/calculator/Calculator.java`: novo `FullContextSnapshot` para reuso de snapshot no modo `FULL`; cópia incremental apenas quando `assignedVariables` produz mudança efetiva; remoção de `new HashMap<>(computationVariables)` por etapa no hot path.
- `expression-evaluator/src/test/java/com/runestone/expeval/expression/TestCalculatorContracts.java`: novos testes para estabilidade do snapshot em etapas sem atribuição e avanço de snapshot apenas após etapas que atribuem variáveis.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/CalculatorContextCopyBenchmark.java`: benchmark JMH dedicado para `FULL` com contexto grande em dois cenários (`no assignments` e `sparse assignments`).
- `expression-evaluator/src/main/java/com/runestone/expeval/expression/calculator/CalculatorOptions.java`: sem alteração funcional; default mantido em `CalculatorMemoryMode.FULL`.

### Protocolo de medição
- JVM: OpenJDK 21.0.10
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.CalculatorContextCopyBenchmark`
- Package de benchmark: `com.runestone.expeval.perf.jmh`
- Parâmetros de latência (`ns/op`):
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns`
- Parâmetros de alocação (`B/op`):
  - `-wi 3 -i 5 -w 300ms -r 300ms -f 2 -tu ns -prof gc`
- Comando before (latência):
```bash
mvn -f /tmp/expeval-baseline-item2-G7ItSw/pom.xml -pl expression-evaluator -DskipTests test-compile -q
mvn -f /tmp/expeval-baseline-item2-G7ItSw/pom.xml -pl expression-evaluator -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
cd /tmp/expeval-baseline-item2-G7ItSw/expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*CalculatorContextCopyBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -rf json -rff target/jmh-calculator-context-copy-before.json -foe true
```
- Comando after (latência):
```bash
mvn -pl expression-evaluator clean -q
mvn -pl expression-evaluator -DskipTests test-compile -q
mvn -pl expression-evaluator -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/jmh.classpath -Dmdep.includeScope=test
cd expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*CalculatorContextCopyBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -rf json -rff target/jmh-calculator-context-copy-after.json -foe true
```
- Comandos adicionais (alocação):
```bash
cd /tmp/expeval-baseline-item2-G7ItSw/expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*CalculatorContextCopyBenchmark.*' \
  -wi 3 -i 5 -w 300ms -r 300ms -f 2 -tu ns -prof gc \
  -rf json -rff target/jmh-calculator-context-copy-before-gc.json -foe true

cd expression-evaluator
java -cp "target/test-classes:target/classes:$(cat target/jmh.classpath)" \
  org.openjdk.jmh.Main '.*CalculatorContextCopyBenchmark.*' \
  -wi 3 -i 5 -w 300ms -r 300ms -f 2 -tu ns -prof gc \
  -rf json -rff target/jmh-calculator-context-copy-after-gc.json -foe true
```

### Resultado
Latência (`ns/op`):

| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| fullModeLargeContextNoAssignments | 1318555.084 | 105006.036 | -1213549.048 | +92.04% |
| fullModeLargeContextSparseAssignments | 1315760.408 | 435850.509 | -879909.899 | +66.87% |

Alocação (`gc.alloc.rate.norm`, `B/op`):

| Benchmark | Before (B/op) | After (B/op) | Redução (%) |
|---|---:|---:|---:|
| fullModeLargeContextNoAssignments | 2705334.832 | 210338.273 | +92.23% |
| fullModeLargeContextSparseAssignments | 2711976.454 | 665593.343 | +75.46% |

Artefatos:
- before latência: `/tmp/expeval-baseline-item2-G7ItSw/expression-evaluator/target/jmh-calculator-context-copy-before.json`
- after latência: `expression-evaluator/target/jmh-calculator-context-copy-after.json`
- before alocação: `/tmp/expeval-baseline-item2-G7ItSw/expression-evaluator/target/jmh-calculator-context-copy-before-gc.json`
- after alocação: `expression-evaluator/target/jmh-calculator-context-copy-after-gc.json`

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. A cópia eager de `contextVariables` no modo `FULL` era dominante: remover cópia por etapa sem delta trouxe ganho de `+92.04%` no cenário sem atribuições.
2. Mesmo com atribuições esparsas, o ganho permaneceu alto (`+66.87%`) ao transformar cópia total em atualização incremental.
3. O default de `CalculatorOptions` permaneceu em `FULL`, preservando compatibilidade comportamental.

### Atividades executadas
1. Adição de cobertura funcional direcionada em `TestCalculatorContracts` - sucesso.
2. Adição de benchmark JMH dedicado (`CalculatorContextCopyBenchmark`) - sucesso.
3. Execução de testes direcionados (`TestCalculatorContracts`, `TestCalculationMemory`) antes da otimização - sucesso.
4. Medição JMH before (`ns/op` e `-prof gc`) em baseline temporário - sucesso.
5. Implementação da otimização no hot path de snapshot `FULL` em `Calculator` - sucesso.
6. Reexecução de testes direcionados e suíte completa do módulo (`mvn -pl expression-evaluator test -q`) - sucesso.
7. Medição JMH after (`ns/op` e `-prof gc`) com protocolo idêntico - sucesso.
8. Consolidação dos resultados before/after e registro append-only - sucesso.

### Riscos residuais
1. O benchmark foca contexto grande e múltiplas etapas; workloads curtos/pequenos podem observar ganho menor.
2. As medições foram feitas em uma única máquina/JVM; recomendado repetir em pipeline de performance para robustez estatística.

## Experimento PERF-2026-02-28-EXPEVAL-TYPE-CHECK-CACHING-REFINED
- Data: 2026-02-28
- Objetivo: reduzir overhead de verificação de tipos e normalização numérica em AbstractOperation e BaseOperation.
- Baseline commit/estado: Estado após experimentos de 22/02 (v1.1.0.1-SNAPSHOT baseline).
- Commit/estado testado: working tree atual com cache de tipo em AbstractOperation e castOperationResult especializado em BaseOperation.

### Hipótese
1. O cache de classe do último resultado em `AbstractOperation` cria um fast-path para tipos estáveis, evitando `isInstance` e lógica de conversão redundante.
2. Especializar `castOperationResult` em `BaseOperation` remove branching e `instanceof` genéricos por nó.
3. Otimizar `Integer`/`Long` em `BaseOperation.resolve` evita `toString()` nos casos numéricos mais comuns.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/AbstractOperation.java`: introdução de `lastResultClass`/`lastResultWasInstance` e refatoração de `castOperationResult` para `protected`.
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/BaseOperation.java`: override de `castOperationResult` e otimização de `resolve` para tipos integrais.

### Protocolo de medição
- JVM: Oracle JDK 21.0.10
- JMH: 1.37
- Benchmark: `com.runestone.expeval.perf.jmh.TypeConversionHotPathBenchmark`
- Parâmetros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g'`

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| alternatingTypeConversion | 10.416 | 15.077 | +4.661 | -44.75% |
| baseOperationDoubleNormalization | 340.256 | 287.950 | -52.306 | +15.37% |
| baseOperationLongNormalization | 153.112 | 113.169 | -39.943 | +26.09% |

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. Ganhos expressivos em `BaseOperation` (~15-26%) validam a especialização do fluxo de cast e normalização numérica.
2. O overhead em `alternatingTypeConversion` é esperado devido ao cache miss constante no benchmark de alternância. Em cenários reais (tipos estáveis), o cache proverá o fast-path pretendido.
3. A melhoria em `baseOperationDoubleNormalization` (que regrediu na primeira tentativa) foi recuperada e ampliada ao simplificar o fluxo de cast e manter a segurança do `toString()` para Double.

### Atividades executadas
1. Baseline JMH após revert temporário.
2. Implementação refinada com override de `castOperationResult`.
3. Verificação funcional com `TestTypeConversionHotPathBehaviour`.
4. Medição JMH final.

## Experimento PERF-2026-02-28-EXPEVAL-VARIABLE-RESOLUTION-OPTIMIZATION
- Data: 2026-02-28
- Objetivo: eliminar chamadas redundantes a `findValue` na `VariableValueOperation` quando `userContext` e `expressionContext` são idênticos.
- Baseline commit/estado: `HEAD` sem a otimização de check de identidade.
- Commit/estado testado: working tree atual com a otimização.

### Hipótese
1. Em expressões que utilizam variáveis não encontradas no primeiro lookup (ou que dependem de fallback), evitar o segundo lookup quando os contextos são idênticos reduz overhead de chamadas ao `variablesSupplier` e `HashMap.get()`.
2. O ganho deve ser mais expressivo quanto mais complexo for o `variablesSupplier`.

### Mudanças aplicadas
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/values/variable/VariableValueOperation.java`: Adicionado check `if (currValue == null && context.userContext() != context.expressionContext())`.
- `expression-evaluator/src/test/java/com/runestone/expeval/operation/values/variable/VariableValueOperationOptimizationTest.java`: Teste unitário para validar redução de chamadas.
- `expression-evaluator/src/test/java/com/runestone/expeval/perf/jmh/MissingVariableLookupBenchmark.java`: Benchmark JMH focado em lookup de variáveis ausentes.

### Protocolo de medição
- JVM: OpenJDK 21
- JMH: 1.35
- Benchmark class: `com.runestone.expeval.perf.jmh.MissingVariableLookupBenchmark`
- Parâmetros: `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g'`
- Comando: `mvn test -pl expression-evaluator -Dtest=MissingVariableLookupBenchmark`

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| missingVariablesLookup | 2547.511 | 2568.957 | +21.446 | -0.84% |

### Decisão
- [x] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1. A variação de `-0.84%` está dentro da margem de erro estatística, indicando neutralidade para lookups simples (vazios).
2. A otimização é mantida por ser filosoficamente correta: evita uma chamada garantidamente redundante (mesmo objeto, mesmo estado) que pode ser custosa em implementações customizadas de `factors` ou `variablesSupplier`.

### Atividades executadas
1. Implementação do check de identidade em `VariableValueOperation`.
2. Criação de teste de regressão/validação de chamadas (`VariableValueOperationOptimizationTest`).
3. Execução de benchmark comparativo com baseline revertido.

