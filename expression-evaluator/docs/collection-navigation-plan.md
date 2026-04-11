# Plano: Navegação em Collections e Maps (JSONPath-like)

## Contexto

O `expression-evaluator` já suporta navegação em objetos via dot-notation (`obj.prop`, `obj?.method(args)`). O objetivo é estender a gramática e o runtime para suportar navegação em collections e maps com sintaxe JSONPath, incluindo indexação (`[0]`), wildcards (`[*]`), deep scan (`..`), slices (`[0:2]`), filtros (`[?(@.price < 10)]` e `map[?(@.key =~ "^foo" and @.value.active)]`) e funções de collection (`..sum()`, `..customEval(2, true)`).

**Motivação:** Permitir expressões como `store.book[*].author`, `store..price`, `store.book[?(@.price < 10)].title`, `store.book[*].price..sum()`, `store..price..customEval(2, true)` e `store.bookByIsbn[?(@.value.price < 10)]` diretamente na linguagem de expressões.

---

## Sintaxe Suportada

### Operadores de Navegação

| Operador | Descrição |
|---|---|
| `@` | Elemento corrente em um predicado de filtro |
| `*` | Wildcard — qualquer propriedade ou índice |
| `..` | Deep scan — desce recursivamente no grafo a partir do nó atual |
| `.<name>` | Acesso a propriedade filha (existente) |
| `["<key>"]` | Acesso a chave literal de `Map<String, ?>` |
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
| `=~` / `!~` | Correspondência de regex reutilizando o formato atual da linguagem (`STRING`) |
| `and` / `or` | Combinação de múltiplas condições dentro do mesmo `?(...)` |

### Semântica Específica para `Map`

- Lookup de chave em mapa é sempre explícito via `map["foo"]`; `map.foo` nunca faz lookup por chave.
- `map["foo-bar"]` e `map["foo"]` permitem lookup por chaves literais em `Map<String, ?>`; se a chave não existir, o resultado é `null`.
- `map.*` e `map[*]` são equivalentes e retornam a collection de values do mapa.
- `map[?(@.key ... and @.value ...)]` filtra entradas do mapa, mas preserva o resultado como `Map<K,V>`.
- Dentro de filtros sobre mapa, `@.key` e `@.value` são aliases semânticos resolvidos contra slots dedicados do avaliador; não há wrapper por entrada.
- `map..keys()` retorna a collection de chaves; `map..values()` retorna a collection de values.
- Deep scan em mapas percorre apenas os values; chaves não entram no grafo navegável.
- `["a","b"]` em mapa não é suportado na primeira versão; múltiplas chaves literais no mesmo subscript geram erro semântico para manter o runtime simples e sem alocações extras.

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
    "bookByIsbn": {
      "0-553-21311-3": { "title": "Moby Dick", "isbn": "0-553-21311-3", "price": 8.99 },
      "0-395-19395-8": { "title": "The Lord of the Rings", "isbn": "0-395-19395-8", "price": 22.99 }
    },
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
| `store..book[?(@.author =~ "(?i).*REES")]` | Filtro com regex case-insensitive |
| `store.bookByIsbn["0-553-21311-3"].title` | Lookup por chave literal |
| `store.bookByIsbn[*].price` | Prices dos values do mapa |
| `store.bookByIsbn[?(@.key =~ "^0-553" and @.value.price < 10)]` | Filtra mapa por chave e valor, preservando `Map<K,V>` |
| `store.bookByIsbn..keys()` | Collection com as chaves do mapa |
| `store.bookByIsbn..values()..count()` | Cardinalidade dos values do mapa |
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
| `SemanticResolver.java` | Propagação de `CollectionType`/`MapType` |
| `ExecutionPlanBuilder.java` | Compilação dos novos steps |
| `FunctionCatalog.java` | Reuso da resolução de overloads para `..<func>(...)` |
| `FunctionDescriptor.java` | Binding da collection implícita como primeiro argumento |
| `ResolvedType.java` | Adição de `CollectionType`/`MapType` |
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
  MapProjectionKind.java           (novo enum: KEYS, VALUES)
  VectorAggregationKind.java       (novo enum: SUM, AVG, MIN, MAX, COUNT)
```

---

## Fase 2 — Sistema de Tipos: `CollectionType` e `MapType`

**Arquivos novos:** `com/runestone/expeval/types/CollectionType.java` e `com/runestone/expeval/types/MapType.java`

```java
public record CollectionType(ResolvedType elementType) implements ResolvedType {
    public CollectionType { Objects.requireNonNull(elementType); }
}

public record MapType(ResolvedType keyType, ResolvedType valueType) implements ResolvedType {
    public MapType {
        Objects.requireNonNull(keyType);
        Objects.requireNonNull(valueType);
    }
}
```

**Modificar `ResolvedType.java`:**
```java
public sealed interface ResolvedType
    permits ScalarType, UnknownType, VectorType, ObjectType, NullType, CollectionType, MapType {}
```

**Rationale:** `VectorType.INSTANCE` é um enum singleton (tipo não-parametrizado) que mantém sua semântica atual de "vetor sem tipo de elemento conhecido". `CollectionType` e `MapType` são os novos tipos parametrizados introduzidos quando os tipos internos são conhecidos. Mudar `VectorType` para parametrizado quebraria todos os `switch` exhaustivos existentes.

**Modificar `ResolvedTypes.merge`:**
- `CollectionType(A)` + `CollectionType(B)` → `CollectionType(merge(A, B))`
- `MapType(K1, V1)` + `MapType(K2, V2)` → `MapType(merge(K1, K2), merge(V1, V2))`
- `CollectionType` + `VectorType` → `VectorType.INSTANCE`
- demais combinações → `UnknownType.INSTANCE`

**Modificar `PropertyDescriptor.java`:** Adicionar `@Nullable ResolvedType elementType`.

**Modificar a resolução de tipos Java em um helper central (`JavaTypeResolver` ou equivalente):** Usar `java.lang.reflect.Type`/`ParameterizedType` para extrair:
- o tipo `E` de `Collection<E>` ou `List<E>`;
- os tipos `K` e `V` de `Map<K,V>`.

Se `E` ou `V` estiverem registrados via `registerTypeHint`, usar `ObjectType(...)`; caso contrário, usar `ResolvedTypes.fromJavaType(...)`. Para `Map<K,V>`, a descoberta gera `MapType(keyType, valueType)`.

Esse helper deve ser reutilizado em:
- `ExpressionEnvironmentBuilder.discoverTypeMetadata(...)`;
- descoberta de tipos de retorno e parâmetros de métodos;
- registro de funções no `FunctionCatalog`;
- metadados de `ExternalSymbolCatalog`.

**Filtro sobre mapa:** sem novo `ResolvedType` público para entry. O `SemanticResolver` cria uma metadata sintética apenas para o contexto de filtro de mapa, expondo `@.key` e `@.value` como propriedades válidas do current element.

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
```

> **Nota sobre `COLON_OP`:** O fragment `Colon` usado em `TIME` e `DATETIME` não é afetado — esses tokens têm prioridade por aparecerem antes. `COLON_OP` só é reconhecido dentro de `[...]`, contexto onde `TIME`/`DATETIME` nunca ocorrem.

> **Nota sobre `DOUBLE_PERIOD`:** Deve aparecer **antes** de `PERIOD` no lexer para que `..` não seja lexado como dois pontos separados.

### Regex em filtros

Reutiliza exatamente o regex já suportado pela linguagem: o operando direito de `=~` / `!~` continua sendo um `STRING`.

Flags devem ser expressas inline no próprio padrão Java regex, por exemplo `(?i)` para case-insensitive.

### Novas regras de subscript

```antlr
subscript
    : LBRACKET subscriptSpec (COMMA subscriptSpec)* RBRACKET
    ;

subscriptSpec
    : MULT                                               # wildcardSubscript
    | STRING                                             # stringKeySubscript
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
    | filterValue REGEX_MATCH STRING
    | filterValue REGEX_NOT_MATCH STRING
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

**Restrição semântica para subscripts de mapa:** `StringKeySubscript` só é válido como único `subscriptSpec` dentro de `[...]`. Exemplos:
- válido: `map["foo"]`
- inválido: `map["foo","bar"]`
- inválido: `map["foo", 0]`

### Novos alternatives em `memberChain`

```antlr
memberChain
    : DOUBLE_PERIOD MULT                                              # deepScanWildcard
    | DOUBLE_PERIOD IDENTIFIER                                        # deepScanProperty
    | DOUBLE_PERIOD IDENTIFIER LPAREN
          (allEntityTypes (COMMA allEntityTypes)*)?
      RPAREN                                                          # collectionFunctionAccess
    | PERIOD MULT                                                     # childWildcardAccess
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
    CollectionIndexStep, MapKeyStep, CollectionSliceStep, WildcardStep,
    FilterPredicateStep, DeepScanStep, CollectionFunctionStep, MapProjectionStep, VectorAggregationStep {}

// [0], [-1], [0,1]
record CollectionIndexStep(List<ExpressionNode> indices) implements MemberAccess { ... }

// ["foo"]
record MapKeyStep(String key) implements MemberAccess { ... }

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

// ..keys(), ..values()
record MapProjectionStep(MapProjectionKind kind) implements MemberAccess { ... }

// ..sum(), ..avg(), ..count()
record VectorAggregationStep(VectorAggregationKind kind) implements MemberAccess { ... }
```

**Representação de `@` no AST:** `@.price`, `@.key` e `@.value` são construídos como `PropertyChainNode(rootIdentifier="@", chain=[PropertyAccess(...)])`. `@` sozinho vira `IdentifierNode(name="@")`. O sentinel `"@"` é reconhecido durante avaliação como "elemento corrente" — sem necessidade de novo `ExpressionNode`.

---

## Fase 5 — `SemanticAstBuilder`

**Arquivo:** `SemanticAstBuilder.java`

Adicionar ao `ExpressionVisitor`:

- `visitSubscriptAccess` → despacha por tipo de `subscriptSpec`:
  - `IndexSubscript` → `CollectionIndexStep(List.of(buildSignedIntNode(...)))`
  - `WildcardSubscript` → `WildcardStep()`
  - `StringKeySubscript` → `MapKeyStep(unquote(ctx.STRING().getText()))` quando for o único spec; caso contrário, erro semântico `INVALID_MAP_SUBSCRIPT`
  - `Slice*` → `CollectionSliceStep(start?, end?)`
  - `FilterSubscript` → `FilterPredicateStep(buildFilterPredicate(ctx))`
  - múltiplos specs inteiros → `CollectionIndexStep(listaDeIndices)`

- `visitDeepScanProperty` → `DeepScanStep("propertyName")`
- `visitDeepScanWildcard` → `DeepScanStep(null)`
- `visitChildWildcardAccess` → `WildcardStep()`
- `visitCollectionFunctionAccess` → `buildCollectionFunctionStep(name, args)`

**`buildFilterPredicate`:** Nova classe interna `FilterPredicateVisitor` que transforma o contexto ANTLR `filterPredicate` em `ExpressionNode` (usando `BinaryOperationNode` com `AND`/`OR`, `REGEX_MATCH`, etc.). `and` e `or` combinam `filterRelation`s dentro do mesmo `?()`, por exemplo `?(@.price < 10 and @.isbn)`.

**`buildCollectionFunctionStep`:**
```java
private PropertyChainNode.MemberAccess buildCollectionFunctionStep(String name, List<ExpressionNode> arguments) {
    if (isBuiltInMapProjection(name)) {
        return new PropertyChainNode.MapProjectionStep(resolveMapProjectionKind(name));
    }
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

**`resolveMapProjectionKind`:**
```java
static MapProjectionKind resolveMapProjectionKind(String name) {
    return switch (name.toLowerCase()) {
        case "keys"   -> KEYS;
        case "values" -> VALUES;
        default -> throw new IllegalArgumentException("unsupported built-in map projection: " + name);
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
| `MapKeyStep("foo")` | `MapType(K, V)` | `V` |
| `WildcardStep` em `MapType` | `MapType(K, V)` | `CollectionType(V)` |
| `FilterPredicateStep` em `MapType` | `MapType(K, V)` | `MapType(K, V)` |
| `MapProjectionStep(KEYS/VALUES)` | `MapType(K, V)` | `CollectionType(K)` / `CollectionType(V)` |
| `CollectionSliceStep` | `CollectionType(E)` / `VectorType` | mesmo que entrada |
| `WildcardStep` | `CollectionType(E)` | `CollectionType(E)` — ativa vector mode |
| `FilterPredicateStep` | `CollectionType(E)` | `CollectionType(E)` |
| `DeepScanStep(name)` | `ObjectType(C)` | `CollectionType(resolveProperty(name, C))` |
| `DeepScanStep(null)` | `ObjectType(C)` | `CollectionType(UnknownType)` |
| `CollectionFunctionStep(name, args)` | `CollectionType(E)` / `VectorType` | `descriptor.returnType()` |
| `VectorAggregationStep(COUNT)` | `CollectionType(E)` | `ScalarType.NUMBER` |
| `VectorAggregationStep(SUM/AVG/MIN/MAX)` | `CollectionType(E)` | `ScalarType.NUMBER` |
| `PropertyAccess` em vector mode | `CollectionType(E)` | `CollectionType(resolveProperty(prop, E))` |

**Resolução de predicados de filtro:**
- collection: resolver o sub-tree em sub-sessão com `"@"` resolvendo para o `elementType` da `CollectionType` corrente;
- map: resolver `@.key` e `@.value` como aliases especiais do contexto corrente, sem criar objetos intermediários por entry.

**Resolução de funções de collection:** `..<name>(arg1, arg2)` monta uma lista lógica de argumentos `[collectionAtual, arg1, arg2]` e procura candidatos no `FunctionCatalog` com aridade `argumentosExplícitos + 1`. Apenas descritores cujo primeiro parâmetro seja compatível com collection/vector participam da seleção. O binding final continua sendo `ResolvedFunctionBinding`, sem introduzir um catálogo paralelo.

**Built-ins de map:** `..keys()` e `..values()` são tratados como built-ins sobre `MapType`. `..keys()` retorna `CollectionType(K)` e `..values()` retorna `CollectionType(V)`. Não dependem de overloads do `FunctionCatalog` na primeira versão.

**Propagação após função customizada:** Se `descriptor.returnType()` for `CollectionType` ou `VectorType.INSTANCE`, a chain permanece em vector mode e pode continuar com novos steps. Se o retorno for escalar ou objeto, a chain volta ao modo escalar.

**Erros semânticos novos:**
- `INVALID_CURRENT_ELEMENT` — `@` fora de um predicado de filtro
- `INVALID_MEMBER_ACCESS` — `CollectionIndexStep`/`MapKeyStep`/`WildcardStep` em tipo incompatível
- `INVALID_MAP_PROPERTY_ACCESS` — `map.foo` é inválido; mapas só aceitam `["foo"]`, `[*]`, `.*`, filtro e `..keys()/..values()`
- `INVALID_MAP_SUBSCRIPT` — combinações como `["a","b"]` ou `["a", 0]`
- `TYPE_MISMATCH` — `VectorAggregationStep`/`CollectionFunctionStep` em tipo não-collection
- `UNKNOWN_COLLECTION_FUNCTION` — nenhuma função elegível encontrada após `..`
- `INCOMPATIBLE_COLLECTION_FUNCTION_ARGUMENTS` — overloads existem, mas não aceitam os argumentos após inserir a collection como argumento 0

---

## Fase 7 — `ExecutionPlanBuilder`

**Arquivo:** `ExecutionPlanBuilder.java`

Estender `buildPropertyChain`:

```
CollectionIndexStep   → ExecutableIndexAccess(indexNodes, single=true/false)
MapKeyStep            → ExecutableMapKeyAccess(key)
WildcardStep          → ExecutableWildcard()
CollectionSliceStep   → ExecutableSliceAccess(startNode?, endNode?)
FilterPredicateStep   → ExecutableFilterPredicate(buildNode(predicate))
DeepScanStep          → ExecutableDeepScan(propertyName?)
CollectionFunctionStep → ExecutableCollectionFunction(binding, explicitArgumentNodes, vectorResult)
Map built-ins (keys/values) → ExecutableMapProjection(KEYS/VALUES)
VectorAggregationStep → ExecutableVectorAggregation(kind)
```

Atualizar `countNodeEvents` para contabilizar events em predicados de filtro e expressões de índice.

Regex em filtros: reutilizar o fluxo atual de `REGEX_MATCH` / `REGEX_NOT_MATCH` com `STRING`, inclusive compilação via `Pattern.compile(unquote(...))`. Não há novo literal `/.../flags`; flags continuam inline no padrão, por exemplo `(?i).*REES`.

---

## Fase 8 — Novos `ExecutableAccess` em `ExecutablePropertyChain`

**Arquivo:** `ExecutablePropertyChain.java`

```java
sealed interface ExecutableAccess permits
    ExecutableFieldGet, ExecutableMethodInvoke,
    ReflectivePropertyAccess, ReflectiveMethodInvoke,
    ExecutableIndexAccess, ExecutableMapKeyAccess, ExecutableSliceAccess, ExecutableWildcard,
    ExecutableFilterPredicate, ExecutableDeepScan,
    ExecutableCollectionFunction, ExecutableMapProjection, ExecutableVectorAggregation {}

record ExecutableIndexAccess(List<ExecutableNode> indices, boolean single) implements ExecutableAccess {}
record ExecutableMapKeyAccess(String key) implements ExecutableAccess {}
record ExecutableSliceAccess(@Nullable ExecutableNode start, @Nullable ExecutableNode end) implements ExecutableAccess {}
record ExecutableWildcard() implements ExecutableAccess {}
record ExecutableFilterPredicate(ExecutableNode predicate) implements ExecutableAccess {}
record ExecutableDeepScan(@Nullable String propertyName) implements ExecutableAccess {}
record ExecutableCollectionFunction(
    ResolvedFunctionBinding binding,
    List<ExecutableNode> arguments,
    boolean vectorResult
) implements ExecutableAccess {}
record ExecutableMapProjection(MapProjectionKind kind) implements ExecutableAccess {}
record ExecutableVectorAggregation(VectorAggregationKind kind) implements ExecutableAccess {}
```

---

## Fase 9 — `AbstractObjectEvaluator` (Runtime)

**Arquivo:** `AbstractObjectEvaluator.java`

### Campos temporários para filtros

```java
// Seguro: AbstractObjectEvaluator é instanciado por-avaliação, não compartilhado entre threads
@Nullable private Object currentFilterElement;
@Nullable private Object currentMapFilterKey;
@Nullable private Object currentMapFilterValue;
```

### Lógica de avaliação em `evaluatePropertyChain`

Substituir `boolean vectorMode` por um estado explícito de navegação:

```java
enum NavigationMode { SCALAR, COLLECTION, MAP }
```

Isso evita ambiguidades entre coleção e mapa após wildcard, filtro e built-ins.

Switch planejado:

```java
case ExecutableWildcard ignored -> {
    if (current instanceof Map<?, ?> map) {
        mode = NavigationMode.COLLECTION;
        yield new ArrayList<>(map.values());
    }
    mode = NavigationMode.COLLECTION;
    yield toList(current);
}
case ExecutableMapKeyAccess key -> {
    mode = NavigationMode.SCALAR;
    yield applyMapKey(current, key.key());
}
case ExecutableIndexAccess idx -> {
    if (mode == NavigationMode.COLLECTION) yield mapSingleIndex(current, idx, scope);
    yield applyIndex(current, idx, scope);
}
case ExecutableSliceAccess slice -> {
    mode = NavigationMode.COLLECTION;
    yield applySlice(current, slice, scope);
}
case ExecutableFilterPredicate filter -> {
    if (current instanceof Map<?, ?>) {
        mode = NavigationMode.MAP;
        yield applyMapFilter(current, filter, scope);
    }
    mode = NavigationMode.COLLECTION;
    yield applyFilter(current, filter, scope);
}
case ExecutableDeepScan scan -> {
    mode = NavigationMode.COLLECTION;
    yield applyDeepScan(current, scan);
}
case ExecutableCollectionFunction function -> {
    mode = function.vectorResult() ? NavigationMode.COLLECTION : NavigationMode.SCALAR;
    yield applyCollectionFunction(current, function, scope);
}
case ExecutableMapProjection projection -> {
    mode = NavigationMode.COLLECTION;
    yield applyMapProjection(current, projection);
}
case ExecutableVectorAggregation agg -> {
    mode = NavigationMode.SCALAR;
    yield applyAggregation(current, agg);
}
// Em collection mode, accesses existentes mapeiam elemento a elemento:
case ExecutableFieldGet fieldGet when mode == NavigationMode.COLLECTION ->
    mapElements(current, el -> invokeGetter(el, fieldGet));
case ReflectivePropertyAccess rpa when mode == NavigationMode.COLLECTION ->
    mapElements(current, el -> resolvePropertyReflective(source, el, rpa.name()));
```

Reconhecer `"@"`, `@.key` e `@.value` em `evaluateExpr`:
```java
case IdentifierNode id when "@".equals(id.name()) -> currentFilterElement;
// PropertyChainNode com rootIdentifier="@":
// - em filtro de collection: root = currentFilterElement
// - em filtro de map: "@.key" e "@.value" leem slots dedicados currentMapFilterKey/currentMapFilterValue
```

### Helpers principais

**`applyIndex`:** `((BigDecimal) idx).intValueExact()` para índice. Negativo: `size + i`. Out-of-bounds → `null` (não lança exceção).

**`applySlice`:** `list.subList(start, end)` → `new ArrayList<>(subList)` (mutável para steps seguintes).

**`applyMapKey`:** lookup direto em `Map`. Chave ausente → `null` para manter compatibilidade com `??`.

**`applyMapProjection`:**
- `KEYS` → `new ArrayList<>(map.keySet())`
- `VALUES` → `new ArrayList<>(map.values())`

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

Para `Map`, o filtro usa implementação separada para preservar shape e sem alocação por entry:

```java
private Map<Object, Object> applyMapFilter(Object current, ExecutableFilterPredicate filter, ExecutionScope scope) {
    Map<?, ?> map = asMap(current);
    Map<Object, Object> result = new LinkedHashMap<>(map.size());
    for (Map.Entry<?, ?> entry : map.entrySet()) {
        currentMapFilterKey = entry.getKey();
        currentMapFilterValue = entry.getValue();
        Object test = evaluateExpr(filter.predicate(), scope);
        if (isTruthy(test)) result.put(entry.getKey(), entry.getValue());
    }
    currentMapFilterKey = null;
    currentMapFilterValue = null;
    return result;
}
```

`currentMapFilterKey` / `currentMapFilterValue` são campos temporários do avaliador, evitando alocação de wrapper por entrada.

**`applyDeepScan`:** BFS iterativo com `ArrayDeque` + conjunto por identidade real para detecção de ciclos:

```java
private List<Object> applyDeepScan(Object root, ExecutableDeepScan scan) {
    List<Object> results = new ArrayList<>();
    Deque<Object> queue = new ArrayDeque<>();
    Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    queue.add(root);
    while (!queue.isEmpty()) {
        Object node = queue.poll();
        if (node == null) continue;
        if (isContainerOrObject(node) && !visited.add(node)) continue;
        if (node instanceof List<?> list) {
            if (scan.propertyName() == null) results.addAll(list);
            queue.addAll(list);
            continue;
        }
        if (node instanceof Map<?, ?> map) {
            if (scan.propertyName() == null) results.addAll(map.values());
            queue.addAll(map.values());
            continue;
        }
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

Para mapas vale a mesma regra: `store.bookByIsbn[?(@.value.price <= store.expensive)]` continua enxergando `store.expensive` no escopo externo.

---

## Considerações de Performance (GC)

| Técnica | Local |
|---|---|
| `ArrayList` pré-alocado com `list.size()` | Todos os helpers que retornam listas |
| `currentFilterElement` / `currentMapFilterKey` / `currentMapFilterValue` como campos de instância | filtros — zero alocação por elemento/entry |
| `ArrayDeque` para BFS | `applyDeepScan` — mais eficiente que `LinkedList` |
| `Collections.newSetFromMap(new IdentityHashMap<>())` | `applyDeepScan` — corrige colisões sem alocação por nó além do necessário |
| Regex compilado em `ExecutionPlanBuilder` | Uma vez, na compilação |
| `intValueExact()` para índices | `applyIndex` — evita perda silenciosa de `intValue()` |
| `MethodHandle` precompilado para typed path e function catalog | Existente — mantido nos novos steps |

---

## Estrutura Final de Pacotes

```
internal.navigation/
  TypeIntrospectionSupport.java    (movido de internal.runtime)
  ReflectiveAccessCache.java       (movido de internal.runtime)
  MapProjectionKind.java           (novo enum)
  VectorAggregationKind.java       (novo enum)

types/
  CollectionType.java              (novo)
  MapType.java                     (novo)
  ResolvedType.java                (modificado: +CollectionType, +MapType)
  ResolvedTypes.java               (modificado: merge com CollectionType/MapType)

catalog/
  PropertyDescriptor.java          (modificado: +elementType)

internal/ast/
  PropertyChainNode.java           (modificado: +9 MemberAccess variants)

internal/runtime/
  ExecutablePropertyChain.java     (modificado: +9 ExecutableAccess variants)
  AbstractObjectEvaluator.java     (modificado: currentFilterElement + helpers)
  SemanticResolver.java            (modificado: CollectionType/MapType propagation)
  ExecutionPlanBuilder.java        (modificado: compilação dos novos steps)
  SemanticAstBuilder.java          (modificado: visitor para novos contextos ANTLR)
```

---

## Entrega em Fases (PRs incrementais)

| # | PR | Conteúdo |
|---|---|---|
| P1 | Reorganização | Mover `TypeIntrospectionSupport`/`ReflectiveAccessCache`; criar `internal.navigation`; `CollectionType`; `MapType`; `PropertyDescriptor.elementType` |
| P2 | Gramática + AST | Tokens, regras ANTLR, `MemberAccess` variants, `SemanticAstBuilder` extensions |
| P3 | Semântica | `SemanticResolver` com propagação de `CollectionType`/`MapType`, binding de `..<func>(...)`, built-ins de map e novos erros |
| P4 | Execution Plan | `ExecutionPlanBuilder` + `ExecutableAccess` variants, incluindo função de collection e projeções de map |
| P5 | Avaliador core | Index, map-key, slice, wildcard, filter, agregação e função customizada em `AbstractObjectEvaluator` |
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
store.bookByIsbn.*.price       → lista com os prices dos values do mapa

// Filtro
store.book[?(@.isbn)].title                          → títulos com ISBN
store.book[?(@.price < 10)].title                    → títulos baratos
store.book[?(@.price < 10 and @.isbn)].title         → títulos baratos com ISBN
store..book[?(@.price <= store.expensive)]            → outer-scope ref
store.book[?(@.author =~ "(?i).*REES")].title        → ["Sayings of the Century"]
store.bookByIsbn[?(@.key =~ "^0-553")][*].title      → títulos cujas chaves começam com 0-553
store.bookByIsbn[?(@.value.price < 10)]              → mapa filtrado, preservando chaves
store.bookByIsbn..keys()                             → collection com ISBNs
store.bookByIsbn..values()..count()                  → 2
store.bookByIsbn["0-553-21311-3","0-395-19395-8"]    → INVALID_MAP_SUBSCRIPT

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
store.bookByIsbn.foo           → INVALID_MAP_PROPERTY_ACCESS
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
