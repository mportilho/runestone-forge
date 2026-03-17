# Plano: Reducao da Superficie Publica — `exp-eval-mk2`

## Status de Execucao

| Etapa | Status | Observacoes |
|---|---|---|
| 1 — Criar `ExpressionCompilationException` | **CONCLUIDA** | Criados `api/CompilationIssue.java` e `api/ExpressionCompilationException.java` |
| 2 — Consolidar pipeline em `ExpressionRuntimeSupport` | **CONCLUIDA** | Adicionado `compile()` static factory; `from()` rebaixado para package-private; `MathExpression` e `LogicalExpression` removem imports de `ExpressionCompiler` e `CompiledExpression` |
| 3 — `ExpressionCompiler` e `CompiledExpression` package-private | **PARCIAL** | `api` nao os referencia mais (objetivo principal atingido). Package-private completo e impossivel sem mesclar `internal.compiler` e `internal.runtime`: `CompiledExpression` e referenciado por `AbstractRuntimeEvaluator`, `MathEvaluator`, `LogicalEvaluator` e `ExpressionRuntimeSupport`, todos em pacote diferente. Dependencia `internal.runtime` → `api` introduzida intencionalmente para conversao de excecao no `compile()` factory. |
| 4 — `ExecutionScope` package-private | **CONCLUIDA** | Removido `public` de `ExecutionScope`; `createExecutionScope()` rebaixado para package-private |
| 5 — `internal.semantic.*` package-private | pendente | — |
| 6 — Mover `ast.*` para `internal.ast.*` | pendente | — |
| 7 — Mover `SemanticAstBuilder` para `internal.ast.mapping` | pendente | — |
| 8 — Mover `grammar/language` para `internal.grammar.language` | pendente | — |
| 9 — Migrar testes afetados | pendente | — |
| 10 — `ExpressionEnvironmentId` package-private | pendente | — |
| 11 — Verificacao final | pendente | — |

**Verificacao pos-etapas 1-4:** `mvn -q -pl exp-eval-mk2 test` — PASSOU (sem falhas).

## Contexto

O achado "Exposicao excessiva de tipos internos" do `architecture-and-code-quality-review.md` esta
parcialmente endereçado: os pacotes `compiler`, `runtime` e `semantic` foram fisicamente movidos para
`internal.*`, mas seus tipos ainda estao declarados `public`. Alem disso, o pacote `ast` (20 tipos)
ainda esta fora de `internal` e com visibilidade publica, embora seja exclusivamente um artefato do
pipeline interno.

**Restricao:** sem uso de JPMS. A estrategia de encapsulamento e: convencao de pacote (`internal`)
+ modificador `package-private` onde possivel.

---

## API Publica Alvo (contrato externo desejado)

Tipos que o consumidor do modulo tem direito de conhecer:

```
api/
    LogicalExpression           (public)
    MathExpression              (public)
    ExpressionCompilationException  (public, NOVA)

environment/
    ExpressionEnvironment       (public)
    ExpressionEnvironmentBuilder (public)
    ExpressionEnvironmentId     (package-private)  ← rebaixar

catalog/
    FunctionCatalog             (public)
    FunctionDescriptor          (public)
    FunctionRef                 (public)
    ExternalSymbolCatalog       (public)
    ExternalSymbolDescriptor    (public)

grammar/exception/
    ParsingException            (public)
    SyntaxError                 (public)

types/
    ResolvedType                (public)
    ScalarType                  (public)
    UnknownType                 (public)
    VectorType                  (public)
    ResolvedTypes               (package-private)  ← avaliar
```

`ExpressionResultType` (hoje em `grammar.language`) sera movido para `internal` apos a etapa 2,
pois os consumidores externos nunca o referenciam diretamente.

---

## Diagnostico: por que os tipos em `internal.*` ainda sao `public`

A raiz esta em `api.MathExpression` e `api.LogicalExpression`, que importam de `internal.*`:

```
api.MathExpression / api.LogicalExpression
  └─ internal.compiler.ExpressionCompiler    (public — api instancia diretamente)
  └─ internal.compiler.CompiledExpression    (public — api passa como argumento)
  └─ internal.runtime.ExpressionRuntimeSupport (public — api armazena como campo)
```

Enquanto `api` referenciar `ExpressionCompiler` e `CompiledExpression`, eles nao podem ser
`package-private`. A solucao e eliminar essas referencias da `api` consolidando o ponto de entrada
do pipeline em `ExpressionRuntimeSupport`.

---

## Etapas

### Etapa 1 — Criar `ExpressionCompilationException` em `api`

**O que fazer:**
Criar `api.ExpressionCompilationException` (public, RuntimeException) com:
- campo `source: String` — expressao que falhou
- campo `issues: List<String>` — mensagens de erro (sem tipos internos expostos)

**Por que antes de tudo:**
`SemanticResolutionException` (em `internal.semantic`) carrega `List<SemanticIssue>`. Enquanto for
`public` e referenciada em testes externos, `SemanticIssue` precisa ser `public`. Criando uma
excecao publica independente, toda a hierarquia de `SemanticResolutionException` + `SemanticIssue`
pode tornar-se `package-private`.

**Impacto em testes:**
`ExpressionCompilerTest` referencia `SemanticResolutionException` e `SemanticIssue` diretamente.
Esses testes serao migrados na Etapa 9.

---

### Etapa 2 — Consolidar acesso ao pipeline em `ExpressionRuntimeSupport`

**O que fazer:**
Adicionar factory method estatico em `ExpressionRuntimeSupport`:

```java
// interno, chamado apenas por MathExpression e LogicalExpression
public static ExpressionRuntimeSupport compile(
    String source,
    ExpressionResultType resultType,
    ExpressionEnvironment environment
) { ... }
```

Internamente esse metodo: cria `ExpressionCompiler`, executa `compile(...)`, captura
`SemanticResolutionException` e relanca como `ExpressionCompilationException`, retorna
`ExpressionRuntimeSupport`.

Atualizar `MathExpression` e `LogicalExpression`:
- substituir `COMPILER.compile(...)` + `ExpressionRuntimeSupport.from(...)` pela chamada ao novo metodo
- remover o campo estatico `COMPILER`
- remover imports de `ExpressionCompiler` e `CompiledExpression`

**Resultado:** `api` passa a importar apenas `ExpressionRuntimeSupport` de `internal.*`.

---

### Etapa 3 — Tornar `ExpressionCompiler` e `CompiledExpression` package-private

**Pre-condicao:** Etapa 2 concluida (nenhum tipo fora de `internal.compiler` os referencia).

**O que fazer:**
- Remover `public` de `ExpressionCompiler` e `CompiledExpression`
- Verificar que `ExpressionCacheKey` ja e `package-private` (confirmado)

**Verificacao:** compilar o modulo; nenhum erro esperado fora de `internal.compiler`.

---

### Etapa 4 — Tornar `ExecutionScope` package-private

**O que fazer:**
- Remover `public` de `ExecutionScope`
- Confirmar que `ExecutionScope` so e usado dentro de `internal.runtime`
  (por `ExpressionRuntimeSupport`, `AbstractRuntimeEvaluator`, `MathEvaluator`, `LogicalEvaluator`)

`ExecutionScope.find()` e `assign()` usam `SymbolRef` e `RuntimeValue`, que ja sao
`package-private`, reforçando que a classe e interna.

---

### Etapa 5 — Tornar todos os tipos em `internal.semantic` package-private

**9 classes:** `SemanticResolver`, `SemanticModel`, `SemanticIssue`, `SemanticIssueSeverity`,
`SymbolRef`, `SymbolKind`, `ResolvedFunctionBinding`, `ResolutionContext`,
`SemanticResolutionException`.

**Pre-condicao:** `ExpressionCompilationException` criada (Etapa 1) e os testes afetados ainda nao
migrados (Etapa 9 cuida disso).

**Atencao:** o compilador Java vai apontar erros nos testes `ExpressionCompilerTest` e
`SemanticResolverTest`. Isso e esperado — sera resolvido na Etapa 9. Rodar `mvn test` so depois
da Etapa 9.

---

### Etapa 6 — Mover `ast.*` para `internal.ast.*`

**20 tipos a mover:**
`Node`, `ExpressionNode`, `AssignmentNode`, `ExpressionFileNode`, `BinaryOperationNode`,
`BinaryOperator`, `UnaryOperationNode`, `UnaryOperator`, `PostfixOperationNode`,
`PostfixOperator`, `ConditionalNode`, `FunctionCallNode`, `IdentifierNode`, `LiteralNode`,
`VectorLiteralNode`, `NodeId`, `SourceSpan`, `SimpleAssignmentNode`,
`DestructuringAssignmentNode`, `AstValidation`.

**Por que mover?** Nenhum tipo de `ast` e referenciado pela API publica. `ExpressionFileNode`
aparece apenas em `ExpressionCompiler` (interno), `SemanticAstBuilder` (interno),
`SemanticResolver` (interno) e `AbstractRuntimeEvaluator` (interno).

**Visibilidade apos mover:**
- `Node`, `ExpressionNode`, `AssignmentNode` (sealed interfaces — permitem subtipos em outros
  arquivos do mesmo pacote): devem ser `package-private` ou manter `public` restrito ao pacote
  `internal.ast`. Como sao `sealed`, os `permits` devem estar no mesmo pacote ou subpacote.
  Verificar se todos os permitidos estao em `internal.ast`.
- Demais: `package-private`.

**Atualizar imports em:**
- `internal.compiler.ExpressionCompiler`
- `internal.compiler.CompiledExpression`
- `internal.semantic.*` (varios)
- `internal.runtime.AbstractRuntimeEvaluator` e descendentes

**Atencao:** `SemanticResolverTest` importa tipos de `ast` diretamente. Sera resolvido na Etapa 9.

---

### Etapa 7 — Mover `ast/mapping/SemanticAstBuilder` para `internal.ast.mapping`

**O que fazer:**
- Mover `SemanticAstBuilder` para `com.runestone.expeval2.internal.ast.mapping`
- Atualizar import em `internal.compiler.ExpressionCompiler`
- `SemanticAstBuilder` pode ser `package-private` se `internal.compiler` e `internal.ast.mapping`
  forem pacotes diferentes → manter `package-private` dentro de `internal.ast.mapping` requer que
  `ExpressionCompiler` esteja no mesmo pacote. Como estao em pacotes distintos, manter visibilidade
  reduzida ao maximo via convencao (sem `public` desnecessario), mas tecnicamente precisara de
  visibilidade de pacote. **Decisao:** manter `SemanticAstBuilder` com visibilidade default
  (package-private) e mover `ExpressionCompiler` para `internal.ast.mapping`, ou manter
  `SemanticAstBuilder` acessivel via pacote comum. Alternativa simples: deixar `SemanticAstBuilder`
  `package-private` e criar um metodo estatico de fachada em `internal.ast.mapping` que
  `ExpressionCompiler` chame — ou simplesmente aceitar que `SemanticAstBuilder` e visivel dentro
  de `internal.*` sem ser `public` no sentido externo (convencao e suficiente aqui).

**Mover tambem:**
`SemanticAstBuilderTest` → `internal.ast.mapping` (Etapa 9).

---

### Etapa 8 — Mover `grammar/language` para `internal.grammar.language`

**O que mover:**
- `ExpressionEvaluatorV2ParserFacade` (usado apenas por `ExpressionCompiler`)
- `ParseResult` (referencia `ExpressionFileNode`, que ja estara em `internal.ast`)
- `ExpressionResultType` (usado apenas por internos apos Etapa 2)

**O que permanecer em `grammar/language` (publico):**
- Nada. `grammar/language` fica vazio e pode ser removido.

**O que permanecer em `grammar/exception` (publico):**
- `ParsingException` e `SyntaxError` — excecoes que o chamador pode precisar capturar.

**Atencao:** verificar `ExpressionEvaluatorV2ParserFacadeTest`. Esse teste esta em
`grammar.language` e acessa `ExpressionEvaluatorV2ParserFacade` diretamente. Como testes estao no
mesmo modulo Maven (nao em modulo separado), podem usar package-private se colocados no mesmo
pacote. Mover o teste para `internal.grammar.language`.

---

### Etapa 9 — Migrar testes afetados para pacotes `internal.*`

| Teste atual | Pacote destino |
|---|---|
| `compiler.ExpressionCompilerTest` | `internal.compiler` |
| `semantic.SemanticResolverTest` | `internal.semantic` |
| `ast.mapping.SemanticAstBuilderTest` | `internal.ast.mapping` |
| `grammar.language.ExpressionEvaluatorV2ParserFacadeTest` | `internal.grammar.language` |

Testes em `grammar.language` de gramatica/corpus (`ExpressionEvaluatorV2GrammarTest`, etc.) nao
referenciam tipos internos diretamente — verificar se precisam mover.

Testes em `ast.*` (hierarquia, nodeId, sourceSpan etc.) referenciam tipos de `ast` que passarao
para `internal.ast` — mover para `internal.ast`.

---

### Etapa 10 — Tornar `ExpressionEnvironmentId` package-private

**O que fazer:**
- Remover `public` de `ExpressionEnvironmentId`
- Tornar `ExpressionEnvironment.environmentId()` package-private
- Confirmar que so `internal.compiler.ExpressionCacheKey` o usa (para chave de cache)

Se `ExpressionEnvironmentId` precisar cruzar de `environment` para `internal.compiler`, a
alternativa e usar `ExpressionEnvironment` diretamente como chave (por identidade de objeto, ou
expor um `long id()` primitivo que nao exige o tipo publico).

---

### Etapa 11 — Verificacao final

```shell
mvn -q -pl exp-eval-mk2 test
```

Todos os testes devem passar. Conferir:
- Nenhum tipo de `internal.*` e importado fora de `internal.*` (salvo `ExpressionRuntimeSupport`
  em `api`)
- Nenhum tipo de `ast.*` existe (pacote migrado para `internal.ast.*`)
- `grammar/language` esta vazio ou removido

---

## Ordem de execucao recomendada

```
1  → ExpressionCompilationException (nova, em api)
2  → Consolidar pipeline em ExpressionRuntimeSupport (factory compile)
3  → ExpressionCompiler e CompiledExpression → package-private
4  → ExecutionScope → package-private
5  → internal.semantic.* → package-private (9 classes)
6  → ast.* → mover para internal.ast.*
7  → SemanticAstBuilder → mover para internal.ast.mapping
8  → grammar/language → mover para internal.grammar.language
9  → Migrar testes afetados
10 → ExpressionEnvironmentId → package-private
11 → mvn test (verificacao final)
```

---

## Superficie restante em `internal.*` com visibilidade > package-private

Apos todas as etapas, o unico tipo `public` em `internal.*` que permanece acessivel de fora e:

- `internal.runtime.ExpressionRuntimeSupport` — necessario porque `api` o armazena como campo

Isso e aceitavel: a convencao `internal` comunica "nao faz parte do contrato publico" mesmo sem
enforcement do compilador. A alternativa seria introduzir uma interface publica em `api` que
`ExpressionRuntimeSupport` implementa, eliminando qualquer import de `internal.*` da `api`. Essa
evolucao pode ser feita em etapa posterior se desejado.

---

## Riscos e pontos de atencao

| Risco | Mitigacao |
|---|---|
| Sealed interfaces em `internal.ast` — `permits` deve listar todos os subtipos no mesmo pacote | Verificar declaracoes de `Node`, `ExpressionNode`, `AssignmentNode` apos mover |
| Testes de gramatica que usam `ParseResult.root()` retornando `ExpressionFileNode` | Mover esses testes para `internal.*` ou adaptar para nao usar `ExpressionFileNode` diretamente |
| `ExpressionEnvironmentId` cruzando pacote `environment` → `internal.compiler` | Avaliar alternativa de expor primitivo de identidade em vez de tipo |
| Compilacao quebra temporariamente entre Etapa 5 e Etapa 9 | Realizar ambas as etapas em sequencia rapida; nao comitar estado intermedio quebrado |
