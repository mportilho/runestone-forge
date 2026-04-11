# Plano: Navegação em Collections e Maps (JSONPath-like)

## Contexto

O `expression-evaluator` já suporta navegação em objetos via dot-notation (`obj.prop`, `obj?.method(args)`). O objetivo é estender a gramática e o runtime para suportar navegação em collections e maps com sintaxe JSONPath, incluindo indexação (`[0]`), wildcards (`[*]`), deep scan (`..`), slices (`[0:2]`), filtros (`[?(@.price < 10)]`) e funções de collection (`..sum()`, `..customEval(2, true)`).

**Motivação:** Permitir expressões como `store.book[*].author`, `store..price`, `store.book[?(@.price < 10)].title`, `store.book[*].price..sum()` e `store..price..customEval(2, true)` diretamente na linguagem de expressões.

---

## Sintaxe Suportada

### Operadores de Navegação

| Operador | Descrição |
|---|---|
| `@` | Elemento corrente em um predicado de filtro |
| `*` | Wildcard — qualquer propriedade ou índice |
| `..` | Deep scan — desce recursivamente no grafo a partir do nó atual |
| `.<name>` | Acesso a propriedade filha (existente) |
| `[<n>]` | Índice de lista (0-based, negativo = do final) |
| `[n,m]` | Multi-índice — retorna sub-lista com esses índices |
| `[start:end]` | Slice — da posição `start` (inclusive) até `end` (exclusive) |
| `[?(<expr>)]` | Filtro — mantém elementos onde `<expr>` é verdadeiro |

### Funções de Collection (sufixo `..`)

Invocadas ao final de um path; recebem implicitamente a collection acumulada como primeiro argumento lógico:

| Função | Saída |
|---|---|
| `..sum()` | Soma de uma lista de números — `BigDecimal` |
| `..avg()` | Média — `BigDecimal` |
| `..min()` | Mínimo — `BigDecimal` |
| `..max()` | Máximo — `BigDecimal` |
| `..count()` / `..length()` / `..size()` | Cardinalidade — `BigDecimal` |
| `..<func>(arg1, arg2, ...)` | Chama função do `FunctionCatalog` como `func(<collection>, arg1, arg2, ...)` |

**Convenção para funções customizadas via `ExpressionEnvironmentBuilder`:**

- `registerStaticProvider(...)` e `registerInstanceProvider(...)` continuam descobrindo funções normalmente; não é necessária nova API pública.
- Em `store..price..customEval(2, true)`, a resolução trata a chamada como `customEval(store..price, 2, true)`.
- O primeiro parâmetro declarado da função customizada, após o eventual `MathContext` injetado pelo builder, deve ser a collection corrente (`Collection`, `List` ou equivalente coerível para vetor).
- Funções cujo primeiro parâmetro não seja collection continuam disponíveis apenas como chamadas globais normais, por exemplo `customEval(store..price, 2, true)`.

### Operadores de Filtro Adicionais (dentro de `[?(...)]`)

| Operador | Descrição |
|---|---|
| `in` | Esquerda existe na direita |
| `nin` | Esquerda não existe na direita |
| `=~` / `!~` | Correspondência de regex (suporta `/padrão/flags`) |
| `and` / `or` | Combinação de múltiplas condições dentro do mesmo `?(...)` |

### Exemplos (baseados no grafo de objetos abaixo)

```json
{
  "store": {
    "book": [
      { "category": "reference", "author": "Nigel Rees",     "title": "Sayings of the Century",  "price": 8.95  },
      { "category": "fiction",   "author": "Evelyn Waugh",   "title": "Sword of Honour",          "price": 12.99 },
      { "category": "fiction",   "author": "Herman Melville","title": "Moby Dick",       "isbn": "0-553-21311-3", "price": 8.99  },
      { "category": "fiction",   "author": "J. R. R. Tolkien","title": "The Lord of the Rings", "isbn": "0-395-19395-8", "price": 22.99 }
    ],
    "bicycle": { "color": "red", "price": 19.95 },
    "expensive": 10
  }
}
```

| Expressão | Resultado |
|---|---|
| `store.book[*].author` | Autores de todos os livros |
| `store..author` | Todos os autores, em qualquer nível |
| `store.*` | Todos os filhos diretos (books + bicycle) |
| `store..price` | Todos os preços |
| `store..book[2]` | Terceiro livro |
| `store..book[-2]` | Penúltimo livro |
| `store..book[0,1]` | Primeiros dois livros |
| `store..book[:2]` | Livros de índice 0 até 1 |
| `store..book[1:2]` | Livro de índice 1 |
| `store..book[-2:]` | Últimos dois livros |
| `store..book[2:]` | Do índice 2 ao final |
| `store..book[?(@.isbn)]` | Livros com ISBN |
| `store.book[?(@.price < 10)]` | Livros mais baratos que 10 |
| `store.book[?(@.price < 10 and @.isbn)]` | Múltiplas condições no mesmo filtro |
| `store..book[?(@.price <= store.expensive)]` | Filtro com referência ao escopo externo |
| `store..book[?(@.author =~ /.*REES/i)]` | Filtro com regex case-insensitive |
| `store..*` | Todos os valores do grafo |
| `store..book..length()` | Número de livros |
| `store..price..customEval(2, true)` | Equivale a `customEval(store..price, 2, true)` |

---

## Arquivos Críticos

| Arquivo | Tipo de Mudança |
|---|---|
| `ExpressionEvaluator.g4` | Novos tokens e regras de gramática |
| `PropertyChainNode.java` | Novos variants de `MemberAccess` |
| `ExecutablePropertyChain.java` | Novos variants de `ExecutableAccess` |
| `AbstractObjectEvaluator.java` | Novos helpers de avaliação + `currentFilterElement` |
| `SemanticAstBuilder.java` | Mapeamento dos novos contextos ANTLR |
| `SemanticResolver.java` | Propagação de `CollectionType` |
| `ExecutionPlanBuilder.java` | Compilação dos novos steps |
| `FunctionCatalog.java` | Reuso da resolução de overloads para `..<func>(...)` |
| `FunctionDescriptor.java` | Binding da collection implícita como primeiro argumento |
| `ResolvedType.java` | Adição de `CollectionType` |
| `PropertyDescriptor.java` | Campo `elementType` |
| `ExpressionEnvironmentBuilder.java` | Introspecção de generic type parameters + contrato do primeiro parâmetro collection |

---

## Fase 1 — Novo Pacote `internal.navigation`

**Objetivo:** Reorganizar as classes de introspecção/reflexão num sub-módulo interno dedicado.

### Mover (sem mudança de lógica, apenas de pacote)

- `internal.runtime.TypeIntrospectionSupport` → `internal.navigation.TypeIntrospectionSupport`
- `internal.runtime.ReflectiveAccessCache` → `internal.navigation.ReflectiveAccessCache`

Atualizar imports em: `AbstractObjectEvaluator`, `ExecutionPlanBuilder`, `ExpressionEnvironmentBuilder`.

### Criar no novo pacote

```
com.runestone.expeval.internal.navigation/
  TypeIntrospectionSupport.java    (movido)
  ReflectiveAccessCache.java       (movido)
  VectorAggregationKind.java       (novo enum: SUM, AVG, MIN, MAX, COUNT)
```

---

## Fase 2 — Sistema de Tipos: `CollectionType`

**Arquivo novo:** `com/runestone/expeval/types/CollectionType.java`

```java
public record CollectionType(ResolvedType elementType) implements ResolvedType {
    public CollectionType { Objects.requireNonNull(elementType); }
}
```

**Modificar `ResolvedType.java`:**
```java
public sealed interface ResolvedType
    permits ScalarType, UnknownType, VectorType, ObjectType, NullType, CollectionType {}
```

**Rationale:** `VectorType.INSTANCE` é um enum singleton (tipo não-parametrizado) que mantém sua semântica atual de "vetor sem tipo de elemento conhecido". `CollectionType` é o novo tipo parametrizado introduzido quando o tipo do elemento é conhecido. Mudar `VectorType` para parametrizado quebraria todos os `switch` exhaustivos existentes.

**Modificar `ResolvedTypes.merge`:**
- `CollectionType(A)` + `CollectionType(B)` → `CollectionType(merge(A, B))`
- `CollectionType` + `VectorType` → `VectorType.INSTANCE`
- demais combinações → `UnknownType.INSTANCE`

**Modificar `PropertyDescriptor.java`:** Adicionar `@Nullable ResolvedType elementType`.

**Modificar `ExpressionEnvironmentBuilder.discoverTypeMetadata`:** Usar `java.lang.reflect.ParameterizedType` para extrair o tipo `E` de `Collection<E>` ou `List<E>`. Se `E` está registrado via `registerTypeHint`, `elementType = ObjectType(E.class)`. Caso contrário, `elementType = ResolvedTypes.fromJavaType(E)`.

**Contrato adicional para funções de collection:** Nenhuma mudança de API pública no builder. A extensão é semântica: `path..fn(a, b)` passa a ser elegível quando existir um `FunctionDescriptor` compatível com `fn(path, a, b)`, isto é, com primeiro parâmetro compatível com collection/vector após a eventual injeção de `MathContext`.

---

## Fase 3 — Gramática (ANTLR)

**Arquivo:** `ExpressionEvaluator.g4`

### Novos tokens lexer

Inserir antes de `PERIOD` (para evitar que `..` seja lexado como dois `PERIOD`):

```antlr
DOUBLE_PERIOD : '..' ;
QUESTION      : '?' ;
AT            : '@' ;
COLON_OP      : ':' ;     // token real para slices
REGEX_FLAGS   : [gimsuy]+ ;
```

> **Nota sobre `COLON_OP`:** O fragment `Colon` usado em `TIME` e `DATETIME` não é afetado — esses tokens têm prioridade por aparecerem antes. `COLON_OP` só é reconhecido dentro de `[...]`, contexto onde `TIME`/`DATETIME` nunca ocorrem.

> **Nota sobre `DOUBLE_PERIOD`:** Deve aparecer **antes** de `PERIOD` no lexer para que `..` não seja lexado como dois pontos separados.

### Regex literal

```antlr
regexLiteral : DIV ~[/]* DIV REGEX_FLAGS? ;
```

Reutiliza `DIV` como delimitador (não é ambíguo pois regex só aparece dentro de `filterRelation`).

### Novas regras de subscript

```antlr
subscript
    : LBRACKET subscriptSpec (COMMA subscriptSpec)* RBRACKET
    ;

subscriptSpec
    : MULT                                               # wildcardSubscript
    | signedInteger COLON_OP signedInteger?              # sliceFromStartSubscript
    | COLON_OP signedInteger                             # sliceToEndSubscript
    | signedInteger                                      # indexSubscript
    | QUESTION LPAREN filterPredicate RPAREN             # filterSubscript
    ;

signedInteger : MINUS? POSITIVE ;

filterPredicate : filterAtom ((AND | OR) filterAtom)* ;

filterAtom
    : LPAREN filterPredicate RPAREN
    | filterRelation
    ;

filterRelation
    : filterValue comparisonOperator filterValue
    | filterValue REGEX_MATCH regexLiteral
    | filterValue REGEX_NOT_MATCH regexLiteral
    | filterValue                               // truthy check: @.isbn (non-null)
    ;

filterValue
    : AT memberChain*               // @.price, @.isbn
    | referenceTarget               // referência ao escopo externo: store.expensive
    | numericEntity
    | stringConcatExpression
    | NULL
    ;
```

`comparisonOperator` é uma nova regra auxiliar que agrupa `GT`, `GEQ`, `LT`, `LEQ`, `EQUAL`, `NOT_EQUAL` — extraída das alternatives de comparação existentes.

### Novos alternatives em `memberChain`

```antlr
memberChain
    : DOUBLE_PERIOD MULT                                              # deepScanWildcard
    | DOUBLE_PERIOD IDENTIFIER                                        # deepScanProperty
    | DOUBLE_PERIOD IDENTIFIER LPAREN
          (allEntityTypes (COMMA allEntityTypes)*)?
      RPAREN                                                          # collectionFunctionAccess
    | PERIOD IDENTIFIER                                               # propertyAccess        (existente)
    | SAFE_NAV IDENTIFIER                                             # safePropertyAccess    (existente)
    | PERIOD IDENTIFIER LPAREN ... RPAREN                             # methodCallAccess      (existente)
    | SAFE_NAV IDENTIFIER LPAREN ... RPAREN                           # safeMethodCallAccess  (existente)
    | subscript                                                       # subscriptAccess
    ;
```

---

## Fase 4 — Novos `MemberAccess` em `PropertyChainNode`

**Arquivo:** `PropertyChainNode.java`

Adicionar ao `sealed interface MemberAccess` como inner records (padrão existente):

```java
public sealed interface MemberAccess permits
    PropertyAccess, SafePropertyAccess, MethodCallAccess, SafeMethodCallAccess,
    CollectionIndexStep, CollectionSliceStep, WildcardStep,
    FilterPredicateStep, DeepScanStep, CollectionFunctionStep, VectorAggregationStep {}

// [0], [-1], [0,1]
record CollectionIndexStep(List<ExpressionNode> indices) implements MemberAccess { ... }

// [start:end], [:end], [start:]
record CollectionSliceStep(
    @Nullable ExpressionNode start,
    @Nullable ExpressionNode end
) implements MemberAccess { ... }

// [*]
record WildcardStep() implements MemberAccess {}

// [?(@.price < 10)]
record FilterPredicateStep(ExpressionNode predicate) implements MemberAccess { ... }

// ..author, ..*
record DeepScanStep(@Nullable String propertyName) implements MemberAccess { ... }

// ..customEval(2, true)
record CollectionFunctionStep(String name, List<ExpressionNode> arguments) implements MemberAccess { ... }

// ..sum(), ..avg(), ..count()
record VectorAggregationStep(VectorAggregationKind kind) implements MemberAccess { ... }
```

**Representação de `@` no AST:** `@.price` é construído como `PropertyChainNode(rootIdentifier="@", chain=[PropertyAccess("price")])`. `@` sozinho vira `IdentifierNode(name="@")`. O sentinel `"@"` é reconhecido durante avaliação como "elemento corrente" — sem necessidade de novo `ExpressionNode`.

---

## Fase 5 — `SemanticAstBuilder`

**Arquivo:** `SemanticAstBuilder.java`

Adicionar ao `ExpressionVisitor`:

- `visitSubscriptAccess` → despacha por tipo de `subscriptSpec`:
  - `IndexSubscript` → `CollectionIndexStep(List.of(buildSignedIntNode(...)))`
  - `WildcardSubscript` → `WildcardStep()`
  - `Slice*` → `CollectionSliceStep(start?, end?)`
  - `FilterSubscript` → `FilterPredicateStep(buildFilterPredicate(ctx))`
  - múltiplos specs inteiros → `CollectionIndexStep(listaDeIndices)`

- `visitDeepScanProperty` → `DeepScanStep("propertyName")`
- `visitDeepScanWildcard` → `DeepScanStep(null)`
- `visitCollectionFunctionAccess` → `buildCollectionFunctionStep(name, args)`

**`buildFilterPredicate`:** Nova classe interna `FilterPredicateVisitor` que transforma o contexto ANTLR `filterPredicate` em `ExpressionNode` (usando `BinaryOperationNode` com `AND`/`OR`, `REGEX_MATCH`, etc.). `and` e `or` combinam `filterRelation`s dentro do mesmo `?()`, por exemplo `?(@.price < 10 and @.isbn)`.

**`buildCollectionFunctionStep`:**
```java
private PropertyChainNode.MemberAccess buildCollectionFunctionStep(String name, List<ExpressionNode> arguments) {
    if (isBuiltInVectorAggregation(name)) {
        return new PropertyChainNode.VectorAggregationStep(resolveAggregationKind(name));
    }
    return new PropertyChainNode.CollectionFunctionStep(name, arguments);
}
```

**`resolveAggregationKind`:**
```java
static VectorAggregationKind resolveAggregationKind(String name) {
    return switch (name.toLowerCase()) {
        case "sum"                     -> SUM;
        case "avg", "average"          -> AVG;
        case "min"                     -> MIN;
        case "max"                     -> MAX;
        case "count", "length", "size" -> COUNT;
        default -> throw new IllegalArgumentException("unsupported built-in aggregation: " + name);
    };
}
```

---

## Fase 6 — `SemanticResolver`

**Arquivo:** `SemanticResolver.java`

Estender `resolvePropertyChain` com propagação de tipo para os novos steps:

| Step | Tipo de entrada | Tipo de saída |
|---|---|---|
| `CollectionIndexStep(1 índice)` | `CollectionType(E)` | `E` |
| `CollectionIndexStep(n índices)` | `CollectionType(E)` | `CollectionType(E)` |
| `CollectionSliceStep` | `CollectionType(E)` / `VectorType` | mesmo que entrada |
| `WildcardStep` | `CollectionType(E)` | `CollectionType(E)` — ativa vector mode |
| `FilterPredicateStep` | `CollectionType(E)` | `CollectionType(E)` |
| `DeepScanStep(name)` | `ObjectType(C)` | `CollectionType(resolveProperty(name, C))` |
| `DeepScanStep(null)` | `ObjectType(C)` | `CollectionType(UnknownType)` |
| `CollectionFunctionStep(name, args)` | `CollectionType(E)` / `VectorType` | `descriptor.returnType()` |
| `VectorAggregationStep(COUNT)` | `CollectionType(E)` | `ScalarType.NUMBER` |
| `VectorAggregationStep(SUM/AVG/MIN/MAX)` | `CollectionType(E)` | `ScalarType.NUMBER` |
| `PropertyAccess` em vector mode | `CollectionType(E)` | `CollectionType(resolveProperty(prop, E))` |

**Resolução de predicados de filtro:** Resolver o sub-tree em sub-sessão com `"@"` resolvendo para o `elementType` da `CollectionType` corrente.

**Resolução de funções de collection:** `..<name>(arg1, arg2)` monta uma lista lógica de argumentos `[collectionAtual, arg1, arg2]` e procura candidatos no `FunctionCatalog` com aridade `argumentosExplícitos + 1`. Apenas descritores cujo primeiro parâmetro seja compatível com collection/vector participam da seleção. O binding final continua sendo `ResolvedFunctionBinding`, sem introduzir um catálogo paralelo.

**Propagação após função customizada:** Se `descriptor.returnType()` for `CollectionType` ou `VectorType.INSTANCE`, a chain permanece em vector mode e pode continuar com novos steps. Se o retorno for escalar ou objeto, a chain volta ao modo escalar.

**Erros semânticos novos:**
- `INVALID_CURRENT_ELEMENT` — `@` fora de um predicado de filtro
- `INVALID_MEMBER_ACCESS` — `CollectionIndexStep`/`WildcardStep` em tipo não-collection
- `TYPE_MISMATCH` — `VectorAggregationStep`/`CollectionFunctionStep` em tipo não-collection
- `UNKNOWN_COLLECTION_FUNCTION` — nenhuma função elegível encontrada após `..`
- `INCOMPATIBLE_COLLECTION_FUNCTION_ARGUMENTS` — overloads existem, mas não aceitam os argumentos após inserir a collection como argumento 0

---

## Fase 7 — `ExecutionPlanBuilder`

**Arquivo:** `ExecutionPlanBuilder.java`

Estender `buildPropertyChain`:

```
CollectionIndexStep   → ExecutableIndexAccess(indexNodes, single=true/false)
WildcardStep          → ExecutableWildcard()
CollectionSliceStep   → ExecutableSliceAccess(startNode?, endNode?)
FilterPredicateStep   → ExecutableFilterPredicate(buildNode(predicate))
DeepScanStep          → ExecutableDeepScan(propertyName?)
CollectionFunctionStep → ExecutableCollectionFunction(binding, explicitArgumentNodes, vectorResult)
VectorAggregationStep → ExecutableVectorAggregation(kind)
```

Atualizar `countNodeEvents` para contabilizar events em predicados de filtro e expressões de índice.

Regex em filtros: estender `buildRegexNode` para aceitar `/pattern/flags`. Flags mapeiam para bitmask `java.util.regex.Pattern`.

---

## Fase 8 — Novos `ExecutableAccess` em `ExecutablePropertyChain`

**Arquivo:** `ExecutablePropertyChain.java`

```java
sealed interface ExecutableAccess permits
    ExecutableFieldGet, ExecutableMethodInvoke,
    ReflectivePropertyAccess, ReflectiveMethodInvoke,
    ExecutableIndexAccess, ExecutableSliceAccess, ExecutableWildcard,
    ExecutableFilterPredicate, ExecutableDeepScan,
    ExecutableCollectionFunction, ExecutableVectorAggregation {}

record ExecutableIndexAccess(List<ExecutableNode> indices, boolean single) implements ExecutableAccess {}
record ExecutableSliceAccess(@Nullable ExecutableNode start, @Nullable ExecutableNode end) implements ExecutableAccess {}
record ExecutableWildcard() implements ExecutableAccess {}
record ExecutableFilterPredicate(ExecutableNode predicate) implements ExecutableAccess {}
record ExecutableDeepScan(@Nullable String propertyName) implements ExecutableAccess {}
record ExecutableCollectionFunction(
    ResolvedFunctionBinding binding,
    List<ExecutableNode> arguments,
    boolean vectorResult
) implements ExecutableAccess {}
record ExecutableVectorAggregation(VectorAggregationKind kind) implements ExecutableAccess {}
```

---

## Fase 9 — `AbstractObjectEvaluator` (Runtime)

**Arquivo:** `AbstractObjectEvaluator.java`

### Campo para current element em filtros

```java
// Seguro: AbstractObjectEvaluator é instanciado por-avaliação, não compartilhado entre threads
@Nullable private Object currentFilterElement;
```

### Lógica de avaliação em `evaluatePropertyChain`

Adicionar flag local `boolean vectorMode` e estender o switch sobre `ExecutableAccess`:

```java
case ExecutableWildcard ignored -> {
    vectorMode = true;
    yield toList(current);
}
case ExecutableIndexAccess idx -> {
    if (vectorMode) yield mapSingleIndex(current, idx, scope);
    yield applyIndex(current, idx, scope);
}
case ExecutableSliceAccess slice -> {
    vectorMode = true;
    yield applySlice(current, slice, scope);
}
case ExecutableFilterPredicate filter -> {
    vectorMode = true;
    yield applyFilter(current, filter, scope);
}
case ExecutableDeepScan scan -> {
    vectorMode = true;
    yield applyDeepScan(current, scan);
}
case ExecutableCollectionFunction function -> {
    vectorMode = function.vectorResult();
    yield applyCollectionFunction(current, function, scope);
}
case ExecutableVectorAggregation agg -> {
    vectorMode = false;
    yield applyAggregation(current, agg);
}
// Em vector mode, accesses existentes mapeiam elemento a elemento:
case ExecutableFieldGet fieldGet when vectorMode ->
    mapElements(current, el -> invokeGetter(el, fieldGet));
case ReflectivePropertyAccess rpa when vectorMode ->
    mapElements(current, el -> resolvePropertyReflective(source, el, rpa.name()));
```

Reconhecer `"@"` em `evaluateExpr`:
```java
case IdentifierNode id when "@".equals(id.name()) -> currentFilterElement;
// PropertyChainNode com rootIdentifier="@": tratar como root = currentFilterElement
```

### Helpers principais

**`applyIndex`:** `((BigDecimal) idx).intValueExact()` para índice. Negativo: `size + i`. Out-of-bounds → `null` (não lança exceção).

**`applySlice`:** `list.subList(start, end)` → `new ArrayList<>(subList)` (mutável para steps seguintes).

**`applyFilter`:**
```java
private List<Object> applyFilter(Object collection, ExecutableFilterPredicate filter, ExecutionScope scope) {
    List<?> list = asList(collection);
    List<Object> result = new ArrayList<>(list.size()); // pre-sized: evita resize
    for (Object element : list) {
        currentFilterElement = element;
        Object test = evaluateExpr(filter.predicate(), scope);
        if (isTruthy(test)) result.add(element);
    }
    currentFilterElement = null;
    return result;
}
```

**`applyDeepScan`:** BFS iterativo com `ArrayDeque` + `Set<Integer>` (identity hash codes) para detecção de ciclos sem `IdentityHashMap`:

```java
private List<Object> applyDeepScan(Object root, ExecutableDeepScan scan) {
    List<Object> results = new ArrayList<>();
    Deque<Object> queue = new ArrayDeque<>();
    Set<Integer> visited = new HashSet<>();
    queue.add(root);
    while (!queue.isEmpty()) {
        Object node = queue.poll();
        if (node == null || !visited.add(System.identityHashCode(node))) continue;
        if (node instanceof List<?> list) { queue.addAll(list); continue; }
        if (scan.propertyName() == null) {
            collectAllProperties(node, results, queue);
        } else {
            Object val = tryGetProperty(node, scan.propertyName());
            if (val != null) results.add(val);
            enqueueChildObjects(node, queue);
        }
    }
    return results;
}
```

**`applyAggregation`:** Loop simples; sem `Stream` nas operações críticas (avaliar com JMH se necessário).

**`applyCollectionFunction`:**
```java
private Object applyCollectionFunction(Object current, ExecutableCollectionFunction function, ExecutionScope scope) {
    FunctionDescriptor descriptor = function.binding().descriptor();
    Object[] args = new Object[descriptor.arity()];
    args[0] = runtimeServices.coerce(current, descriptor.parameterTypes().getFirst());
    for (int index = 0; index < function.arguments().size(); index++) {
        Object value = evaluateExpr(function.arguments().get(index), scope);
        args[index + 1] = runtimeServices.coerce(value, descriptor.parameterTypes().get(index + 1));
    }
    Object result = descriptor.invoke(args);
    return runtimeServices.coerceToResolvedType(result, function.binding().returnType());
}
```

Idealmente, extrair um helper comum para invocação de `ResolvedFunctionBinding` e reutilizá-lo em `evaluateFunctionCall(...)` e `applyCollectionFunction(...)`, evitando duplicação de coerção, auditoria e tratamento de exceções.

**`mapElements`:** `ArrayList` pré-alocado com `list.size()`:
```java
private List<Object> mapElements(Object collection, ThrowingFunction<Object, Object> mapper) {
    List<?> list = (List<?>) collection;
    List<Object> result = new ArrayList<>(list.size());
    for (Object el : list) result.add(el != null ? mapper.apply(el) : null);
    return result;
}
```

### Outer-scope em filtros

`store..book[?(@.price <= store.expensive)]` — `store.expensive` resolve contra o `scope` externo porque `evaluateExpr(filter.predicate(), scope)` passa o mesmo `scope`. Nenhum tratamento especial é necessário.

---

## Considerações de Performance (GC)

| Técnica | Local |
|---|---|
| `ArrayList` pré-alocado com `list.size()` | Todos os helpers que retornam listas |
| `currentFilterElement` como campo de instância | `applyFilter` — zero alocação por elemento |
| `ArrayDeque` para BFS | `applyDeepScan` — mais eficiente que `LinkedList` |
| Identity hash em `Set<Integer>` para ciclos | `applyDeepScan` — evita `IdentityHashMap` |
| Regex compilado em `ExecutionPlanBuilder` | Uma vez, na compilação |
| `intValueExact()` para índices | `applyIndex` — evita perda silenciosa de `intValue()` |
| `MethodHandle` precompilado para typed path e function catalog | Existente — mantido nos novos steps |

---

## Estrutura Final de Pacotes

```
internal.navigation/
  TypeIntrospectionSupport.java    (movido de internal.runtime)
  ReflectiveAccessCache.java       (movido de internal.runtime)
  VectorAggregationKind.java       (novo enum)

types/
  CollectionType.java              (novo)
  ResolvedType.java                (modificado: +CollectionType)
  ResolvedTypes.java               (modificado: merge com CollectionType)

catalog/
  PropertyDescriptor.java          (modificado: +elementType)

internal/ast/
  PropertyChainNode.java           (modificado: +7 MemberAccess variants)

internal/runtime/
  ExecutablePropertyChain.java     (modificado: +7 ExecutableAccess variants)
  AbstractObjectEvaluator.java     (modificado: currentFilterElement + helpers)
  SemanticResolver.java            (modificado: CollectionType propagation)
  ExecutionPlanBuilder.java        (modificado: compilação dos novos steps)
  SemanticAstBuilder.java          (modificado: visitor para novos contextos ANTLR)
```

---

## Entrega em Fases (PRs incrementais)

| # | PR | Conteúdo |
|---|---|---|
| P1 | Reorganização | Mover `TypeIntrospectionSupport`/`ReflectiveAccessCache`; criar `internal.navigation`; `CollectionType`; `PropertyDescriptor.elementType` |
| P2 | Gramática + AST | Tokens, regras ANTLR, `MemberAccess` variants, `SemanticAstBuilder` extensions |
| P3 | Semântica | `SemanticResolver` com propagação de `CollectionType`, binding de `..<func>(...)` e novos erros |
| P4 | Execution Plan | `ExecutionPlanBuilder` + `ExecutableAccess` variants, incluindo função de collection |
| P5 | Avaliador core | Index, slice, wildcard, filter, agregação e função customizada em `AbstractObjectEvaluator` |
| P6 | Deep scan | `applyDeepScan` com BFS + detecção de ciclos |
| P7 | Testes | `CollectionNavigationTest` + suite de regressão |

---

## Verificação

### Testes de regressão (não devem quebrar)

- `api/ObjectNavigationTest`
- `api/ObjectNavigationCircularReferenceTest`
- `internal/runtime/ObjectNavigationPlanTest`
- `internal/runtime/NullMembershipTest`
- `api/AuditTrailExpressionTest`
- `internal/runtime/ConstantFoldingPlanTest`

### Novos cenários (`CollectionNavigationTest`)

```java
// Índice
store.book[0].title            → "Sayings of the Century"
store.book[-1].title           → "The Lord of the Rings"
store.book[0,1]                → lista com primeiros 2 livros

// Slice
store.book[:2]                 → 2 primeiros livros
store.book[-2:]                → 2 últimos livros

// Wildcard
store.book[*].author           → lista com 4 autores

// Filtro
store.book[?(@.isbn)].title                          → títulos com ISBN
store.book[?(@.price < 10)].title                    → títulos baratos
store.book[?(@.price < 10 and @.isbn)].title         → títulos baratos com ISBN
store..book[?(@.price <= store.expensive)]            → outer-scope ref
store.book[?(@.author =~ /.*REES/i)].title           → ["Sayings of the Century"]

// Deep scan
store..author                  → todos os autores
store..price                   → todos os preços

// Agregação
store.book[*].price..sum()     → 53.92
store..book..count()           → 4

// Função custom via ExpressionEnvironmentBuilder
store..price..customEval(2, true)   → equivale a customEval(store..price, 2, true)
store..price..normalize()..count()  → chaining continua se normalize() retornar collection

// Erros semânticos esperados
@.price (fora de filtro)       → INVALID_CURRENT_ELEMENT
store.expensive..sum()         → TYPE_MISMATCH (scalar, não collection)
store..price..round(2)         → UNKNOWN_COLLECTION_FUNCTION se não existir overload com 1º parâmetro collection
```

### Comandos

```shell
# Suite completa
mvn clean test -pl expression-evaluator

# Só os novos testes
mvn clean test -pl expression-evaluator -Dtest=CollectionNavigationTest

# Regenerar gramática após mudanças no .g4
mvn -pl expression-evaluator -Pantlr-generate generate-sources
```
