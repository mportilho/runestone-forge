# Plano: Redução da Superfície Pública - `exp-eval-mk2`

## Contexto

O achado "Exposição excessiva de tipos internos" do documento `architecture-and-code-quality-review.md`
aponta que tipos como `CompiledExpression`, `SemanticModel`, `MutableBindings`, `ExecutionScope`,
`FunctionCatalog`, `FunctionDescriptor` e outros são declarados `public` sem necessidade clara de
consumo externo, ampliando a superfície pública e dificultando a evolução interna.

Este plano organiza a correção em três fases com critérios claros de decisão.

---

## Critério de Classificação

Um tipo deve permanecer `public` apenas se **pelo menos uma** das condições for verdadeira:

1. Aparece no retorno ou parâmetro de um método público de um tipo do pacote `api`
2. É necessário para que o consumidor configure ou consulte o comportamento da engine
3. Faz parte de uma abstração com variabilidade real esperada por consumidores externos

Qualquer tipo que não satisfaça esses critérios deve ser `package-private`.

---

## Inventário e Classificação

### Superfície pública legítima (permanecem `public`)

| Tipo                           | Pacote    | Justificativa                                                 |
|--------------------------------|-----------|---------------------------------------------------------------|
| `MathExpression`               | `api`     | Fachada principal do usuário                                  |
| `LogicalExpression`            | `api`     | Fachada principal do usuário                                  |
| `ExpressionEnvironment`        | `api`     | Configuração passada pelo usuário                             |
| `ExpressionEnvironmentBuilder` | `api`     | Construção da configuração                                    |
| `ResolvedType`                 | `types`   | Parâmetro de `registerExternalSymbol()` no builder            |
| `ResolvedTypes`                | `types`   | Factory usada pelo usuário no builder                         |
| `FunctionCatalog`              | `catalog` | Retornado por `ExpressionEnvironment.functionCatalog()`       |
| `FunctionDescriptor`           | `catalog` | Retornado pelos métodos de `FunctionCatalog`                  |
| `ExternalSymbolCatalog`        | `catalog` | Retornado por `ExpressionEnvironment.externalSymbolCatalog()` |
| `ExternalSymbolDescriptor`     | `catalog` | Retornado pelos métodos de `ExternalSymbolCatalog`            |

### Tipos internos indevidamente públicos

#### Grupo A — Totalmente internos, zero consumo externo (package-private imediato)

Esses tipos não aparecem em nenhuma assinatura pública da fachada `api`. Podem ser
tornados `package-private` sem quebrar a API pública.

**Compilador (`compiler`)**

| Tipo                 | Motivo                                                                               |
|----------------------|--------------------------------------------------------------------------------------|
| `CompiledExpression` | Circula apenas entre `compiler` e `runtime`; usuário nunca a manuseia                |
| `ExpressionCacheKey` | Chave de cache interna do compilador                                                 |
| `ExpressionCompiler` | Orquestrador interno; usuário acessa apenas via `MathExpression`/`LogicalExpression` |

**AST (`ast` e `ast.mapping`)** — todos os 20 tipos

| Tipo                                                                                          |
|-----------------------------------------------------------------------------------------------|
| `Node`, `ExpressionNode`, `AssignmentNode` (sealed interfaces)                                |
| `ExpressionFileNode`, `LiteralNode`, `IdentifierNode`, `BinaryOperationNode`                  |
| `UnaryOperationNode`, `PostfixOperationNode`, `FunctionCallNode`                              |
| `ConditionalNode`, `SimpleAssignmentNode`, `DestructuringAssignmentNode`, `VectorLiteralNode` |
| `BinaryOperator`, `UnaryOperator`, `PostfixOperator`                                          |
| `NodeId`, `SourceSpan`                                                                        |
| `SemanticAstBuilder`                                                                          |

**Semântica (`semantic`)** — todos os 9 tipos

| Tipo                                                     |
|----------------------------------------------------------|
| `SemanticResolver`, `SemanticModel`, `ResolutionContext` |
| `SymbolRef`, `ResolvedFunctionBinding`                   |
| `SemanticIssue`, `SemanticIssueSeverity`, `SymbolKind`   |
| `SemanticResolutionException`                            |

**Sistema de tipos (`types`)** — implementações concretas

| Tipo          | Motivo                                                                            |
|---------------|-----------------------------------------------------------------------------------|
| `ScalarType`  | Implementação interna de `ResolvedType`; usuário usa `ResolvedTypes` como factory |
| `VectorType`  | Idem                                                                              |
| `UnknownType` | Idem                                                                              |

**Runtime — core (`runtime`)**

| Tipo                       | Motivo                                                                            |
|----------------------------|-----------------------------------------------------------------------------------|
| `AbstractRuntimeEvaluator` | Classe base interna dos evaluators                                                |
| `MathEvaluator`            | Evaluator interno; instanciado pelo `ExpressionRuntimeSupport`                    |
| `LogicalEvaluator`         | Idem                                                                              |
| `MutableBindings`          | Bindings mutáveis usados apenas na montagem interna                               |
| `ExecutionScope`           | Escopo de execução; circula apenas dentro do `runtime`                            |
| `ExpressionRuntimeSupport` | Orquestrador do runtime; invocado apenas por `MathExpression`/`LogicalExpression` |

**Runtime — valores (`runtime.values`)** — todos os 9 tipos

| Tipo                                         |
|----------------------------------------------|
| `RuntimeValue` (sealed interface)            |
| `NumberValue`, `BooleanValue`, `StringValue` |
| `DateValue`, `TimeValue`, `DateTimeValue`    |
| `VectorValue`, `NullValue`                   |

**Parser/gramática (`grammar.language` e `grammar.exception`)** — todos os 9 tipos

| Tipo                                                                 |
|----------------------------------------------------------------------|
| `ExpressionEvaluatorV2ParserFacade`, `ParseResult`                   |
| `ParseExecutor`, `AntlrParseExecutor`                                |
| `PredictionStrategy`, `WarmupInput`, `CollectingSyntaxErrorListener` |
| `ParsingException`, `SyntaxError`                                    |

**Engine context (`engine.context`)** — outros internos

| Tipo                      | Motivo                                                |
|---------------------------|-------------------------------------------------------|
| `ExpressionEnvironmentId` | ID interno do ambiente; nunca manipulado pelo usuário |
| `FunctionRef`             | Referência interna a funções no catálogo              |

---

#### Grupo B — Tipos com decisão de API pendente

Esses tipos estão expostos via `ExpressionEnvironment` mas têm assinaturas que referenciam
tipos puramente internos (`RuntimeValue`), tornando-os inutilizáveis externamente sem acesso
a esses tipos. Exigem uma decisão explícita antes de serem tratados.

**`RuntimeValueFactory` (runtime)**

Exposta por `ExpressionEnvironment.runtimeValueFactory()`. Seus métodos públicos:

```java
RuntimeValue from(Object rawValue)

RuntimeValue from(Object rawValue, ResolvedType expectedType)
```

`RuntimeValue` é um tipo interno. Externamente, o retorno é opaco e inutilizável.

**Decisão requerida:** Um consumidor externo realmente precisa converter valores Java para
`RuntimeValue`? Se não, remover o accessor de `ExpressionEnvironment` e tornar
`RuntimeValueFactory` package-private.
Resposta: um consumidor externo não precisa fazer esta conversão

**`RuntimeCoercionService` (runtime)**

Exposta por `ExpressionEnvironment.runtimeCoercionService()`. Seus métodos públicos:

```java
BigDecimal asNumber(RuntimeValue value)

boolean asBoolean(RuntimeValue value)
// ... etc
```

Parâmetros do tipo `RuntimeValue` (interno). O mesmo problema: externamente não há como
obter um `RuntimeValue` para passar.

**Decisão requerida:** Mesmo raciocínio de `RuntimeValueFactory`. Se não há uso legítimo
externo, remover o accessor e tornar package-private.
Resposta: um consumidor externo não precisa fazer esta conversão

**`CompilationEnvironment` e `RuntimeEnvironment` (engine.context)**

Interfaces implementadas por `ExpressionEnvironment`. Os pacotes internos (`compiler`,
`runtime`, `semantic`) tipam seus parâmetros contra essas interfaces em vez da classe
concreta. Usuários nunca digitam contra elas.

**Problema:** Em Java sem `module-info.java`, interfaces `package-private` em `engine.context`
não são visíveis para `compiler` ou `runtime`. Para que vários pacotes as consumam,
precisam ser `public`.

**Propósito arquitetural reconhecido:**

Embora possuam uma única implementação (`ExpressionEnvironment`), essas interfaces **não são
interfaces prematuras** — são "role interfaces" com papel de desacoplamento direcional. Elas
impedem que os pacotes do pipeline (`compiler`, `runtime`, `semantic`) importem diretamente o
tipo de fachada pública `ExpressionEnvironment`. `ExpressionCompiler` depende apenas do contrato
mínimo de compilação; `ExpressionRuntimeSupport` depende apenas do contrato de runtime. O
argumento contra "interface com única implementação" se aplica à variabilidade antecipada — não
a essa separação de responsabilidades.

**Problema real:** estão em `engine.context`, um pacote que precisa ser `public` para ser visível
entre `compiler`, `runtime` e `semantic`. O resultado é que aparecem como parte da API pública
sem sê-lo de fato.

**Decisão recomendada — manter a semântica, corrigir a visibilidade:**

A solução correta é `module-info.java` com `exports ... to`, que permite declarar as interfaces
como acessíveis apenas para os pacotes consumidores internos sem torná-las parte da API pública.

Enquanto o `module-info.java` não é adotado, a opção pragmática é:

- Renomear o pacote de `engine.context` para `engine.internal`, sinalizando claramente que não
  faz parte da API pública
- Tornar `ExpressionEnvironmentId` package-private dentro de `engine.internal`
- Documentar `CompilationEnvironment` e `RuntimeEnvironment` com `@apiNote` explicando o papel

Essas interfaces **não entram no escopo desta refatoração** como problema a eliminar —
são arquiteturalmente corretas e o seu custo de visibilidade é o limite do modelo de módulos
do Java sem JPMS.

---

## Fases de Execução

### Fase 1 — Grupo A: tipos sem consumo externo (baixo risco)

**Escopo:** ~55 tipos nos pacotes `ast`, `ast.mapping`, `semantic`, `compiler`, `runtime`,
`runtime.values`, `grammar`, `engine.context`, `catalog` (apenas `FunctionRef`),
`types` (implementações concretas).

**Ação:** remover o modificador `public` de cada tipo. Nenhuma mudança de comportamento;
apenas restrição de visibilidade.

**Validação:** `mvn -q -pl exp-eval-mk2 test` deve passar sem alteração.

**Nota:** `ExpressionCacheKey` e `ExpressionEnvironmentId` também são Grupo A mas
requerem que `CompiledExpression` e `ExpressionEnvironment` (seus produtores) sejam
ajustados se os construtores referenciam esses tipos publicamente.

---

### Fase 2 — Grupo B: decisão de API e remoção de accessors inúteis

**Etapa 2a — Decidir `RuntimeValueFactory` e `RuntimeCoercionService`**

1. Verificar se há algum teste de integração ou consumidor externo que chama esses
   accessors de `ExpressionEnvironment`
2. Se não houver: remover `runtimeValueFactory()` e `runtimeCoercionService()` de
   `ExpressionEnvironment` e tornar ambos package-private no pacote `runtime`
3. Se houver: tornar `RuntimeValue` público (abrindo o sealed type) e manter os accessors —
   mas documentar a decisão explicitamente

**Etapa 2b — Resolver `CompilationEnvironment` e `RuntimeEnvironment`**

Recomendação: Opção A (mover para dentro do pacote `api`).

1. Mover `CompilationEnvironment.java` e `RuntimeEnvironment.java` para o pacote `api`
2. Remover o modificador `public` de ambas as interfaces
3. Ajustar os imports nos pacotes `compiler`, `runtime` e `semantic` para referenciar
   os tipos agora em `api` — ou refatorar para que recebam `ExpressionEnvironment`
   diretamente (quebrando a necessidade das interfaces no pipeline interno)
4. Deletar o pacote `engine.context` se ficar vazio
5. Deletar o pacote `engine` se ficar vazio

---

### Fase 3 — Limpeza do construtor público de `FunctionCatalog` e `ExternalSymbolCatalog`

Os construtores públicos de `FunctionCatalog(Map<...> descriptorsByName)` e
`ExternalSymbolCatalog(Map<...> symbolsByName)` permitem instanciação externa sem necessidade.

**Ação:** Tornar os construtores package-private. Usuários obtêm instâncias exclusivamente
via `ExpressionEnvironment`, não por instanciação direta.

Validar: `ExpressionEnvironmentBuilder` (no pacote `api`, diferente de `catalog`) precisa
criar essas instâncias — ajustar visibilidade ou mover a lógica de criação para dentro do
próprio pacote `catalog` via um método factory estático package-private.

---

## Resumo das Mudanças por Pacote

| Pacote              | Ação principal                                                                                                                                                                                                          |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ast`               | Tornar todos os 19 tipos package-private                                                                                                                                                                                |
| `ast.mapping`       | Tornar `SemanticAstBuilder` package-private                                                                                                                                                                             |
| `semantic`          | Tornar todos os 9 tipos package-private                                                                                                                                                                                 |
| `compiler`          | Tornar `CompiledExpression`, `ExpressionCacheKey`, `ExpressionCompiler` package-private                                                                                                                                 |
| `runtime`           | Tornar `AbstractRuntimeEvaluator`, `MathEvaluator`, `LogicalEvaluator`, `MutableBindings`, `ExecutionScope`, `ExpressionRuntimeSupport` package-private; decidir sobre `RuntimeValueFactory` e `RuntimeCoercionService` |
| `runtime.values`    | Tornar todos os 9 tipos package-private                                                                                                                                                                                 |
| `grammar.language`  | Tornar todos os tipos package-private exceto `ExpressionResultType` (verificar se é usado externamente)                                                                                                                 |
| `grammar.exception` | Tornar `ParsingException` e `SyntaxError` package-private                                                                                                                                                               |
| `types`             | Tornar `ScalarType`, `VectorType`, `UnknownType` package-private (manter `ResolvedType` e `ResolvedTypes` públicos)                                                                                                     |
| `catalog`           | Tornar construtores de `FunctionCatalog` e `ExternalSymbolCatalog` package-private; tornar `FunctionRef` package-private                                                                                                |
| `engine.context`    | Renomear para `engine.internal`; tornar `ExpressionEnvironmentId` package-private; manter `CompilationEnvironment` e `RuntimeEnvironment` públicas (role interfaces com propósito arquitetural); adicionar `@apiNote` documentando o papel |

---

## Riscos e Pré-condições

- Fase 1 tem risco mínimo: mudança puramente de visibilidade, comportamento inalterado
- Fase 2a pode revelar testes que instanciam diretamente tipos internos — esses testes devem
  ser reescritos para testar via fachada pública
- Fase 2b exige cuidado com o ciclo `api -> compiler -> api` já mapeado: a movimentação das
  interfaces pode melhorar o ciclo se `compiler` e `runtime` passarem a depender de `api`
  apenas pelas interfaces (em vez de pela classe concreta `ExpressionEnvironment`)
- Verificar se `ExpressionResultType` (grammar) é referenciado externamente antes de
  torná-lo package-private

---

## Critério de Conclusão

O achado está resolvido quando:

1. `mvn -q -pl exp-eval-mk2 test` passa sem alterações
2. Nenhum tipo dos Grupos A e B permanece `public` sem justificativa documentada
3. Os únicos tipos `public` no módulo são os listados na tabela "Superfície pública legítima"
   mais os catálogos e seus descritores (com construtores package-private)
