## SMELL-001: Array coercion via `Array.set` reflection in `RuntimeCoercionService`

**Date:** 2026-03-19
**Status:** Implemented and benchmarked — ACCEPT (`PERF-018`, `PERF-019`)
**Location:** `RuntimeCoercionService.coerce(RuntimeValue, Class<?>)`

### Baseline code under review (`HEAD`)

```java
if (targetType.isArray() && value instanceof RuntimeValue.VectorValue(List<RuntimeValue> elements)) {
    Class<?> componentType = targetType.getComponentType();
    Object array = Array.newInstance(componentType, elements.size());
    for (int i = 0; i < elements.size(); i++) {
        Array.set(array, i, coerce(elements.get(i), componentType));
    }
    return array;
}
```

### Smell 1 — `Array.set` reflexivo com boxing implícito (Severidade: Medium)

`Array.set` é uma operação reflexiva. Para arrays com `componentType` primitivo (`double[]`, `int[]`, `long[]`), cada escrita exige que o valor seja passado como `Object`, forçando boxing/unboxing por reflexão a cada iteração. Para arrays de `BigDecimal[]` — o caso mais frequente neste avaliador — o overhead é menor (sem primitivos), mas a chamada reflexiva ainda existe por elemento.

**Refactor aplicado:** especializar os tipos mais comuns antes do fallback genérico:

```java
if (componentType == BigDecimal.class) {
    BigDecimal[] typed = new BigDecimal[elements.size()];
    for (int i = 0, n = elements.size(); i < n; i++) {
        typed[i] = asNumber(elements.get(i));
    }
    return typed;
}
// fallback genérico com Array.set permanece para outros tipos
```

Isso elimina a reflexão no caminho quente e evita o overhead de boxing para o tipo predominante.

### Smell 2 — `elements.size()` reavaliado a cada iteração (Severidade: Low)

`elements.size()` é chamado na condição do loop (`i < elements.size()`), repetindo a chamada N+1 vezes. O JIT frequentemente aplica hoisting, mas o padrão é suspeito se `elements` for uma implementação de `List` não-RandomAccess.

**Refactor aplicado:**

```java
int n = elements.size();
for (int i = 0; i < n; i++) { ... }
```

### Smell 3 — `coerce` recursivo com overhead completo por elemento (Severidade: Medium)

Para cada elemento do vetor, `coerce` percorre toda a cadeia de verificações: `Objects.requireNonNull`, checagem `RuntimeValue.class`, checagem `NullValue`, `targetType.isInstance`, além das checagens de tipo sequenciais. Como o `componentType` é uniforme para todos os elementos, esse overhead é repetido N vezes sem necessidade.

Especializar o loop interno (ver Smell 1) resolve este ponto como efeito colateral para os tipos conhecidos. Para arrays de referência genéricos, o overhead recursivo permanece, mas o custo reflexivo de escrita foi removido em `PERF-019`.

### Decisão de benchmark

| Smell | Benchmark necessário? | Condição |
|---|---|---|
| `Array.set` reflexivo | Sim | Antes de aplicar o refactor, se o caminho for quente |
| `size()` no loop | Não | Melhoria local, risco zero |
| `coerce` recursivo | Depende | Resolvido junto com Smell 1 na especialização por tipo |

### Resultado do benchmark

Benchmark JMH executado em 2026-03-19 com `RuntimeCoercionArrayBenchmark`, comparando o baseline reflexivo equivalente ao `HEAD` contra a implementação otimizada no working tree atual:

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | Before (B/op) | After (B/op) |
|---|---:|---:|---:|---:|---:|
| `coerceBigDecimalArray` | 3242.407 | 188.667 | +94.18% | 528.045 | 528.003 |
| `coerceDoubleArray` | 3581.717 | 173.093 | +95.17% | 7184.050 | 1040.002 |
| `coerceComparableArray` | 3337.086 | 511.807 | +84.66% | 528.046 | 528.007 |

Leitura dos dados:

- `BigDecimal[]`: o ganho é majoritariamente de CPU; a alocação fica praticamente estável porque o array final continua existindo nos dois cenários.
- `double[]`: além do ganho de latência, houve forte redução de alocação ao remover boxing/reflection do caminho quente.
- `Comparable[]`: houve ganho forte de CPU ao trocar `Array.set(...)` por escrita direta em arrays de referência; a alocação permaneceu estável.

Conclusão: o refactor deve ser mantido. O benchmark confirmou ganho real tanto nos tipos especializados quanto no principal caso do fallback genérico de referência.
