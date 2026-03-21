# Cache Evaluation — `ExpressionCompiler`

> **Scope:** `ExpressionCompiler` (internal cache) and `ExpressionRuntimeSupport` (static `COMPILER` field).
> **Date:** 2026-03-21

---

## Context

`ExpressionCompiler` holds a Caffeine cache keyed by `(source, environmentId, resultType)` with a
hard-coded maximum of 1 024 entries. `ExpressionRuntimeSupport` exposes the compiler as a JVM-wide
static singleton:

```java
// ExpressionRuntimeSupport.java
private static final ExpressionCompiler COMPILER = new ExpressionCompiler();
```

All calls to `compileMath`, `compileLogical`, and `compileAssignments` share this single instance
and therefore the same cache.

`ExpressionEnvironmentId` is a simple `String`-value record. `ExpressionEnvironmentBuilder.build()`
assigns an ID via `UUID.randomUUID()` — a new random value each time `build()` is called:

```java
// ExpressionEnvironmentBuilder.java
return new ExpressionEnvironment(new ExpressionEnvironmentId(UUID.randomUUID().toString()), ...);
```

This detail is critical to understanding all cache correctness and efficiency findings below.

---

## Findings

### 1. Static singleton — JVM-wide cache with no lifecycle control

The static `COMPILER` means the Caffeine cache lives for the entire JVM lifetime. Consequences:

- Cache size and eviction policy cannot be configured per deployment or per use case.
- The cache cannot be invalidated externally (e.g., after a configuration reload or a
  function-catalog update).
- In environments with multiple `ClassLoader`s (e.g., Spring DevTools hot-reload, OSGi), multiple
  independent static instances can coexist silently, causing incoherent cache state.

#### Proposition

Make the `ExpressionCompiler` an injectable, lifecycle-managed component rather than a static
singleton. In a plain-Java context, pass it as a constructor argument; in a Spring context, expose
it as a `@Bean` scoped to the application lifetime. This allows the cache to be configured,
replaced, and invalidated without touching static state.

As a minimum short-term measure, expose a package-private `invalidateCache()` method so tests and
reload scenarios can clear state without creating a new JVM:

```java
// ExpressionCompiler.java (package-private, for controlled use only)
void invalidateCache() {
    cache.invalidateAll();
}
```

---

### 2. Random `ExpressionEnvironmentId` — correctness guaranteed, reuse impossible

Because `build()` assigns a `UUID.randomUUID()` as the environment ID, **every call to `build()`
produces a unique ID**, even when the configuration is identical. This has two consequences:

1. **Correctness is guaranteed by construction:** a cache hit can only occur when the exact same
   `ExpressionEnvironment` instance is reused. No two independently-built environments can ever
   share a cache entry, so false hits are structurally impossible.

2. **Cache reuse across equivalent environments is impossible:** two environments built from the
   same providers and symbols will compile the same expression twice, doubling compilation work and
   consuming twice the cache budget. This is a significant efficiency loss in any scenario where
   environments are rebuilt (e.g., per-request builders, test setups, Spring context refreshes).

#### Proposition

Choose a strategy based on the intended environment lifecycle:

**Option A — Document the immutable-instance contract (minimal change).**
Environments must be created once and reused. Document this contract explicitly in
`ExpressionEnvironmentBuilder.build()` and `ExpressionRuntimeSupport`. Suitable when the
application already owns long-lived environment instances.

**Option B — Content-derived ID (better cache efficiency).**
Derive the ID deterministically from the environment configuration — e.g., a hash of the sorted
provider class names, external symbol descriptors, and `MathContext` precision:

```java
// ExpressionEnvironmentBuilder.java
private static String deriveEnvironmentId(
        List<Class<?>> staticProviders,
        List<Object> instanceProviders,
        Map<String, ExternalSymbolRegistration> externalSymbols,
        MathContext mathContext) {

    List<String> parts = new ArrayList<>();
    staticProviders.forEach(c -> parts.add("s:" + c.getName()));
    instanceProviders.forEach(o -> parts.add("i:" + o.getClass().getName()));
    externalSymbols.forEach((name, reg) -> parts.add("x:" + name + ":" + reg.declaredType()));
    parts.add("mc:" + mathContext.getPrecision() + ":" + mathContext.getRoundingMode());
    Collections.sort(parts);
    return Integer.toHexString(parts.hashCode()); // or SHA-256 for collision safety
}
```

This allows independently-built environments with identical configuration to share cache entries,
making the singleton COMPILER genuinely effective.

---

### 3. Dead validation code and inconsistent exception types

`compileUncached` guards `source` and `resultType` with `Objects.requireNonNull`:

```java
private CompiledExpression compileUncached(String source, ExpressionResultType resultType, ...) {
    Objects.requireNonNull(source, "source must not be null");        // unreachable
    Objects.requireNonNull(resultType, "resultType must not be null"); // unreachable
    ...
}
```

These checks are never reached. The `ExpressionCacheKey` compact constructor already rejects them
before the loader is invoked:

- `null` or blank `source` → `IllegalArgumentException` from the compact constructor.
- `null` `resultType` → `NullPointerException` from `Objects.requireNonNull` inside the compact
  constructor.

Meanwhile, `compile()` only explicitly guards `environment`. The same invalid input can therefore
throw different exception types depending on which validation fires first.

#### Proposition

Consolidate all input validation in `compile()`, before the cache key is constructed. Remove the
redundant checks from `compileUncached`:

```java
// ExpressionCompiler.java
public CompiledExpression compile(String source, ExpressionResultType resultType,
                                  ExpressionEnvironment environment) {
    if (source == null || source.isBlank()) {
        throw new IllegalArgumentException("source must not be blank");
    }
    Objects.requireNonNull(resultType, "resultType must not be null");
    Objects.requireNonNull(environment, "environment must not be null");
    ExpressionCacheKey cacheKey = new ExpressionCacheKey(source, environment.environmentId(), resultType);
    return cache.get(cacheKey, ignored -> compileUncached(source, resultType, environment));
}

private CompiledExpression compileUncached(String source, ExpressionResultType resultType,
                                           ExpressionEnvironment environment) {
    // no redundant null checks here — inputs are already validated by compile()
    ...
}
```

---

### 4. No TTL — potential staleness if environment immutability is not enforced

```java
this.cache = Caffeine.newBuilder().maximumSize(1_024).build();
```

Entries are never expired — only evicted under size pressure. If an `ExpressionEnvironment` were
mutated after first use (e.g., symbols added to a shared catalog), any cached expression compiled
against the old state would remain in the cache as long as the `environmentId` is unchanged.

In the current implementation, `ExpressionEnvironment` is structurally immutable (all fields
`final`, no setters). However, the catalogs it holds (`FunctionCatalog`, `ExternalSymbolCatalog`)
are not verified as immutable here. If either catalog exposes mutable state, a post-construction
mutation would bypass the cache key and silently return stale compiled expressions.

#### Proposition

Two complementary actions:

1. **Document the immutability contract.** Add a Javadoc note to `ExpressionEnvironment` and
   `FunctionCatalog`/`ExternalSymbolCatalog` stating that instances must be immutable after
   construction. This is already structurally true for `ExpressionEnvironment`; verify and document
   it for the catalog types as well.

2. **Add a TTL as a safety net if catalog immutability cannot be guaranteed.**
   A conservative TTL (e.g., 1 hour) prevents indefinitely stale entries without meaningfully
   affecting warm-path performance:

   ```java
   this.cache = Caffeine.newBuilder()
       .maximumSize(1_024)
       .expireAfterWrite(Duration.ofHours(1))
       .build();
   ```

---

### 5. Hard-coded, non-configurable cache parameters

The maximum size (1 024) and the absence of an eviction policy are hardcoded. In deployments with
large expression vocabularies or many distinct environments this limit may be too low, causing
frequent evictions and recompilation. In memory-constrained environments it may be too high.

#### Proposition

Accept an optional `CacheConfig` value object in the `ExpressionCompiler` constructor, with
sensible defaults for size and TTL:

```java
// ExpressionCompiler.java
public record CacheConfig(long maximumSize, Duration expireAfterWrite) {
    public static CacheConfig defaults() {
        return new CacheConfig(1_024, null); // null = no TTL
    }
}

ExpressionCompiler(ExpressionEvaluatorV2ParserFacade parserFacade, SemanticAstBuilder astBuilder,
                   SemanticResolver semanticResolver, ExecutionPlanBuilder planBuilder,
                   CacheConfig cacheConfig) {
    ...
    Caffeine<Object, Object> builder = Caffeine.newBuilder()
        .maximumSize(cacheConfig.maximumSize());
    if (cacheConfig.expireAfterWrite() != null) {
        builder.expireAfterWrite(cacheConfig.expireAfterWrite());
    }
    this.cache = builder.build();
}
```

The no-arg public constructor retains backward compatibility by using `CacheConfig.defaults()`.

---

## Summary

| # | Finding                                                                          | Severity |
|---|----------------------------------------------------------------------------------|----------|
| 1 | Static singleton prevents lifecycle and per-deployment control                   | Medium   |
| 2 | Random `environmentId` guarantees correctness but prevents reuse across equivalent environments | Medium |
| 3 | Null checks in `compileUncached` are dead code; exception types are inconsistent | Low      |
| 4 | No TTL; staleness risk if catalog immutability is not structurally enforced      | Low      |
| 5 | Cache size and eviction policy are hard-coded, not configurable                  | Low      |

The most impactful finding is **#2**: the random UUID strategy makes the cache correct but
effectively useless whenever environments are rebuilt from the same configuration. Addressing it —
either by enforcing long-lived environment instances (Option A) or by deriving the ID from content
(Option B) — will have the greatest effect on actual cache efficiency.
