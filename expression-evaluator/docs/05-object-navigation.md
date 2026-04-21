# Object Navigation

The engine can traverse object graphs and collections directly within expressions. Navigation uses dot notation for objects and subscript notation for collections.

## Object Properties

| Syntax | Behavior |
|---|---|
| `obj.prop` | Access a property — getter, record component, or public field |
| `obj.method()` | Call an instance method |
| `obj?.prop` | Null-safe access — returns `null` if `obj` is null instead of throwing |
| `obj?.method()` | Null-safe method call |
| `obj.prop ?? "default"` | Null coalescing after navigation |

```
customer.address.city                // throws if address is null
customer?.address?.city              // returns null if address is null
customer?.address?.city ?? "Unknown" // "Unknown" if any step is null
```

For the compiler to validate property names at compile time, register a type hint for the class. Without one, navigation still works at runtime through reflection, but a typo in a property name only fails when the expression is evaluated.

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .addAllFunctions()
    .registerTypeHint(Customer.class)
    .registerTypeHint(Address.class)
    .build();
```

## Collection Subscripts

| Syntax | Behavior | Example |
|---|---|---|
| `list[0]` | Element by index (zero-based) | `prices[0]` → first element |
| `list[-1]` | Last element | `prices[-1]` |
| `list[0:2]` | Slice — indices 0 and 1 (end is exclusive) | `prices[0:2]` → `[5, 15]` |
| `list[*]` | All elements (copy for lists, all values for maps) | `prices[*]` |
| `list[?(@ > 10)]` | Filter by predicate | `prices[?(@ > 10)]` → `[15, 25]` |

> [!NOTE]
> Slice notation `[start:end]` is exclusive on the end, matching Python conventions. `prices[0:2]` returns indices 0 and 1 — not 0, 1, and 2.

> [!NOTE]
> `prices[10:20]` looks like a time literal (`HH:MM`), but the parser treats it as a slice with start=10, end=20 when it appears inside `[...]`.

### Property Projection After Collection Steps

After a wildcard `[*]`, a slice `[start:end]`, or a filter `[?(...)]`, you can access a property directly. The property access is automatically projected over every element of the resulting collection, producing a new list.

```
books[*].price           // list of prices for all books
books[0:2].price         // list of prices for books at index 0 and 1
books[?(@.author = "Alice")].price   // list of prices for Alice's books
```

This is the right approach when you want exactly the top-level `price` property of each element. Note that `..ds(price)` is **not** equivalent here — it performs a recursive deep scan and would also collect `price` from any nested objects within each element.

```
books[*].price..sum()                        // sum of all prices
books[?(@.author = "Alice")].price..avg()    // average price of Alice's books
books[0:2].price..sum()                      // sum of first two book prices
```

## Predicates with `@`

`@` represents the current element in filter predicates. It supports comparisons, range checks, membership, and regex:

```
prices[?(@ > 10)]                    // filter numbers
names[?(@ =~ '[A-Z].*')]             // filter by regex
items[?(@ between 5 and 20)]        // range filter
statuses[?(@ in ["active", "done"])] // membership filter
```

## Deep Scan

The deep scan method (`..ds(...)`) traverses all elements of a collection and either projects a property or applies an aggregation function.

### Projecting a Property

```
books..ds(price)     // collect "price" from every element → [10.0, 20.0, ...]
books..ds()          // wildcard — collect all leaf values
```

## Built-in Collection Functions

### Aggregation Functions

| Syntax | Description |
|---|---|
| `list..sum()` | Sum of all elements |
| `list..avg()` | Average |
| `list..min()` | Minimum |
| `list..max()` | Maximum |
| `list..count()` | Count of elements |
| `list..prod()` | Product of all elements |

```
orders..ds(amount)..sum()   // total amount across all orders
items..count()               // count items
```

### Lambda Processing (sum and prod only)

`sum` and `prod` accept a lambda to transform each element before aggregating:

```
orders..sum(@ -> @.price * @.qty)     // sum of (price * qty) for each order
items..prod(@ -> @.price)            // product of all prices
```

> [!WARNING]
> Only `sum` and `prod` accept inline lambda transformations. Using `@ -> expr` with `avg`, `min`, `max`, or `count` is a compile-time error. To transform first and then use those aggregations, use `..map()` followed by the aggregation.

### Element-wise Transform — `..map(@ -> expr)`

`..map(@ -> expr)` transforms each element of a list or map into a new list. Unlike the inline lambdas in `sum` and `prod`, `..map()` always returns a **collection**, making it composable with any subsequent aggregation.

The lambda is required — `..map()` without `@ -> expr` is a compile-time error.

```
nums..map(@ -> @ * 2)..sum()               // double each element, then sum
nums..map(@ -> @ ^ 2)..sum()              // square each element, then sum
nums..map(@ -> @ * 2)..avg()              // double each, then average
orders..map(@ -> @.price * @.qty)..sum()  // compute line totals and sum
```

Maps are also supported — `@` is bound to each entry, with `@.key` and `@.value` providing access:

```
inventory..map(@ -> @.value * 2)..sum()   // double each map value, then sum
inventory..map(@ -> @.value)..count()     // count entries via value projection
```

`..map()` can be chained:

```
nums..map(@ -> @ * 2)..map(@ -> @ + 1)..sum()   // [1,2,3] → [2,4,6] → [3,5,7] → 15
```

`..map()` does **not** accept positional arguments — only the `@ -> expr` lambda form is valid.

### Map Projections

| Syntax | Description |
|---|---|
| `map..keys()` | All keys of a map |
| `map..values()` | All values of a map |

You can also filter a map before projecting keys or values. The filter keeps the result as a map, but only with the entries that match the predicate.

This is especially useful when the keys are complex objects. The predicate can inspect key properties through `@.key`, and the filtered result still behaves like a map:

```text
accounts[?(@.key.domain = "acme")]          // filtered map with only acme keys
accounts[?(@.key.id > 1)]                   // filtered map by key id
portfolio[?(@.key.domain = "acme" and @.value.balance > 1000)]    // filtered map by key and value
```

In each case, the `[?(...)]` filter returns a reduced map first. `..values()` then projects the remaining values, and `..count()` or `..sum()` works on that projected collection.

## Circular References

The engine detects object cycles during navigation and throws `ExpressionEvaluationException`. There is no way to configure a depth limit — the check is automatic.

## Null Behavior in Deep Scan

When a property does not exist on an element, or an intermediate node is null, deep scan returns an empty collection rather than throwing. This matches JsonPath conventions.

```
books..ds(nonexistentProp)   // → empty list, no exception
nullableList..ds(price)      // → empty list if nullableList is null
```

## Type Hints and Compilation

Without type hints, property names are resolved at runtime through reflection. Any errors surface at evaluation time. With type hints, the compiler validates property names and method signatures during `compile()` and `validate()`, producing cleaner error messages.

```java
// Without type hint: works at runtime, no compile-time checking
MathExpression expr = MathExpression.compile("customer.nme", env); // typo

// With type hint: fails at compile time with UNKNOWN_PROPERTY
env = ExpressionEnvironment.builder()
    .addAllFunctions()
    .registerTypeHint(Customer.class)
    .build();
MathExpression expr = MathExpression.compile("customer.nme", env); // compile error
```

## Practical Example

```java
// Average price of orders above the minimum threshold,
// from a customer's active orders
String expression =
    "customer?.orders[?(@ = 'active')]..ds(price)[?(@ > minPrice)]..avg()";

MathExpression expr = MathExpression.compile(expression, env);
BigDecimal avg = expr.compute(Map.of("customer", customer, "minPrice", 50));
```
