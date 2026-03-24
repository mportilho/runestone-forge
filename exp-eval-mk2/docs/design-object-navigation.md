# Design: Navegação de Objetos no exp-eval-mk2

Suporte a expressões do tipo `usuario.nome` e `cliente.pedido.calcularTotal()` via type hints
registrados em `ExpressionEnvironmentBuilder`.

---

## Visão geral das camadas impactadas

```
registerTypeHint(Class<?>)
  → TypeHintCatalog (novo)
  → ExpressionEnvironment (novo campo)
  → ResolutionContext (novo campo)
  → SemanticResolver (novo resolveMemberAccess)
  → ExecutionPlanBuilder (novos MethodHandles)
  → AbstractObjectEvaluator (novo case)
```

---

## 1. Sistema de tipos — novo `ObjectType`

O sealed `ResolvedType` precisa de um quarto membro que carregue a classe Java concreta:

```java
// types/ObjectType.java
public record ObjectType(Class<?> javaClass) implements ResolvedType {}
```

```java
// ResolvedType.java — adicionar ObjectType ao permits
public sealed interface ResolvedType
    permits ScalarType, VectorType, UnknownType, ObjectType {}
```

`ResolvedTypes.fromJavaType()` **não muda** — custom classes continuam retornando `UnknownType`.
O mapeamento para `ObjectType` é responsabilidade do `ExpressionEnvironmentBuilder.build()`,
que tem visão conjunta de símbolos externos e hints registrados.

---

## 2. `TypeHintCatalog` — registro de metadados de tipos

```java
// catalog/TypeHintCatalog.java
public final class TypeHintCatalog {
    private final Map<Class<?>, TypeMetadata> metadata;

    public Optional<TypeMetadata> find(Class<?> type) { ... }
    public boolean isRegistered(Class<?> type) { ... }
}

// catalog/TypeMetadata.java
public record TypeMetadata(
    Class<?> javaClass,
    Map<String, PropertyDescriptor> properties,    // "nome"           → {MethodHandle, ResolvedType}
    Map<String, List<MethodDescriptor>> methods    // "calcularTotal"  → [{handle, paramTypes, returnType}]
) {}
```

A descoberta usa reflexão durante o `build()`, não em tempo de avaliação:

| Estratégia | Quando aplicar |
|---|---|
| `javaClass.getRecordComponents()` | Java records — mapeamento direto |
| Getters `getXxx()` / `isXxx()` públicos | POJOs convencionais |
| Campos públicos | Fallback |
| Métodos públicos não-estáticos (não-Object) | Chamadas de método na cadeia |

---

## 3. Grammar — regra de navegação

O token `PERIOD` já existe mas não é usado em nenhuma regra do parser.
A mudança cirúrgica fica em `referenceTarget`:

```antlr
referenceTarget
    : function                                                           # functionReferenceTarget
    | IDENTIFIER memberChain*                                            # identifierReferenceTarget
    ;

memberChain
    : PERIOD IDENTIFIER                                                  # propertyAccess
    | PERIOD IDENTIFIER LPAREN
          (allEntityTypes (COMMA allEntityTypes)*)?
      RPAREN                                                             # methodCallAccess
    ;
```

Exemplos que passam a ser válidos:

```
usuario.nome
cliente.pedido.calcularTotal()
cliente.pedido.calcularTotal(taxa)
empresa.gerente.nome
```

Quando a lista `memberChain` é vazia (só `IDENTIFIER` sem nenhum `PERIOD`),
o `SemanticAstBuilder` continua criando um `IdentifierNode` — sem mudança no caminho comum.

---

## 4. AST — `PropertyChainNode`

```java
// internal/ast/PropertyChainNode.java
public record PropertyChainNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    String rootIdentifier,
    List<MemberAccess> chain
) implements ExpressionNode {

    public sealed interface MemberAccess
        permits PropertyAccess, MethodCallAccess {}

    public record PropertyAccess(String name)
        implements MemberAccess {}

    public record MethodCallAccess(String name, List<ExpressionNode> arguments)
        implements MemberAccess {}
}
```

`ExpressionNode` recebe `PropertyChainNode` no `permits`.

---

## 5. Resolução semântica — `resolveMemberAccess`

O `SemanticResolver` analisa a cadeia segmento a segmento:

```java
private ResolvedType resolveMemberAccess(PropertyChainNode node) {
    ResolvedType current = resolveIdentifier(new IdentifierNode(..., node.rootIdentifier()));

    for (MemberAccess segment : node.chain()) {
        if (current instanceof UnknownType) {
            // Sem informação de tipo — aceita sem erro (mesmo critério dos parâmetros
            // Temporal em DateTimeFunctions)
            current = UnknownType.INSTANCE;
            continue;
        }
        if (!(current instanceof ObjectType objectType)) {
            issues.add(SemanticIssue.error("INVALID_MEMBER_ACCESS",
                "type " + current + " does not support member access"));
            return UnknownType.INSTANCE;
        }
        TypeMetadata metadata = context.typeHintCatalog()
            .find(objectType.javaClass()).orElse(null);
        if (metadata == null) {
            // Classe presente mas hint não registrado → UnknownType, sem erro
            current = UnknownType.INSTANCE;
            continue;
        }
        current = switch (segment) {
            case PropertyAccess p  -> resolveProperty(metadata, p.name(), node);
            case MethodCallAccess m -> resolveMethod(metadata, m, node);
        };
    }
    return current;
}
```

**Regra de tolerância**: tipo não registrado retorna `UnknownType` em vez de erro,
permitindo uso dinâmico quando o caller não precisa de validação estática.

---

## 6. Execution Plan — `ExecutablePropertyChain`

```java
// internal/runtime/ExecutablePropertyChain.java
public record ExecutablePropertyChain(
    SymbolRef root,
    List<ExecutableAccess> chain    // MethodHandles pré-construídos no build()
) implements ExecutableNode {

    public sealed interface ExecutableAccess
        permits ExecutableFieldGet, ExecutableMethodInvoke {}

    /** Acesso a propriedade: handle(receiver) → value */
    public record ExecutableFieldGet(
        String name,
        MethodHandle getter
    ) implements ExecutableAccess {}

    /** Chamada de método: handle(receiver, arg0, ...) → value */
    public record ExecutableMethodInvoke(
        String name,
        MethodHandle handle,
        List<ExecutableNode> arguments,
        List<Class<?>> parameterTypes
    ) implements ExecutableAccess {}
}
```

O `ExecutionPlanBuilder` constrói os `MethodHandle`s durante a fase de build (não em runtime),
usando a `TypeHintCatalog` para localizar os handles corretos.
Isso mantém o custo de reflexão fora do caminho quente de avaliação.

---

## 7. Avaliação runtime

Novo `case` no `evaluateExpr()` de `AbstractObjectEvaluator`:

```java
case ExecutablePropertyChain chain -> evaluatePropertyChain(chain, scope);
```

```java
private Object evaluatePropertyChain(ExecutablePropertyChain node, ExecutionScope scope) {
    Object current = scope.find(node.root());
    if (current == ExecutionScope.UNBOUND) throw unboundVariableException(...);

    for (ExecutableAccess access : node.chain()) {
        if (current == null) throw new ExpressionEvaluationException(
            "null value encountered navigating '" + node.root().name() + "'");

        current = switch (access) {
            case ExecutableFieldGet g -> g.getter().invoke(current);
            case ExecutableMethodInvoke m -> {
                Object[] args = evaluateAndCoerceArgs(m.arguments(), m.parameterTypes(), scope);
                yield m.handle().invokeWithArguments(prepend(current, args));
            }
        };
    }
    return current;
}
```

**Política de null**: lança `ExpressionEvaluationException` ao encontrar `null` no meio
da cadeia — comportamento explícito e rastreável.

---

## 8. `ExpressionEnvironmentBuilder.registerTypeHint()`

```java
// campo novo no builder
private final List<Class<?>> typeHints = new ArrayList<>();

public ExpressionEnvironmentBuilder registerTypeHint(Class<?> type) {
    Objects.requireNonNull(type, "type must not be null");
    typeHints.add(type);
    return this;
}
```

No `build()`, três efeitos:

**a) Constrói o `TypeHintCatalog`** por reflexão antes de qualquer outra etapa:
```java
TypeHintCatalog typeHintCatalog = buildTypeHintCatalog(typeHints);
```

**b) Eleva o `declaredType` de símbolos externos** cujo runtime class tem hint registrado:
```java
// ao criar ExternalSymbolDescriptor para cada símbolo:
ResolvedType declaredType = typeHintCatalog.isRegistered(defaultValue.getClass())
    ? new ObjectType(defaultValue.getClass())
    : ResolvedTypes.fromJavaType(defaultValue.getClass());
```

**c) Inclui os hints no hash do `ExpressionEnvironmentId`** para que environments com hints
diferentes gerem chaves de cache distintas:
```java
typeHints.forEach(c -> parts.add("th:" + c.getName()));
```

---

## Uso final

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .registerTypeHint(Usuario.class)
    .registerTypeHint(Pedido.class)
    .registerExternalSymbol("usuario", usuarioPadrao, true)
    .addMathFunctions()
    .build();

// Validação estática em compile-time
ValidationResult vr = MathExpression.validate("usuario.pedido.valor * 1.1", env);
vr.valid();          // true (se Usuario.pedido → Pedido e Pedido.valor → NUMBER)
vr.userVariables();  // {"usuario"}

// Avaliação
BigDecimal result = MathExpression.compile("usuario.pedido.valor * 1.1", env)
    .compute(Map.of("usuario", usuarioConcreto));

// Chamada de método na cadeia
BigDecimal total = MathExpression.compile("cliente.pedido.calcularTotal(taxa)", env)
    .compute(Map.of("cliente", clienteConcreto, "taxa", BigDecimal.valueOf(0.05)));
```

---

## Resumo das mudanças por camada

| Camada | Arquivo(s) | Natureza |
|---|---|---|
| Grammar | `ExpressionEvaluatorV2.g4` | Nova regra `memberChain` em `referenceTarget` |
| Tipos | `ResolvedType.java`, novo `ObjectType.java` | Novo `permits` no sealed |
| Catalog | Novos `TypeHintCatalog.java`, `TypeMetadata.java` | Adição |
| AST | `ExpressionNode.java`, novo `PropertyChainNode.java` | Novo `permits` + novo nó |
| SemanticAstBuilder | `SemanticAstBuilder.java` | Visitor para `memberChain` |
| SemanticResolver | `SemanticResolver.java` | Novo `resolveMemberAccess()` |
| ExecutableNode | `ExecutableNode.java`, novo `ExecutablePropertyChain.java` | Novo `permits` + novo nó |
| ExecutionPlanBuilder | `ExecutionPlanBuilder.java` | Conversão com MethodHandles |
| AbstractObjectEvaluator | `AbstractObjectEvaluator.java` | Novo `case` |
| ExpressionEnvironmentBuilder | `ExpressionEnvironmentBuilder.java` | `registerTypeHint()` + ajuste no `build()` |
| ExpressionEnvironment | `ExpressionEnvironment.java` | Novo campo `TypeHintCatalog` |
| ResolutionContext | `ResolutionContext.java` | Novo campo `TypeHintCatalog` |

Todas as alterações são aditivas (novos `permits`, novos `case`, novo método no builder)
exceto a grammar e o `build()`, que sofrem modificações pontuais.
