> **Documento histórico e não normativo.** Este arquivo descreve o módulo anterior e não define o contrato de `exp-mk3`; para decisões vigentes, consulte o plano de implementação e os ADRs do módulo.

O módulo `expression-evaluator` é uma biblioteca Java para compilar expressões em texto e avaliá-las depois com variáveis fornecidas em runtime.

A ideia central é:

```text
texto da expressão
  -> parser ANTLR
  -> AST interna
  -> resolução semântica
  -> plano de execução otimizado
  -> avaliação com valores externos
```

Ele não interpreta a string diretamente a cada execução. A expressão é compilada uma vez em um plano reutilizável, thread-safe, e depois chamada várias vezes com `compute(...)`.

**Tipos De Expressão**
A API pública expõe três tipos principais:

- `MathExpression`: retorna `BigDecimal`.
- `LogicalExpression`: retorna `boolean`.
- `AssignmentExpression`: executa uma sequência de atribuições e retorna `Map<String, Object>`.

Exemplo de uso típico:

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .addAllFunctions()
    .build();

MathExpression expr = MathExpression.compile("a + b * 2", env);

BigDecimal result = expr.compute(Map.of("a", 10, "b", 5));
```

**Pipeline De Funcionamento**
O fluxo interno principal está em `ExpressionCompiler`:

```text
ExpressionEvaluatorParserFacade
  -> SemanticAstBuilder
  -> SemanticResolver
  -> ExecutionPlanBuilder
  -> ExpressionRuntimeSupport
```

Cada etapa tem uma responsabilidade clara:

- `ExpressionEvaluatorParserFacade`: usa a gramática ANTLR para transformar texto em parse tree.
- `SemanticAstBuilder`: converte a parse tree em uma AST própria do projeto.
- `SemanticResolver`: resolve símbolos, tipos, funções, propriedades, métodos e erros semânticos.
- `ExecutionPlanBuilder`: transforma a AST semântica em nós executáveis, aplicando otimizações.
- `ExpressionRuntimeSupport`: ponte entre API pública e runtime de execução.

**Gramática**
A sintaxe é definida em:

`expression-evaluator/src/main/antlr4/com/runestone/expeval/internal/grammar/ExpressionEvaluator.g4`

Ela suporta:

- Operações matemáticas: `+`, `-`, `*`, `/`, `mod`, `^`, `sqrt`, `%`, `!`.
- Operações lógicas: `and`, `or`, `xor`, `nand`, `nor`, `xnor`, `!`, `~`.
- Comparações: `=`, `!=`, `<>`, `>`, `>=`, `<`, `<=`.
- `in`, `not in`, `between`, `not between`.
- Regex: `=~`, `!~`.
- Condicionais: `if ... then ... else ... endif` e `if(cond; a; b)`.
- Literais: número, string, data, hora, datetime, boolean, `null`.
- Vetores: `[1, 2, 3]`.
- Atribuições: `x = valor;`.
- Destructuring: `[a, b] = [1, 2];`.
- Navegação em objetos: `obj.prop`, `obj.method()`, `obj?.prop`.
- Navegação em coleções/mapas: índices, slices, wildcard, filtros, projeções e agregações.
- Coalescência nula: `valor ?? fallback`.
- Hints de tipo: `<number>`, `<text>`, `<date>`, `<vector>`, etc.

**AST**
A AST interna fica em `internal.ast`.

A estrutura base é:

- `Node`: interface raiz selada.
- `ExpressionFileNode`: raiz do arquivo/expressão compilada.
- `ExpressionNode`: interface para expressões.
- `AssignmentNode`: interface para atribuições.
- `NodeId`: identifica cada nó.
- `SourceSpan`: guarda posição no texto original para erro/auditoria.

O `ExpressionFileNode` contém:

```java
List<AssignmentNode> assignments;
ExpressionNode resultExpression;
```

Ou seja, uma expressão pode ter atribuições antes do resultado final:

```text
tax = price * 0.1;
total = price + tax;
total
```

Principais nós de expressão:

- `LiteralNode`
- `IdentifierNode`
- `BinaryOperationNode`
- `UnaryOperationNode`
- `TernaryOperationNode`
- `PostfixOperationNode`
- `FunctionCallNode`
- `ConditionalNode`
- `VectorLiteralNode`
- `PropertyChainNode`

**Modelo Semântico**
Depois da AST, o `SemanticResolver` produz um `SemanticModel`.

Ele guarda:

- AST original.
- Tipo resolvido de cada nó.
- Símbolos usados por cada identificador.
- Símbolos internos e externos.
- Bindings de funções resolvidas.
- Lista de problemas semânticos.

Estrutura importante:

```java
SemanticModel(
    ExpressionFileNode ast,
    Map<NodeId, ResolvedType> resolvedTypes,
    Map<NodeId, SymbolRef> symbolByNodeId,
    Map<String, SymbolRef> externalSymbolsByName,
    Map<String, SymbolRef> internalSymbolsByName,
    Map<NodeId, ResolvedFunctionBinding> functionBindings,
    List<SemanticIssue> issues
)
```

**Sistema De Tipos**
O módulo usa um sistema de tipos próprio, simples, chamado `ResolvedType`.

Tipos principais:

- `ScalarType`: `NUMBER`, `BOOLEAN`, `STRING`, `DATE`, `TIME`, `DATETIME`.
- `VectorType`: vetor sem tipo de elemento conhecido.
- `CollectionType`: coleção com tipo de elemento conhecido.
- `MapType`: mapa com tipo de chave e valor conhecidos.
- `ObjectType`: objeto Java registrado via type hint.
- `UnknownType`: tipo não conhecido em compilação.
- `NullType`: valor nulo.

Esse sistema é usado para:

- Validar expressões.
- Resolver overloads de funções.
- Resolver propriedades e métodos.
- Decidir coerções.
- Validar se uma `MathExpression` realmente retorna número.
- Validar se uma `LogicalExpression` realmente retorna booleano.

**Símbolos**
Variáveis são representadas por `SymbolRef`.

Um símbolo pode ser:

- `INTERNAL`: criado por atribuições dentro da expressão.
- `EXTERNAL`: fornecido pelo ambiente ou pelo `Map` passado em `compute`.

Exemplo:

```text
tax = price * 0.1;
total = price + tax
```

Nesse caso:

- `tax` e `total` são internos.
- `price` é externo.

Durante a montagem do plano, cada símbolo recebe um índice numérico estável. Isso permite que o runtime use arrays em vez de mapas para procurar valores.

**Ambiente**
O `ExpressionEnvironment` define o contrato externo da expressão.

Ele contém:

- `FunctionCatalog`: funções disponíveis.
- `ExternalSymbolCatalog`: símbolos externos com default.
- `TypeHintCatalog`: metadados de tipos Java navegáveis.
- `DataConversionService`: serviço de conversão.
- `MathContext`: precisão matemática comum.
- `transcendentalMathContext`: precisão para funções trigonométricas/logarítmicas.
- `ExpressionEnvironmentId`: hash determinístico usado no cache.

O builder permite registrar:

- Funções estáticas.
- Funções de instância.
- Funções built-in.
- Símbolos externos.
- Type hints para navegação em objetos.
- Contextos matemáticos.

Exemplo:

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .addMathFunctions()
    .addStringFunctions()
    .registerExternalSymbol("taxRate", new BigDecimal("0.1"), true)
    .registerTypeHint(Customer.class)
    .build();
```

**Catálogo De Funções**
Funções são descobertas por reflexão a partir de classes/provider objects.

Cada função vira um `FunctionDescriptor`, com:

- Nome.
- Tipos Java dos parâmetros.
- Tipos semânticos dos parâmetros.
- Tipo de retorno.
- `MethodHandle` para invocação.
- Flag `foldable`, indicando se pode ser pré-calculada quando os argumentos são constantes.

Funções built-in incluem:

- Matemática.
- Logaritmos.
- Trigonometria.
- Strings.
- Datas/horas.
- Comparáveis.
- Financeiras estilo Excel.

**Plano De Execução**
Depois da resolução semântica, o `ExecutionPlanBuilder` cria um `ExecutionPlan`.

Estrutura principal:

```java
ExecutionPlan(
    List<ExecutableAssignment> assignments,
    ExecutableNode resultExpression,
    Object[] defaults,
    Map<String, ExternalBindingPlan> externalBindings,
    int externalSymbolsCount,
    int maxAuditEvents,
    List<AuditEvent> foldedVariableReads
)
```

Esse plano é a forma compilada da expressão.

Ele contém:

- Atribuições já convertidas para nós executáveis.
- Expressão final executável.
- Valores default dos símbolos externos.
- Metadados de bindings externos.
- Informações para auditoria.
- Nós já otimizados.

**Nós Executáveis**
O runtime não executa diretamente a AST original. Ele executa uma segunda árvore de nós, baseada em `ExecutableNode`.

Principais nós:

- `ExecutableLiteral`
- `ExecutableIdentifier`
- `ExecutableBinaryOp`
- `ExecutableUnaryOp`
- `ExecutableTernaryOp`
- `ExecutablePostfixOp`
- `ExecutableFunctionCall`
- `ExecutableConditional`
- `ExecutableVectorLiteral`
- `ExecutablePropertyChain`
- `ExecutableNullCoalesce`
- `ExecutableRegexOp`

Essa camada é importante porque já carrega decisões feitas em compilação, como:

- Função exata resolvida.
- Símbolo já ligado ao índice correto.
- Regex já compilada.
- Constantes já dobradas.
- Propriedades/métodos já parcialmente resolvidos.
- Defaults externos já organizados em arrays.

**Otimizações**
O `ExecutionPlanBuilder` faz constant folding.

Exemplos:

```text
2 + 3
```

Pode virar diretamente:

```text
5
```

Também pode dobrar:

- Operações binárias constantes.
- Operações unárias constantes.
- Operações postfix constantes.
- `between` com operandos constantes.
- `??` quando o lado esquerdo é constante não nulo.
- Funções foldable com argumentos constantes.
- Vetores constantes.
- Condicionais com condição constante.
- Partes de navegação em propriedades quando a raiz é constante.
- Regex, compilando `Pattern` uma vez.

Isso reduz trabalho em runtime.

**ExecutionScope**
Cada chamada de `compute(...)` cria um `ExecutionScope`.

Ele é o estado isolado daquela execução.

Internamente usa arrays, não mapas:

- Camada de símbolos internos.
- Camada de overrides externos.
- Camada de defaults externos.

Também usa o sentinela `ExecutionScope.UNBOUND`, diferente de `null`.

Isso é importante porque:

- `UNBOUND` significa “não existe valor”.
- `null` significa “existe valor e ele é nulo”.

O scope também cacheia valores dinâmicos:

- `currDate`
- `currTime`
- `currDateTime`

Dentro da mesma execução, leituras repetidas retornam o mesmo valor.

**Runtime**
A avaliação fica principalmente em `AbstractObjectEvaluator`.

Fluxo simplificado:

```text
cria ExecutionScope
  -> executa assignments em ordem
  -> avalia expressão final
  -> converte resultado para tipo esperado
```

Para `AssignmentExpression`, ele executa as atribuições e monta um `Map` com os valores finais.

Os avaliadores concretos são pequenos:

- `MathEvaluator`: exige resultado `BigDecimal`.
- `LogicalEvaluator`: exige resultado `Boolean`.

A avaliação de nós é recursiva:

- Literal retorna valor.
- Identificador busca no `ExecutionScope`.
- Binário avalia esquerda/direita e chama `OperatorEvaluator`.
- Função chama `RuntimeInvocationSupport`.
- Propriedade chama `PropertyChainOps`.
- Condicional avalia somente o ramo necessário.
- Vetor avalia elementos.
- Regex usa `Pattern` pré-compilado.

**Coerção De Tipos**
O runtime usa `RuntimeServices` e `RuntimeCoercionService`.

Ele converte valores para:

- `BigDecimal`
- `Boolean`
- `String`
- `LocalDate`
- `LocalTime`
- `LocalDateTime`
- `List`
- Arrays quando necessário
- Tipos esperados por funções Java

As coerções acontecem principalmente:

- Ao aplicar valores externos passados em `compute`.
- Ao carregar defaults.
- Ao chamar funções.
- Ao retornar propriedades/métodos com tipo conhecido.
- Ao normalizar resultados semânticos.

**Navegação Em Objetos**
A navegação usa `PropertyChainNode` na AST e `ExecutablePropertyChain` no runtime.

Suporta:

- `obj.prop`
- `obj?.prop`
- `obj.method(arg)`
- `obj?.method(arg)`
- `list[0]`
- `list[1:3]`
- `map["key"]`
- `[*]`
- filtros `[?(...)]`
- operações de coleção como `..sum()`, `..map(...)`, `..keys()`, `..values()`

Com `registerTypeHint(...)`, o sistema descobre metadados Java:

- Record components.
- Getters.
- Campos públicos.
- Métodos públicos.

Sem type hint, a navegação ainda pode compilar como `UnknownType`, mas parte da resolução fica para runtime via reflexão.

**Cache**
O `ExpressionEngine` possui um cache de compilação usando Caffeine.

A chave é:

```text
(source, environmentId, resultType)
```

Então a mesma expressão, no mesmo ambiente e com o mesmo tipo de resultado, reaproveita o plano compilado.

Há:

- Engine default singleton.
- Engine isolado configurável.
- `CacheConfig` com tamanho máximo e TTL opcional.

**Auditoria**
Todas as APIs têm `computeWithAudit(...)`.

A auditoria registra:

- Leituras de variáveis.
- Leituras de valores dinâmicos.
- Chamadas de função.
- Atribuições.

Retorna:

```java
AuditResult<T>
```

com:

- Resultado.
- `ExpressionAuditTrace`.

Isso permite explicar como uma expressão chegou ao resultado.

**Pontos Centrais Para Uma Nova Versão**
Para construir uma nova versão no futuro, as estruturas-base mais importantes a preservar conceitualmente são:

- Separar claramente `parse`, `AST`, `semântica`, `plano executável` e `runtime`.
- Manter uma API pública simples: `compile(...)` e `compute(...)`.
- Compilar uma vez e executar muitas vezes.
- Ter um `Environment` explícito para funções, símbolos, tipos e precisão matemática.
- Ter um modelo semântico separado da AST, em vez de misturar resolução com parsing.
- Usar uma representação intermediária executável, parecida com `ExecutableNode`.
- Separar símbolos internos e externos.
- Usar índices/arrays para runtime se performance continuar sendo prioridade.
- Manter `UNBOUND` separado de `null`.
- Fazer coerção apenas nas bordas: entrada externa, funções, propriedades e resultado.
- Tratar auditoria como recurso opcional, não como parte obrigatória da execução normal.
- Manter cache por fonte + ambiente + tipo de resultado.

**Resumo Final**
O `expression-evaluator` é essencialmente um compilador pequeno de expressões:

```text
linguagem textual
  -> AST
  -> análise semântica
  -> plano otimizado
  -> execução com escopo isolado
```

A parte mais valiosa para uma futura reescrita não é uma classe específica, mas a arquitetura em camadas: parser separado, AST limpa, modelo semântico, ambiente explícito, plano executável imutável e runtime sem estado compartilhado por execução.
