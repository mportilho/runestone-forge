# Plano de Ação: Otimização de userFunction, conditional e logarithmChain

**Data:** 2026-03-22
**Baseline:** resultados de `CrossModuleComparisonBenchmark` rodado com `forks=0`, JDK 21.0.10
**Escopo:** exp-eval-mk2 — apenas o lado `mk2`. O legacy (`expression-evaluator`) é baseline de referência, não é alterado.

---

## 1. Situação Atual

Os três cenários abaixo são os únicos onde o mk2 **não supera** o legacy em pelo menos uma das duas dimensões (tempo ou alocação):

| Cenário | Grupo 1 (compute) Δ tempo | Grupo 1 Δ B/op | Grupo 2 (audit) Δ tempo | Grupo 2 Δ B/op |
|---------|:---:|:---:|:---:|:---:|
| userFunction | +10% ⚠️ | −7% ≈ | +33% ❌ | +36% ❌ |
| conditional | +16% ⚠️ | −25% ✅ | +68% ❌ | +34% ❌ |
| logarithmChain | +59% ❌¹ | +46% ❌¹ | +58% ❌¹ | +45% ❌¹ |

¹ O deficit do logarithmChain é parcialmente explicado pela diferença de `MathContext` (DECIMAL128 no mk2 vs. precisão padrão no legacy). O plano distingue o que é overhead de pipeline do que é diferença de contrato.

---

## 2. Análise de Causa Raiz por Cenário

### 2.1. userFunction

**Expressão:** `weighted(a,b,c) + weighted(d,e,f) + weighted(g,h,i) + weighted(j,k,l)`
**4 chamadas a `weighted(BigDecimal, BigDecimal, BigDecimal)` por invocação.**

#### Caminhos quentes identificados (grupo compute)

| Alocação | Por chamada | Total (4×) | Descrição |
|----------|:-----------:|:----------:|-----------|
| `new Object[3]` | 1 | 4 | Array de argumentos para invocação via `ResolvedFunctionBinding.invoke()` |
| `NumberValue` por arg | 3 | 12 | Cada variável lida do escopo retorna `RuntimeValue.NumberValue` |
| `.raw()` + coerção | 3 | 12 | Extração de `BigDecimal` de cada `NumberValue` para preencher o `Object[]` |
| `NumberValue` resultado | 1 | 4 | Wrapping do `BigDecimal` retornado pela função |

**Overhead extra no modo audit (grupo 2):**

| Alocação | Por chamada | Total (4×) | Descrição |
|----------|:-----------:|:----------:|-----------|
| `AuditEvent.FunctionCall` record | 1 | 4 | Um evento por chamada |
| Referência ao `Object[] args` | — | — | Reutiliza o mesmo array criado para invocação — sem alocação extra |
| `VariableRead` events | 3 | 12 | Cada leitura de argumento gera um evento se a variável não for literal |

**Conclusão:** o overhead é concentrado na criação de `Object[]` por chamada e nos 12 `NumberValue` intermediários descartados após extração de `.raw()`. O audit piora porque acrescenta 12 `VariableRead` + 4 `FunctionCall` events sobre o mesmo caminho.

---

#### Caminhos quentes identificados (grupo compute)

**Expressão:** `if a > b then a * c - d + e - f else g * h - i + j * k - l endif`

| Alocação | Qtd | Descrição |
|----------|:---:|-----------|
| `ExecutionScope` (read-only) | 1 | Criado por invocação; sem assignment na expressão |
| `NumberValue` por variável lida | 2–7 | Condição (`a`, `b`) + ramo tomado (3–5 variáveis) |
| `BooleanValue` da condição | 1 | Resultado de `a > b`; imediatamente descartado por `asBoolean()` |

**Por que o mk2 é mais lento que o legacy em tempo (−25% em B/op mas +16% em ns/op):**
O legacy usa uma árvore de `AbstractOperation` cujo despacho é uma chamada de método virtual direta. O mk2 tem um loop sobre `conditions()` (lista imutável) + despacho polimórfico via `switch` no evaluator para cada nó. Para expressões simples (1 condição, 2 ramos), o custo fixo do plan walker — incluindo o acesso à lista `conditions.get(0)` — é maior que o saving de alocação.

**Overhead extra no modo audit (grupo 2):**

| Alocação | Qtd | Descrição |
|----------|:---:|-----------|
| `AuditCollector` | 1 | Por invocação |
| `ArrayList<>(maxAuditEvents)` | 1 | Pré-alocado com capacidade pessimista (conta **todos** os ramos, mesmo os não executados) |
| `VariableRead` event por var | 2–7 | Apenas as variáveis lidas no ramo efetivamente executado |
| `ConditionalEvent` | 1 | Registra o ramo tomado e o valor booleano da condição |

O `maxAuditEvents` para a expressão condicional é calculado contando **todos os nós de todas as condições e todos os ramos** — uma estimativa pessimista que pré-aloca capacidade em excesso para a lista de eventos.

---

### 2.3. logarithmChain

**Expressão:** `ln(a) + ln(b) + … + lb(g) + lb(h) + … + lb(l)` (12 chamadas no total)

#### Camada 1: diferença de contrato (não é overhead do pipeline)

| Componente | MathContext | Dígitos de precisão |
|---|---|:---:|
| legacy | `MathContext` default do operador `BigDecimal` | ~15 (double) ou indefinido |
| mk2 | `MathContext.DECIMAL128` | 34 |

A precisão extra de DECIMAL128 faz com que `BigDecimalMath.log()` execute mais iterações internas e aloque `BigDecimal` temporários maiores. Este custo é proporcional à precisão, não ao pipeline do mk2.

**Estimativa do overhead atribuível ao pipeline vs. precisão:**
O cenário `CrossModuleExpressionEngineBenchmark.mk2LogarithmChainDecimal64` (mesmas funções, `DECIMAL64`) não tem um correspondente no benchmark atual — seria o único jeito de isolar o custo do pipeline do custo da precisão.

#### Camada 2: overhead do pipeline mk2 sobre o BigMath (cenário compute)

| Alocação | Por chamada | Total (12×) | Descrição |
|----------|:-----------:|:-----------:|-----------|
| `new Object[1]` | 1 | 12 | Array de argumento (1 elemento por `ln`/`lb`) |
| `NumberValue` do arg | 1 | 12 | Extração do escopo |
| `BigDecimal` (coerção arg→BigDecimal) | 0¹ | 0 | `.raw()` extrai direto; sem alocação de coerção |
| `BigDecimal` resultado BigMath | 1–3 | 12–36 | `ln` aloca 1; `log(base, x)` aloca 3 (dois intermediários + resultado) |
| `NumberValue` resultado | 1 | 12 | Wrapping do retorno da função |

¹ A coerção de `NumberValue` → `BigDecimal` chama `.raw()` diretamente; sem conversão de tipo adicional.

**Overhead extra no modo audit:**
Para o logarithmChain, o overhead do audit é desprezível (−1% tempo, −0% B/op) porque cada iteração do BigMath domina com ~80 µs, tornando o custo de 12 `FunctionCall` events irrelevante.

---

## 3. Propostas de Melhoria

### 3.1. Ações para `userFunction`

---

#### [UF-1] Eliminar `Object[]` por chamada via invocação direta tipada

**Grupo alvo:** Grupo 1 (compute) e Grupo 2 (audit)
**Dimensão impactada:** tempo (primário), B/op (secundário)
**Impacto estimado:** −15 a −25% em ns/op para o cenário userFunction
**Esforço:** alto (exige mudança no gerador de `ExecutableFunctionCall` e em `ResolvedFunctionBinding`)

**Diagnóstico:**
Toda chamada de função passa por:
```java
Object[] args = new Object[arity];
for (int i = 0; i < arity; i++) {
    args[i] = runtimeServices.coerce(evaluateExpression(node.arguments().get(i), scope), paramTypes[i]);
}
Object rawResult = binding.invoke(args);
```
O `Object[]` é criado por chamada, usado apenas internamente, e descartado. Para aritys conhecidos em tempo de compilação (1, 2, 3, 4), esse array pode ser evitado.

**Proposta:**
Gerar, durante a fase de `ExecutionPlanBuilder`, um `ExecutableFunctionCall` especializado por arity. Para arity ≤ 4, usar uma interface funcional tipada que recebe os argumentos como parâmetros individuais e invoca a função sem array:

```java
// Invocador especializado para arity 3
@FunctionalInterface
interface TriFunction {
    Object apply(Object a, Object b, Object c);
}
```

O `ResolvedFunctionBinding` exporia múltiplos invokers gerados a partir do `MethodHandle` via `MethodHandles.explicitCastArguments()` ou `MethodHandle.asCollector()` / `MethodHandle.asSpreader()`. O plano escolheria o invoker baseado no arity.

**Pré-requisito:** medir com profiler se o custo dominante é de fato a alocação do array ou o despacho do MethodHandle. Use `-prof gc` + `-prof stack` no JMH standalone.

---

#### [UF-2] Coerção de argumento em fast-path para `NumberValue → BigDecimal`

**Grupo alvo:** Grupo 1 e Grupo 2
**Dimensão impactada:** tempo
**Impacto estimado:** −5 a −10% em ns/op
**Esforço:** baixo

**Diagnóstico:**
O caminho de coerção geral em `RuntimeCoercionService` é um `switch` sobre `targetType`. Para o caso `BigDecimal.class`, o caminho vai para `asNumber(value)` → `value.raw()`. Esse `switch` é executado 3 vezes por chamada de `weighted`, 12 vezes no total.

**Proposta:**
No `AbstractRuntimeEvaluator`, antes de chamar o coercionService, adicionar um fast-path inline:
```java
Object coerced;
if (evaluated instanceof RuntimeValue.NumberValue nv && paramType == BigDecimal.class) {
    coerced = nv.raw(); // fast path: sem switch, sem virtual dispatch
} else {
    coerced = runtimeServices.coerce(evaluated, paramType);
}
```
Esse padrão pode ser expandido para `Boolean.class` e `String.class` também. É uma otimização de hot path válida desde que o branch predictor do JIT aprenha o padrão dominante.

---

#### [UF-3] Pool de `Object[]` por arity para chamadas sem audit

**Grupo alvo:** Grupo 1 (compute only)
**Dimensão impactada:** B/op
**Impacto estimado:** −20 a −40% em B/op para userFunction
**Esforço:** médio
**Restrição:** incompatível com o modo audit (o evento `FunctionCall` guarda referência ao array; reusar o array corromperia eventos já gravados)

**Proposta:**
Criar um `ThreadLocal<Object[][]>` indexado por arity (máximo 8) no evaluator do modo `compute()`. Antes da chamada, pegar o array do pool; após a chamada, devolvê-lo. Como o evaluator é single-thread por invocação e os arrays não escapam para fora do frame de `evaluateFunctionCall`, o reuso é seguro.

```java
// Apenas no modo sem audit
private static final ThreadLocal<Object[][]> ARG_POOL = ThreadLocal.withInitial(
    () -> new Object[9][] // índice = arity
);

Object[] args = getOrCreateArgs(arity); // pega do pool ou aloca
try {
    fillArgs(args, node.arguments(), scope);
    return binding.invoke(args);
} finally {
    clearArgs(args); // limpa referências para não vazar
    // não devolve ao pool; o ThreadLocal mantém o array
}
```

**Contra-indicação:** aumenta complexidade do evaluator. Validar com alocação medida antes de implementar.

---

#### [UF-4] Lazy audit para argumentos: gravar apenas quando o trace for inspecionado

**Grupo alvo:** Grupo 2 (computeWithAudit)
**Dimensão impactada:** tempo e B/op
**Impacto estimado:** −20 a −30% em ns/op para userFunction no modo audit
**Esforço:** alto (muda semântica da API de auditoria)

**Diagnóstico:**
No modo audit, cada chamada de `weighted` gera:
- 3 × `VariableRead` events (um por argumento: a, b, c)
- 1 × `FunctionCall` event

Se o usuário chama `computeWithAudit()` mas não inspeciona os argumentos das chamadas (caso comum em logging de rastreabilidade de alto nível), esse detalhe é computado desnecessariamente.

**Proposta:**
Introduzir um nível de granularidade de audit controlado por `AuditLevel` enum:

```java
public enum AuditLevel {
    ASSIGNMENTS_ONLY,   // apenas AssignmentEvent — custo mínimo
    FUNCTION_CALLS,     // FunctionCall + AssignmentEvent (sem VariableRead)
    FULL                // comportamento atual
}
```

`computeWithAudit()` continuaria com `FULL` para compatibilidade. Um novo método `computeWithAudit(AuditLevel level)` permitiria selecionar a granularidade. Internamente, `AbstractRuntimeEvaluator` verificaria o nível antes de registrar `VariableRead` events.

**Trade-off:** muda API pública. Discussão separada de design necessária antes de implementar.

---

### 3.2. Ações para `conditional`

---

#### [CO-1] Capacidade pessimista do `ArrayList` de events: usar a capacidade do ramo mais longo

**Grupo alvo:** Grupo 2 (audit)
**Dimensão impactada:** B/op
**Impacto estimado:** −10 a −30% em B/op para conditional no modo audit
**Esforço:** baixo (mudança em `ExpressionRuntimeSupport.countMaxAuditEvents`)

**Diagnóstico:**
O método `countMaxAuditEvents` conta eventos de **todos** os ramos de um `ExecutableConditional`, somando condições + todos os resultados + else. Em runtime, apenas **um** ramo é executado. A pré-alocação do `ArrayList<AuditEvent>` usa essa soma pessimista, reservando memória que nunca será usada.

Para a expressão `if a > b then … else …`:
- Eventos contados: `cond(a,b)` + `then(a,c,d,e,f)` + `else(g,h,i,j,k,l)` = 2 + 5 + 6 = 13 events pré-alocados
- Eventos reais executados: 2 (condição) + 3–5 (ramo tomado) = 5–7

**Proposta:**
Alterar `countMaxAuditEvents` para condicional: em vez de somar todos os ramos, usar o **máximo entre os ramos**:

```java
case ExecutableConditional c -> {
    int condCost = 0;
    for (ExecutableNode cond : c.conditions()) {
        condCost += countNodeEvents(cond);
    }
    // MAX dos ramos em vez de soma
    int maxBranchCost = countNodeEvents(c.elseExpression());
    for (ExecutableNode res : c.results()) {
        maxBranchCost = Math.max(maxBranchCost, countNodeEvents(res));
    }
    yield condCost + maxBranchCost;
}
```

**Risco:** se em expressões aninhadas com múltiplos condicionais o `MAX` for muito menor que o `SUM`, o `ArrayList` pode crescer em runtime para expressões que executam múltiplos ramos em sequência (não aninhados). Esse crescimento é O(1) amortizado mas gera GC. Validar com casos de teste de expressões com múltiplos condicionais sequenciais.

---

#### [CO-2] Especializar `ExecutableConditional` para o caso de condição única

**Grupo alvo:** Grupo 1 (compute) e Grupo 2 (audit)
**Dimensão impactada:** tempo
**Impacto estimado:** −5 a −12% em ns/op para conditional
**Esforço:** médio

**Diagnóstico:**
O método `evaluateConditional` usa um loop `for (int i = 0; i < node.conditions().size(); i++)`. Para `if … then … else … endif` simples (1 condição), esse loop tem overhead de verificação de bounds e acesso via `List.get(0)`. Além disso, `node.conditions()` retorna um `List.copyOf()` — acesso por índice é O(1) mas menos eficiente que acesso direto a campo.

**Proposta:**
Criar uma subvariante `ExecutableSimpleConditional` para o caso `conditions.size() == 1`:

```java
record ExecutableSimpleConditional(
    ExecutableNode condition,   // campo direto, sem List
    ExecutableNode thenExpr,    // campo direto
    ExecutableNode elseExpr     // campo direto
) implements ExecutableNode {}
```

O `ExecutionPlanBuilder` escolheria entre `ExecutableSimpleConditional` e `ExecutableConditional` dependendo do número de condições. O evaluator teria um case especializado:

```java
case ExecutableSimpleConditional sc -> {
    boolean cond = runtimeServices.asBoolean(evaluateExpression(sc.condition(), scope));
    return evaluateExpression(cond ? sc.thenExpr() : sc.elseExpr(), scope);
}
```

Essa especialização elimina o loop, o acesso por índice e o overhead de `conditions.get(0)`.

**Observação:** a maioria das expressões condicionais de negócio (`if age > 18 then … else …`) tem exatamente 1 condição. Essa é a variante de maior volume.

---

#### [CO-3] Inline do `BooleanValue` para `asBoolean()` sem alocação de wrapper

**Grupo alvo:** Grupo 1 (compute) e Grupo 2 (audit)
**Dimensão impactada:** B/op
**Impacto estimado:** −8 a −16 B/op por conditional invocation
**Esforço:** baixo (mudança localizada no evaluator)

**Diagnóstico:**
A condição `a > b` é avaliada como uma `ExecutableBinaryOp` que retorna um `RuntimeValue.BooleanValue`. Esse wrapper é imediatamente descartado por `runtimeServices.asBoolean()`. O wrapping existe para uniformidade do tipo de retorno dos nós, mas é desperdício para condicionais.

**Proposta:**
Adicionar um método especializado `evaluateAsBoolean(ExecutableNode, ExecutionScope)` no evaluator que, para `ExecutableBinaryOp` de comparação, retorna `boolean` diretamente sem criar `BooleanValue`:

```java
private boolean evaluateAsBoolean(ExecutableNode node, ExecutionScope scope) {
    return switch (node) {
        case ExecutableBinaryOp op when op.operator().isComparison() -> {
            RuntimeValue left = evaluateExpression(op.left(), scope);
            RuntimeValue right = evaluateExpression(op.right(), scope);
            yield runtimeServices.compareAndTest(op.operator(), left, right);
        }
        default -> runtimeServices.asBoolean(evaluateExpression(node, scope));
    };
}
```

O `evaluateConditional` chamaria `evaluateAsBoolean(condition, scope)` em vez de `runtimeServices.asBoolean(evaluateExpression(condition, scope))`.

---

### 3.3. Ações para `logarithmChain`

---

#### [LC-1] Adicionar cenário com `DECIMAL64` ao benchmark para isolar overhead de pipeline

**Grupo alvo:** Grupo 1 e Grupo 2
**Dimensão impactada:** análise (pré-requisito para LC-2 e LC-3)
**Esforço:** mínimo (adicionar variante ao `CrossModuleComparisonBenchmark`)

**Diagnóstico:**
Atualmente o benchmark compara legacy (precisão padrão) vs. mk2 (DECIMAL128). Não é possível separar o custo de precisão do custo de pipeline sem uma comparação com a mesma precisão em ambos os lados.

**Proposta:**
Adicionar ao `CrossModuleComparisonBenchmark` o benchmark `logarithmChainDecimal64_mk2Compute` e `logarithmChainDecimal64_mk2Audit`, usando `CrossModuleExpressionBenchmarkSupport.newMk2LogarithmChainExpressionDecimal64()` (já existe no suporte). Isso mostrará o overhead puro do pipeline mk2 para funções transcendentais sem a distorção de precisão.

Espera-se que o resultado com DECIMAL64 seja significativamente mais próximo do legacy, revelando o overhead real atribuível ao pipeline (estimado em 10–25% após equalization de precisão).

---

#### [LC-2] Eliminar `Object[1]` por chamada em funções de arity 1

**Grupo alvo:** Grupo 1 (compute) — audit irrelevante neste cenário
**Dimensão impactada:** B/op
**Impacto estimado:** redução de ~12 × 32 bytes = ~384 B/op (estimativa de layout de array + overhead)
**Esforço:** médio (ver [UF-1] — mesma mudança de invoker especializado por arity)

Esta ação é um subconjunto de [UF-1] aplicado especificamente a `arity == 1`. Para `ln(x)` e `lb(x)`, a chamada pode ser especializada como:

```java
// Invoker especializado para arity 1
interface UnaryFunctionInvoker {
    Object apply(Object arg);
}
```

O `ResolvedFunctionBinding` exporia `invokeUnary(Object arg)` que chama o `MethodHandle` com um único argumento sem criar array. O `ExecutionPlanBuilder` escolheria esse caminho quando `arity == 1`.

**Impacto no logarithmChain:** este cenário tem 12 chamadas de arity 1. Eliminar os 12 `Object[1]` reduziria B/op em ~384 bytes (estimativa de 32 bytes por array pequeno no JVM com overhead de header). O ganho relativo sobre os 1.6 MB/op do BigMath seria de ~0.02% — **marginal**. O argumento real para [LC-2] é reduzir latência de GC em cenários com alto volume de chamadas de arity 1 simples (não transcendentais).

---

#### [LC-3] Oferecer `MathContext` configurável por expressão, não apenas por ambiente

**Grupo alvo:** Grupo 1 e Grupo 2
**Dimensão impactada:** tempo e B/op (via precision trade-off)
**Esforço:** médio

**Diagnóstico:**
O `MathContext` é configurado no `ExpressionEnvironment` e se aplica a **todas** as expressões que usam aquele ambiente. Se uma aplicação tem 100 expressões que precisam de DECIMAL128 e 10 que poderiam operar com DECIMAL64 (onde a perda de precisão é aceitável), não há como otimizar seletivamente.

**Proposta:**
Adicionar à API pública a possibilidade de configurar o `MathContext` por expressão individual durante a compilação:

```java
MathExpression.compile(source, environment, MathContext.DECIMAL64);
```

Ou via método fluente na expressão já compilada:

```java
MathExpression expr = MathExpression.compile(source, environment);
expr.withPrecision(MathContext.DECIMAL64); // downgrades precision for this instance
```

Internamente, o `ExecutionScope` passaria o `MathContext` para as funções que o consomem. O `RuntimeServices` já tem acesso ao `MathContext` do ambiente — seria necessário apenas permitir override por instância de expressão.

**Trade-off:** complexidade de API vs. flexibilidade de otimização. Requer decisão de design antes da implementação.

---

#### [LC-4] Cache de resultado de funções puras sobre argumentos literais (constant folding estendido)

**Grupo alvo:** Grupo 1 (compute)
**Dimensão impactada:** tempo e B/op — especificamente para expressões onde `ln` e `lb` recebem literais
**Esforço:** baixo (o mecanismo de constant folding já existe)

**Diagnóstico:**
O constant folding do mk2 já detecta e colapsa chamadas de funções marcadas como `foldable` quando **todos os argumentos são literais**. `MathFunctions.ln()` e `MathFunctions.lb()` são registradas como `foldable = true` em `ExpressionEnvironmentBuilder.addMathFunctions()`.

No cenário de benchmark, os argumentos são variáveis (`a`, `b`, `c`...) — portanto o folding não se aplica. Mas em cenários de produção onde a expressão é `ln(2.718281828) + lb(1024)`, o resultado já deveria estar colapsado em compile-time.

**Verificação:** confirmar que `addMathFunctions()` registra `ln` e `lb` com `foldable = true`. Se não estiver, esta é uma ação de uma linha:
```java
// Em ExpressionEnvironmentBuilder ou onde as funções são registradas
.registerStaticProvider(MathFunctions.class, /* foldable = */ true)
```

**Impacto no benchmark:** nenhum (as variáveis impedem folding). Mas melhora produção quando expressões têm argumentos constantes.

---

## 4. Matriz de Prioridade

| ID | Cenário | Grupo | Impacto | Esforço | Sequência |
|---|---|---|:---:|:---:|:---:|
| UF-2 | userFunction | 1 e 2 | médio | baixo | **1** |
| CO-3 | conditional | 1 e 2 | baixo | baixo | **2** |
| CO-1 | conditional | 2 | médio | baixo | **3** |
| LC-1 | logarithmChain | 1 e 2 | diagnóstico | mínimo | **4** |
| LC-4 | logarithmChain | 1 | baixo (produção) | baixo | **5** |
| CO-2 | conditional | 1 e 2 | médio | médio | **6** |
| UF-1 | userFunction | 1 e 2 | alto | alto | **7** |
| UF-3 | userFunction | 1 | médio | médio | **8** |
| LC-2 | logarithmChain | 1 | baixo | médio | **9** |
| LC-3 | logarithmChain | 1 e 2 | variável | médio | **10** |
| UF-4 | userFunction | 2 | alto | alto | **11** |

**Critério de sequência:** baixo esforço + retorno imediato primeiro; ações estruturais maiores depois; mudanças de API ao final.

---

## 5. Protocolo de Validação

Para cada ação implementada:

1. **Validação funcional:** rodar `mvn test -pl exp-eval-mk2` — todos os testes existentes devem passar sem modificação.
2. **Benchmark de confirmação:** rodar `CrossModuleComparisonBenchmarkMain` (runner rápido) e comparar o delta com o baseline do documento `cross-module-benchmark-context.md` seção 7.
3. **Critério de aceitação:** a ação é aceita se:
   - O cenário alvo melhora ≥ 5% na dimensão prometida (tempo ou B/op).
   - Nenhum cenário não-alvo regride > 3% em tempo ou > 5% em B/op.
4. **Registro:** atualizar a seção 7 do `cross-module-benchmark-context.md` com os novos números e indicar qual ação foi responsável pela mudança.

---

## 6. O Que Não Está no Escopo deste Plano

- **Otimizações de compilação** (parse, AST, semantic resolution, plan building): o custo de compilação é pago uma vez e cacheado. Melhorias no compile path não afetam os benchmarks de execução.
- **Cenários literalDense, variableChurn e powerChain**: já superam o legacy nos dois grupos. Não precisam de ação imediata.
- **legacy (`expression-evaluator`)**: é baseline de referência apenas. Não é alterado por este plano.
- **Correção de `MathContext` no logarithmChain**: a diferença de precisão é uma diferença de contrato, não um bug. A decisão de qual `MathContext` usar em produção é do consumidor da API. A ação LC-3 é uma melhoria de API, não uma correção.
