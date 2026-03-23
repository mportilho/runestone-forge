# Contexto de Benchmark Comparativo: expression-evaluator vs exp-eval-mk2

Este documento registra todas as descobertas da investigação inicial que levou à construção do benchmark `CrossModuleComparisonBenchmark`. Seu objetivo é eliminar a fase de investigação em execuções futuras: ao ler este documento, um agente ou desenvolvedor terá contexto suficiente para adicionar cenários, ajustar configurações ou interpretar novos resultados sem precisar reler o código-fonte dos dois módulos do zero.

---

## 1. Os Dois Componentes

### 1.1. expression-evaluator (legacy)

**Módulo Maven:** `expression-evaluator`
**Pacote raiz:** `com.runestone.expeval`

**Ponto de entrada para benchmark:** `com.runestone.expeval.expression.Expression`

```java
Expression expr = new Expression("a * b + c");
expr.setVariable("a", new BigDecimal("2"));
expr.setVariable("b", new BigDecimal("3"));
expr.setVariable("c", new BigDecimal("1"));
BigDecimal result = expr.evaluate();
```

**Características relevantes para o benchmark:**
- `Expression` é mutável: `setVariable()` escreve direto no estado interno.
- O objeto `Expression` pode ser reusado entre chamadas (mesmo objeto, novas variáveis).
- Parse da expressão ocorre na construção; evaluate percorre a árvore de `AbstractOperation` a cada chamada.
- Funções customizadas são registradas via `expression.addFunction(name, methodType, lambda)`.
- Suporta `ln()` e `lb()` nativamente; usa `ch.obermuhlner.BigMath` internamente.
- Não tem separação explícita entre compilação e execução do ponto de vista da API pública.
- Não tem modo de auditoria; retorna apenas o valor calculado.

**Como adicionar função customizada (necessário para cenário userFunction):**
```java
Expression expr = new Expression(USER_FUNCTION_EXPRESSION);
expr.addFunction("weighted", MethodType.methodType(
    BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class
), args -> CustomFunctionFixture.weighted(
    (BigDecimal) args[0], (BigDecimal) args[1], (BigDecimal) args[2]
));
```

---

### 1.2. exp-eval-mk2 (novo)

**Módulo Maven:** `exp-eval-mk2`
**Pacote raiz:** `com.runestone.expeval2`
**API pública:** `com.runestone.expeval2.api`

**Pontos de entrada para benchmark:**
- `MathExpression` — para expressões que retornam `BigDecimal`.
- `LogicalExpression` — para expressões que retornam `boolean`.

```java
// Modo simples (sem ambiente)
MathExpression expr = MathExpression.compile("a * b + c");
BigDecimal result = expr.compute(Map.of("a", 2, "b", 3, "c", 1));

// Com auditoria
AuditResult<BigDecimal> audit = expr.computeWithAudit(Map.of("a", 2, "b", 3, "c", 1));
BigDecimal value = audit.result();
List<AuditEvent> events = audit.events();
```

**Características relevantes para o benchmark:**
- O objeto pré-compilado pode ser reusado entre chamadas (cenário esperado de produção).
- `compile()` executa o pipeline completo: parse → AST → semantic resolution → execution plan. Esse custo ocorre uma vez.
- `compute()` executa apenas o `ExecutionPlan` já construído — é o caminho quente.
- `computeWithAudit()` executa o mesmo plano mas coleta eventos em um `List<AuditEvent>` ao longo da execução. Custo extra varia por tipo de evento: `VariableRead`, `AssignmentEvent`, `FunctionCallEvent` (este aloca `List.of(args)` por chamada), `ConditionalEvent`.
- Funções customizadas são registradas no `ExpressionEnvironment`, não na expressão.
- `ln()` e `lb()` requerem `addMathFunctions()` no ambiente; não estão disponíveis sem isso.
- `MathContext` default é `DECIMAL128` (34 dígitos); pode ser alterado via `withMathContext()`.
- No benchmark cross-module atual, o custo de materializar o `Map<String, Object>` de input faz parte da medição por ser custo observado pelo chamador da API pública.

**Como registrar função customizada no mk2:**
```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .registerStaticProvider(CustomFunctionFixture.class)
    .build();
MathExpression expr = MathExpression.compile(USER_FUNCTION_EXPRESSION, env);
```

A função `weighted` deve ser um método estático público em `CustomFunctionFixture`, anotado ou não — o sistema descobre via reflection.

**Como habilitar `ln()` e `lb()`:**
```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .addMathFunctions()
    .build();
MathExpression expr = MathExpression.compile(LOGARITHM_CHAIN_EXPRESSION, env);
```

---

## 2. Dependência do exp-eval-mk2 sobre o legacy

O módulo `exp-eval-mk2` declara o `expression-evaluator` como dependência de **escopo test**:

```xml
<dependency>
    <groupId>io.github.runestone-forge</groupId>
    <artifactId>expression-evaluator</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

Isso significa que **todos os benchmarks comparativos devem residir no módulo `exp-eval-mk2`**, no diretório de testes. Não é possível criar benchmarks cross-module no `expression-evaluator` sem inverter a dependência.

---

## 3. Localização dos Arquivos de Benchmark

| Arquivo | Caminho |
|---|---|
| Benchmark principal | `exp-eval-mk2/src/test/java/com/runestone/expeval2/perf/jmh/CrossModuleComparisonBenchmark.java` |
| Runner rápido | `exp-eval-mk2/src/test/java/com/runestone/expeval2/perf/CrossModuleComparisonBenchmarkMain.java` |
| Runner agregado com quadros | `exp-eval-mk2/src/test/java/com/runestone/expeval2/perf/jmh/CrossModuleBenchmarkRunner.java` |
| Suporte compartilhado | `exp-eval-mk2/src/test/java/com/runestone/expeval2/perf/CrossModuleExpressionBenchmarkSupport.java` |
| Fixture de função customizada | `exp-eval-mk2/src/test/java/com/runestone/expeval2/perf/CrossModuleExpressionBenchmarkSupport.CustomFunctionFixture` (nested) |

O arquivo `CrossModuleExpressionBenchmarkSupport.java` é o **ponto central de extensão**: novas expressões, novos construtores de componente e novos pools de frames são adicionados ali. O benchmark principal apenas consome esse suporte.

---

## 4. Cenários Implementados

Todos os cenários usam as 12 variáveis `{a, b, c, d, e, f, g, h, i, j, k, l}`.

### 4.0. Intenção de medição

O `CrossModuleComparisonBenchmark` mede **custo fim a fim da API pública**, não apenas o hot path interno do evaluator.

Por isso, o custo por chamada inclui:
- **legacy**: `setVariable(...)` repetido conforme necessário + `evaluate()`
- **mk2**: materialização do `Map<String, Object>` de bindings + `compute(Map)` ou `computeWithAudit(Map)`

Esse desenho é intencional: o benchmark representa o custo percebido pelo chamador real das duas APIs. Se o objetivo futuro for medir apenas o motor interno, deve ser criado outro benchmark com os bindings pré-materializados fora do corpo do `@Benchmark`.

### 4.1. literalDense

**Expressão:** `seed + 1001.0001 + 1002.0002 + … + 1064.0064` (64 literais + 1 variável)

**Origem:** `ExpressionEvaluatorV2ExecutionPlanBenchmark` — versão sem variável; aqui adicionamos `seed` para forçar ao menos uma leitura de variável por chamada e evitar que o mk2 colapso a expressão inteira para constante em compile-time.

**O que estressou:** pipeline de adição em cadeia, constant folding (mk2 colapsa os 64 literais no plano compilado), binding de variável simples.

**Pool de dados:** `BigDecimal[] LITERAL_SEEDS[256]` com valores `index+1.{index*37 % 1000}`.

**Construção:**
```java
// legacy
new Expression(LITERAL_DENSE_EXPRESSION); // LITERAL_DENSE_EXPRESSION é constante pública

// mk2
MathExpression.compile(LITERAL_DENSE_EXPRESSION); // sem ambiente especial
```

**Aplicação de frame na API pública:**
```java
expression.setVariable("seed", CrossModuleExpressionBenchmarkSupport.literalSeed(index++));
// mk2:
expression.compute(Map.of("seed", CrossModuleExpressionBenchmarkSupport.literalSeed(index++)));
```

---

### 4.2. variableChurn

**Expressão:** `a * b + c * d - e + f * g - h + i * j + k - l + (a * c) + (b * d)`

**Origem:** `CrossModuleExpressionBenchmarkSupport.VARIABLE_CHURN_EXPRESSION` — cenário já existente no suporte compartilhado.

**O que estressou:** binding de 12 variáveis por chamada, lookup de variável no escopo, aritmética mista (mul/add/sub) sem funções externas.

**Pool de dados:** `Frame[] VARIABLE_FRAMES[256]` com seed `(3L, 11L)`.

---

### 4.3. userFunction

**Expressão:** `weighted(a, b, c) + weighted(d, e, f) + weighted(g, h, i) + weighted(j, k, l)`

**Função:** `weighted(left, middle, right) = left * 0.5 + middle * 1.5 - right * 0.25`

**Origem:** `CrossModuleExpressionBenchmarkSupport.USER_FUNCTION_EXPRESSION` — cenário já existente.

**O que estressou:** despacho de função customizada (4 chamadas por invocação), coerção de argumentos, overhead de `FunctionCallEvent` no modo audit (aloca `List.of(3 args)` × 4 = 4 listas por chamada).

**Pool de dados:** `Frame[] USER_FUNCTION_FRAMES[256]` com seed `(17L, 23L)` — seeds diferentes de `VARIABLE_FRAMES` para evitar que o JIT detecte padrão comum.

**Registro no legacy:**
```java
expr.addFunction("weighted",
    MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
    args -> CustomFunctionFixture.weighted((BigDecimal) args[0], (BigDecimal) args[1], (BigDecimal) args[2])
);
```

**Registro no mk2:** `registerStaticProvider(CustomFunctionFixture.class)` — os métodos públicos estáticos são descobertos por reflection.

---

### 4.4. conditional

**Expressão:** `if a > b then a * c - d + e - f else g * h - i + j * k - l endif`

**Origem:** `CrossModuleExpressionBenchmarkSupport.CONDITIONAL_EXPRESSION` — cenário já existente.

**O que estressou:** ramificação dependente de comparação entre variáveis, custo de `ConditionalEvent` no modo audit.

**Pool de dados:** `VARIABLE_FRAMES` (mesmos frames do variableChurn).

---

### 4.5. powerChain

**Expressão:** `a^2 + b^2 - c^2 + d^2 - e^2 + f^2 + g^2 - h^2 + i^2 - j^2 + k^2 - l^2`

**Origem:** `CrossModuleExpressionBenchmarkSupport.POWER_CHAIN_EXPRESSION` — cenário já existente.

**O que estressou:** operador de exponenciação, 12 operações de potência por chamada, padrão de add/sub intercalado.

**Pool de dados:** `VARIABLE_FRAMES`.

---

## 5. Grupos de Comparação e Modos de Execução

O benchmark `CrossModuleComparisonBenchmark` produz três variantes por cenário:

| Sufixo do método | Componente | Modo | Incluso nos grupos |
|---|---|---|---|
| `_legacy` | expression-evaluator | `evaluate()` | Grupo 1, Grupo 2 (baseline) |
| `_mk2Compute` | exp-eval-mk2 | `compute(Map)` | Grupo 1 |
| `_mk2Audit` | exp-eval-mk2 | `computeWithAudit(Map)` | Grupo 2 |

**Grupo 1** compara o custo fim a fim da API pública: legacy `evaluate()` vs mk2 `compute(Map)`.
**Grupo 2** compara o custo fim a fim da API pública com rastreabilidade: legacy `evaluate()` vs mk2 `computeWithAudit(Map)`.

Os quadros de resultados devem ser produzidos separadamente por grupo.

---

## 6. Configuração de Execução

### 6.1. Configuração canônica (resultados de registro)

```
@Warmup(iterations = 5, time = 500, timeUnit = MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
```

 Com 15 benchmarks (5 cenários × 3 variantes), em `forks=3` com 15 iterações de 500ms cada: a execução continua sendo de **ordem de dezenas de minutos**. Requer um JVM standalone (não Maven in-process).

### 6.2. Configuração do runner rápido (`CrossModuleComparisonBenchmarkMain`)

```
forks=0          (in-process, sem isolamento de JVM)
warmupIterations=3, warmupTime=500ms
measurementIterations=5, measurementTime=500ms
GCProfiler ativo
```

Com 15 benchmarks em `forks=0`: **~3–5 minutos de execução**.

**Limitação do `forks=0`**: a JVM é a mesma do processo Maven. O estado de JIT e GC é contaminado pelo processo pai. Os resultados são direcionalmente corretos para comparações relativas, mas não devem ser usados como números absolutos de produção.

### 6.3. Comando de execução

```shell
# Compilar tudo (com dependências)
mvn test-compile -pl exp-eval-mk2 -am -q

# Rodar o runner rápido
mvn exec:java \
  -pl exp-eval-mk2 \
  -Dexec.mainClass=com.runestone.expeval2.perf.CrossModuleComparisonBenchmarkMain \
  -Dexec.classpathScope=test \
  -q
```

Os resultados são impressos no stdout e gravados em `/tmp/cross-module-comparison.txt`.

---

## 7. Resultados de Referência (Baseline — 2026-03-22)

Executados com `forks=0`, JDK 21.0.10, `-Xms1g -Xmx1g` (herdado do processo Maven).

### Grupo 1 — `legacy.evaluate()` vs `mk2.compute(Map)`

| Cenário | legacy ns/op | mk2.compute(Map) ns/op | Δ tempo | legacy B/op | mk2.compute(Map) B/op | Δ alocação |
|---|---:|---:|:---:|---:|---:|:---:|
| literalDense | 5 416 | 2 092 | **−61%** ✅ | 9 792 | 3 632 | **−63%** ✅ |
| variableChurn | 2 113 | 1 536 | **−27%** ✅ | 2 609 | 1 064 | **−59%** ✅ |
| powerChain | 3 320 | 2 948 | **−11%** ✅ | 4 023 | 2 280 | **−43%** ✅ |
| userFunction | 1 644 | 1 805 | +10% ⚠️ | 1 496 | 1 384 | −7% ≈ |
| conditional | 1 059 | 1 233 | +16% ⚠️ | 696 | 520 | **−25%** ✅ |

### Grupo 2 — `legacy.evaluate()` vs `mk2.computeWithAudit(Map)`

| Cenário | legacy ns/op | mk2.computeWithAudit(Map) ns/op | Δ tempo | legacy B/op | mk2.computeWithAudit(Map) B/op | Δ alocação |
|---|---:|---:|:---:|---:|---:|:---:|
| literalDense | 5 416 | 2 259 | **−58%** ✅ | 9 792 | 3 832 | **−61%** ✅ |
| variableChurn | 2 113 | 1 886 | **−11%** ✅ | 2 609 | 1 680 | **−36%** ✅ |
| powerChain | 3 320 | 3 266 | −2% ≈ | 4 023 | 2 784 | **−31%** ✅ |
| userFunction | 1 644 | 2 188 | +33% ❌ | 1 496 | 2 032 | +36% ❌ |
| conditional | 1 059 | 1 784 | +68% ❌ | 696 | 936 | +34% ❌ |

### Overhead incremental de `computeWithAudit(Map)` sobre `compute(Map)`

| Cenário | compute(Map) ns/op | audit(Map) ns/op | Δ tempo | compute(Map) B/op | audit(Map) B/op | Δ alocação |
|---|---:|---:|:---:|---:|---:|:---:|
| literalDense | 2 092 | 2 259 | +8% | 3 632 | 3 832 | +6% |
| variableChurn | 1 536 | 1 886 | +23% | 1 064 | 1 680 | +58% |
| powerChain | 2 948 | 3 266 | +11% | 2 280 | 2 784 | +22% |
| userFunction | 1 805 | 2 188 | +21% | 1 384 | 2 032 | +47% |
| conditional | 1 233 | 1 784 | +45% | 520 | 936 | +80% |

---

## 8. Interpretação das Causas Raiz

**Leitura metodológica importante:**
Os números acima combinam custo do evaluator com custo de preparação exigido pela API pública. No legacy, isso inclui as chamadas a `setVariable(...)`; no mk2, isso inclui a materialização do `Map<String, Object>` passado a `compute(Map)` ou `computeWithAudit(Map)`.

**Por que o mk2 vence em literalDense e variableChurn:**
Mesmo com o custo do `Map` incluído, o pipeline compilado do mk2 ainda reduz o trabalho de execução em cenários onde o plano já embute simplificações relevantes. No caso de literalDense, os 64 literais são colapsados em um único `BigDecimal` constante durante a fase de constant folding — a expressão efetivamente executada é apenas `seed + <constante>`. Em variableChurn, o custo do mapa ainda não é suficiente para neutralizar totalmente a vantagem do plano compilado em execuções estáveis.

**Por que o legacy vence em conditional e userFunction:**
Em cenários sub-microsegundo, o custo fixo do plan walker do mk2 e do preparo do `Map` pode exceder o custo do cálculo em si para expressões simples. O legacy, por usar mutação incremental do objeto `Expression` antes do `evaluate()`, tende a pagar menos overhead estrutural por chamada em condicionais curtas e em funções customizadas pequenas.

**Por que `logarithmChain` foi removido do benchmark principal:**
O custo de `BigMath` para transcendentais (`ln`, `lb`) com `DECIMAL128` no mk2 é dominado pela diferença de precisão em relação ao legacy. Isso torna o cenário ruim para leitura arquitetural do benchmark principal, que hoje quer responder sobre custo fim a fim da API pública. Se esse cenário voltar, ele deve entrar como benchmark separado e explicitamente contextualizado por `MathContext`.

**Por que o audit piora mais em variableChurn e conditional:**
- `variableChurn`: 12 variáveis geram 12 `VariableRead` events. Mais importante: a presença de qualquer assignment na expressão força a cópia de um `HashMap` para criar o escopo mutável, mas no variableChurn sem assignment o custo vem apenas dos 12 eventos.
- `conditional`: além dos `VariableRead` events, o `ConditionalEvent` registra o ramo tomado e o valor das condições, acrescentando alocações extras e acesso à lista de eventos mesmo quando a expressão é trivial.

---

## 9. Como Adicionar um Novo Cenário

1. Definir a expressão como constante pública em `CrossModuleExpressionBenchmarkSupport`:
   ```java
   public static final String MEU_CENARIO_EXPRESSION = "…";
   ```

2. Adicionar os construtores de componente:
   ```java
   public static Expression newLegacyMeuCenario() { … }
   public static MathExpression newMk2MeuCenario() { … }
   ```

3. Se o cenário usa um pool de frames específico, adicionar e expor:
   ```java
   private static final Frame[] MEU_CENARIO_FRAMES = buildFrames(seedBase, seedMult);
   public static Frame meuCenarioFrame(int index) {
       return MEU_CENARIO_FRAMES[index & (FRAME_COUNT - 1)];
   }
   ```

4. Adicionar a classe de estado em `CrossModuleComparisonBenchmark`:
   ```java
   @State(Scope.Thread)
   public static class MeuCenarioState {
       Expression legacy;
       MathExpression mk2;
       int legacyIndex;
       int mk2Index;

       @Setup(Level.Trial)
       public void setUp() {
           legacy = CrossModuleExpressionBenchmarkSupport.newLegacyMeuCenario();
           mk2    = CrossModuleExpressionBenchmarkSupport.newMk2MeuCenario();
       }
   }
   ```

5. Adicionar os três métodos de benchmark (`_legacy`, `_mk2Compute`, `_mk2Audit`), preservando a intenção de medir a API pública fim a fim:
   - legacy: aplicar bindings via `setVariable(...)`
   - mk2: materializar o `Map<String, Object>` e chamar `compute(Map)` / `computeWithAudit(Map)`

6. Atualizar a seção **7 — Resultados de Referência** neste documento com os novos números.

---

## 10. Pontos de Atenção para Execuções Futuras

- **Versão do JDK**: os resultados de referência foram produzidos com JDK 21.0.10 (Oracle). JDKs diferentes (OpenJDK, GraalVM) produzem perfis de JIT distintos — especialmente para `BigDecimal` e `MethodHandle`.
- **`forks=0` contamina**: ao rodar via `mvn exec:java`, o JIT do Maven já aqueceu classes comuns (Caffeine, reflection, etc.). Isso favorece artificialmente o mk2, que usa Caffeine para seu cache de compilação. Para benchmarks de registro, use `forks >= 3`.
- **Expressão `LITERAL_DENSE_EXPRESSION` é gerada em tempo de carga de classe**: o conteúdo exato pode ser verificado em `CrossModuleExpressionBenchmarkSupport.LITERAL_DENSE_EXPRESSION`.
- **O mk2 cacheia o plano compilado**: chamar `MathExpression.compile()` com a mesma expressão e mesmo `environmentId` retorna o plano do cache Caffeine. No `@Setup(Level.Trial)`, se um benchmark anterior já compilou a mesma expressão, o custo de compilação é zero. Isso não afeta a medição de `compute()` (que não compila), mas é relevante se os benchmarks de compilação estiverem no mesmo run.
- **O benchmark principal não mede evaluator puro**: o custo do `Map<String, Object>` no mk2 é parte deliberada da medição. Não interprete esses números como custo isolado do runtime interno.
- **`CustomFunctionFixture`** vive como classe estática aninhada em `CrossModuleExpressionBenchmarkSupport`. Se precisar de uma função diferente para um novo cenário, crie uma classe separada em vez de modificar a existente.
