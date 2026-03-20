# Audit Trail Design — `computeWithAudit()`

**Status:** Planejamento
**Data:** 2026-03-20
**Módulo:** `exp-eval-mk2`

---

## Objetivo

Adicionar um método `computeWithAudit()` em `MathExpression` e `LogicalExpression` que, além de calcular a expressão, retorna dados de auditoria da execução: variáveis acessadas com seus valores, chamadas de função com argumentos de entrada e resultado, e atribuições intermediárias.

A coleta deve ter **overhead zero quando não solicitada** e penalidade mínima quando ativa.

---

## Contexto arquitetural

O pipeline de avaliação percorre uma árvore de `ExecutableNode` via `AbstractRuntimeEvaluator.evaluateExpression()` recursivamente, passando `ExecutionScope` em cada nível. O escopo é o canal natural para injetar um coletor opcional de auditoria sem alterar as assinaturas dos métodos de avaliação.

Pontos relevantes do pipeline atual:

- **Leitura de variável** → `case ExecutableIdentifier` → `scope.find(id.ref())`
- **Literal dinâmico** → `case ExecutableDynamicLiteral` → `scope.resolveDynamic(dyn.kind())` — representa `currDate`, `currTime`, `currDateTime`; o escopo faz cache do valor resolvido para consistência dentro da execução, mas cada ocorrência na árvore de nós deve ser capturada individualmente na auditoria como `VariableRead` com `kind = INTERNAL`
- **Chamada de função** → `evaluateFunctionCall()` → `descriptor.invoke(args)`
- **Atribuição** → `executeAssignment()` → `scope.assign(target, value)`
- **Compilação em cache** → `ExpressionCompiler` por `(source, environmentId, resultType)` — não é afetada pois o `AuditCollector` é por execução, não por expressão compilada.

---

## Estratégia de performance: opt-in com null-guard

O campo `AuditCollector` no `ExecutionScope` é `null` em execuções normais. O JIT elimina null-checks previsíveis no caminho quente. A penalidade (alocação de `ArrayList`, gravação de eventos) ocorre apenas quando `computeWithAudit()` é chamado explicitamente.

Operações binárias (`+`, `>`, `&&`, etc.) são **intencionalmente excluídas** da coleta padrão — são numerosas e seus resultados são deriváveis das variáveis e chamadas de função já capturadas. Uma opção `AuditOptions.includeBinaryOps()` pode ser adicionada futuramente.

---

## Estrutura de dados

### `AuditEvent` — sealed interface

```java
// pacote: com.runestone.expeval2.internal.runtime.audit
sealed interface AuditEvent permits
    AuditEvent.VariableRead,
    AuditEvent.FunctionCall,
    AuditEvent.AssignmentEvent {

    record VariableRead(String name, SymbolKind kind, Object value, ResolvedType type)
        implements AuditEvent {}

    record FunctionCall(String functionName, List<Object> inputArgs, Object result, int callDepth)
        implements AuditEvent {}

    record AssignmentEvent(String targetName, Object newValue)
        implements AuditEvent {}
}
```

- `VariableRead` — capturada a cada resolução de `ExecutableIdentifier` e de `ExecutableDynamicLiteral`. Para literais dinâmicos, os campos são preenchidos assim:
  - `name` — nome canônico na sintaxe da expressão: `"currDate"`, `"currTime"` ou `"currDateTime"` (derivado de `DynamicInstant`)
  - `kind` — `SymbolKind.INTERNAL`
  - `value` / `type` — extraídos do `RuntimeValue` retornado por `scope.resolveDynamic()`
- `FunctionCall` — capturada após `descriptor.invoke()`, com os argumentos já coercidos e o resultado bruto.
- `AssignmentEvent` — capturada após `scope.assign()` em expressões com `:=`.
- `callDepth` em `FunctionCall` permite reconstruir aninhamento de chamadas.

### `AuditCollector` — acumulador leve

```java
// pacote: com.runestone.expeval2.internal.runtime.audit
final class AuditCollector {
    private final long startNanos = System.nanoTime();
    private final List<AuditEvent> events = new ArrayList<>();
    private int callDepth = 0;

    void record(AuditEvent event) { events.add(event); }
    void enterCall()  { callDepth++; }
    void exitCall()   { callDepth--; }
    int  callDepth()  { return callDepth; }

    ExpressionAuditTrace buildTrace() {
        return new ExpressionAuditTrace(
            List.copyOf(events),
            Duration.ofNanos(System.nanoTime() - startNanos)
        );
    }
}
```

Sem sincronização — a avaliação é single-threaded por escopo.

### `AuditResult<T>` — par resultado + rastreio

```java
// pacote: com.runestone.expeval2.api
public record AuditResult<T>(T value, ExpressionAuditTrace trace) {}
```

### `ExpressionAuditTrace` — visões convenientes sobre os eventos

```java
// pacote: com.runestone.expeval2.api
public record ExpressionAuditTrace(List<AuditEvent> events, Duration evaluationTime) {

    /**
     * Snapshot dos valores das variáveis: para cada nome, o valor da última leitura.
     * Mantém a ordem de primeira aparição via LinkedHashMap.
     */
    public Map<String, Object> variableSnapshot() {
        return events.stream()
            .filter(AuditEvent.VariableRead.class::isInstance)
            .map(AuditEvent.VariableRead.class::cast)
            .collect(Collectors.toMap(
                AuditEvent.VariableRead::name,
                AuditEvent.VariableRead::value,
                (a, b) -> b,
                LinkedHashMap::new
            ));
    }

    /** Lista ordenada das chamadas de função com entradas e saídas. */
    public List<AuditEvent.FunctionCall> functionCalls() {
        return events.stream()
            .filter(AuditEvent.FunctionCall.class::isInstance)
            .map(AuditEvent.FunctionCall.class::cast)
            .toList();
    }

}
```

---

## Modificações necessárias

### 1. `ExecutionScope` — injeção do coletor

Adicionar campo `@Nullable AuditCollector` e nova factory; as factories existentes continuam sem mudança.

```java
final class ExecutionScope {
    // campos existentes...
    private final @Nullable AuditCollector audit;

    // factories existentes permanecem iguais (audit = null)
    static ExecutionScope readOnly(Map<SymbolRef, RuntimeValue> values) {
        return new ExecutionScope(values, false, null);
    }
    static ExecutionScope fromIsolated(Map<SymbolRef, RuntimeValue> values) {
        return new ExecutionScope(values, true, null);
    }

    // nova factory
    static ExecutionScope auditedReadOnly(Map<SymbolRef, RuntimeValue> values, AuditCollector audit) {
        return new ExecutionScope(values, false, audit);
    }
    static ExecutionScope auditedIsolated(Map<SymbolRef, RuntimeValue> values, AuditCollector audit) {
        return new ExecutionScope(values, true, audit);
    }

    boolean hasAudit() { return audit != null; }
    @Nullable AuditCollector audit() { return audit; }
}
```

### 2. `AbstractRuntimeEvaluator` — quatro pontos de coleta

```java
// --- Leitura de variável ---
case ExecutableIdentifier id -> {
    RuntimeValue value = scope.find(id.ref())
        .orElseThrow(() -> new IllegalStateException(
            "missing value for symbol '" + id.ref().name() + "'"));
    if (scope.hasAudit()) {
        scope.audit().record(new AuditEvent.VariableRead(
            id.ref().name(), id.ref().kind(), value.raw(), value.type()));
    }
    yield value;
}

// --- Literal dinâmico (currDate, currTime, currDateTime) ---
// Registrado como VariableRead com kind=INTERNAL; name derivado de DynamicInstant.
case ExecutableDynamicLiteral dyn -> {
    RuntimeValue value = scope.resolveDynamic(dyn.kind());
    if (scope.hasAudit()) {
        scope.audit().record(new AuditEvent.VariableRead(
            dyn.kind().canonicalName(), SymbolKind.INTERNAL, value.raw(), value.type()));
    }
    yield value;
}

// --- Chamada de função ---
private RuntimeValue evaluateFunctionCall(ExecutableFunctionCall node, ExecutionScope scope) {
    FunctionDescriptor descriptor = node.binding().descriptor();
    Object[] args = evaluateArgs(node, scope);   // sem mudança

    if (scope.hasAudit()) scope.audit().enterCall();
    Object rawResult = descriptor.invoke(args);
    RuntimeValue result = runtimeValueFactory.from(rawResult, node.binding().returnType());

    if (scope.hasAudit()) {
        AuditCollector audit = scope.audit();
        audit.exitCall();
        audit.record(new AuditEvent.FunctionCall(
            descriptor.name(),
            List.of(args),
            result.raw(),
            audit.callDepth()
        ));
    }
    return result;
}

// --- Atribuição ---
private void executeAssignment(ExecutableAssignment assignment, ExecutionScope scope) {
    RuntimeValue value = evaluateExpression(assignment.expression(), scope);
    scope.assign(assignment.target(), value);
    if (scope.hasAudit()) {
        scope.audit().record(new AuditEvent.AssignmentEvent(
            assignment.target().name(), value.raw()));
    }
}
```

### 3. `ExpressionRuntimeSupport` — novos métodos de computação

```java
public AuditResult<BigDecimal> computeMathWithAudit() {
    AuditCollector collector = new AuditCollector();
    ExecutionScope scope = createAuditedExecutionScope(collector);
    BigDecimal result = mathEvaluator.evaluate(scope);
    return new AuditResult<>(result, collector.buildTrace());
}

public AuditResult<Boolean> computeLogicalWithAudit() {
    AuditCollector collector = new AuditCollector();
    ExecutionScope scope = createAuditedExecutionScope(collector);
    boolean result = logicalEvaluator.evaluate(scope);
    return new AuditResult<>(result, collector.buildTrace());
}

private ExecutionScope createAuditedExecutionScope(AuditCollector collector) {
    if (hasAssignments) {
        return ExecutionScope.auditedIsolated(bindings.copyValues(), collector);
    }
    return ExecutionScope.auditedReadOnly(bindings.valuesReadOnly(), collector);
}
```

### 4. `MathExpression` e `LogicalExpression` — API pública

```java
// MathExpression
public AuditResult<BigDecimal> computeWithAudit() {
    return runtime.computeMathWithAudit();
}

// LogicalExpression
public AuditResult<Boolean> computeWithAudit() {
    return runtime.computeLogicalWithAudit();
}
```

---

## Arquivos afetados

| Arquivo | Tipo de mudança |
|---|---|
| `internal/runtime/audit/AuditEvent.java` | **novo** — sealed interface com 3 records (`VariableRead` cobre variáveis e literais dinâmicos) |
| `internal/runtime/audit/AuditCollector.java` | **novo** — acumulador com timer |
| `api/AuditResult.java` | **novo** — record `AuditResult<T>` |
| `api/ExpressionAuditTrace.java` | **novo** — views `variableSnapshot()`, `functionCalls()` |
| `internal/runtime/ExecutionScope.java` | campo `@Nullable AuditCollector` + 2 factories auditadas |
| `internal/runtime/AbstractRuntimeEvaluator.java` | 4 null-guarded hooks (identifier, dynamic literal, function, assignment) |
| `internal/runtime/ExpressionRuntimeSupport.java` | `computeMathWithAudit()`, `computeLogicalWithAudit()`, `createAuditedExecutionScope()` |
| `api/MathExpression.java` | `computeWithAudit()` |
| `api/LogicalExpression.java` | `computeWithAudit()` |

---

## Exemplo de uso

```java
MathExpression expr = MathExpression.compile("round(rate * principal / 100, 2)");
expr.setValue("rate", new BigDecimal("12.5"))
    .setValue("principal", new BigDecimal("50000"));

AuditResult<BigDecimal> audit = expr.computeWithAudit();

System.out.println("Resultado: " + audit.value());
// Resultado: 6250.00

audit.trace().variableSnapshot().forEach((k, v) ->
    System.out.println("  var " + k + " = " + v));
// var rate = 12.5
// var principal = 50000

audit.trace().functionCalls().forEach(fc ->
    System.out.printf("  %s(%s) => %s%n", fc.functionName(), fc.inputArgs(), fc.result()));
// round([6250.000..., 2]) => 6250.00

System.out.println("Tempo: " + audit.trace().evaluationTime().toNanos() + " ns");
```

---

## Decisões de design

- **`AuditCollector` por execução, não por expressão compilada** — a compilação continua em cache; o coletor é criado novo a cada chamada de `computeWithAudit()`.
- **Sem interface de listener/callback** — evita indireção de `invokeinterface` no caminho quente; a coleta é inline com null-guard.
- **Operações binárias excluídas por padrão** — deriváveis das variáveis e funções capturadas; candidata a `AuditOptions` futuro.
- **`ExecutableDynamicLiteral` mapeado para `VariableRead`** — são identificadores com valores resolvidos sob demanda, tal como variáveis externas. O campo `kind = INTERNAL` distingue a origem no snapshot; `name` usa o nome canônico da sintaxe (`currDate`, etc.) via `DynamicInstant.canonicalName()`, tornando o `variableSnapshot()` um mapa completo de todos os identificadores usados na expressão.
- **`callDepth` em `FunctionCall`** — permite reconstruir aninhamento de chamadas (ex: `round(sqrt(x), 2)`) sem estrutura de árvore.
- **`List.copyOf(args)` no evento** — snapshot imutável dos argumentos coercidos; evita referência a array mutável.
- **`List.copyOf(events)` no `buildTrace()`** — o `AuditCollector` é descartado após a execução; a trace é imutável e segura para compartilhar.
