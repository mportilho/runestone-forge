# Plano de Implementação — Constant Folding para Funções Puras

**Data:** 2026-03-21
**Contexto:** PERF-027 — `BigDecimalMath.log()` aloca ~140 KB por chamada de `ln()` via série de Taylor.
**Objetivo:** Para funções marcadas como foldable com argumentos literais, calcular o resultado uma única vez durante a compilação e armazenar no `ExecutableFunctionCall`, evitando a invocação da série de Taylor a cada `compute()`.

---

## Premissas

1. **Não converter `ExecutableFunctionCall` em `ExecutableLiteral`** — preservar o tipo do nó para que objetos de auditoria e validação continuem vendo uma chamada de função, não uma constante.

2. **Folding apenas em funções internas marcadas explicitamente** — funções registradas pelo usuário via `registerStaticProvider` / `registerInstanceProvider` não são foldable por padrão, pois podem ter efeitos colaterais ou precisar ser reavaliadas a cada `compute()`.

---

## Visão Geral do Fluxo

```
build time:
  ExecutionPlanBuilder.buildFunctionCall()
    → descriptor.isFoldable() && todos os args são ExecutableLiteral?
    → sim: descriptor.invoke(coercedArgs) → armazena (foldedArgs, foldedResult) em ExecutableFunctionCall

runtime (compute()):
  AbstractRuntimeEvaluator.evaluateFunctionCall()
    → node.isFolded()?
    → sim: wrap foldedResult via runtimeValueFactory.from() + emite audit com foldedArgs
    → não: caminho atual inalterado
```

O `ExecutionPlan` resultante já é cacheado pelo Caffeine em `ExpressionCompiler` por `(source, environmentId, resultType)`, portanto o folding ocorre **uma única vez por expressão única**, independentemente de quantas vezes `compute()` é chamado.

---

## Arquivos a Modificar

### 1. `FunctionDescriptor` — `catalog` package

Adicionar `private final boolean foldable`. O construtor público existente permanece inalterado (delega com `foldable = false`). Novo construtor package-private com o parâmetro. Accessor público `isFoldable()`.

```java
// Construtor público existente — assinatura inalterada
public FunctionDescriptor(String name, List<Class<?>> parameterTypes,
                           List<ResolvedType> parameterResolvedTypes,
                           ResolvedType returnType, MethodHandle invoker) {
    this(name, parameterTypes, parameterResolvedTypes, returnType, invoker, false);
}

// Novo construtor package-private
FunctionDescriptor(String name, List<Class<?>> parameterTypes,
                   List<ResolvedType> parameterResolvedTypes,
                   ResolvedType returnType, MethodHandle invoker, boolean foldable) {
    // código existente inalterado +
    this.foldable = foldable;
}

public boolean isFoldable() { return foldable; }
```

Nenhum caller externo quebra — o construtor público mantém assinatura idêntica.

---

### 2. `ExpressionEnvironmentBuilder` — `environment` package

Modificar `toDescriptor(Method, Object)` para aceitar `boolean foldable` internamente: `toDescriptor(Method, Object, boolean)`. A sobrecarga pública existente delega com `false`.

Modificar `addMathFunctions()` para registrar com `foldable = true`. O método público `registerStaticProvider(Class<?>)` permanece sem mudanças — funções de usuário continuam não-foldable por padrão.

```java
public ExpressionEnvironmentBuilder addMathFunctions() {
    return registerStaticProvider(MathFunctions.class, true);  // foldable=true
}

// Método público existente — inalterado, foldable=false
public ExpressionEnvironmentBuilder registerStaticProvider(Class<?> providerClass) {
    return registerStaticProvider(providerClass, false);
}

// Variante privada com foldable
private ExpressionEnvironmentBuilder registerStaticProvider(Class<?> providerClass, boolean foldable) {
    discoverFunctions(providerClass, null, true, foldable)
        .forEach(d -> catalogBuilder.computeIfAbsent(d.name(), k -> new ArrayList<>()).add(d));
    return this;
}
```

`discoverFunctions()` e `toDescriptor()` recebem `foldable` internamente — sem impacto na API pública.

---

### 3. `ExecutableFunctionCall` — `internal.runtime` package

Adicionar dois campos opcionais (`foldedArgs`, `foldedResult`) ao record, com factory methods estáticos para os dois casos e accessor `isFolded()`.

```java
record ExecutableFunctionCall(
    ResolvedFunctionBinding binding,
    List<ExecutableNode> arguments,
    Object[] foldedArgs,      // null se não folded
    Object foldedResult       // null se não folded
) implements ExecutableNode {

    ExecutableFunctionCall {
        Objects.requireNonNull(binding, "binding must not be null");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
    }

    static ExecutableFunctionCall of(ResolvedFunctionBinding binding, List<ExecutableNode> arguments) {
        return new ExecutableFunctionCall(binding, arguments, null, null);
    }

    static ExecutableFunctionCall folded(ResolvedFunctionBinding binding, List<ExecutableNode> arguments,
                                         Object[] foldedArgs, Object foldedResult) {
        return new ExecutableFunctionCall(binding, arguments, foldedArgs, foldedResult);
    }

    boolean isFolded() { return foldedResult != null; }
}
```

`foldedArgs` preserva os valores de entrada coercidos — necessário para emitir `AuditEvent.FunctionCall` com os mesmos args que o caminho normal emitiria.

---

### 4. `ExecutionPlanBuilder` — `internal.runtime` package

**Problema:** o builder não tem `RuntimeCoercionService`, que é necessário para coercir os args literais antes de invocar a função (ex.: literal `Integer` para parâmetro `BigDecimal`).

**Solução:** `ExecutionPlanBuilder` recebe um `RuntimeCoercionService` via construtor. Quem o instancia (`ExpressionCompiler`) tem acesso a `DataConversionService` via `ExpressionEnvironment` e pode criar e injetar o serviço.

> **Ponto a confirmar durante a implementação:** verificar se `ExpressionCompiler` já recebe ou pode receber `DataConversionService` para criar o `RuntimeCoercionService` de folding.

Mudança em `buildFunctionCall()`:

```java
private ExecutableNode buildFunctionCall(FunctionCallNode f, SemanticModel model) {
    ResolvedFunctionBinding binding = model.findFunctionBinding(f.nodeId())
        .orElseThrow(() -> new IllegalStateException(
            "missing function binding for '" + f.functionName() + "'"));
    List<ExecutableNode> arguments = f.arguments().stream()
        .map(arg -> buildNode(arg, model))
        .toList();

    FunctionDescriptor descriptor = binding.descriptor();
    if (descriptor.isFoldable() && arguments.stream().allMatch(ExecutableLiteral.class::isInstance)) {
        int arity = descriptor.arity();
        Object[] args = new Object[arity];
        for (int i = 0; i < arity; i++) {
            RuntimeValue rv = ((ExecutableLiteral) arguments.get(i)).precomputed();
            args[i] = coercionService.coerce(rv, descriptor.parameterTypes().get(i));
        }
        Object rawResult = descriptor.invoke(args);
        return ExecutableFunctionCall.folded(binding, arguments, args, rawResult);
    }

    return ExecutableFunctionCall.of(binding, arguments);
}
```

---

### 5. `AbstractRuntimeEvaluator` — `internal.runtime` package

Adicionar short-circuit no início de `evaluateFunctionCall()`. O audit continua sendo emitido com os mesmos campos — comportamento idêntico ao caminho normal.

```java
private RuntimeValue evaluateFunctionCall(ExecutableFunctionCall node, ExecutionScope scope) {
    if (node.isFolded()) {
        RuntimeValue result = runtimeValueFactory.from(node.foldedResult(), node.binding().returnType());
        AuditCollector audit = scope.audit();
        if (audit != null) {
            audit.enterCall();
            audit.exitCall();
            audit.record(new AuditEvent.FunctionCall(
                node.binding().descriptor().name(),
                node.foldedArgs(),
                result.raw(),
                audit.callDepth()
            ));
        }
        return result;
    }

    // código existente inalterado a partir daqui
    FunctionDescriptor descriptor = node.binding().descriptor();
    int arity = descriptor.arity();
    Object[] args = new Object[arity];
    for (int i = 0; i < arity; i++) {
        RuntimeValue evaluated = evaluateExpression(node.arguments().get(i), scope);
        args[i] = runtimeCoercionService.coerce(evaluated, descriptor.parameterTypes().get(i));
    }
    AuditCollector audit = scope.audit();
    if (audit != null) audit.enterCall();
    Object rawResult = descriptor.invoke(args);
    RuntimeValue result = runtimeValueFactory.from(rawResult, node.binding().returnType());
    if (audit != null) {
        audit.exitCall();
        audit.record(new AuditEvent.FunctionCall(descriptor.name(), args, result.raw(), audit.callDepth()));
    }
    return result;
}
```

---

## Cadeia de Dependência das Mudanças

```
FunctionDescriptor            ← adiciona isFoldable()
         ↓
ExpressionEnvironmentBuilder  ← addMathFunctions() passa foldable=true para toDescriptor()
         ↓
ExecutionPlanBuilder           ← recebe RuntimeCoercionService; buildFunctionCall() faz folding
         ↓ (via ExpressionCompiler — verificar acesso a DataConversionService)
ExecutableFunctionCall         ← adiciona foldedArgs + foldedResult + isFolded()
         ↓
AbstractRuntimeEvaluator       ← short-circuit se isFolded(), audit preservado
```

---

## Cobertura Esperada

Expressões que se beneficiam imediatamente do folding:

| Expressão | Situação |
|---|---|
| `ln(1.05) * n` | folded — `ln(1.05)` vira constante |
| `ln(a) * n` | não folded — `a` é variável |
| `sqrt(2) + x` | folded — `sqrt(2)` vira constante |
| `lb(256)` | folded |
| `ln(a) + ln(a)` | não folded — `a` é variável, mas PERF-028 (memoização de `log(2)`) ajuda `lb()` |
| `ln(myFunc(x))` | não folded — argumento não é literal |

O padrão financeiro mais comum — `fator * ln(taxa)` onde `taxa` é um literal — é diretamente coberto.

---

## Observação sobre `runtimeValueFactory.from()` no caminho folded

O `runtimeValueFactory.from(node.foldedResult(), returnType)` ainda é chamado a cada `compute()`. Trata-se apenas de um wrapping leve (sem cálculo numérico), portanto o custo é desprezível comparado à eliminação da série de Taylor.

---

## Testes a Adicionar

- Verificar que `ExecutableFunctionCall.isFolded()` é `true` para `ln(literal)` após build
- Verificar que `compute()` retorna o resultado correto para expressões com args literais
- Verificar que o `AuditEvent.FunctionCall` é emitido normalmente no caminho folded
- Verificar que `ln(variable)` **não** é folded (campo `foldedResult` deve ser `null`)
- Benchmark `mk2LogarithmChainDecimal128` com expressão de literais antes/depois
