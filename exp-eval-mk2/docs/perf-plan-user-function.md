# Plano de Melhoria — Cenário `userFunction`

## Contexto

Comparação cross-module medida após as Fases 1, 2 e 4 (PERF-014):

| Métrica | expression-evaluator | exp-eval-mk2 | Diferença mk2 |
|---|---:|---:|---:|
| ns/op | 842 | 1.060 | −25.8% (mais lento) |
| B/op | 440 | 1.400 | +960 B/op |

Expressão do cenário:

```
weighted(a, b, c) + weighted(d, e, f) + weighted(g, h, i) + weighted(j, k, l)
```

Quatro chamadas de função com aridade 3, sem assignments, 12 variáveis externas.

## Mapa de alocações por `compute()` (estimado)

| Origem | Estimativa | Localização |
|---|---:|---|
| `new HashMap<>(values)` em `copyValues()` | ~580 B | `MutableBindings.java` |
| `new Object[arity]` × 4 chamadas de função | ~160 B | `AbstractRuntimeEvaluator.java:78` |
| `Optional.ofNullable(...)` × 12 lookups de identificador | 0–192 B | `ExecutionScope.java:20` |
| `new RuntimeValue.NumberValue(...)` × 7 wrappings | ~112 B | `AbstractRuntimeEvaluator.java` |
| `new ExecutionScope(...)` | ~16 B | `ExecutionScope.java` |
| **Total estimado** | **~870–1.060 B** | |

### Por que o legado aloca menos

- `FunctionOperation.paramsBuffer` é um `Object[]` pré-alocado reutilizado a cada chamada (`FunctionOperation.java:52`).
- O legado usa um composite tree; as variáveis ficam nos nós — não existe cópia de mapa por execução.
- `OperationCallSite.call()` faz conversão de tipo inline sem listas intermediárias.

## Ordem de execução recomendada

Fase 6 primeiro (maior impacto isolado), depois Fase 5, depois Fase 7.

---

## Fase 5 — Pre-alocação do buffer de argumentos por nó de chamada

**Status:** REJEITADO

**Hipótese:** `evaluateFunctionCall` faz `new Object[arity]` a cada invocação. Com 4 chamadas de aridade 3, são ~160 B/op que podem ser eliminados pré-alocando o buffer no próprio nó, junto ao plan de execução.

**Critério de aceite:** Redução ≥80 B/op no cenário `mk2UserFunction` + testes passando.

### Arquivos

- `ExecutableFunctionCall.java`
- `AbstractRuntimeEvaluator.java`

### Mudança em `ExecutableFunctionCall`

Hoje é um record (imutável por design). Precisa virar uma classe para ter campo mutável:

```java
// antes
record ExecutableFunctionCall(ResolvedFunctionBinding binding, List<ExecutableNode> arguments)
    implements ExecutableNode { ... }

// depois
final class ExecutableFunctionCall implements ExecutableNode {

    private final ResolvedFunctionBinding binding;
    private final List<ExecutableNode> arguments;
    final Object[] argsBuffer;  // package-private, pré-alocado na construção

    ExecutableFunctionCall(ResolvedFunctionBinding binding, List<ExecutableNode> arguments) {
        this.binding = Objects.requireNonNull(binding, "binding must not be null");
        this.arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
        this.argsBuffer = new Object[binding.descriptor().arity()];
    }

    ResolvedFunctionBinding binding() { return binding; }
    List<ExecutableNode> arguments() { return arguments; }
}
```

### Mudança em `AbstractRuntimeEvaluator.evaluateFunctionCall`

```java
// antes
private RuntimeValue evaluateFunctionCall(ExecutableFunctionCall node, ExecutionScope scope) {
    FunctionDescriptor descriptor = node.binding().descriptor();
    int arity = descriptor.arity();
    Object[] args = new Object[arity];  // ← eliminar
    for (int i = 0; i < arity; i++) {
        RuntimeValue evaluated = evaluateExpression(node.arguments().get(i), scope);
        args[i] = runtimeCoercionService.coerce(evaluated, descriptor.parameterTypes().get(i));
    }
    Object rawResult = descriptor.invoke(args);
    return runtimeValueFactory.from(rawResult, node.binding().returnType());
}

// depois
private RuntimeValue evaluateFunctionCall(ExecutableFunctionCall node, ExecutionScope scope) {
    FunctionDescriptor descriptor = node.binding().descriptor();
    Object[] args = node.argsBuffer;    // ← buffer pré-alocado no nó
    int arity = descriptor.arity();
    for (int i = 0; i < arity; i++) {
        RuntimeValue evaluated = evaluateExpression(node.arguments().get(i), scope);
        args[i] = runtimeCoercionService.coerce(evaluated, descriptor.parameterTypes().get(i));
    }
    Object rawResult = descriptor.invoke(args);
    Arrays.fill(args, null);            // ← limpar para não reter referências
    return runtimeValueFactory.from(rawResult, node.binding().returnType());
}
```

### Implicação de thread-safety

`ExpressionRuntimeSupport` já não é thread-safe: `MutableBindings.values` é mutado por `setValue()`. O buffer compartilhado por nó é consistente com esse contrato existente. Nenhuma mudança de API necessária, mas o contrato single-thread deve ser documentado.

---

## Fase 6 — Eliminar cópia do HashMap para expressões sem assignments

**Status:** APLICADO

**Hipótese:** `compute()` cria `new HashMap<>(values)` em todo o caminho, mesmo quando a expressão não tem nenhum assignment. O HashMap de 12 entradas custa ~580 B/op. Para expressões sem assignments, o `ExecutionScope` nunca recebe chamadas de `assign()` — a cópia é puramente defensiva e desnecessária.

Esta é a maior oportunidade de redução de alocação disponível no cenário `userFunction`.

**Critério de aceite:** Redução ≥400 B/op no cenário `mk2UserFunction` + `mk2VariableChurn` não regride + testes passando.

### Arquivos

- `MutableBindings.java`
- `ExecutionScope.java`
- `ExpressionRuntimeSupport.java`

### Passo 1 — `MutableBindings`: expor acesso direto sem cópia

```java
// Novo método — expõe a referência interna como read-only
// Usar apenas quando nenhum assign() será chamado no ExecutionScope
Map<SymbolRef, RuntimeValue> valuesReadOnly() {
    return values;
}
```

### Passo 2 — `ExecutionScope`: adicionar factory read-only

```java
private final Map<SymbolRef, RuntimeValue> values;
private final boolean mutable;

private ExecutionScope(Map<SymbolRef, RuntimeValue> values, boolean mutable) {
    this.values = Objects.requireNonNull(values, "values must not be null");
    this.mutable = mutable;
}

static ExecutionScope readOnly(Map<SymbolRef, RuntimeValue> sharedValues) {
    return new ExecutionScope(
        Objects.requireNonNull(sharedValues, "sharedValues must not be null"),
        false
    );
}

static ExecutionScope fromIsolated(Map<SymbolRef, RuntimeValue> freshValues) {
    return new ExecutionScope(
        Objects.requireNonNull(freshValues, "freshValues must not be null"),
        true
    );
}

public void assign(SymbolRef symbolRef, RuntimeValue value) {
    if (!mutable) {
        throw new IllegalStateException(
            "assign() is not allowed on a read-only ExecutionScope"
        );
    }
    values.put(
        Objects.requireNonNull(symbolRef, "symbolRef must not be null"),
        Objects.requireNonNull(value, "value must not be null")
    );
}
```

### Passo 3 — `ExpressionRuntimeSupport`: cachear flag e bifurcar

```java
// Novo campo final determinado na construção
private final boolean hasAssignments;

private ExpressionRuntimeSupport(CompiledExpression compiledExpression, ...) {
    // campos existentes...
    this.hasAssignments = !compiledExpression.executionPlan().assignments().isEmpty();
}

ExecutionScope createExecutionScope() {
    if (hasAssignments) {
        return ExecutionScope.fromIsolated(bindings.copyValues());   // caminho atual
    }
    return ExecutionScope.readOnly(bindings.valuesReadOnly());       // sem cópia
}
```

### Risco

Se houver bug na detecção (expressão com assignments entra no caminho read-only), `assign()` lança `IllegalStateException`. O erro é detectável imediatamente em teste — sem regressão silenciosa.

---

## Fase 7 — Eliminar `Optional` no lookup de identificadores

**Status:** DESCARTADO

**Hipótese:** `ExecutionScope.find()` retorna `Optional<RuntimeValue>`. Em 12 lookups por `compute()` no cenário `userFunction`, isso pode criar 12 `Optional` por execução (~192 B/op). O JIT elimina objetos escalares com escape analysis com frequência, então o ganho real pode ser zero. Medir antes de aceitar.

**Critério de aceite:** Melhora ≥1% em ns/op no cenário `mk2VariableChurn` (mais lookups por operação), ou descartada se dentro do ruído de medição (±17 ns/op típico).

### Arquivos

- `ExecutionScope.java`
- `AbstractRuntimeEvaluator.java`

### Mudança em `ExecutionScope`

```java
// antes
public Optional<RuntimeValue> find(SymbolRef symbolRef) {
    Objects.requireNonNull(symbolRef, "symbolRef must not be null");
    return Optional.ofNullable(values.get(symbolRef));
}

// depois — manter find() para compatibilidade, adicionar acesso direto
@Nullable
RuntimeValue findOrNull(SymbolRef symbolRef) {
    Objects.requireNonNull(symbolRef, "symbolRef must not be null");
    return values.get(symbolRef);
}
```

### Mudança em `AbstractRuntimeEvaluator`

```java
// antes
case ExecutableIdentifier id -> scope.find(id.ref())
    .orElseThrow(() -> new IllegalStateException(
        "missing value for symbol '" + id.ref().name() + "'"));

// depois
case ExecutableIdentifier id -> {
    RuntimeValue found = scope.findOrNull(id.ref());
    if (found == null) {
        throw new IllegalStateException(
            "missing value for symbol '" + id.ref().name() + "'");
    }
    yield found;
}
```

---

## Fase 8 — Validação JMH e registro PERF-015

**Status:** PENDENTE

### Protocolo de medição (por fase)

Após cada fase individualmente:

```shell
# Tempo por operação — protocolo padrão
java -Xms1g -Xmx1g -cp <classpath> org.openjdk.jmh.Main \
  "CrossModuleExpressionEngineBenchmark" \
  -wi 5 -w 500ms -i 10 -r 500ms -f 3 -tu ns -bm avgt

# Perfil de alocação — apenas cenários userFunction e variableChurn
java -Xms1g -Xmx1g -cp <classpath> org.openjdk.jmh.Main \
  "CrossModuleExpressionEngineBenchmark.(legacy|mk2)(UserFunction|VariableChurn)" \
  -f 1 -wi 5 -w 500ms -i 10 -r 500ms -tu ns -bm avgt -prof gc
```

### Critério de aceite global (Fase 8)

| Métrica | Alvo |
|---|---|
| `mk2UserFunction` ns/op | ≤ 900 ns/op |
| `mk2UserFunction` B/op | ≤ 800 B/op |
| `mk2VariableChurn` ns/op | sem regressão (dentro de ±5%) |
| Testes do módulo | 100% passando via `mvn -q -pl exp-eval-mk2 test` |

### Registro

Criar entrada PERF-015 em `exp-eval-mk2/docs/perf/performance-history.md` com:
- Data de execução
- Resultado de cada fase (Before / After / % improvement)
- Perfil de alocação B/op antes e depois
- Decisão: ACCEPT ou DISCARD por fase
