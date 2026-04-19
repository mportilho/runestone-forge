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
- [ ] Confirmar artifact id e group id para o bloco de dependência Maven
- [ ] Verificar se há versão publicada no Maven Central ou repositório interno

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
- [ ] Documentar o comportamento exato do `ExpressionEnvironmentId` quando dois builders têm configurações idênticas (são mesmo id?)
- [ ] Confirmar se `ExpressionCompiler` é um singleton global ou pode ter múltiplas instâncias
- [ ] Especificar regras exatas de descoberta de métodos em provedores customizados (quais assinaturas são aceitas?)

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
- [ ] Esclarecer precedência exata de `??` em relação a outros operadores binários
- [ ] Documentar comportamento de `%` como percentual vs. operador modulo (são distintos?)
- [ ] Detalhar regras de parsing de data/hora: quais formatos exatos são aceitos pelo lexer?
- [ ] Confirmar se vetores vazios `[]` são mesmo proibidos e qual a mensagem de erro exibida

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
- [ ] Confirmar lista completa de métodos expostos por cada provedor (quais métodos públicos são ignorados pelo scanner de provedores?)
- [ ] Documentar quais funções têm sobrecargas (multiple arity) e quais parâmetros são opcionais

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
- [ ] Confirmar se slice `[start:end]` é exclusivo ou inclusivo no `end`
- [ ] Documentar comportamento de deep-scan quando um nó intermediário é nulo
- [ ] Detalhar funções de agregação disponíveis: `sum()`, `prod()`? Quais outros?
- [ ] Verificar comportamento de `[*]` em listas aninhadas

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
- [ ] Confirmar se `computeWithAudit` tem overhead significativo vs. `compute` (relevante para hot paths)
- [ ] Documentar se eventos de constant folding (funções dobradas em compilação) aparecem na trilha

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
- [ ] Confirmar lista exaustiva de códigos de erro para cada tipo de exceção — não há enum de códigos documentado
- [ ] Verificar se `FunctionInvocationException` expõe o código do erro interno ou apenas o nome da função

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
- [ ] Confirmar se há algum estado compartilhado mutável em `ExecutablePlan` que possa causar problemas em alta concorrência
- [ ] Documentar o comportamento de `currDate`/`currTime`/`currDateTime` em ambientes multi-thread (são avaliados por chamada?)

---

### 4.10 Limitações Conhecidas (`known-limitations.md`)

**O que documentar:**

| Limitação | Detalhes |
|---|---|
| Vetores vazios | `[]` é sintaxe inválida — vetores precisam de ao menos um elemento |
| Funções variadics nativas | Somente as built-in suportam variadics; provedores customizados precisam de overloads explícitos |
| Recursão | Funções customizadas não podem ser recursivas via expressão |
| Tipos numéricos | O motor usa `BigDecimal` internamente; floats/doubles são convertidos com potencial perda de informação de formato |
| Deep-scan de funções de alta ordem | `..filter()` e transformações em `..sum()` estão planejadas mas não implementadas (ver `vector-higher-order-functions.md`) |
| Cache global | O `ExpressionCompiler` singleton mantém cache global; TTL e tamanho são configuráveis via system properties mas afetam todas as instâncias no mesmo JVM |

**Fontes de extração:**
- `vector-higher-order-functions.md` (features planejadas)
- Análise da gramática (`[]` vazio)
- Testes de benchmark e thread-local

**Lacunas identificadas:**
- [ ] Identificar outras limitações não documentadas explorando os testes com `@Disabled` ou comentários `TODO`/`FIXME` no código

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

Antes da escrita final da documentação, os seguintes pontos precisam de validação:

### Alta Prioridade

- [ ] **Artifact coordinates:** confirmar `groupId`, `artifactId` e versão publicada para o bloco de dependência Maven
- [ ] **Códigos de erro:** não há enum centralizado de códigos — confirmar lista exaustiva com os mantenedores ou via análise de código
- [ ] **Métodos descobertos em provedores customizados:** quais assinaturas de método são aceitas pelo scanner? Há suporte a tipos primitivos? A parâmetros `null`?
- [ ] **Cache global vs. por instância:** confirmar se múltiplos `ExpressionEnvironment` com configurações diferentes compartilham o mesmo cache e como o `ExpressionEnvironmentId` isola entradas

### Média Prioridade

- [ ] **Overhead do modo auditoria:** documentar se `computeWithAudit` tem custo mensurável em relação a `compute`
- [ ] **Comportamento de `currDate`/`currTime`/`currDateTime`:** são avaliados uma vez por expressão ou uma vez por chamada de `compute()`?
- [ ] **Precisão de slice:** `list[0:2]` retorna `[0, 1]` ou `[0, 1, 2]`?
- [ ] **Função de raiz `root`:** qual sintaxe exata? `root(n, x)` ou `n root x`?
- [ ] **Funções de agregação em deep-scan:** quais funções além de `sum()` estão disponíveis?

### Baixa Prioridade

- [ ] **Suporte a variadics em provedores customizados:** confirmar se é possível e como declarar
- [ ] **Comportamento de `||` com tipos não-string:** concatenação faz conversão automática?
- [ ] **Expressões com comentários:** a gramática suporta comentários inline?
- [ ] **Limite de profundidade de navegação:** há limite máximo de encadeamento de propriedades?

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
