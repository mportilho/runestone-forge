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
| `[start:end]` | Slice — da posição `start` (inclusive) até `end` (exclusive) |
| `[?(<expr>)]` | Filtro — mantém elementos onde `<expr>` é verdadeiro |

### Funções de Collection/Map (sufixo `..`)

Invocadas ao final de um path; recebem implicitamente a collection ou o mapa acumulado como primeiro argumento lógico:

| Função | Saída |
|---|---|
| `..sum()` | Soma de uma lista de números — `BigDecimal` |
| `..avg()` | Média — `BigDecimal` |
| `..min()` | Mínimo — `BigDecimal` |
| `..max()` | Máximo — `BigDecimal` |
| `..count()` / `..length()` / `..size()` | Cardinalidade — `BigDecimal` |
| `..<func>(arg1, arg2, ...)` | Chama função do `FunctionCatalog` como `func(<target>, arg1, arg2, ...)`, onde `<target>` pode ser collection ou map |

**Convenção para funções customizadas via `ExpressionEnvironmentBuilder`:**

- `registerStaticProvider(...)` e `registerInstanceProvider(...)` continuam descobrindo funções normalmente; não é necessária nova API pública.
- Em `store..price..customEval(2, true)`, a resolução trata a chamada como `customEval(store..price, 2, true)`.
- Em `store.bookByIsbn..customEval(true)`, a resolução trata a chamada como `customEval(store.bookByIsbn, true)`.
- O primeiro parâmetro declarado da função customizada, após o eventual `MathContext` injetado pelo builder, deve ser o target corrente: `Collection`, `List`, `Map` ou equivalente coerível.
- Funções cujo primeiro parâmetro não seja compatível com o target corrente continuam disponíveis apenas como chamadas globais normais, por exemplo `customEval(store..price, 2, true)`.

### Operadores de Filtro Adicionais (dentro de `[?(...)]`)

| Operador | Descrição |
|---|---|
| `in` | Esquerda existe na direita (direita deve ser coleção) |
| `nin` | Esquerda não existe na direita |
| `=~` / `!~` | Correspondência de regex reutilizando o formato atual da linguagem (`STRING`) |
| `and` / `or` | Combinação de múltiplas condições dentro do mesmo `?(...)` |

### Referências Externas em Predicados de Filtro

Dentro de `[?(...)]`, referências ao escopo externo são restritas a **dot-notation simples** — apenas identificadores separados por ponto, sem subscripts, wildcards, chamadas de método ou safe-nav. A restrição é imposta pela própria gramática (não por validação semântica):

- **Válido:** `store.expensive`, `config.maxPrice`, `someVar`, `a.b.c`
- **Inválido (parse error):** `store.book[0]`, `store.*`, `store..price`, `store.book[?(...)]`, `obj?.prop`

### Semântica de Deep Scan (`..`)

O deep scan (`..name` ou `..*`) percorre o grafo via BFS e coleta tanto nós intermediários (listas, mapas, objetos) quanto folhas escalares. Regras de desduplicação:

- **Objetos, listas e mapas:** desduplicados por identidade de referência (`IdentityHashMap`) — um objeto instanciado uma única vez aparece uma única vez nos resultados, mesmo que referenciado em múltiplos nós do grafo.
- **Valores escalares (String, BigDecimal, Boolean, etc.):** sem desduplicação — dois elementos com o mesmo valor escalar produzem duas entradas. Isso é o comportamento esperado: `store..price` com dois livros a `8.99` retorna `[8.99, 8.99]`.

### Semântica de `in` / `nin`

- `left in right` retorna `true` se a coleção `right` contém `left`; usa `equals` elemento a elemento.
- `left nin right` é a negação: `true` se `left` não está em `right`.
- Se `right` resolver para `null` ou tipo não-coleção em runtime: `in` → `false`; `nin` → `true`.
- O `SemanticResolver` emite erro `INCOMPATIBLE_IN_OPERANDS` quando o tipo de `right` for provadamente não-coleção em tempo de compilação.

### Semântica Específica para `Map`

- Lookup de chave em mapa é sempre explícito via `map["foo"]`; `map.foo` nunca faz lookup por chave.
- `map["foo-bar"]` e `map["foo"]` permitem lookup por chaves literais em `Map<String, ?>`; se a chave não existir, o resultado é `null`.
- `map.*` e `map[*]` são equivalentes e retornam a collection de values do mapa.
- `map[?(@.key ... and @.value ...)]` filtra entradas do mapa, mas preserva o resultado como `Map<K,V>`.
- Dentro de filtros sobre mapa, `@.key` e `@.value` são aliases semânticos resolvidos contra slots dedicados do avaliador.
- `map..keys()` retorna a collection de chaves; `map..values()` retorna a collection de values.
- Deep scan em mapas percorre apenas os values; chaves não entram no grafo navegável.

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
    "expensive": 10,
    "validCategories": ["reference", "fiction"]
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
| `store..book[:2]` | Livros de índice 0 até 1 |
| `store..book[1:2]` | Livro de índice 1 |
| `store..book[-2:]` | Últimos dois livros |
| `store..book[2:]` | Do índice 2 ao final |
| `store..book[?(@.isbn)]` | Livros com ISBN |
| `store.book[?(@.price < 10)]` | Livros mais baratos que 10 |
| `store.book[?(@.price < 10 and @.isbn)]` | Múltiplas condições no mesmo filtro |
| `store..book[?(@.price <= store.expensive)]` | Filtro com referência ao escopo externo |
| `store..book[?(@.author =~ "(?i).*REES")]` | Filtro com regex case-insensitive |
| `store.book[?(@.category in store.validCategories)]` | Filtro com `in` referenciando coleção externa |
| `store.book[?(@.category nin store.validCategories)]` | Filtro com `nin` |
| `store.bookByIsbn["0-553-21311-3"].title` | Lookup por chave literal |
| `store.bookByIsbn[*].price` | Prices dos values do mapa |
| `store.bookByIsbn[?(@.key =~ "^0-553" and @.value.price < 10)]` | Filtra mapa por chave e valor, preservando `Map<K,V>` |
| `store.bookByIsbn..keys()` | Collection com as chaves do mapa |
| `store.bookByIsbn..values()..count()` | Cardinalidade dos values do mapa |
| `store.bookByIsbn..customEval(true)` | Equivale a `customEval(store.bookByIsbn, true)` |
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
| `AbstractObjectEvaluator.java` | Novos helpers de avaliação + `FilterContext` stack |
| `SemanticAstBuilder.java` | Mapeamento dos novos contextos ANTLR |
| `SemanticResolver.java` | Propagação de `CollectionType`/`MapType` |
| `ExecutionPlanBuilder.java` | Compilação dos novos steps |
| `FunctionCatalog.java` | Reuso da resolução de overloads para `..<func>(...)` |
| `FunctionDescriptor.java` | Binding da collection implícita como primeiro argumento |
| `ResolvedType.java` | Adição de `CollectionType`/`MapType` |
| `PropertyDescriptor.java` | Campo `elementType` |
| `ExpressionEnvironmentBuilder.java` | Introspecção de generic type parameters + contrato do primeiro parâmetro collection/map |
| `NavigationMode.java` | Novo enum compartilhado (compilação + runtime) |
| `FilterContext.java` | Novo record para contexto de filtro aninhado |

---

## Fase 1 — Novo Pacote `internal.navigation`

**Objetivo:** Reorganizar as classes de introspecção/reflexão num sub-módulo interno dedicado e adicionar os tipos de suporte à navegação em collections.

### Mover (sem mudança de lógica, apenas de pacote)

- `internal.runtime.TypeIntrospectionSupport` → `internal.navigation.TypeIntrospectionSupport`
- `internal.runtime.ReflectiveAccessCache` → `internal.navigation.ReflectiveAccessCache`

Atualizar imports em: `AbstractObjectEvaluator`, `ExecutionPlanBuilder`, `ExpressionEnvironmentBuilder`.

> **Nota sobre `ReflectiveAccessCache`:** O cache usa `ConcurrentHashMap` sem limite de tamanho. Como armazena metadados de reflexão por `Class<?>` e o conjunto de classes em uso numa aplicação é efetivamente estático em runtime normal, isso é aceitável. Documentar no Javadoc da classe como limitação conhecida: ambientes com classloaders customizados (OSGi, hot-reload) podem observar retenção de classes.

### Criar no novo pacote

```
com.runestone.expeval.internal.navigation/
  TypeIntrospectionSupport.java    (movido)
  ReflectiveAccessCache.java       (movido)
  MapProjectionKind.java           (novo enum: KEYS, VALUES)
  VectorAggregationKind.java       (novo enum: SUM, AVG, MIN, MAX, COUNT)
  NavigationMode.java              (novo enum: SCALAR, COLLECTION, MAP)
  FilterContext.java               (novo record)
```

**`NavigationMode`** é o tipo único compartilhado entre compilação (`ExecutableCollectionFunction.resultMode`) e runtime (`AbstractObjectEvaluator.evaluatePropertyChain`). Elimina a duplicação de dois enums com valores idênticos.

**`FilterContext`** encapsula o estado corrente de um predicado de filtro, permitindo filtros aninhados via pilha:

```java
public record FilterContext(
    @Nullable Object element,    // elemento corrente em filtro de collection
    @Nullable Object mapKey,     // chave corrente em filtro de map
    @Nullable Object mapValue    // value corrente em filtro de map
) {
    /** Contexto para filtro de collection: somente element é definido. */
    public static FilterContext ofElement(Object element) {
        return new FilterContext(element, null, null);
    }

    /** Contexto para filtro de map: somente mapKey e mapValue são definidos. */
    public static FilterContext ofMapEntry(Object key, Object value) {
        return new FilterContext(null, key, value);
    }

    public boolean isMapContext() { return mapKey != null; }
}
```

### Testes mínimos do P1

- Verificar que imports em `AbstractObjectEvaluator`, `ExecutionPlanBuilder` e `ExpressionEnvironmentBuilder` compilam após a movimentação.
- Suite de regressão completa deve passar sem alteração de comportamento.

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

**Contrato adicional para funções de collection/map:** Nenhuma mudança de API pública no builder. A extensão é semântica: `path..fn(a, b)` passa a ser elegível quando existir um `FunctionDescriptor` compatível com `fn(path, a, b)`, isto é, com primeiro parâmetro compatível com collection/vector/map após a eventual injeção de `MathContext`.

### Testes mínimos do P2 (sistema de tipos)

- `CollectionType` e `MapType` constróem, igualam e fazem hash corretamente.
- `ResolvedTypes.merge` cobre todos os novos pares.
- `JavaTypeResolver` extrai `E` de `List<Book>`, `K`/`V` de `Map<String, Book>`, e `UnknownType` para raw types.

---

## Fase 3 — Gramática (ANTLR)

**Arquivo:** `ExpressionEvaluator.g4`

### Breaking change: token `TIME`

O token `TIME` passa a exigir formato `HH:MM:SS` com exatamente dois `:` — mínimo de 6 dígitos:

```antlr
TIME : [0-9]{2} ':' [0-9]{2} ':' [0-9]{2} ;
```

**Motivação:** Com `TIME` permissivo (ex.: `HH:MM`), o ANTLR poderia consumir `0:2` em `[0:2]` como `TIME` em vez de `POSITIVE COLON_OP POSITIVE`, quebrando slices silenciosamente. O formato `HH:MM:SS` nunca compete com o contexto de slice.

**Breaking change:** literais `TIME` no formato `HH:MM` (sem segundos) passam a ser erro de parse. Documentar no changelog. O token `DATETIME` deve ser revisado no mesmo PR para garantir consistência com o novo formato de `TIME`.

**Teste de regressão obrigatório no P3:** verificar que `HH:MM` sem segundos gera `ParseException` explícita.

### Novos tokens lexer

Inserir na ordem indicada (prioridade ANTLR: primeiro match vence em empate de tamanho):

```antlr
// Antes de PERIOD — impede que '..' seja lexado como dois PERIOD separados
DOUBLE_PERIOD : '..' ;

// Após TIME e DATETIME — ':' isolado só alcança COLON_OP quando TIME não faz match
COLON_OP : ':' ;

// Antes de IDENTIFIER — keywords do filtro devem ter prioridade sobre identificadores
IN       : 'in'  ;
NIN      : 'nin' ;

QUESTION : '?' ;
AT       : '@'  ;
```

> **Nota sobre `COLON_OP`:** Com `TIME` restrito a `HH:MM:SS`, o `:` em `[0:2]` nunca compete com `TIME`. `COLON_OP` é declarado após `TIME` e `DATETIME` para que aqueles tenham prioridade, mas dentro de `[...]` o contexto nunca gera `TIME`.

> **Nota sobre `IN` / `NIN`:** Devem ser declarados antes de `IDENTIFIER` para que o lexer os reconheça como keywords. Como só aparecem dentro de `filterRelation` (contexto `[?(...)]`), não há risco de conflito com identificadores externos.

### Regras de subscript (sem multi-índice)

Cada alternative é independente — sem repetição por vírgula:

```antlr
subscript
    : LBRACKET MULT RBRACKET                                              # wildcardSubscript
    | LBRACKET STRING RBRACKET                                            # stringKeySubscript
    | LBRACKET signedInteger COLON_OP signedInteger? RBRACKET             # sliceWithStartSubscript
    | LBRACKET COLON_OP signedInteger RBRACKET                            # sliceToEndSubscript
    | LBRACKET signedInteger RBRACKET                                     # indexSubscript
    | LBRACKET QUESTION LPAREN filterPredicate RPAREN RBRACKET            # filterSubscript
    ;

signedInteger : MINUS? POSITIVE ;
```

> **Motivação para flat alternatives:** Combinações mistas como `["a","b"]` ou `[0, ?(...)]` são inválidas por construção gramatical, sem necessidade de validação semântica adicional. O parser emite erro de sintaxe diretamente.

> **Nomeação:** `sliceWithStartSubscript` indica que o start está presente (≥1 dígito antes de `:`). `sliceToEndSubscript` indica que apenas o end está presente (`:n`). `[:]` não corresponde a nenhum alternative e também gera erro de parse.

> **Restrição semântica para subscripts de mapa:** `StringKeySubscript` é o único subscript válido para mapa com chave literal. Qualquer outro subscript em `MapType` gera erro semântico (não gramatical), pois a gramática é type-agnóstica.

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

### Predicados de filtro

```antlr
filterPredicate : filterAtom ((AND | OR) filterAtom)* ;

filterAtom
    : LPAREN filterPredicate RPAREN
    | filterRelation
    ;

filterRelation
    : filterValue comparisonOperator filterValue
    | filterValue REGEX_MATCH STRING
    | filterValue REGEX_NOT_MATCH STRING
    | filterValue IN  filterValue
    | filterValue NIN filterValue
    | filterValue                               // truthy check: @.isbn (non-null)
    ;

filterValue
    : AT memberChain*                           // @.price, @.isbn, @ sozinho
    | IDENTIFIER (PERIOD IDENTIFIER)*           // referência externa simples: store.expensive
    | numericEntity
    | stringConcatExpression
    | NULL
    ;
```

`comparisonOperator` é uma nova regra auxiliar que agrupa `GT`, `GEQ`, `LT`, `LEQ`, `EQUAL`, `NOT_EQUAL`.

**`IDENTIFIER (PERIOD IDENTIFIER)*` em `filterValue`:** restringe referências externas a dot-notation simples — sem subscripts, wildcards, chamadas de método ou safe-nav. A restrição é gramatical: qualquer tentativa de usar `[...]` ou `?.` gera parse error com mensagem clara.

**Regex em filtros:** o operando direito de `=~` / `!~` é um `STRING`. Flags inline no padrão Java regex (ex.: `(?i)` para case-insensitive). Compilado em `ExecutionPlanBuilder` uma única vez.

**`in` / `nin` — lado direito:** qualquer `filterValue` válido. O `SemanticResolver` verifica que o lado direito resolve para `CollectionType` quando o tipo for determinável; caso contrário, deixa para validação em runtime.

### Testes mínimos do P3 (gramática)

- Parse de cada novo `subscriptSpec` isolado: `[*]`, `["foo"]`, `[2]`, `[-1]`, `[1:3]`, `[:2]`, `[2:]`, `[?(@.isbn)]`.
- Parse de `filterRelation` com `<`, `>`, `=~`, `in`, `nin`, truthy check.
- Parse de `filterValue` com `@.price`, `store.expensive`, `numericEntity`, `NULL`.
- `[0:2]` tokenizado corretamente como `POSITIVE COLON_OP POSITIVE` (não como `TIME`).
- `HH:MM` sem segundos gera `ParseException`.
- Expressões existentes de `TIME` (`HH:MM:SS`) continuam parseando.

---

## Fase 4 — Novos `MemberAccess` em `PropertyChainNode`

**Arquivo:** `PropertyChainNode.java`

Adicionar ao `sealed interface MemberAccess` como inner records (padrão existente):

```java
public sealed interface MemberAccess permits
    PropertyAccess, SafePropertyAccess, MethodCallAccess, SafeMethodCallAccess,
    CollectionIndexStep, MapKeyStep, CollectionSliceStep, WildcardStep,
    FilterPredicateStep, DeepScanStep, CollectionFunctionStep, MapProjectionStep, VectorAggregationStep {}

// [0], [-1]
record CollectionIndexStep(ExpressionNode index) implements MemberAccess { ... }

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

- `visitSubscriptAccess` → despacha por tipo de `subscript`:
  - `indexSubscript` → `CollectionIndexStep(buildSignedIntNode(ctx))`
  - `wildcardSubscript` → `WildcardStep()`
  - `stringKeySubscript` → `MapKeyStep(unquote(ctx.STRING().getText()))`
  - `sliceWithStartSubscript` → `CollectionSliceStep(start, end?)` onde `end` é `null` se ausente
  - `sliceToEndSubscript` → `CollectionSliceStep(null, end)`
  - `filterSubscript` → `FilterPredicateStep(buildFilterPredicate(ctx))`

- `visitDeepScanProperty` → `DeepScanStep("propertyName")`
- `visitDeepScanWildcard` → `DeepScanStep(null)`
- `visitChildWildcardAccess` → `WildcardStep()`
- `visitCollectionFunctionAccess` → `buildCollectionFunctionStep(name, args)`

**`buildFilterPredicate`:** Nova classe interna `FilterPredicateVisitor` que transforma o contexto ANTLR `filterPredicate` em `ExpressionNode`. Operações:
- `filterRelation` com operador de comparação → `BinaryOperationNode(op, buildFilterValue(left), buildFilterValue(right))`
- `filterRelation` com `=~` → `BinaryOperationNode(REGEX_MATCH, ...)`
- `filterRelation` com `!~` → `BinaryOperationNode(REGEX_NOT_MATCH, ...)`
- `filterRelation` com `in` → `BinaryOperationNode(IN, buildFilterValue(left), buildFilterValue(right))`
- `filterRelation` com `nin` → `BinaryOperationNode(NIN, buildFilterValue(left), buildFilterValue(right))`
- `filterRelation` truthy (só `filterValue`) → `buildFilterValue(ctx)`
- `filterAtom` com parens → recursão em `filterPredicate`
- `AND` / `OR` → `BinaryOperationNode(AND/OR, ...)`

**`buildFilterValue`:** Despacha por tipo de alternative:
- `AT memberChain*` → `PropertyChainNode(rootIdentifier="@", chain=[...])` ou `IdentifierNode("@")` se sem chain
- `IDENTIFIER (PERIOD IDENTIFIER)*` → `PropertyChainNode` convencional com `rootIdentifier=first IDENTIFIER` e `chain=[PropertyAccess(name)...]` — reutiliza o caminho de compilação existente
- `numericEntity`, `stringConcatExpression`, `NULL` → construtores existentes

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

### Testes mínimos do P4/P5 (AST)

- Cada `subscriptSpec` produz o `MemberAccess` correto (incluindo `null` nos campos opcionais de slice).
- `@.price` em `filterValue` → `PropertyChainNode(rootIdentifier="@", chain=[PropertyAccess("price")])`.
- `store.expensive` em `filterValue` → `PropertyChainNode` convencional (mesma forma que fora do filtro).
- `in` / `nin` → `BinaryOperationNode(IN/NIN, ...)` no predicado.

---

## Fase 6 — `SemanticResolver`

**Arquivo:** `SemanticResolver.java`

Estender `resolvePropertyChain` com propagação de tipo para os novos steps:

| Step | Tipo de entrada | Tipo de saída |
|---|---|---|
| `CollectionIndexStep` | `CollectionType(E)` | `E` |
| `MapKeyStep("foo")` | `MapType(K, V)` | `V` |
| `WildcardStep` em `MapType` | `MapType(K, V)` | `CollectionType(V)` |
| `FilterPredicateStep` em `MapType` | `MapType(K, V)` | `MapType(K, V)` |
| `MapProjectionStep(KEYS/VALUES)` | `MapType(K, V)` | `CollectionType(K)` / `CollectionType(V)` |
| `CollectionFunctionStep(name, args)` | `MapType(K, V)` | `descriptor.returnType()` |
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
- collection: resolver o sub-tree em sub-sessão com `"@"` resolvendo para o `elementType` da `CollectionType` corrente.
- map: resolver `@.key` e `@.value` como aliases especiais do contexto corrente.
- `IDENTIFIER (PERIOD IDENTIFIER)*` em `filterValue`: resolve exatamente como `PropertyChainNode` convencional — reutiliza `resolvePropertyChain` com root no escopo externo.

**Resolução de `in` / `nin` em predicados:**
- Lado esquerdo: qualquer `ResolvedType` (scalar ou unknown).
- Lado direito: deve ser `CollectionType`, `VectorType` ou `UnknownType`. Se for provadamente scalar ou `ObjectType` não-coleção, emitir `INCOMPATIBLE_IN_OPERANDS`.
- Tipo de retorno do predicado: `ScalarType.BOOLEAN`.

**Resolução de funções de collection/map:** `..<name>(arg1, arg2)` monta uma lista lógica de argumentos `[targetAtual, arg1, arg2]` e procura candidatos no `FunctionCatalog` com aridade `argumentosExplícitos + 1`. Apenas descritores cujo primeiro parâmetro seja compatível com o target corrente (`Collection`, `VectorType` ou `MapType`) participam da seleção. O binding final continua sendo `ResolvedFunctionBinding`, sem introduzir um catálogo paralelo.

**Built-ins de map:** `..keys()` e `..values()` são tratados como built-ins sobre `MapType`. `..keys()` retorna `CollectionType(K)` e `..values()` retorna `CollectionType(V)`. Têm prioridade sobre funções registradas com o mesmo nome.

**Propagação após função customizada:** Se `descriptor.returnType()` for `CollectionType` ou `VectorType.INSTANCE`, a chain permanece em collection mode. Se o retorno for `MapType`, volta ao modo map. Se for escalar ou objeto, volta ao modo escalar.

**Erros semânticos novos:**
- `INVALID_CURRENT_ELEMENT` — `@` fora de um predicado de filtro
- `INVALID_MEMBER_ACCESS` — `CollectionIndexStep`/`MapKeyStep`/`WildcardStep` em tipo incompatível
- `INVALID_MAP_PROPERTY_ACCESS` — `map.foo` é inválido; mapas só aceitam `["foo"]`, `[*]`, `.*`, filtro e `..keys()/..values()`
- `TYPE_MISMATCH` — `VectorAggregationStep` em tipo não-collection
- `UNKNOWN_COLLECTION_FUNCTION` — nenhuma função elegível encontrada após `..`
- `INCOMPATIBLE_COLLECTION_FUNCTION_ARGUMENTS` — overloads existem, mas não aceitam os argumentos após inserir o target corrente como argumento 0
- `INCOMPATIBLE_IN_OPERANDS` — lado direito de `in`/`nin` provadamente não é coleção

### Testes mínimos do P6 (semântica)

- Propagação de tipo para cada linha da tabela acima.
- `@` fora de filtro → `INVALID_CURRENT_ELEMENT`.
- `map.foo` → `INVALID_MAP_PROPERTY_ACCESS`.
- `in` com right-side scalar → `INCOMPATIBLE_IN_OPERANDS`.
- `store.expensive` em `filterValue` resolve corretamente contra escopo externo.

---

## Fase 7 — `ExecutionPlanBuilder`

**Arquivo:** `ExecutionPlanBuilder.java`

Estender `buildPropertyChain`:

```
CollectionIndexStep    → ExecutableIndexAccess(indexNode)
MapKeyStep             → ExecutableMapKeyAccess(key)
WildcardStep           → ExecutableWildcard()
CollectionSliceStep    → ExecutableSliceAccess(startNode?, endNode?)
FilterPredicateStep    → ExecutableFilterPredicate(buildNode(predicate))
DeepScanStep           → ExecutableDeepScan(propertyName?)
CollectionFunctionStep → ExecutableCollectionFunction(binding, explicitArgumentNodes, resultMode)
Map built-ins          → ExecutableMapProjection(KEYS/VALUES)
VectorAggregationStep  → ExecutableVectorAggregation(kind)
```

Atualizar `countNodeEvents` para contabilizar events em predicados de filtro e expressões de índice.

Regex em filtros: reutilizar o fluxo atual de `REGEX_MATCH` / `REGEX_NOT_MATCH` com `STRING`, inclusive compilação via `Pattern.compile(unquote(...))`. Flags continuam inline no padrão, por exemplo `(?i).*REES`.

`in` / `nin` em filtros: `BinaryOperationNode(IN/NIN, ...)` é compilado pelo caminho existente de `buildBinaryOp` — nenhum tratamento especial no `ExecutionPlanBuilder`.

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

record ExecutableIndexAccess(ExecutableNode index) implements ExecutableAccess {}
record ExecutableMapKeyAccess(String key) implements ExecutableAccess {}
record ExecutableSliceAccess(@Nullable ExecutableNode start, @Nullable ExecutableNode end) implements ExecutableAccess {}
record ExecutableWildcard() implements ExecutableAccess {}
record ExecutableFilterPredicate(ExecutableNode predicate) implements ExecutableAccess {}
record ExecutableDeepScan(@Nullable String propertyName) implements ExecutableAccess {}
record ExecutableCollectionFunction(
    ResolvedFunctionBinding binding,
    List<ExecutableNode> arguments,
    NavigationMode resultMode        // NavigationMode de internal.navigation — tipo único compartilhado
) implements ExecutableAccess {}
record ExecutableMapProjection(MapProjectionKind kind) implements ExecutableAccess {}
record ExecutableVectorAggregation(VectorAggregationKind kind) implements ExecutableAccess {}
```

> **Sem `ResultMode` separado:** `ExecutableCollectionFunction` usa diretamente `NavigationMode` de `internal.navigation`. Isso elimina a conversão `switch (resultMode) { case COLLECTION -> NavigationMode.COLLECTION; ... }` que existiria com dois enums distintos.

> **`ExecutableIndexAccess` sem `boolean single`:** multi-índice foi removido do escopo. O campo `index` é sempre um único `ExecutableNode`.

---

## Fase 9 — `AbstractObjectEvaluator` (Runtime)

**Arquivo:** `AbstractObjectEvaluator.java`

### Contexto de filtro aninhado

Substituir campos escalares por uma pilha de `FilterContext`:

```java
// Deque como pilha (push/pop): suporta filtros aninhados como [?(@.authors[?(@.name =~ ".*")])]
private final Deque<FilterContext> filterContextStack = new ArrayDeque<>();
```

A pilha é segura porque `AbstractObjectEvaluator` é instanciado por-avaliação e não é compartilhado entre threads. O `Deque` elimina tanto o problema de cleanup na exceção (via try-finally) quanto a limitação de filtros não-aninhados.

### Lógica de avaliação em `evaluatePropertyChain`

Substituir `boolean vectorMode` por um estado explícito de navegação:

```java
NavigationMode mode = NavigationMode.SCALAR;  // NavigationMode de internal.navigation
```

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
    mode = NavigationMode.SCALAR;
    yield applyIndex(current, idx.index(), scope);
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
    mode = function.resultMode();  // atribuição direta — mesmo tipo
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

Reconhecer `"@"` em `evaluateExpr`:

```java
// IdentifierNode("@") sozinho:
case IdentifierNode id when "@".equals(id.name()) -> {
    if (filterContextStack.isEmpty()) error(INVALID_CURRENT_ELEMENT);
    FilterContext ctx = filterContextStack.peek();
    yield ctx.isMapContext() ? ctx.mapValue() : ctx.element();
}
// PropertyChainNode com rootIdentifier="@":
// - em filtro de collection: root = ctx.element(), chain continua normalmente
// - em filtro de map:
//   - primeiro step PropertyAccess("key")   → ctx.mapKey()
//   - primeiro step PropertyAccess("value") → ctx.mapValue(), chain continua a partir do value
```

### Helpers principais

**`applyIndex`:** `((BigDecimal) evaluateExpr(idx, scope)).intValueExact()` para índice. Negativo: `size + i`. Out-of-bounds → `null`.

**`applySlice`:** `list.subList(start, end)` → `new ArrayList<>(subList)` (mutável para steps seguintes).

**`applyMapKey`:** lookup direto em `Map`. Chave ausente → `null` para manter compatibilidade com `??`.

**`applyMapProjection`:**
- `KEYS` → `new ArrayList<>(map.keySet())`
- `VALUES` → `new ArrayList<>(map.values())`

**`applyFilter`:**
```java
private List<Object> applyFilter(Object collection, ExecutableFilterPredicate filter, ExecutionScope scope) {
    List<?> list = asList(collection);
    List<Object> result = new ArrayList<>(list.size());
    for (Object element : list) {
        filterContextStack.push(FilterContext.ofElement(element));
        try {
            if (isTruthy(evaluateExpr(filter.predicate(), scope))) result.add(element);
        } finally {
            filterContextStack.pop();
        }
    }
    return result;
}
```

**`applyMapFilter`:**
```java
private Map<Object, Object> applyMapFilter(Object current, ExecutableFilterPredicate filter, ExecutionScope scope) {
    Map<?, ?> map = asMap(current);
    Map<Object, Object> result = new LinkedHashMap<>(map.size());
    for (Map.Entry<?, ?> entry : map.entrySet()) {
        filterContextStack.push(FilterContext.ofMapEntry(entry.getKey(), entry.getValue()));
        try {
            if (isTruthy(evaluateExpr(filter.predicate(), scope)))
                result.put(entry.getKey(), entry.getValue());
        } finally {
            filterContextStack.pop();
        }
    }
    return result;
}
```

**`applyDeepScan`:** BFS iterativo com `ArrayDeque` + conjunto por identidade real para detecção de ciclos:

```java
private List<Object> applyDeepScan(Object root, ExecutableDeepScan scan) {
    List<Object> results = new ArrayList<>();
    Deque<Object> queue = new ArrayDeque<>();
    // Deduplicação por identidade de referência para objetos/containers.
    // Scalars (String, BigDecimal, etc.) não são adicionados ao visited — múltiplas
    // ocorrências do mesmo valor escalar geram múltiplas entradas nos resultados.
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

> **`isContainerOrObject`:** retorna `true` para `List`, `Map` e POJOs (classes não-primitivas além de `String`, `Number`, `Boolean`, `Character`, `Enum`). Scalars ficam fora do visited para que dois valores escalares idênticos (ex.: dois livros com `price = 8.99`) gerem duas entradas distintas — comportamento esperado para deep scan de propriedades.

**`applyAggregation`:** Loop simples sobre a lista; sem `Stream` nas operações críticas (avaliar com JMH se necessário).

**`applyCollectionFunction`:**
```java
private Object applyCollectionFunction(Object current, ExecutableCollectionFunction function, ExecutionScope scope) {
    return invokeFunctionBinding(function.binding(), current, function.arguments(), scope);
}
```

**Helper compartilhado `invokeFunctionBinding` (requisito do P9):**

Extrair a lógica de coerção, invocação e tratamento de exceção de `evaluateFunctionCall` para um helper reutilizado tanto por chamadas globais quanto por `applyCollectionFunction`:

```java
private Object invokeFunctionBinding(
        ResolvedFunctionBinding binding,
        @Nullable Object implicitTarget,   // null para chamadas globais
        List<ExecutableNode> explicitArgs,
        ExecutionScope scope) {
    FunctionDescriptor descriptor = binding.descriptor();
    int offset = implicitTarget != null ? 1 : 0;
    Object[] args = new Object[descriptor.arity()];
    if (implicitTarget != null) {
        args[0] = runtimeServices.coerce(implicitTarget, descriptor.parameterTypes().getFirst());
    }
    for (int i = 0; i < explicitArgs.size(); i++) {
        Object value = evaluateExpr(explicitArgs.get(i), scope);
        args[i + offset] = runtimeServices.coerce(value, descriptor.parameterTypes().get(i + offset));
    }
    Object result = descriptor.invoke(args);
    return runtimeServices.coerceToResolvedType(result, binding.returnType());
}
```

**Avaliação de `in` / `nin`** (no switch de `evaluateExpr` para `BinaryOperationNode`):

```java
case IN -> {
    Object left  = evaluateExpr(node.left(), scope);
    Object right = evaluateExpr(node.right(), scope);
    yield right instanceof Collection<?> col ? col.contains(left) : Boolean.FALSE;
}
case NIN -> {
    Object left  = evaluateExpr(node.left(), scope);
    Object right = evaluateExpr(node.right(), scope);
    yield right instanceof Collection<?> col ? !col.contains(left) : Boolean.TRUE;
}
```

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

`store..book[?(@.price <= store.expensive)]` — `store.expensive` é um `PropertyChainNode` convencional que resolve contra o `scope` externo porque `evaluateExpr(filter.predicate(), scope)` passa o mesmo `scope`. Nenhum tratamento especial é necessário.

---

## Considerações de Performance (GC)

| Técnica | Local |
|---|---|
| `ArrayList` pré-alocado com `list.size()` | Todos os helpers que retornam listas |
| `Deque<FilterContext>` como pilha de instância | Filtros — zero alocação por elemento além do `FilterContext` por iteração; suporta aninhamento |
| `ArrayDeque` para BFS | `applyDeepScan` — mais eficiente que `LinkedList` |
| `Collections.newSetFromMap(new IdentityHashMap<>())` | `applyDeepScan` — deduplicação por identidade sem alocação extra por nó |
| Regex compilado em `ExecutionPlanBuilder` | Uma vez, na compilação |
| `intValueExact()` para índices | `applyIndex` — evita perda silenciosa de `intValue()` |
| `MethodHandle` precompilado para typed path e function catalog | Existente — mantido nos novos steps |
| `invokeFunctionBinding` sem alocação de lista intermediária | Helper compartilhado — `Object[]` pré-alocado com `descriptor.arity()` |

---

## Estrutura Final de Pacotes

```
internal.navigation/
  TypeIntrospectionSupport.java    (movido de internal.runtime)
  ReflectiveAccessCache.java       (movido de internal.runtime)
  MapProjectionKind.java           (novo enum)
  VectorAggregationKind.java       (novo enum)
  NavigationMode.java              (novo enum: SCALAR, COLLECTION, MAP)
  FilterContext.java               (novo record: element / mapKey / mapValue)

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
  ExecutablePropertyChain.java     (modificado: +9 ExecutableAccess variants; NavigationMode em lugar de ResultMode)
  AbstractObjectEvaluator.java     (modificado: filterContextStack + helpers + invokeFunctionBinding)
  SemanticResolver.java            (modificado: CollectionType/MapType propagation; in/nin)
  ExecutionPlanBuilder.java        (modificado: compilação dos novos steps)
  SemanticAstBuilder.java          (modificado: visitor para novos contextos ANTLR; filterValue; in/nin)
```

---

## Entrega em Fases (PRs incrementais)

| # | PR | Conteúdo | Testes mínimos |
|---|---|---|---|
| P1 | Reorganização | Mover `TypeIntrospectionSupport`/`ReflectiveAccessCache`; criar `internal.navigation`; `CollectionType`; `MapType`; `PropertyDescriptor.elementType`; `NavigationMode`; `FilterContext` | Regressão completa; compilação de imports |
| P2 | Sistema de tipos | `JavaTypeResolver`; `ResolvedTypes.merge` para novos tipos; `CollectionType`/`MapType` record semântica | Unit tests de merge e resolução de generics |
| P3 | Gramática + AST | Tokens (`DOUBLE_PERIOD`, `COLON_OP`, `IN`, `NIN`, `QUESTION`, `AT`); regras `subscript`, `filterRelation`, `filterValue`; `MemberAccess` variants; `SemanticAstBuilder` extensions | Parse de cada subscriptSpec; `COLON_OP` vs `TIME`; `in`/`nin`; `HH:MM` agora inválido |
| P4 | Semântica | `SemanticResolver` com propagação de `CollectionType`/`MapType`, binding de `..<func>(...)`, `in`/`nin`, built-ins de map e novos erros | Unit tests por linha da tabela de propagação; erros semânticos esperados |
| P5 | Execution Plan | `ExecutionPlanBuilder` + `ExecutableAccess` variants; `invokeFunctionBinding` como helper compartilhado | Compilação dos novos steps; teste de helper comum |
| P6 | Avaliador core | Index, map-key, slice, wildcard, filter (com `Deque<FilterContext>`), `in`/`nin`, agregação e função customizada em `AbstractObjectEvaluator` | Testes end-to-end de cada operação; filtros aninhados; outer-scope ref |
| P7 | Deep scan | `applyDeepScan` com BFS + detecção de ciclos; semântica de deduplicação | `store..price` com duplicatas escalares; ciclo de referência; `..*` |
| P8 | Testes completos | `CollectionNavigationTest` + suite de regressão | Todos os cenários da seção Verificação |

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

// Slice
store.book[:2]                 → 2 primeiros livros
store.book[-2:]                → 2 últimos livros
store.book[1:3]                → livros de índice 1 e 2

// Wildcard
store.book[*].author           → lista com 4 autores
store.bookByIsbn[*].price      → lista com os prices dos values do mapa

// Filtro — collection
store.book[?(@.isbn)].title                          → títulos com ISBN
store.book[?(@.price < 10)].title                    → títulos baratos
store.book[?(@.price < 10 and @.isbn)].title         → títulos baratos com ISBN
store..book[?(@.price <= store.expensive)]            → outer-scope ref
store.book[?(@.author =~ "(?i).*REES")].title        → ["Sayings of the Century"]

// Filtro — in / nin
store.book[?(@.category in store.validCategories)].title → todos (reference e fiction estão na lista)
store.book[?(@.category nin store.validCategories)].title → []

// Filtro — map
store.bookByIsbn[?(@.key =~ "^0-553")][*].title      → títulos cujas chaves começam com 0-553
store.bookByIsbn[?(@.value.price < 10)]              → mapa filtrado, preservando chaves
store.bookByIsbn..keys()                             → collection com ISBNs
store.bookByIsbn..values()..count()                  → 2

// Filtros aninhados
store.book[?(@.price < store.book[?(@.isbn)][0].price)] → livros mais baratos que o primeiro com ISBN

// Deep scan
store..author                  → todos os autores (4)
store..price                   → todos os preços (incluindo duplicatas escalares se houver)

// Agregação
store.book[*].price..sum()     → 53.92
store..book..count()           → 4

// Função customizada
store.bookByIsbn..customEval(true)    → equivale a customEval(store.bookByIsbn, true)
store..price..customEval(2, true)   → equivale a customEval(store..price, 2, true)
store..price..normalize()..count()  → chaining continua se normalize() retornar collection
store.bookByIsbn..normalizeMap()..keys() → chaining continua se normalizeMap() retornar map

// Erros semânticos esperados
@.price (fora de filtro)       → INVALID_CURRENT_ELEMENT
store.expensive..sum()         → TYPE_MISMATCH (scalar, não collection)
store..price..round(2)         → UNKNOWN_COLLECTION_FUNCTION se não existir overload com 1º parâmetro collection
store.bookByIsbn..round(2)     → UNKNOWN_COLLECTION_FUNCTION se não existir overload com 1º parâmetro map
store.bookByIsbn.foo           → INVALID_MAP_PROPERTY_ACCESS
store.book[?(@.price in store.expensive)] → INCOMPATIBLE_IN_OPERANDS (store.expensive é scalar)

// Erros gramaticais (parse error — não semânticos)
store.bookByIsbn["0-553-21311-3","0-395-19395-8"]  → ParseException (multi-key não suportado)
store.book[?(@.price <= store.book[0].price)]      → ParseException (subscript em referência externa de filtro)
12:30 (literal TIME sem segundos)                  → ParseException
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
