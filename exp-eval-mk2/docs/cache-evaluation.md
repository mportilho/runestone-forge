# Cache Evaluation — `ExpressionCompiler`

> **Scope:** `ExpressionCompiler` (internal cache) and `ExpressionRuntimeSupport` (static `COMPILER` field).
> **Date:** 2026-03-21

---

## Context

`ExpressionCompiler` holds a Caffeine cache keyed by `(source, environmentId, resultType)`.
`ExpressionRuntimeSupport` exposes the compiler as a JVM-wide lazy singleton, configurable before
the first compilation via `ExpressionRuntimeSupport.configure(CacheConfig)`:

```java
// ExpressionRuntimeSupport.java
private static volatile ExpressionCompiler COMPILER; // lazily initialised
```

The cache size, TTL, and other parameters are supplied through `CacheConfig` (in the `api` package),
which also reads JVM system properties (`expeval.cache.maximumSize`, `expeval.cache.ttlSeconds`) as
defaults when no explicit configuration is provided.

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

### 1. Static singleton — JVM-wide cache with limited lifecycle control `OPEN`

The lazy `COMPILER` singleton can be configured once at startup via `configure(CacheConfig)`, but
it cannot be replaced or invalidated after initialization. Consequences that remain:

- The cache cannot be invalidated externally after initialization (e.g., after a
  function-catalog update).
- In environments with multiple `ClassLoader`s (e.g., Spring DevTools hot-reload, OSGi), multiple
  independent static instances can coexist silently, causing incoherent cache state.

#### Remaining proposition

Make the `ExpressionCompiler` an injectable, lifecycle-managed component rather than a static
singleton. In a plain-Java context, pass it as a constructor argument; in a Spring context, expose
it as a `@Bean` scoped to the application lifetime. This allows the cache to be replaced and
invalidated without touching static state.

As a complementary short-term measure, expose a package-private `invalidateCache()` method so
tests and reload scenarios can clear state without creating a new JVM:

```java
// ExpressionCompiler.java (package-private, for controlled use only)
void invalidateCache() {
    cache.invalidateAll();
}
```

---

### 2. Random `ExpressionEnvironmentId` — correctness guaranteed, reuse impossible `OPEN`

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

### 3. Dead validation code and inconsistent exception types `RESOLVED`

All input validation is now consolidated in `compile()`, before the cache key is constructed:

```java
// ExpressionCompiler.java
public CompiledExpression compile(String source, ExpressionResultType resultType,
                                  ExpressionEnvironment environment) {
    if (source == null || source.isBlank()) {
        throw new IllegalArgumentException("source must not be blank");
    }
    Objects.requireNonNull(resultType, "resultType must not be null");
    Objects.requireNonNull(environment, "environment must not be null");
    ...
}
```

The redundant `Objects.requireNonNull` calls that were unreachable inside `compileUncached` have
been removed. Exception types are now consistent regardless of which argument is invalid.

---

### 4. No TTL — potential staleness if environment immutability is not enforced `PARTIAL`

TTL support is now available via `CacheConfig.expireAfterWrite`. No TTL is applied by default
(`null` = no expiration), matching the previous behaviour. Callers can opt in at startup:

```java
ExpressionRuntimeSupport.configure(new CacheConfig(1_024, Duration.ofHours(1)));
```

The immutability contract documentation for `ExpressionEnvironment`, `FunctionCatalog`, and
`ExternalSymbolCatalog` has not yet been added. Until it is, silent staleness remains possible if
either catalog exposes mutable state.

#### Remaining proposition

Add a Javadoc note to `ExpressionEnvironment` and `FunctionCatalog`/`ExternalSymbolCatalog`
stating that instances must be immutable after construction. This is already structurally true for
`ExpressionEnvironment`; verify and document it for the catalog types as well.

---

### 5. Hard-coded, non-configurable cache parameters `RESOLVED`

Cache parameters are now configurable through `CacheConfig` (package `com.runestone.expeval2.api`):

```java
public record CacheConfig(long maximumSize, Duration expireAfterWrite) {
    public static CacheConfig defaults() { ... } // reads system properties
}
```

`ExpressionCompiler`'s package-private constructor accepts a `CacheConfig`; the public no-arg
constructor delegates to `CacheConfig.defaults()`, preserving backward compatibility.

`ExpressionRuntimeSupport.configure(CacheConfig)` must be called before the first compilation to
take effect. Alternatively, the JVM system properties `expeval.cache.maximumSize` and
`expeval.cache.ttlSeconds` are read by `CacheConfig.defaults()` when no explicit call is made:

```shell
-Dexpeval.cache.maximumSize=4096 -Dexpeval.cache.ttlSeconds=3600
```

---

## Summary

| # | Finding                                                                                          | Severity | Status   |
|---|--------------------------------------------------------------------------------------------------|----------|----------|
| 1 | Singleton prevents full lifecycle control (no invalidation, ClassLoader risks)                   | Medium   | Open     |
| 2 | Random `environmentId` guarantees correctness but prevents reuse across equivalent environments  | Medium   | Open     |
| 3 | Null checks in `compileUncached` were dead code; exception types were inconsistent               | Low      | Resolved |
| 4 | TTL infrastructure added via `CacheConfig`; immutability contract documentation pending          | Low      | Partial  |
| 5 | Cache size and eviction policy were hard-coded, not configurable                                 | Low      | Resolved |

The most impactful finding is **#2**: the random UUID strategy makes the cache correct but
effectively useless whenever environments are rebuilt from the same configuration. Addressing it —
either by enforcing long-lived environment instances (Option A) or by deriving the ID from content
(Option B) — will have the greatest effect on actual cache efficiency.
