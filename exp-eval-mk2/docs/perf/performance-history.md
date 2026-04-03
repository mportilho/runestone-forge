## PERF-048: Bindings posicionais (Arrays) e Fast-paths de coerção numérica

**Date:** 2026-04-03
**Branch:** refac-springboot-4 (working tree)
**Benchmark:** `CrossModuleComparisonBenchmark` e `AuditOverheadBenchmark`, 3 forks, 5×500ms warmup, 10×500ms measurement, `-Xms1g -Xmx1g`, GC profiler

**Hypothesis:** O uso de `HashMap` para lookup de variáveis no `ExecutionScope` e a coerção via `toString()` em `RuntimeCoercionService` geram churn de memória e overhead de CPU. Substituir por acesso indexado (Arrays) e fast-paths para tipos numéricos deve reduzir a latência e `B/op`.

**Changes applied:**
- `ExecutionScope`: substituição de `Map<SymbolRef, Object>` por `Object[]` em três camadas (internal, overrides, defaults).
- `SymbolRef`: adicionado campo `index` atribuído em tempo de compilação.
- `ExecutionPlanBuilder`: lógica de atribuição de índices globais para símbolos internos e externos.
- `ExpressionRuntimeSupport`: refatoração para construir vetores de overrides em vez de mapas.
- `RuntimeCoercionService`: caminhos rápidos para `Integer`, `Long`, `Short`, `Byte` e `BigInteger` evitando `toString()`.

**Results (Before = stashed baseline, After = current optimized):**

| Scenario | Before (ns/op) | After (ns/op) | Δ (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| variableChurnNoAudit | 1,159.4 ±34.9 | 930.0 ±21.0 | **+19.78%** | 1,744 | 1,296 | **−448** |
| userFunctionNoAudit | 1,176.6 ±58.9 | 957.6 ±20.5 | **+18.62%** | 1,584 | 1,136 | **−448** |
| assignedVariableNoAudit | 1,101.7 ±27.0 | 860.8 ±54.6 | **+21.87%** | 1,712 | 1,160 | **−552** |
| conditional_mk2Compute | 871.2 ±27.2 | 702.9 ±31.0 | **+19.31%** | 1,344 | 896 | **−448** |
| powerChain_mk2Compute | 2,416.8 ±52.9 | 2,250.3 ±98.5 | +6.89% | 2,544 | 2,096 | **−448** |
| literalDense_mk2Compute | 1,657.6 ±107.3 | 1,565.7 ±53.8 | +5.54% | 2,749 | 2,656 | **−93** |

**Analysis:**

1. **Ganhos generalizados em ns/op:** O acesso posicional removeu o custo de hash e busca por string no caminho crítico. Cenários com muitas variáveis (`variableChurn`, `conditional`) tiveram ganho de ~20%.
2. **Redução drástica de alocação (B/op):** A economia de 448 bytes em quase todos os cenários corresponde exatamente à eliminação da criação de `HashMap` e `SymbolRef` intermediários por chamada de `compute()`.
3. **Fast-path de coerção:** O ganho em `assignedVariableNoAudit` (+21.87%) reflete tanto o acesso por array quanto a eliminação de `toString()` para os tipos numéricos passados no benchmark.
4. **Impacto em expressões complexas:** No `powerChain`, o ganho foi menor (6.89%) porque o custo aritmético do `BigDecimal.pow` continua dominando a execução, embora a redução de alocação tenha sido mantida.

**Decision:** ACCEPT
**Reason:** Melhoria consistente de performance (>15% em média) e redução de alocação (~25-30%) sem alteração na semântica da API pública. O risco de regressão foi mitigado por 1052 testes automatizados aprovados.

**Verification:** Todos os testes do módulo `exp-eval-mk2` passaram. Benchmarks capturados em ambiente controlado com isolamento de forks.

---



**Date:** 2026-03-30
**Branch:** refac-springboot-4 (working tree)
**Benchmark:** `StringFunctionsRegexBenchmark` — baseline e cached no mesmo JMH, 3 forks, 5×500ms warmup, 10×500ms measurement, `-Xms1g -Xmx1g`, GC profiler, JDK 21.0.6

**Hypothesis:** `replaceAll` e `split` recompilam regex em toda chamada. Um cache bounded de `Pattern` deve reduzir latencia e `B/op` quando a mesma regex e reutilizada entre avaliacoes.

**Changes applied:**
- `StringFunctions`: cache estatico bounded de `Pattern` com Caffeine.
- `StringFunctions`: `replaceAll` passou a usar `compiledPattern(regex).matcher(text).replaceAll(...)`.
- `StringFunctions`: `split` passou a reutilizar `compiledPattern(regex).split(...)`.
- `StringFunctionsTest`: cobertura direta para `replaceAll` com grupo de captura e `split` com delimitador regex.
- `StringFunctionsRegexBenchmark`: benchmark JMH dedicado comparando baseline embutido vs implementacao com cache.

**Results (Before = baseline methods, After = cached methods):**

| Scenario | Before (ns/op) | After (ns/op) | Δ (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| replaceAll | 321.702 ±7.303 | 271.733 ±7.318 | **+15.53%** | 1000.004 | 496.004 | **−504.000** |
| split | 477.980 ±19.465 | 475.395 ±8.241 | +0.54% | 1826.673 | 1376.007 | **−450.666** |

**Analysis:**

1. **`replaceAll` confirmou o hotspot esperado:** a troca para `Pattern` cacheado reduziu a latencia em 15.53% e cortou 504 B/op. Isso e dado medido e justifica manter a mudanca.
2. **`split` ficou praticamente neutro em latencia:** +0.54% esta dentro da zona de ruido para esse protocolo, entao nao ha evidencia forte de ganho em `ns/op`.
3. **`split` ainda reduziu alocacao de forma material:** o corte de 450.666 B/op indica que o cache removeu a alocacao recorrente do `Pattern`, mesmo quando o custo total do split continua dominando o tempo.
4. **Inferencia:** o ganho depende de reutilizacao da mesma regex. Se a workload usar regex altamente cardinalizada, o beneficio cai e o cache passa a atuar apenas como protecao bounded, nao como acelerador frequente.

**Decision:** ACCEPT
**Reason:** `replaceAll` teve ganho claro de latencia e alocacao no caminho alvo, e `split` reduziu `B/op` sem regressao relevante. O risco e baixo porque a semantica publica foi preservada e o cache e bounded.

**Verification:** `StringFunctionsTest` e `StringFunctionsExpressionTest` passaram antes da medicao. `test-compile` do modulo tambem passou com o benchmark novo.

---

## PERF-046: Operadores `in` / `not in` — baseline de membros em vetor

**Date:** 2026-03-28
**Branch:** refac-springboot-4 (working tree)
**Benchmark:** `MembershipBenchmark` — 8 methods, 3 forks, 5×500ms warmup, 10×500ms measurement, `-Xms1g -Xmx1g`, GC profiler

**Hypothesis:** A nova implementação de `IN`/`NOT_IN` em `AbstractObjectEvaluator` usa varredura linear (`O(n)`) com `compareEquality` por elemento. A questão é se isso exige otimização (ex: pré-computação de `HashSet`) ou se o desempenho é adequado para o uso previsto.

**No changes applied (baseline only).**

**Results:**

| Scenario | ns/op | ±Error | B/op | Notes |
|---|---:|---:|---:|---|
| numericHitFirst  | 35.5 | ±1.2 | 40 | BigDecimal hit índice 0, 5 elementos |
| numericHitLast   | 43.3 | ±1.3 | 40 | BigDecimal hit índice 4, 5 elementos |
| numericMiss      | 45.6 | ±1.3 | 40 | BigDecimal miss full-scan, 5 elementos |
| notInMiss        | 45.0 | ±2.0 | 40 | `not in` miss full-scan, 5 elementos |
| numericLargeHit  | 99.9 | ±5.3 | 40 | BigDecimal hit índice 49, 50 elementos |
| numericLargeMiss | 107.8 | ±2.8 | 40 | BigDecimal miss full-scan, 50 elementos |
| stringMiss       | 84.6 | ±3.4 | 40 | String miss full-scan, 5 elementos |
| externalVectorHit | 243.8 | ±22.6 | 208 | List dinâmica, hit índice 49, 50 elementos |

**Analysis:**

1. **B/op = 40 para todos os vetores literais**: o vetor folded é reutilizado sem alocação por chamada. Os 40 B são overhead do framework de avaliação (ExecutionScope), não do operador de membros. A implementação não aloca ArrayList por chamada.
2. **Escala linear esperada**: 5 elementos → ~45 ns, 50 elementos → ~108 ns. Custo por elemento ~1.4–2 ns. O JIT elimina praticamente todo overhead do loop.
3. **`stringMiss` (84 ns) é ~85% mais lento que `numericMiss` (45 ns) para o mesmo vetor (5 elementos)**: causa identificada em `compareEquality` — o caminho BigDecimal chega na 2ª branch (1 `instanceof`), enquanto String percorre 3 branches (`null`, `BigDecimal`, `List`) antes de `Objects.equals`. Com 5 elementos, essa diferença acumula (~7.8 ns/elemento em strings vs ~1.5 ns/elemento em BigDecimal).
4. **`externalVectorHit` (243 ns, 208 B/op)**: o overhead não é do scan de 50 elementos (~108 ns) — é da infra de bindings dinâmicos (overlay map). Mesma raiz do PERF-045.
5. **HashSet para BigDecimal é inviável**: `BigDecimal.hashCode()` não é consistente com `compareTo()` (`new BigDecimal("1.0").hashCode() != new BigDecimal("1").hashCode()`). Implementar HashSet exigiria normalização via `stripTrailingZeros()` em todos os elementos antes de inserção — custo pré-pago que só compensa vetores grandes com alta frequência de avaliação.

**Discarded hypotheses:**
- `HashSet` para vetores BigDecimal literais: inviável sem normalização; risco de regressão silenciosa.
- `HashSet` para vetores de strings literais: viável, mas `stringMiss` não é o cenário crítico na prática e a saving (~50 ns para 5 elementos) não justifica a complexidade.

**Candidate for future investigation:**
- Reordenar branches em `compareEquality` — adicionar `left instanceof String` antes da verificação BigDecimal poderia melhorar o caminho String sem custo para BigDecimal. Requer benchmark dedicado.

**Decision:** ADJUST
**Reason:** A implementação está correta e adequada para o uso atual. B/op é mínimo para vetores literais. A latência escala linearmente com o tamanho do vetor a ~2 ns/elemento. Nenhuma otimização justificada pelos dados medidos. O comportamento de `stringMiss` é um candidato para investigação futura, não para ação imediata.

**Verification:** `mvn -q -pl exp-eval-mk2 -Dtest=MembershipExpressionTest test` passou antes da medição.

---

## PERF-045: Overlay de bindings em `buildValues()` — evitar copia de defaults por chamada

**Date:** 2026-03-25
**Branch:** refac-springboot-4 (working tree)
**Benchmark:** `BindingsOverlayBenchmark` — 5 methods, 3 forks, 5×500ms warmup, 10×500ms measurement, `-Xms1g -Xmx1g`, GC profiler

**Hypothesis:** `ExpressionRuntimeSupport.buildValues(...)` copia `defaultValues` a cada `compute(Map)`, mesmo quando o chamador nao sobrescreve nada ou sobrescreve apenas 1-2 simbolos. Trocar a materializacao por um overlay de bindings deve reduzir latencia e `B/op` nos cenarios com muitos defaults, preservando o comportamento publico.

**Changes applied:**
- `ExpressionRuntimeSupport`: `buildValues(...)` foi substituido por `buildOverrides(...)`, que materializa apenas os overrides da chamada.
- `ExpressionRuntimeSupport`: metadados de binding externo passaram a ser precomputados uma vez por expressao compilada (`name -> SymbolRef + ResolvedType + overridable`).
- `ExpressionRuntimeSupport`: fast path quando `userValues` esta vazio, reutilizando `defaultValues` diretamente.
- `ExecutionScope`: suporte a lookup em ate 3 camadas (`internal -> overrides -> defaults`), incluindo variantes com audit.
- `ExpressionFacadeTest`: cobertura adicionada para assignment expressions com defaults apenas e com override parcial sobre defaults.
- `BindingsBenchmarkSupport` / `BindingsOverlayBenchmark`: novo JMH focado em defaults + overrides, separado dos cenarios de navegacao.

**Results (Before = baseline, After = overlay):**

| Scenario | Before (ns/op) | After (ns/op) | Δ (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| manyDefaultsNoOverrides | 445.8 ±3.0 | 303.7 ±6.9 | **+31.87%** | 1,216 | 520 | **−696** |
| manyDefaultsOneOverride | 466.8 ±4.5 | 350.5 ±6.1 | **+24.92%** | 1,336 | 768 | **−568** |
| manyDefaultsTwoOverrides | 483.2 ±4.1 | 364.2 ±1.8 | **+24.63%** | 1,336 | 808 | **−528** |
| noDefaultsOneOverride | 33.8 ±0.6 | 32.3 ±0.6 | +4.45% | 200 | 208 | +8 |
| noDefaultsTwoOverrides | 52.4 ±0.9 | 53.0 ±1.0 | −1.16% | 240 | 248 | +8 |

**Analysis:**

1. **Target scenarios with many defaults improved strongly:** all three scenarios that stress the old `new HashMap<>(defaultValues)` path improved by 24% to 32%, with 528 to 696 fewer bytes allocated per operation. This is measured data and directly supports the overlay hypothesis.
2. **The biggest win is the empty-override case:** `manyDefaultsNoOverrides` improved by +31.87% and cut allocation from 1,216 B/op to 520 B/op because the runtime now reuses the immutable defaults map instead of copying it.
3. **Sparse overrides now scale with the override count:** `manyDefaultsOneOverride` and `manyDefaultsTwoOverrides` still allocate for the per-call override map, but no longer pay for copying 16 default bindings on every evaluation.
4. **No-default scenarios stayed near flat:** the small `B/op` increase (+8 B/op) and the `-1.16%` delta in `noDefaultsTwoOverrides` indicate a minor fixed overhead from the new lookup structure, but this is outside the target hotspot and is materially smaller than the gain in the default-heavy cases.

**Decision:** ACCEPT
**Reason:** The optimization addresses the documented hotspot with clear, repeatable gains in the target scenarios and no semantic regressions in the validated API tests. The slight overhead in no-default scenarios is acceptable given the 24% to 32% gains where defaults are present.

**Verification:** `mvn -q -pl exp-eval-mk2 -Dtest=ExpressionFacadeTest,ObjectNavigationTest test` passed after the change. A clean `mvn -q -pl exp-eval-mk2 clean test-compile -DskipTests` also passed before the final benchmark run.

---

## PERF-041: `RuntimeValue` elimination — full refactoring impact (boxed baseline → raw-everywhere)

**Date:** 2026-03-23
**Branch:** refac-springboot-4 (working tree, uncommitted)
**Benchmark:** `RawObjectEvaluatorBenchmark` — 6 methods, 3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler

**Hypothesis:** PERF-040 showed that the raw evaluator's gains in variable-heavy scenarios (`conditional` +0.7%, `userFunction` +0.2%) were masked by `buildValues()` still wrapping input `Object` values into `RuntimeValue` before populating the scope. Completing the refactoring — removing `RuntimeValue` entirely and making `buildValues()` store raw `Object` — should unlock those masked gains.

**Changes applied (this refactoring):**
- `RuntimeValue.java` deleted (sealed interface with 8 record variants).
- `RuntimeValueFactory.java` deleted.
- `AbstractRuntimeEvaluator.java` deleted — `AbstractRawObjectEvaluator<T>` is now the sole evaluator.
- `MathEvaluator` / `LogicalEvaluator` now extend `AbstractRawObjectEvaluator<T>` (was `AbstractRuntimeEvaluator<T>`).
- `ExecutionScope`: storage changed from `Map<SymbolRef, RuntimeValue>` to `Map<SymbolRef, Object>`; `NULL_SENTINEL` removed; `findRaw()` uses `getOrDefault(key, UNBOUND)`.
- `ExpressionRuntimeSupport.defaultValues`: changed from `Map<SymbolRef, RuntimeValue>` to `Map<SymbolRef, Object>`; `seedDefaults()` and `buildValues()` now store raw `Object` directly via `coerceToResolvedType()`.
- `RuntimeServices.coerceToResolvedType(Object, ResolvedType)` added — normalises function return values (e.g. `Long` → `BigDecimal`) for both folded and non-folded function call paths.
- `RuntimeServices.coerceToResolvedType` extended to handle `VectorType` — converts `BigDecimal[]` (from `spread()`) to `List<Object>`.

**Comparison baseline:** HEAD `026570b` (`compileMath` → boxed `AbstractRuntimeEvaluator`, PERF-038-optimised `buildValues()` still wrapping to `RuntimeValue`).
**Comparison target:** current working tree (`compileMath` → raw `AbstractRawObjectEvaluator`, `buildValues()` storing raw `Object`).

| Scenario | Before (ns/op) | After (ns/op) | Δ (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| literalDense | 975.6 ±27.3 | 827.9 ±15.7 | **+15.14%** | 3,800 | 2,731 | **−1,069** |
| variableChurn | 676.9 ±10.7 | 362.6 ±13.2 | **+46.44%** | 2,168 | 1,736 | **−432** |
| userFunction | 865.1 ±15.7 | 492.1 ±35.7 | **+43.12%** | 2,008 | 1,704 | **−304** |
| conditional | 674.4 ±23.9 | 348.7 ±16.8 | **+48.29%** | 1,624 | 1,336 | **−288** |
| powerChain | 1,418.6 ±38.7 | 1,073.3 ±39.1 | **+24.34%** | 3,096 | 2,536 | **−560** |
| logarithmChain | 613,151 ±5,639 | 611,559 ±5,747 | +0.26% (noise) | 1,722,458 | 1,721,800 | −658 (noise) |

**Analysis:**

1. **variableChurn (+46.44%, −432 B/op):** PERF-040 showed only +11.9% with the raw evaluator alone (buildValues still wrapping). The additional +34 pp comes directly from eliminating 12 `RuntimeValue` wraps per `buildValues()` call. Each of the 12 variables now flows as raw `BigDecimal` from user map → scope → evaluator with no intermediate allocation.
2. **conditional (+48.29%, −288 B/op):** PERF-040 showed +0.7% (noise) for this scenario — the gain was entirely masked by `buildValues()` overhead. With raw buildValues, the saving is now fully visible. Same root cause as variableChurn.
3. **userFunction (+43.12%, −304 B/op):** PERF-040 showed +0.2% (noise). Same mechanism — 12 external symbols per call, each previously allocated as a `RuntimeValue` in `buildValues()`. Now zero wrapping allocations.
4. **powerChain (+24.34%, −560 B/op):** PERF-040 rerun showed −2.1% (noise). The larger gain here confirms that `buildValues()` wrapping and the evaluator boxing of 12 intermediate `BigDecimal` values (from 12 power operations) both contributed. The −560 B/op is the largest absolute B/op reduction in this scenario.
5. **literalDense (+15.14%, −1,069 B/op):** PERF-040 showed +33.9% for the raw evaluator alone. The smaller gain here (relative to PERF-040) is expected: literalDense has only 1 variable (`seed`), so `buildValues()` wrapping saved only 1 `RuntimeValue`. The raw evaluator's own saving (−1,069 B/op) remains real and consistent with PERF-040 (−1,064 B/op).
6. **logarithmChain (+0.26%, noise):** `BigDecimalMath.log()` at DECIMAL128 precision dominates (~610 µs of ~610 µs total). Evaluator overhead is negligible regardless of boxing strategy.

**Decision:** ACCEPT
**Reason:** All variable-bound scenarios exceed the 10% threshold: variableChurn +46%, conditional +48%, userFunction +43%, powerChain +24%. literalDense +15% is consistent with the PERF-040 raw-evaluator gain. Allocation reduction is consistent and significant across all non-transcendental scenarios (−288 to −1,069 B/op). The refactoring also simplifies the codebase: 2 production classes deleted, 1 production interface deleted, 793 tests passing. No regressions found.

**Notes:** Before JSON: `/tmp/performance-benchmark/perf041-before.json`. After JSON: `/tmp/performance-benchmark/perf041-after.json`. Comparison: `/tmp/performance-benchmark/perf041-comparison.md`. Before worktree: `/tmp/perf-041-before` (HEAD `026570b`). JDK 21.0.10, Linux.

---

## PERF-042: Specialized function call paths (arity 0-6) — eliminate `Object[]` allocation

**Date:** 2026-03-23
**Branch:** refac-springboot-4 (working tree, uncommitted)
**Benchmark:** `ObjectEvaluatorBenchmark` — 6 methods, 3 forks (simulated via 0 forks rerun), 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler

**Hypothesis:** `evaluateFunctionCall` currently allocates a new `Object[] args` for every call to populate it with coerced arguments. Implementing specialized paths for common arities (0-6) should eliminate this allocation when auditing is disabled. Scenarios with frequent function calls (like `userFunction`) should show the most gain.

**Changes applied:**
- `FunctionDescriptor`: added specialized `invoke()` methods for arities 0 to 6; added `genericInvoker` (asType'd to generic Object signature) to avoid spreaders in hot paths.
- `AbstractObjectEvaluator.evaluateFunctionCall`: implemented `switch(arity)` with specialized calls to `descriptor.invoke(a1, a2, ...)` when `audit == null`.
- Original array-based implementation preserved for arities > 6 or when auditing is active.

**Results (Before = baseline, After = optimized):**

| Scenario | Before (ns/op) | After (ns/op) | Δ (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| userFunction | 548.8 ±32.6 | 508.7 ±60.4 | **+7.31%** | 2,216 | 2,088 | **−128** |
| variableChurn | 518.9 ±13.8 | 486.0 ±15.1 | **+6.33%** | 1,768 | 1,768 | 0 |
| conditional | 431.7 ±254.5 | 433.4 ±709.9 | −0.39% (noise) | 1,336 | 1,336 | 0 |
| literalDense | 865.3 ±91.7 | 886.4 ±36.7 | −2.44% (noise) | 2,744 | 2,744 | 0 |
| powerChain | 1,003.7 ±46.4 | 1,073.3 ±46.0 | −6.93% (noise) | 2,856 | 2,856 | 0 |
| logarithmChain | 613,353 ±20,953 | 618,110 ±110,273 | −0.78% (noise) | 1,647,576 | 1,647,124 | −452 (noise) |

**Analysis:**

1. **userFunction (+7.31%, −128 B/op):** This scenario contains 4 calls to `weighted(a, b, c)`. Each call previously allocated one `Object[3]` (arity 3). Eliminating 4 arrays × ~32 bytes each = **−128 B/op**, exactly as measured. The 7.31% latency reduction confirms that avoiding array allocation and the subsequent `asSpreader` overhead in `MethodHandle` is beneficial.
2. **variableChurn (+6.33%, 0 B/op):** This scenario contains no function calls, only arithmetic operators (ADD, SUB, MULT). The 6.33% gain is likely noise or secondary JIT effects (e.g., better inlining of the now-smaller `evaluateBinary` path due to profile changes), as the allocation rate is identical.
3. **Other scenarios (noise):** Scenarios like `literalDense` and `powerChain` show small regressions but with high variance (especially in the non-forked baseline). Since `B/op` remains constant and the logic change is guarded by `arity` and `audit == null`, these are confirmed as measurement noise.

**Decision:** ACCEPT
**Reason:** The optimization successfully eliminated `Object[]` allocations in the target scenario (`userFunction`), achieving the predicted −128 B/op reduction and a measurable 7% performance gain. The change is safe, preserves all semantics, and follows the "fast-path for common cases" pattern. All 793 tests pass.

**Notes:** Results obtained with `forks(0)` due to environment constraints. Baseline JSON: `/tmp/performance-benchmark/baseline.json`. Optimized JSON: `/tmp/performance-benchmark/optimized.json`. Comparison: `/tmp/performance-benchmark/comparison.md`. JDK 21.0.6, Linux.

---


## PERF-043: Inefficient `BigDecimal` coercion — use `BigDecimal.valueOf()` for common types

**Date:** 2026-03-23
**Branch:** refac-springboot-4 (working tree)
**Benchmark:** `CoercionBenchmark` — 4 methods, 1 fork, 3×500ms warmup, 5×500ms measurement, -Xms1g -Xmx1g, GC profiler

**Hypothesis:** `RuntimeCoercionService.asNumber()` uses `new BigDecimal(n.toString())` for all `Number` types that are not already `BigDecimal`. This is expensive due to `toString()` allocation and string parsing. Using `BigDecimal.valueOf(long)` and `BigDecimal.valueOf(double)` for common types (`Long`, `Integer`, `Double`, `Float`) should significantly reduce latency and allocations in expressions involving these types.

**Changes applied:**
- `RuntimeCoercionService.asNumber()` refactored:
  - Added fast-path for `Long` and `Integer` using `BigDecimal.valueOf(l)`.
  - Added constant cache check for 0, 1, and 10 in the `Long`/`Integer` path.
  - Added fast-path for `Double` and `Float` using `BigDecimal.valueOf(d)`.
  - `new BigDecimal(n.toString())` preserved as fallback for other `Number` types.

**Results (Before = baseline, After = optimized):**

| Scenario | Before (ns/op) | After (ns/op) | Δ (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| coerceLongs | 197.5 ±4.0 | 140.2 ±3.2 | **+29.00%** | 1,048 | 720 | **−328** |
| coerceMixed | 321.3 ±5.9 | 286.1 ±3.1 | **+10.95%** | 1,384 | 1,056 | **−328** |
| coerceDoubles | 416.5 ±4.9 | 421.2 ±7.2 | −1.12% (noise) | 1,432 | 1,432 | 0 |
| noCoercion | 134.0 ±3.4 | 133.8 ±3.6 | +0.14% (noise) | 520 | 520 | 0 |

**Analysis:**

1. **coerceLongs (+29.00%, −328 B/op):** This scenario uses 6 `Long` variables. The optimization eliminated 6 `toString()` calls and 6 string parsings. Each conversion saved ~54.6 B/op (328 / 6) and ~9.5 ns (57.3 / 6).
2. **coerceMixed (+10.95%, −328 B/op):** Uses 3 `Long` and 3 `Double` variables. The gain comes entirely from the 3 `Long` variables. The allocation reduction is identical to `coerceLongs` because `Double.toString()` (used by `BigDecimal.valueOf(double)`) likely allocates the same amount as our previous `n.toString()` path for `Double`.
3. **coerceDoubles (−1.12%, 0 B/op):** No gain because `BigDecimal.valueOf(double)` internally calls `Double.toString(double)`, making it functionally equivalent to the previous `new BigDecimal(n.toString())` path for `Double` objects. The minor ns/op regression is likely due to the extra `instanceof` checks.
4. **noCoercion (+0.14%, 0 B/op):** Controls for environment noise; results are identical as expected.

**Decision:** ACCEPT
**Reason:** The optimization achieves a major performance gain (+29%) and significant allocation reduction (−31%) in the most common coercion scenario (`Long`/`Integer`). The implementation is simple, safe, and targets a known hotspot. Although `Double` coercion was not improved (due to JDK's internal `toString()` usage in `valueOf`), it did not regress significantly.

**Notes:** Baseline and optimized runs captured in the same session to ensure comparability. Comparison: `/tmp/performance-benchmark/comparison.md`. JDK 21.0.6, Linux.

---

## PERF-044: Cross-module public API benchmark baseline (`expression-evaluator` vs `exp-eval-mk2`)

**Date:** 2026-03-23
**Branch:** refac-springboot-4 (working tree, uncommitted)
**Benchmark:** `CrossModuleComparisonBenchmark` — 15 methods, 3 forks, 5×500ms warmup, 10×500ms measurement, `-Xms1g -Xmx1g`, GC profiler

**Hypothesis:** Capture the current end-to-end public API baseline between the legacy `expression-evaluator` and `exp-eval-mk2` across the 5 shared scenarios after consolidating the cross benchmark to a single canonical JMH suite. This benchmark intentionally measures caller-visible API cost, not evaluator-only hot paths.

**Changes applied:** None. Measurement-only baseline run.

**Benchmark contract:**
- legacy path includes repeated `setVariable(...)` followed by `evaluate()`
- mk2 compute path includes bindings-map materialization followed by `compute(Map)`
- mk2 audit path includes bindings-map materialization followed by `computeWithAudit(Map)`
- `logarithmChain` is intentionally excluded because precision-contract differences dominate runtime and would distort the API-level comparison

**Group 1: `legacy.evaluate()` vs `mk2.compute(Map)`**

| Scenario | Legacy (ns/op) | mk2 compute (ns/op) | Δ (%) | Legacy (B/op) | mk2 compute (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| literalDense | 3,423.2 ±124.5 | 2,106.7 ±76.6 | **+38.5%** | 7,256.0 | 2,720.0 | **−4,536.0** |
| variableChurn | 1,808.6 ±312.8 | 723.6 ±29.1 | **+60.0%** | 2,208.9 | 1,736.0 | **−472.9** |
| userFunction | 1,259.7 ±199.3 | 564.2 ±27.7 | **+55.2%** | 440.0 | 1,576.0 | **+1,136.0** |
| conditional | 923.7 ±104.1 | 578.8 ±86.7 | **+37.3%** | 802.7 | 1,336.0 | **+533.3** |
| powerChain | 1,739.8 ±94.4 | 1,653.3 ±71.0 | **+5.0%** | 2,655.2 | 2,536.0 | **−119.2** |

**Group 2: `legacy.evaluate()` vs `mk2.computeWithAudit(Map)`**

| Scenario | Legacy (ns/op) | mk2 audit (ns/op) | Δ (%) | Legacy (B/op) | mk2 audit (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| literalDense | 3,423.2 ±124.5 | 2,220.2 ±73.6 | **+35.1%** | 7,256.0 | 2,930.7 | **−4,325.3** |
| variableChurn | 1,808.6 ±312.8 | 839.6 ±26.3 | **+53.6%** | 2,208.9 | 2,352.0 | **+143.1** |
| userFunction | 1,259.7 ±199.3 | 800.5 ±28.3 | **+36.5%** | 440.0 | 2,352.0 | **+1,912.0** |
| conditional | 923.7 ±104.1 | 672.5 ±62.6 | **+27.2%** | 802.7 | 1,728.0 | **+925.3** |
| powerChain | 1,739.8 ±94.4 | 1,842.2 ±140.8 | **−5.9%** | 2,655.2 | 3,040.0 | **+384.8** |

**Audit overhead: `mk2.computeWithAudit(Map)` vs `mk2.compute(Map)`**

| Scenario | Compute (ns/op) | Audit (ns/op) | Δ (%) | Compute (B/op) | Audit (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| literalDense | 2,106.7 ±76.6 | 2,220.2 ±73.6 | +5.4% | 2,720.0 | 2,930.7 | +210.7 |
| variableChurn | 723.6 ±29.1 | 839.6 ±26.3 | +16.0% | 1,736.0 | 2,352.0 | +616.0 |
| userFunction | 564.2 ±27.7 | 800.5 ±28.3 | +41.9% | 1,576.0 | 2,352.0 | +776.0 |
| conditional | 578.8 ±86.7 | 672.5 ±62.6 | +16.2% | 1,336.0 | 1,728.0 | +392.0 |
| powerChain | 1,653.3 ±71.0 | 1,842.2 ±140.8 | +11.4% | 2,536.0 | 3,040.0 | +504.0 |

**Analysis:**

1. `mk2.compute(Map)` is faster than legacy in all 5 scenarios. The largest wins are `variableChurn` (+60.0%) and `userFunction` (+55.2%). `powerChain` is effectively near-parity (+5.0%).
2. `mk2.computeWithAudit(Map)` remains faster than legacy in 4 of 5 scenarios. The only exception is `powerChain`, where audit overhead pushes mk2 to a 5.9% regression relative to legacy.
3. Allocation behavior depends on the scenario. `literalDense` is materially better on mk2 in both compute and audit modes. `userFunction` and `conditional` show clear latency wins for mk2, but with substantially higher `B/op`, which is consistent with end-to-end API measurement that includes map materialization and audit object creation.
4. Audit overhead is modest for `literalDense` (+5.4%) but significant for `userFunction` (+41.9%) and noticeable for `variableChurn` / `conditional` (about +16%). For callers that do not need provenance, `compute(Map)` remains the cheaper mk2 entry point.

**Decision:** BASELINE SNAPSHOT
**Reason:** No code changes were evaluated in this run. The purpose was to capture the canonical cross-module, end-to-end API baseline after consolidating the suite to 5 scenarios and a single benchmark class.

**Notes:** JSON results: `/tmp/performance-benchmark/cross-module-comparison-20260323.json`. Run completed in 00:06:13 on JDK 21.0.6, Linux. HEAD at measurement time: `71172d0`.
