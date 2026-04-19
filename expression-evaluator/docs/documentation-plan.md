# Plano de Documentação — expression-evaluator

> **Tipo:** Documento de planejamento — não é a documentação final.
> **Data:** 2026-04-19
> **Versão do módulo analisada:** branch `refac-springboot-4`

---

## 1. Propósito do Módulo

O `expression-evaluator` é um motor de avaliação de expressões compiladas para Java 21. Recebe uma expressão textual, a compila para um plano de execução otimizado e avalia o plano com bindings fornecidos em tempo de execução. Suporta expressões matemáticas, lógicas e de atribuição; navegação em objetos e coleções; funções matemáticas, de string, de data/hora e financeiras (Excel); auditoria de execução; e validação estática sem avaliação.

A compilação usa ANTLR4 e produz um `ExecutionPlan` baseado em `MethodHandle`, com cache Caffeine por `(source, environmentId, resultType)`. Expressões compiladas são imutáveis e reutilizáveis em múltiplas threads.

---

## 2. Público-Alvo

| Perfil | Objetivo principal |
|---|---|
| **Desenvolvedor integrador** | Incorporar o motor em uma aplicação; configurar ambiente, registrar funções e símbolos, avaliar expressões |
| **Desenvolvedor de regras de negócio** | Escrever expressões corretas; entender a sintaxe, os operadores e o comportamento de borda |
| **Mantenedor do motor** | Entender o pipeline interno para evoluir, corrigir ou otimizar o motor |

A documentação de uso deve focar principalmente nos dois primeiros perfis. A documentação interna (já iniciada em `runtime-internals.md`) cobre o terceiro perfil.

---

## 3. Estrutura Recomendada da Documentação

A documentação final deve ser dividida nos seguintes documentos, em ordem de leitura sugerida:

```
docs/
├── getting-started.md          # Quickstart: compilar e avaliar em 5 minutos
├── environment-configuration.md # ExpressionEnvironmentBuilder completo
├── expression-syntax.md         # Referência da linguagem (gramática → usuário)
├── built-in-functions.md        # Catálogo de funções disponíveis
├── object-navigation.md         # Navegação em objetos e coleções
├── validation-and-audit.md      # API de validação e trilha de auditoria
├── error-reference.md           # Códigos de erro e tratamento de falhas
├── advanced-topics.md           # Cache, folding, type hints, MathContext
├── known-limitations.md         # Limitações e comportamentos de borda
└── faq.md                       # Perguntas frequentes
```

---

## 4. Detalhamento das Seções

### 4.1 Visão Geral (`getting-started.md`)

**O que documentar:**
- O que o módulo faz e o que não faz (delimitação de escopo)
- Dependência Maven/Gradle para adicionar ao projeto
- Exemplo end-to-end mínimo: construir ambiente → compilar → avaliar
- Tabela dos três tipos de expressão (`MathExpression`, `LogicalExpression`, `AssignmentExpression`) com um exemplo de cada e o tipo de retorno

**Exemplo mínimo a incluir:**
```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .addAllFunctions()
    .build();

MathExpression expr = MathExpression.compile("a + b * 2", env);
BigDecimal result = expr.compute(Map.of("a", 10, "b", 5)); // → 20
```

**Fontes de extração:**
- Javadoc de `MathExpression`, `LogicalExpression`, `AssignmentExpression`
- Testes em `src/test/java/com/runestone/expeval/api/`

**Lacunas identificadas:**
- [x] `groupId = io.github.runestone-forge`, `artifactId = expression-evaluator`, versão atual `1.1.0.1-SNAPSHOT`. Dependências transitivas obrigatórias: `caffeine 3.2.0`, `antlr4-runtime 4.13.1`, `big-math 2.3.2`.
- [x] Não há versão publicada no Maven Central ou repositório interno.

---

### 4.2 Pré-requisitos e Compatibilidade

**O que documentar:**
- Java 21+ obrigatório (uso de `sealed interfaces`, records, `MethodHandle`)
- Dependências transitivas que o usuário precisa conhecer: Caffeine, ANTLR4 runtime, big-math
- Não requer Spring (módulo independente)

**Fontes de extração:**
- `pom.xml` do módulo

---

### 4.3 Configuração do Ambiente (`environment-configuration.md`)

**O que documentar:**

**3a. Construindo um `ExpressionEnvironment`**
- Ciclo de vida: imutável após `build()`, compartilhável entre threads
- `ExpressionEnvironmentId`: gerado automaticamente via SHA-256 do config; muda quando o ambiente muda, invalidando o cache

**3b. Registrando funções**

| Método | Quando usar |
|---|---|
| `addAllFunctions()` | Registra todos os provedores embutidos |
| `addMathFunctions()` | Funções matemáticas e logarítmicas |
| `addStringFunctions()` | Manipulação de strings |
| `addDateTimeFunctions()` | Parsing e formatação de datas |
| `addTrigonometryFunctions()` | Trigonometria + constantes π, e, τ |
| `addComparableFunctions()` | max, min, abs, sign |
| `addExcelFunctions()` | NPV, IRR, PMT, PV, FV |
| `registerStaticProvider(Class, foldable)` | Métodos estáticos customizados |
| `registerInstanceProvider(Object, foldable)` | Métodos de instância customizados |

- Explicar o flag `foldable`: funções marcadas como dobráveis têm seus resultados pré-calculados quando os argumentos são constantes
- Regras de descoberta: métodos públicos, parâmetros reconhecidos pelo tipo, sobrecargas por aridade

**3c. Registrando símbolos externos**
- `registerExternalSymbol(name, defaultValue, overridable)`
- `overridable=false`: símbolo constante, elegível para constant folding
- `overridable=true`: pode ser sobrescrito por binding no `compute()`
- Tipo inferido a partir do `defaultValue`

**3d. Registrando type hints**
- `registerTypeHint(Class<?>)`: habilita navegação tipada em objetos
- Descobre: componentes de record, getters JavaBean, campos públicos, métodos de instância
- Sem type hint: navegação funciona via reflexão em tempo de execução (sem validação semântica em tempo de compilação)

**3e. Configurando precisão matemática**
- `withMathContext(MathContext)`: padrão `DECIMAL128` (34 dígitos significativos)
- `withTranscendentalMathContext(MathContext)`: separado para funções trig e logarítmicas
- Documentar quando faz diferença praticar na escolha do `MathContext`

**3f. Configurando o cache**
- `CacheConfig.defaults()` lê propriedades de sistema:
  - `expeval.cache.maximumSize` (padrão: 1024)
  - `expeval.cache.ttlSeconds` (padrão: sem expiração)
- Cache é por instância de `ExpressionCompiler`; múltiplos ambientes compartilham o mesmo compiler singleton

**Fontes de extração:**
- `ExpressionEnvironmentBuilder.java` (354 linhas — fonte primária)
- `CacheConfig.java`
- `ExpressionEnvironmentId.java`
- Testes: `CacheConfigTest`, `ExpressionCompilerInjectionTest`

**Lacunas identificadas:**
- [x] Comportamento do `ExpressionEnvironmentId` quando dois builders têm configurações idênticas → **mesmo ID** (SHA-256 determinístico sobre partes ordenadas da config). Provedores de instância usam `identityHashCode` → instâncias distintas sempre geram IDs diferentes.
- [x] `ExpressionCompiler` é **singleton JVM-wide** (via `ExpressionRuntimeSupport`) por padrão. Pode-se injetar instância própria via métodos `compile(source, env, compiler)` para controle de ciclo de vida.
- [x] Regras de descoberta de métodos em provedores → ver seção 6 (Alta Prioridade).

---

### 4.4 Referência da Sintaxe (`expression-syntax.md`)

Esta seção é a mais crítica para o público de regras de negócio.

**Organização interna sugerida:**

**a) Tipos de literais**

| Literal | Exemplos |
|---|---|
| Número | `42`, `3.14`, `0xFF`, `007` |
| String | `"hello"` |
| Booleano | `true`, `false` |
| Data | `2024-01-31` |
| Hora | `14:30`, `14:30:00` |
| Data+Hora | `2024-01-31T14:30`, `2024-01-31 14:30:00+03:00` |
| Data atual | `currDate`, `currTime`, `currDateTime` |
| Vetor | `[1, 2, 3]`, `["a", "b"]` |
| Nulo | `null` |

**b) Operadores aritméticos**
- `+`, `-`, `*`, `/`, `mod`, `%` (percentual), `^` (potência), `root`/`√`, `|expr|` (módulo)
- Precedência: postfix → potência → raiz → unário → multiplicativo → aditivo

**c) Operadores lógicos e de comparação**
- `=`, `!=`, `<>`, `>`, `>=`, `<`, `<=`
- `and`, `or`, `not`, `xor`, `xnor`, `nand`, `nor`
- `between`/`not between`
- `in`/`not in`
- `=~` (regex match), `!~` (regex não-match)

**d) Concatenação e coalescência**
- `||` — concatenação de strings
- `??` — null coalescing: `expr ?? default`
- `?.` — safe navigation: `obj?.prop`

**e) Expressões condicionais**
- Forma funcional: `if(cond; thenVal; elseVal)` ou `if(cond, thenVal, elseVal)`
- Forma de bloco: `if cond then val elsif cond then val else val endif`

**f) Type casting**
- `<number>(expr)`, `<text>(expr)`, `<bool>(expr)`, `<date>(expr)`, `<time>(expr)`, `<datetime>(expr)`, `<vector>(expr)`
- Com referências: `<bool>flag`, `<vector>items`

**g) Atribuições**
- Simples: `x = 10;`
- Destrutturação: `[a, b, c] = [1, 2, 3];`
- Bloco de atribuições retorna `Map<String, Object>`

**h) Chamada de funções**
- `func()`, `func(arg1, arg2)`, `func(arg1, arg2, arg3)`

**Fontes de extração:**
- `ExpressionEvaluator.g4` (fonte primária — ~440 linhas)
- Testes de gramática: `ExpressionEvaluatorGrammarTest`, `ExpressionEvaluatorCorpusCoverageTest`
- Testes de feature: `BetweenExpressionTest`, `MembershipExpressionTest`, `StringRegexTest`, etc.

**Lacunas identificadas:**
- [x] **Fallback do `??` aceita expressão completa (numérico e lógico):** `a ?? b + 1` = `a ?? (b+1)` — toda a expressão aritmética é consumida como fallback. Idem para lógico: `flag ?? (x > 0)`. Date/time/datetime/vector ainda usam a forma restrita (`*Entity`). Não é necessário envolver em `if()` para usar aritmética ou lógica como fallback.
- [x] `%` e módulo são **distintos**: `%` é operador postfix de **percentagem** (`50% = 0.5`); o módulo usa a keyword `mod` (infix: `10 mod 3 = 1`).
- [x] Formatos de data/hora: `DATE = YYYY-MM-DD`; `TIME = HH:MM` ou `HH:MM:SS`; `DATETIME = YYYY-MM-DDTHH:MM` ou `YYYY-MM-DD HH:MM:SS±HH:MM`. Offset de fuso horário é opcional em DATETIME.
- [x] Vetores vazios `[]` são **proibidos** pela gramática (`vectorOfEntitiesOperation` exige ao menos um elemento: `LBRACKET allEntityTypes (COMMA allEntityTypes)* RBRACKET`).

---

### 4.5 Catálogo de Funções Embutidas (`built-in-functions.md`)

**Estrutura sugerida:** uma tabela por provedor com colunas nome, assinatura, descrição, exemplo.

**Provedores a documentar:**
- `ComparableFunctions` — max, min, abs, sign, etc.
- `MathFunctions` — mean, sum, sqrt, pow, ceil, floor, round, etc.
- `LogarithmFunctions` — ln, log10, log (base n)
- `StringFunctions` — uppercase, lowercase, trim, substring, replace, split, length, etc.
- `TrigonometryFunctions` — sin, cos, tan, asin, acos, atan, atan2, etc.
- `DateTimeFunctions` — parse, format, year, month, day, hour, minute, second, etc.
- `ExcelFinancialFunctions` — NPV, IRR, PMT, PV, FV, RATE, etc.

**Constantes registradas automaticamente:** `pi`, `e`, `tau` (via `addTrigonometryFunctions()`)

**Fontes de extração:**
- Código fonte das classes `*Functions` em `catalog/functions/`
- Testes: `MathFunctionsExpressionTest`, `StringFunctionsExpressionTest`, `DateTimeFunctionsExpressionTest`, etc.

**Lacunas identificadas:**
- [x] `ComparableFunctions`: apenas `max(T[])` e `min(T[])`. **Não expõe `abs` nem `sign`** — o plano original estava incorreto. Valor absoluto usa o operador `|expr|`.
- [x] `MathFunctions`: `mean`, `geometricMean`, `harmonicMean`, `variance(p, type)`, `stdDev(p, type)`, `meanDev`, `rule3d`, `rule3i`, `distribute`, `spread`. Todas aceitam `MathContext` como primeiro parâmetro (auto-injetado).
- [x] `LogarithmFunctions`: `ln`, `lb` (log base 2), `log(base, value)`, `lnFast` (double), `lbFast` (double), `logFast(base, value)` (double).
- [x] `TrigonometryFunctions`: `sin`, `cos`, `tan`, `asin`, `acos`, `atan`, `atan2`, `sinh`, `cosh`, `tanh`, `asinh`, `acosh`, `atanh`. Registra automaticamente constantes `pi`/`π`, `e`, `tau`/`τ`.
- [x] `StringFunctions`: `concat`, `toUpper`, `toLower`, `trim`, `trimLeft`, `trimRight`, `substring(v, begin)`, `substring(v, begin, end)`, `substringBefore`, `substringAfter`, `substringBeforeLast`, `substringAfterLast`, `padLeft(v, size)`, `padLeft(v, size, padding)`, `padRight(v, size)`, `padRight(v, size, padding)`, `repeat`, `replace`, `replaceFirst`, `replaceAll`, `indexOf`, `lastIndexOf`, `startsWith`, `endsWith`, `contains`, `isEmpty`, `isBlank`, `length`, `split`, `join`.
- [x] **`DateTimeFunctions`** (todos `public static`; nenhum recebe `MathContext`):
  - `secondsBetween(Temporal, Temporal)` → `Long`
  - `minutesBetween(Temporal, Temporal)` → `Long`
  - `hoursBetween(Temporal, Temporal)` → `Long`
  - `daysBetween(Temporal, Temporal)` → `Long`
  - `monthsBetween(Temporal, Temporal)` → `Long`
  - `yearsBetween(Temporal, Temporal)` → `Long`
  - `setDay(Temporal, long)` → `Temporal`
  - `setMonth(Temporal, long)` → `Temporal`
  - `setYear(Temporal, long)` → `Temporal`
  - `setHours(Temporal, long)` → `Temporal`
  - `setMinutes(Temporal, long)` → `Temporal`
  - `setSeconds(Temporal, long)` → `Temporal`
  - `setMidnight(Temporal)` → `Temporal`
  - `setMidday(Temporal)` → `Temporal`
  - `addDay(Temporal, long)` → `Temporal`
  - `addMonth(Temporal, long)` → `Temporal`
  - `addYear(Temporal, long)` → `Temporal`
  - `addHours(Temporal, long)` → `Temporal`
  - `addMinutes(Temporal, long)` → `Temporal`
  - `addSeconds(Temporal, long)` → `Temporal`
  - `subDay(Temporal, long)` → `Temporal`
  - `subMonth(Temporal, long)` → `Temporal`
  - `subYear(Temporal, long)` → `Temporal`
  - `subHours(Temporal, long)` → `Temporal`
  - `subMinutes(Temporal, long)` → `Temporal`
  - `subSeconds(Temporal, long)` → `Temporal`
  - `Temporal` aceita `LocalDate`, `LocalTime` ou `LocalDateTime`; retorno mantém o mesmo tipo concreto.
  - **Não há** funções de parsing, formatação, `year()`, `month()`, `day()`, `hour()`, `minute()`, `second()` — o plano original estava incorreto.

- [x] **`ExcelFinancialFunctions`** (todos `public static`; todos recebem `MathContext` como primeiro parâmetro — auto-injetado, não conta na aridade exposta). **Não há** IRR nem RATE — o plano original estava incorreto. Funções expostas (aridade sem `MathContext`):
  - `fv(r, n, y, p, t: boolean)` — FV com `n` e sinalizador `t` como `BigDecimal`
  - `fv(r, nper: int, pmt, pv, type: int)` — FV com `nper`/`type` como `int`
  - `fv(r, nper: int, c, pv)` — FV com `type=0` implícito
  - `pv(r, n, y, f, t: boolean)` — PV com `BigDecimal`
  - `npv(r, cfs: BigDecimal[])` — NPV com vetor de fluxos de caixa
  - `pmt(r, n, p, f, t: boolean)` — PMT com `BigDecimal`
  - `pmt(r, nper: int, pv, fv, type: int)` — PMT com `int`
  - `pmt(r, nper: int, pv, fv)` — PMT com `fv`, `type=0`
  - `pmt(r, nper: int, pv)` — PMT com `fv=0`, `type=0`
  - `nper(r, y, p, f, t: boolean)` — número de períodos
  - `ipmt(r, per: int, nper: int, pv, fv, type: int)` — juros do período
  - `ipmt(r, per: int, nper: int, pv, fv)` — `type=0`
  - `ipmt(r, per: int, nper: int, pv)` — `fv=0`, `type=0`
  - `ppmt(r, per: int, nper: int, pv, fv, type: int)` — amortização do período
  - `ppmt(r, per: int, nper: int, pv, fv)` — `type=0`
  - `ppmt(r, per: int, nper: int, pv)` — `fv=0`, `type=0`
  - Parâmetros `boolean` e `int` são suportados pelo motor (ver §6 Alta Prioridade sobre tipos primitivos).

---

### 4.6 Navegação em Objetos e Coleções (`object-navigation.md`)

Esta seção merece documento próprio dada a riqueza de operadores.

**Estrutura sugerida:**

**a) Navegação em objetos**

| Sintaxe | Comportamento |
|---|---|
| `obj.prop` | Acesso a propriedade (getter, record component, field) |
| `obj?.prop` | Acesso null-safe; retorna `null` se `obj` for nulo |
| `obj.method()` | Chamada de método de instância |
| `obj?.method()` | Chamada null-safe |
| `obj.prop ?? "default"` | Coalescência com fallback |

**b) Navegação em coleções (JsonPath-like)**

| Sintaxe | Comportamento                                    | Exemplo |
|---|--------------------------------------------------|---|
| `list[0]` | Elemento por índice (zero-based)                 | `prices[0]` → 5 |
| `list[-1]` | Último elemento                                  | `prices[-1]` → último |
| `list[0:2]` | Slice exclusivo                                  | `prices[0:2]` → [5, 15] |
| `list[*]` | Cópia de todos os elementos                      | `prices[*]` → cópia |
| `list[?(@ > 10)]` | Filtro por predicado                             | `prices[?(@ > 10)]` → [15, 25] |
| `list..ds(prop)` | Deep-scan: coleta propriedade de todos elementos | `books..ds(price)` → [10, 20] |
| `list..sum()` | Método built-in de agregação                     | `books..sum()` → 30 |
| `list..ds()` | Wildcard deep-scan                               | |

**c) Placeholder `@`**
- Representa o elemento corrente em predicados de filtro
- Suporta comparações, regex, membership: `[?(@ =~ '[0-9]+')]`

**d) Type hints e validação semântica**
- Sem type hint: navegação funciona mas sem validação em compilação; erros de tipo ocorrem em runtime
- Com type hint registrado: propriedades e métodos são validados em compilação; mensagens de erro mais claras

**e) Referências circulares**
- O motor detecta referências circulares durante navegação e lança `ExpressionEvaluationException`

**Fontes de extração:**
- Testes: `CollectionNavigationTest`, `ObjectNavigationTest`, `ObjectNavigationCircularReferenceTest`
- `runtime-internals.md` seção "Type Hints & Object Navigation"

**Lacunas identificadas:**
- [x] Slice `[start:end]` é **exclusivo no `end`** (Python-style). `[0:2]` → índices 0, 1.
- [x] Deep-scan quando nó intermediário é nulo → retorna coleção vazia (sem exceção). Confirmado em `CollectionNavigationTest.DeepScanEdgeCases.shouldReturnEmptyWhenPropertyNotFound`.
- [x] Funções de agregação: `sum()`, `avg()`, `min()`, `max()`, `count()`, **`prod()`** (produto acumulado). Projeções de mapa: `keys()`, `values()`. Confirmado em `VectorAggregationKind`: SUM, AVG, MIN, MAX, COUNT, PROD.
- [x] `[*]` em listas retorna todos os elementos (identidade para listas). Em mapas retorna todos os valores. Confirmado em `CollectionNavigationTest.WildcardAccess`.

---

### 4.7 Validação e Trilha de Auditoria (`validation-and-audit.md`)

**a) API de validação**
- `expr.validate(source)`: não avalia a expressão; retorna `ValidationResult`
- `ValidationResult` contém: `valid`, `issues`, `userVariables`, `assignedVariables`, `functions`
- Usar para feedback ao usuário antes de persistir ou executar uma expressão
- `formatMessage()`: formata os erros com ponteiro para a posição no source

**b) Trilha de auditoria**
- `computeWithAudit(bindings)` → `AuditResult<T>`
- `AuditResult.trace()` → `ExpressionAuditTrace`
- Eventos registrados (em ordem de execução):
  - `VariableRead`: nome, valor, se é variável do sistema
  - `FunctionCall`: nome, argumentos de entrada, resultado
  - `AssignmentEvent`: nome da variável, valor atribuído
- `variableSnapshot()`: mapa final de todas as variáveis lidas
- `evaluationTime()`: duração da avaliação

**Exemplo a incluir:**
```java
AuditResult<BigDecimal> result = expr.computeWithAudit(Map.of("a", 10, "b", 5));
result.value();                          // → BigDecimal
result.trace().variableSnapshot();       // → {a=10, b=5}
result.trace().functionCalls();          // → lista de chamadas
result.trace().evaluationTime();         // → Duration
```

**Fontes de extração:**
- Testes: `AuditTrailExpressionTest`
- Javadoc de `AuditResult`, `ExpressionAuditTrace`, `AuditEvent`

**Lacunas identificadas:**
- [x] **Overhead do modo auditoria:** benchmark JMH em `AuditOverheadBenchmark` executado. Overhead medido de **+22% a +36.6%** em relação a `compute()` sem auditoria.
- [x] **Comportamento de constant folding na trilha de auditoria:**
  - Símbolos externos com `overridable=false` são dobrados em `ExecutableLiteral` → **emitem 0 eventos `VariableRead`** na trilha. Confirmado em `AuditTrailExpressionTest.foldedExternalEmitsVariableReadEvent`.
  - Funções dobradas em compilação (quando todos os argumentos são constantes) permanecem como `ExecutableFunctionCall.folded()` e **ainda emitem `FunctionCall`** na trilha de auditoria mesmo que já foram calculadas em tempo de compilação. Confirmado em `AuditTrailExpressionTest.foldedFunctionCallStillEmitsFunctionCallEvent`.

---

### 4.8 Referência de Erros (`error-reference.md`)

**Estrutura:** tabela com código, fase, descrição, exemplo de expressão que o gera, e como corrigir.

**Fase de compilação (sintaxe):**
| Código | Descrição |
|---|---|
| `SYNTAX_ERROR` | Token inesperado ou estrutura inválida |
| `UNEXPECTED_TOKEN` | Token fora do contexto esperado |

**Fase de compilação (semântica):**
| Código | Descrição |
|---|---|
| `UNKNOWN_FUNCTION` | Função não registrada no catálogo |
| `INVALID_FUNCTION_ARITY` | Número de argumentos incorreto |
| `UNKNOWN_SYMBOL` | Variável não registrada |
| `INCOMPATIBLE_FUNCTION_ARGUMENTS` | Tipos de argumentos incompatíveis com a assinatura |
| `AMBIGUOUS_FUNCTION` | Múltiplas sobrecargas igualmente elegíveis |

**Fase de avaliação (runtime):**
| Código | Descrição |
|---|---|
| `NULL_IN_CHAIN` | Propriedade acessada em objeto nulo sem `?.` |
| `INDEX_OUT_OF_BOUNDS` | Índice fora dos limites da coleção |
| `TYPE_MISMATCH` | Valor de tipo incompatível com a operação |
| `ZERO_DIVISION` | Divisão por zero |

**Formato padrão de mensagem de erro:**
```
  my_expression[invalid_func()]
  ^^^^^^^^^^^^^^
  SYNTAX_ERROR at 1:5 — expected valid identifier
```

**Fontes de extração:**
- Testes: `ExpressionErrorMessageTest`
- Classes de exceção em `com.runestone.expeval.api`

**Lacunas identificadas:**
- [x] Lista exaustiva de `IssueCode` já consolidada na §6 Alta Prioridade. Erros de runtime são strings inline em `ExpressionEvaluationException` — não há enum para erros de runtime.
- [x] **`FunctionInvocationException`** expõe apenas `functionName()` (String) e `getCause()` (Throwable). Não há código de erro nem `IssueCode` associado — é uma exceção de runtime pura que encapsula qualquer exceção lançada pelo método customizado. Verificado em `FunctionInvocationException.java`.

---

### 4.9 Tópicos Avançados (`advanced-topics.md`)

**a) Constant Folding**
- Expressões constantes são pré-calculadas em tempo de compilação
- Funções marcadas como `foldable` com argumentos constantes são pré-avaliadas
- Símbolos externos com `overridable=false` são tratados como constantes
- Resultado: avaliações subsequentes têm overhead zero para subexpressões constantes

**b) Injeção de compilador customizado**
- `MathExpression.compile(source, env, compiler)` aceita `ExpressionCompiler` explícito
- Permite controlar ciclo de vida do cache por caso de uso

**c) Data Conversion Service**
- `conversionService(DataConversionService)`: customiza coerção de tipos
- Útil para integrar tipos de domínio não suportados nativamente

**d) Expressões com `AssignmentExpression`**
- Sequências de atribuições e uso de variáveis definidas na mesma sequência
- Destrutturação: `[a, b] = [1, 2]; a + b`
- Retorno é `Map<String, Object>` com todas as variáveis atribuídas

**e) Uso em ambientes multi-thread**
- `ExpressionEnvironment`, `MathExpression`, `LogicalExpression`, `AssignmentExpression` são thread-safe
- `ExpressionEnvironmentBuilder` não é thread-safe (uso restrito ao setup)
- `ExecutionScope` é criado por chamada de `compute()` — não compartilhado entre threads

**Fontes de extração:**
- Testes: `ConstantFoldingExpressionTest`, `ExpressionCompilerInjectionTest`, `AssignmentExpressionTest`
- `runtime-internals.md` seções de cache, folding, execution scope

**Lacunas identificadas:**
- [x] **Thread safety de `ExecutionPlan`:** o `ExecutionPlan` é um record com `defaults: Object[]` que funciona como template **read-only** após a compilação. O array nunca é mutado — cada chamada a `compute()` cria um `ExecutionScope` próprio (via `ExecutionScope.from()` ou `ExecutionScope.readOnly()`) que copia ou encapsula o array de defaults. Não há estado mutável compartilhado entre threads. Confirmado pela análise de `ExpressionRuntimeSupport.createExecutionScope()`.
- [x] Comportamento de `currDate`/`currTime`/`currDateTime` em multi-thread já documentado na §6 Média Prioridade: cada `compute()` cria um `ExecutionScope` novo, portanto cada chamada re-avalia os instantes dinâmicos.

---

### 4.10 Limitações Conhecidas (`known-limitations.md`)

**O que documentar:**

| Limitação | Detalhes |
|---|---|
| Vetores vazios | `[]` é sintaxe inválida — vetores precisam de ao menos um elemento |
| Parâmetros array em provedores | Provedores customizados devem usar `BigDecimal[]` (array explícito); variadics Java (`BigDecimal...`) **não funcionam** — o MethodHandle de variadics tem semântica diferente de invocação |
| Recursão | Funções customizadas não podem ser recursivas via expressão |
| Tipos numéricos | O motor usa `BigDecimal` internamente; floats/doubles são convertidos com potencial perda de informação de formato |
| `..filter()` deep-scan | `..filter()` **não existe** como agregação deep-scan. Filtragem de coleções usa o subscript `[?(predicate)]`. Confirmado: `VectorAggregationKind` tem apenas SUM, AVG, MIN, MAX, COUNT, PROD. |
| Lambda em `..avg/min/max/count` | `..sum(@ -> expr)` e `..prod(@ -> expr)` aceitam lambda de transformação. `..avg()`, `..min()`, `..max()`, `..count()` **não aceitam** lambda — compile-time error. |
| Cache global | O `ExpressionCompiler` singleton mantém cache global; TTL e tamanho são configuráveis via system properties mas afetam todas as instâncias no mesmo JVM |

**Fontes de extração:**
- `vector-higher-order-functions.md` (features planejadas)
- Análise da gramática (`[]` vazio)
- Testes de benchmark e thread-local

**Lacunas identificadas:**
- [x] Não há testes com `@Disabled` nos sources de teste. Não há `TODO` nem `FIXME` no código de produção ou de teste do módulo `expression-evaluator`. Verificado via `grep -rn "@Disabled\|TODO\|FIXME"`. Nenhuma limitação oculta conhecida pendente de documentação.
- [x] **`<date>currDate` é inválido** — `currDate`/`currTime`/`currDateTime` são literais de data/hora diretamente, não referências. O prefixo de tipo `<date>` só é válido antes de `referenceTarget` (variáveis e funções).

---

### 4.11 Boas Práticas de Uso

**O que documentar:**

1. **Reutilização de expressões compiladas:** compilar uma vez, avaliar muitas vezes
2. **Compartilhamento de `ExpressionEnvironment`:** criar uma instância por configuração de aplicação, não por request
3. **Registrar type hints para objetos de domínio:** previne erros de runtime e habilita validação estática
4. **Usar `validate()` antes de persistir expressões:** detectar erros cedo
5. **Marcar funções customizadas puras como `foldable`:** permite otimização em tempo de compilação
6. **Separar `mathContext` e `transcendentalMathContext`:** ajustar precisão de forma granular
7. **Evitar registrar o mesmo provedor múltiplas vezes:** provedores duplicados criam sobrecargas ambíguas
8. **Preferir `overridable=false` para constantes:** elimina overhead de lookup em runtime

---

### 4.12 Troubleshooting

**O que documentar:**

| Sintoma | Causa provável | Resolução |
|---|---|---|
| `SemanticResolutionException: UNKNOWN_FUNCTION` | Provedor não registrado ou método não público | Registrar o provedor; verificar visibilidade do método |
| `ExpressionEvaluationException: NULL_IN_CHAIN` | Objeto nulo em cadeia de navegação | Usar `?.` para navegação null-safe |
| Resultado diferente do esperado em aritmética | Precisão de `MathContext` insuficiente | Ajustar `withMathContext()` |
| Cache não invalidado após mudança de ambiente | `ExpressionEnvironmentId` não mudou | Verificar se a configuração do ambiente realmente mudou |
| `FunctionInvocationException` | Exceção lançada pela função customizada | Verificar lógica interna da função; adicionar tratamento de nulo |
| Expressão compila mas falha em runtime | Type hint ausente; tipo de objeto não registrado | Registrar `registerTypeHint(MyClass.class)` |

---

### 4.13 FAQ

**Perguntas candidatas (a validar):**
- Como registrar minha própria função?
- Posso usar o motor sem Spring?
- Como controlar o tamanho do cache de expressões?
- É thread-safe usar a mesma `MathExpression` em múltiplas threads?
- Posso usar expressões com tipos de domínio customizados?
- Qual a diferença entre `MathExpression` e `AssignmentExpression`?
- Como depurar uma expressão que produz resultado errado?
- O motor suporta expressões aninhadas (expressão dentro de argumento de função)?

---

## 5. Informações a Extrair de Arquivos Existentes

| Arquivo/Pacote | Informação a extrair |
|---|---|
| `ExpressionEnvironmentBuilder.java` | Todos os métodos do builder e seus parâmetros |
| `ExpressionEvaluator.g4` | Sintaxe formal → tabela de operadores e precedência |
| `catalog/functions/*.java` | Assinaturas exatas e comportamento de cada função built-in |
| `api/*.java` (exceções) | Códigos de erro e campos de cada exceção |
| `src/test/java/**/*Test.java` | Exemplos de uso reais, casos de borda, comportamentos esperados |
| `runtime-internals.md` | Detalhes do pipeline (adaptar para seção de tópicos avançados) |
| `vector-higher-order-functions.md` | Limitações: features planejadas mas não implementadas |
| `pom.xml` | Versão, dependências, artifact coordinates |

---

## 6. Lacunas Consolidadas e Pontos de Validação

> **Atualizado em 2026-04-19** — Lacunas preenchidas via análise de código + testes em `DocumentationGapVerificationTest`.

### Alta Prioridade

- [x] **Artifact coordinates:** `groupId = io.github.runestone-forge`, `artifactId = expression-evaluator`, versão atual `1.1.0.1-SNAPSHOT` (ver `pom.xml` do módulo e parent POM).

- [x] **Códigos de erro:** existe enum `com.runestone.expeval.api.IssueCode` com a lista completa. Códigos de compilação (semânticos): `SYNTAX_ERROR`, `RESULT_TYPE_MISMATCH`, `TYPE_MISMATCH`, `INVALID_CURRENT_ELEMENT`, `INVALID_MEMBER_ACCESS`, `INVALID_MAP_PROPERTY_ACCESS`, `INVALID_METHOD_ARITY`, `INVALID_FUNCTION_ARITY`, `UNKNOWN_PROPERTY`, `UNKNOWN_METHOD`, `UNKNOWN_FUNCTION`, `UNKNOWN_COLLECTION_FUNCTION`, `AMBIGUOUS_METHOD`, `AMBIGUOUS_FUNCTION`, `INCOMPATIBLE_COMPARISON`, `INCOMPATIBLE_IN_OPERANDS`, `INCOMPATIBLE_METHOD_ARGUMENTS`, `INCOMPATIBLE_FUNCTION_ARGUMENTS`, `INCOMPATIBLE_COLLECTION_FUNCTION_ARGUMENTS`. Erros de runtime são strings inline em `ExpressionEvaluationException` (ex: `INDEX_OUT_OF_BOUNDS`, `NULL_IN_CHAIN`).

- [x] **Métodos descobertos em provedores customizados:** via `Class.getMethods()` (inclui métodos públicos herdados). Regras:
  - Provedores estáticos: apenas métodos `public static` (excluindo métodos de `Object`, `synthetic`, `bridge`).
  - Provedores de instância: apenas métodos públicos não-estáticos (mesmas exclusões).
  - Se o **primeiro parâmetro** for `MathContext`, ele é **injetado automaticamente** pelo ambiente (não faz parte da aridade exposta na expressão). Trig/log usam `transcendentalMathContext`; demais usam `mathContext`.
  - Parâmetros e retorno devem ser tipos reconhecidos por `ResolvedTypes.fromJavaType` (`BigDecimal`, `String`, `Boolean`, `LocalDate`, `LocalTime`, `LocalDateTime`, arrays e `List<?>` desses tipos, `Integer`, `Double`, `BigDecimal[]`, etc.).
  - Primitivos (`int`, `double`, `boolean`) **são suportados** como parâmetros e como tipo de retorno. `ResolvedTypes.fromJavaType` os reconhece explicitamente: `boolean` → `ScalarType.BOOLEAN`; demais primitivos numéricos → `ScalarType.NUMBER`. Os próprios providers embutidos usam primitivos (`lnFast(double)`, `substring(String, int)`, `variance(..., int)`, etc.).
  - Sobrecargas por aridade são registradas como entradas separadas.

- [x] **Cache global vs. por instância:** `ExpressionRuntimeSupport` mantém um singleton JVM-wide `ExpressionCompiler`. Dois `ExpressionEnvironment` com **configurações estáticas idênticas** (mesmos provedores, símbolos externos, `MathContext`) compartilham cache via o mesmo `ExpressionEnvironmentId` (SHA-256 sobre partes ordenadas). Provedores de instância usam `System.identityHashCode` — objetos distintos sempre produzem IDs diferentes mesmo que sejam da mesma classe. Para usar um compiler próprio, passe `ExpressionCompiler` explícito nos métodos `compile(source, env, compiler)`.

### Média Prioridade

- [x] **Overhead do modo auditoria:** benchmark JMH em `AuditOverheadBenchmark` executado. Overhead medido de **+22% a +36.6%** em relação a `compute()` sem auditoria. Detalhes:
  - **Variable Churn (12 variáveis, sem atribuição/funções)**: +36.6% overhead — cenário de leitura intensiva, máxima quantidade de eventos `VariableRead`
  - **Assigned Variable (1 atribuição + leituras)**: +30.4% overhead — força mutable-scope (HashMap copy) e gera `AssignmentEvent`
  - **User Function (4 chamadas)**: +22.0% overhead — menor impacto; eventos `FunctionCall` mais esparsos
  
  **Interpretação:** Overhead é consistente e mensurável mas previsível. Cresce com número de operações rastreáveis (variáveis > atribuições > funções). Para hot paths (>100k evals/s), considerar `compute()` sem auditoria; para debugging e validação, overhead aceitável.

- [x] **Comportamento de `currDate`/`currTime`/`currDateTime`:** avaliados **uma vez por chamada de `compute()`**, com cache dentro do `ExecutionScope` via `EnumMap<DynamicInstant, Object> dynamicCache`. Múltiplas referências ao mesmo literal dentro de uma expressão retornam o mesmo instante. Entre chamadas diferentes de `compute()`, cada chamada recria o `ExecutionScope` e portanto re-avalia o instante.

- [x] **Precisão de slice:** `list[start:end]` é **exclusivo no `end`** (Python-style). `prices[0:2]` retorna índices 0 e 1. Confirmado em `CollectionNavigationTest.SliceAccess`.

- [x] **Função de raiz `root`:** sintaxe infix: `a root b` (b-ésima raiz de a). `√` é sinônimo Unicode de `root`. Também existe `sqrt(x)` como forma funcional para raiz quadrada. **Não existe** forma `root(n, x)` como chamada de função. Exemplos: `8 root 3 = 2`; `sqrt(9) = 3`; `16 √ 2 = 4`.

- [x] **Funções de agregação em deep-scan:** `sum()`, `avg()`, `min()`, `max()`, `count()`, **`prod()`** (produto acumulado — `VectorAggregationKind.PROD`). Projeções de mapa: `keys()`, `values()`. `sum(@ -> expr)` e `prod(@ -> expr)` aceitam lambda de transformação (confirmar em `VectorHigherOrderFunctionsTest`). `avg/min/max/count` não aceitam lambda. Projeções confirmadas em `CollectionNavigationTest.VectorAggregations` e `CollectionNavigationTest.MapProjections`.

### Baixa Prioridade

- [x] **Parâmetros array em provedores customizados:** arrays explícitos (`BigDecimal[]`, `String[]`) funcionam corretamente — o motor converte a lista de argumentos `[...]` para o array correspondente. Variadics Java (`BigDecimal...`) **NÃO funcionam**: `getParameterTypes()` retorna `BigDecimal[]` para ambas as formas, mas o `MethodHandle` gerado por `unreflect()` para um método variadic é um "varargs-collector" com semântica de invocação diferente — resulta em `ClassCastException` em runtime. Conclusão: **sempre usar `BigDecimal[]` (nunca `BigDecimal...`) em métodos de provedores**. Confirmado em `DocumentationGapVerificationTest.ArrayParamProvider`.

- [x] **Comportamento de `||` com tipos não-string:** `||` **não aceita** números ou booleanos sem cast explícito. A gramática restringe `||` ao contexto de `stringConcatExpression`, que aceita apenas `stringEntity` (literais string, referências com `<text>`, decisões `if` com resultado string). Tentar `1 || "b"` lança `ParsingException`. Confirmado em `StringConcatenationTest.TypeError`.

- [x] **Expressões com comentários:** a gramática suporta **`//` (linha)** e **`/* */` (bloco)**, ambos tratados como `skip` pelo lexer. Comentários podem aparecer em qualquer posição — inclusive dentro de blocos de atribuição. Confirmado em `DocumentationGapVerificationTest.CommentSupport`.

- [x] **Limite de profundidade de navegação:** não há limite configurável ou hardcoded no código. O único limite prático é a detecção de referências circulares durante navegação (lança `ExpressionEvaluationException`).

### Achados adicionais (não estavam no plano original)

- [x] **`ComparableFunctions` não expõe `abs` nem `sign`:** a classe tem apenas `max(T[])` e `min(T[])`. O valor absoluto é expresso com o operador `|expr|` (MODULUS). Não há função `sign()` embutida.

- [x] **`%` é operador de PERCENTAGEM (postfix), não de módulo.** `50%` = `0.5`. O módulo é feito com a keyword `mod` (infix). Exemplo: `10 mod 3 = 1`.

- [x] **Fallback do `??` por tipo:** `numericReferenceOperation` usa `mathExpression` e `logicalReferenceOperation` usa `logicalExpression` como fallback — qualquer expressão composta é válida. `stringReferenceOperation` já usava `stringConcatExpression`. Date/time/datetime/vector ainda aceitam apenas `*Entity` (literal, referência, `if`). Portanto `a ?? b + 1` = `a ?? (b+1)` e `flag ?? (x > 0)` é sintaxe válida sem necessidade de `if()`.

- [x] **`NOT` lógico:** aceita tanto `~` (til) quanto `¬` (Unicode ¬ U+00AC) quanto `!` como operadores de negação lógica.

- [x] **Literais de tempo como slice:** `prices[10:20]` é ambíguo com o token TIME (`HH:MM`). O lexer trata como `sliceTimeSubscript` e o processa como slice `start=10, end=20`. Confirmado em `CollectionNavigationTest.SliceAccess.shouldHandleTimeLookingSliceAsStartEnd`.

- [x] **`ExpressionEnvironmentId` para configurações idênticas:** dois `ExpressionEnvironment` construídos com as mesmas chamadas de configuração produzem o mesmo ID (SHA-256 determinístico sobre partes ordenadas). Confirmado em `DocumentationGapVerificationTest.EnvironmentIdHashing`.

- [x] **`currDate` não aceita prefixo `<date>`:** `<date>currDate` é inválido na gramática — `currDate` já é literalmente um `dateEntity`, não uma `referenceTarget`. Usar `d = currDate;` diretamente em atribuições. Confirmado em `DocumentationGapVerificationTest.DynamicLiteralPerCall`.

---

## 7. Exemplos Práticos e Cenários Reais Sugeridos

A documentação final deve incluir exemplos progressivos, do simples ao complexo:

### Cenário 1: Motor de regras de elegibilidade
```
// Idade e status determinam elegibilidade
"age >= 18 and status = 'active' and score between 700 and 850"
```

### Cenário 2: Cálculo de preço com desconto
```
// Desconto progressivo baseado em quantidade
"if(qty >= 100; price * 0.85; if(qty >= 50; price * 0.90; price))"
```

### Cenário 3: Processamento de lista de preços
```
// Média de preços acima de um limiar
"mean(prices[?(@ > minPrice)])"
```

### Cenário 4: Navegação em objeto de domínio com fallback
```
// Nome do cliente com fallback para "Anônimo"
"customer?.profile?.displayName ?? 'Anônimo'"
```

### Cenário 5: Cálculo financeiro (Excel)
```
// Valor presente de investimento
"PV(rate / 12; nper * 12; pmt)"
```

### Cenário 6: Sequência de atribuições com reutilização
```
"base = price * qty;
 tax = base * taxRate;
 discount = if(qty > 10; base * 0.05; 0);
 total = base + tax - discount"
```

---

## 8. Ordem de Escrita Recomendada

1. `getting-started.md` — maior impacto para novos usuários
2. `expression-syntax.md` — consulta mais frequente; base para os outros docs
3. `built-in-functions.md` — completar após confirmar assinaturas
4. `environment-configuration.md` — após resolver lacunas do builder
5. `object-navigation.md` — após confirmar comportamentos de borda
6. `error-reference.md` — após confirmar lista de códigos
7. `validation-and-audit.md` — seção mais isolada, menor dependência
8. `advanced-topics.md` — para usuários já familiarizados
9. `known-limitations.md` — após varredura de TODOs no código
10. `faq.md` — após as outras seções estarem estáveis

---

## 9. Boas Práticas de Escrita a Seguir

- **Exemplos antes de explicações:** mostrar o código que funciona, depois explicar por que
- **Uma ideia por parágrafo:** evitar misturar conceitos distintos em blocos longos
- **Tabelas para referência:** operadores, funções, erros, configurações — sempre em tabela
- **Exemplos progressivos:** do caso mais simples ao mais complexo dentro de cada seção
- **Comportamento de borda explícito:** o que acontece com `null`? Com listas vazias? Com tipos errados?
- **Decisões não-óbvias destacadas em `> [!NOTE]` ou `> [!WARNING]`:** ex., vetores vazios são inválidos, `%` é percentual (não módulo)
- **Sem jargão interno:** evitar termos como `ExecutionPlan`, `SemanticModel`, `ExecutionScope` na documentação de uso; usar termos do domínio do usuário
- **Links cruzados:** entre seções relacionadas (ex., seção de type hints → seção de navegação em objetos)
