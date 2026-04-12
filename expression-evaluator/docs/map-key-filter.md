# Map Key Filter Syntax — `@.key` in Collection Navigation

## Overview

Map filter predicates (`[?(...)]`) can now reference the **map key** object directly via the `@.key`
sentinel. This complements the existing `@` (or `@.value`) access that always points to the map value.

```
map[?(@.key.property op externalRef)]
```

---

## Syntax

### Access sentinels inside `[?(...)]` on a Map

| Syntax | Resolves to |
|--------|-------------|
| `@` | the map **value** for the current entry |
| `@.value` | idem — explicit alias for `@` |
| `@.value.prop` | property `prop` of the map value |
| `@.key` | the map **key** object for the current entry |
| `@.key.prop` | property `prop` of the map key object |
| `@.key.prop.nested` | nested navigation on the key object |

> `@.key` and `@.value` are **runtime sentinels** resolved by the evaluator.
> They are not grammar keywords; the grammar already supported `AT memberChain*`
> in `filterValue`, so no grammar changes are required.

---

## Supported Patterns

All examples below assume the external symbols are registered via
`ExpressionEnvironment.builder().registerExternalSymbol(...)`.

### 1. Filter by numeric key property

```java
// items: Map<UserId, Account>,  threshold: int
"items[?(@.key.id > threshold)]..count()"
```

Returns count of entries whose key's `id` is greater than `threshold`.

### 2. Filter by String key property

```java
// accounts: Map<UserId, Account>,  domain: String
"accounts[?(@.key.domain = domain)]..values()..count()"
```

Returns count of accounts whose key's `domain` matches the external symbol `domain`.

### 3. Aggregate values after key-based filter

```java
// balances: Map<UserId, Account>,  limit: int
"balances[?(@.key.id < limit)]..values()..balance..sum()"
```

Filters entries by key `id`, then deep-scans the values for `balance` and sums them.

### 4. AND condition on key properties

```java
// data: Map<UserId, String>,  minId: int,  org: String
"data[?(@.key.id >= minId and @.key.domain = org)]..count()"
```

Both conditions must hold on the **same key object**.

### 5. OR condition on key properties

```java
// collection: Map<UserId, String>,  maxId: int,  targetOrg: String
"collection[?(@.key.id > maxId or @.key.domain = targetOrg)]..count()"
```

At least one condition must hold.

### 6. Zero-match edge case

```java
"map[?(@.key.id > threshold)]..count()"  // → 0  when no key satisfies the predicate
```

### 7. All-match edge case

```java
"map[?(@.key.domain = env)]..count()"  // → map.size()  when all keys satisfy the predicate
```

---

## Grammar (unchanged)

The grammar rule that enables this syntax (no modification needed):

```antlr
filterValue
    : AT memberChain*   # currentElementFilterValue
    | ...
    ;
```

`memberChain` already handles `.IDENTIFIER` (dot property access), so `@.key`, `@.key.id`,
and `@.key.domain` all parse correctly out of the box.

---

## Implementation Details

### Relevant classes

| Class | Role |
|-------|------|
| `FilterContext` | Carries `mapKey` / `mapValue` for the current map entry being tested |
| `AbstractObjectEvaluator` | Pushes/pops `FilterContext` in `applyFilter()`; evaluates `@` at runtime |
| `SemanticResolver` | Resolves filter predicates on maps with `UnknownType` (no type info available for keys) |
| `ExecutionPlanBuilder` | Builds `ExecutablePropertyChain` with reflective steps when type is `UnknownType` |

### FilterContext stack

`AbstractObjectEvaluator` maintains a per-thread `Deque<FilterContext>` (`FILTER_CTX`).
For map entries, `applyFilter()` pushes `FilterContext.ofMapEntry(entry.getKey(), entry.getValue())`
before evaluating the predicate and pops it in a `finally` block.

```java
// AbstractObjectEvaluator.applyFilter — already in production
for (Map.Entry<?, ?> entry : map.entrySet()) {
    FilterContext ctx = FilterContext.ofMapEntry(entry.getKey(), entry.getValue());
    stack.push(ctx);
    try {
        if (asBoolean(evaluateExpr(predicate, scope))) {
            result.add(entry.getValue());
        }
    } finally {
        stack.pop();
    }
}
```

### Root cause of the missing feature

When `@.key.id` is compiled, the chain produces:

```
ExecutablePropertyChain(
    root  = ExecutableIdentifier("@"),
    chain = [ReflectivePropertyAccess("key"), ReflectivePropertyAccess("id")]
)
```

All steps are `ReflectivePropertyAccess`, so `legacyOnly = true` and
`evaluateLegacyPropertyChain` is invoked. That method calls:

```java
Object current = evaluateExpr(root, scope);
// → evaluateExpr for "@" always returns ctx.mapValue() in a map context
// → then ReflectivePropertyAccess("key") tries to resolve .key on the VALUE — wrong
```

### The fix — sentinel detection at chain start

In **both** `evaluateLegacyPropertyChain` and `evaluatePropertyChain`, immediately after resolving
`current` from the root, a sentinel check is inserted:

```java
Object current = evaluateExpr(node.root(), scope);
List<ExecutablePropertyChain.ExecutableAccess> chain = node.chain();
int chainStart = 0;
if (isCurrentElementRef(node.root()) && !chain.isEmpty()
        && chain.getFirst() instanceof ExecutablePropertyChain.ReflectivePropertyAccess rpa) {
    FilterContext mapCtx = FILTER_CTX.get().peek();
    if (mapCtx != null && mapCtx.isMapContext()) {
        if ("key".equals(rpa.name())) {
            current = mapCtx.mapKey();   // redirect to key object
            chainStart = 1;              // skip "key" step — it was the sentinel
        } else if ("value".equals(rpa.name())) {
            current = mapCtx.mapValue(); // explicit @.value — same as bare @
            chainStart = 1;
        }
    }
}
// iterate chain from chainStart, not from 0
```

Private helper:

```java
private static boolean isCurrentElementRef(ExecutableNode root) {
    return root instanceof ExecutableIdentifier id && CURRENT_ELEMENT_REF.equals(id.ref().name());
}
```

`evaluateLegacyPropertyChain` uses `chain.subList(chainStart, chain.size())` in the for-each loop.
`evaluatePropertyChain` converts the loop to indexed (`for (int i = chainStart; i < chain.size(); i++)`).

### Why two methods need the fix

| Path | When used | Example |
|------|-----------|---------|
| `evaluateLegacyPropertyChain` | all chain steps are legacy (typed getter, reflective property/method) | `@.key.id` → `[Reflective("key"), Reflective("id")]` |
| `evaluatePropertyChain` | at least one non-legacy step (index, filter, aggregation…) | `@.key.items[0]` → `[Reflective("key"), Reflective("items"), IndexAccess(0)]` |

---

## Semantic Resolver Note

`SemanticResolver.resolveMapFilterPredicate` already contains a comment acknowledging the intent:

```java
private void resolveMapFilterPredicate(
        ExpressionNode predicate, MapType mapType, PropertyChainNode node) {
    // During map filter resolution, @.key and @.value are the two valid access patterns.
    // We simply resolve with UnknownType for @ (the evaluator handles it at runtime).
    resolveFilterPredicate(predicate, UnknownType.INSTANCE, node);
}
```

No change is needed here; type resolution remains `UnknownType` and runtime handles the semantics.

---

## Test Specification

Located in `CollectionNavigationTest$MapKeyFilterSyntax`
(`expression-evaluator/src/test/java/com/runestone/expeval/api/CollectionNavigationTest.java`, line 1046).

Domain model used by the tests:
```java
record UserId(long id, String domain) {}
record Account(String type, BigDecimal balance) {}
```

| Test method | Expression | Setup | Expected result |
|-------------|-----------|-------|-----------------|
| `shouldFilterMapByKeyIdDirectly` | `items[?(@.key.id > threshold)]..count()` | keys id=1,2,3; threshold=1 | `2` (id=2, id=3) |
| `shouldFilterMapByKeyDomainAndGetValues` | `accounts[?(@.key.domain = domain)]..values()..count()` | 2×acme + 1×corp; domain="acme" | `2` |
| `shouldAggregateValuesFromFilteredKeys` | `balances[?(@.key.id < limit)]..values()..balance..sum()` | ids 1,2,3 → 100,200,300; limit=3 | `300` (100+200) |
| `shouldFilterMapWithKeyAndCondition` | `data[?(@.key.id >= minId and @.key.domain = org)]..count()` | minId=2, org="acme" | `2` (id2Acme, id4Acme) |
| `shouldFilterMapWithKeyOrCondition` | `collection[?(@.key.id > maxId or @.key.domain = targetOrg)]..count()` | maxId=8, targetOrg="org1" | `3` |
| `shouldReturnZeroForNoKeyMatches` | `map[?(@.key.id > threshold)]..count()` | threshold=100, all ids ≤ 2 | `0` |
| `shouldReturnAllForAllKeyMatches` | `map[?(@.key.domain = env)]..count()` | all domains="prod", env="prod" | `3` |

---

## Limitations

- Key type must be a **JavaBean / record** with accessible getters or public fields (standard reflective access).
- Nested key navigation (`@.key.address.city`) works as long as intermediate values are non-null;
  use safe-navigation (`?.`) for nullable intermediates if the grammar supports it in filter context.
- `@.key` alone (without further property access) is not useful in comparisons since complex objects
  don't have a natural scalar comparison; use a key property instead.
- Map keys of primitive/wrapper/String types have no properties to navigate; `@.key` would return
  the key itself, but `@.key.someProperty` would fail at runtime.
