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

### 2. Random `ExpressionEnvironmentId` — correctness guaranteed, reuse impossible `RESOLVED`

`ExpressionEnvironmentBuilder.build()` now derives the environment ID deterministically via SHA-256
over the sorted list of provider class names, external symbol descriptors, and `MathContext`
parameters:

```java
// ExpressionEnvironmentBuilder.java
private static String deriveEnvironmentId(
        List<Class<?>> staticProviders,
        List<Object> instanceProviders,
        Map<String, ExternalSymbolRegistration> externalSymbols,
        MathContext mathContext) {
    List<String> parts = new ArrayList<>();
    staticProviders.forEach(c -> parts.add("s:" + c.getName()));
    instanceProviders.forEach(o -> parts.add("i:" + o.getClass().getName() + "@" + System.identityHashCode(o)));
    externalSymbols.forEach((name, reg) -> parts.add("x:" + name + ":" + reg.declaredType() + ":" + reg.overridable()));
    parts.add("mc:" + mathContext.getPrecision() + ":" + mathContext.getRoundingMode());
    Collections.sort(parts);
    String content = String.join("|", parts);
    byte[] hash = MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8));
    return HexFormat.of().formatHex(hash, 0, 8);
}
```

Two environments built from the same static providers, external symbols, and `MathContext` now
share the same cache entries, making the compiler cache genuinely effective for those cases.

**Note on instance providers:** the identity hash code (`System.identityHashCode`) is included for
instance-based providers. This is conservative: two distinct instances of the same class get
different IDs even if their state is equivalent. Full reuse for instance providers requires either
sharing the same instance or implementing content-based equality in the provider itself.

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
| 2 | Random `environmentId` replaced by SHA-256 content-derived ID                                   | Medium   | Resolved |
| 3 | Null checks in `compileUncached` were dead code; exception types were inconsistent               | Low      | Resolved |
| 4 | TTL infrastructure added via `CacheConfig`; immutability contract documentation pending          | Low      | Partial  |
| 5 | Cache size and eviction policy were hard-coded, not configurable                                 | Low      | Resolved |

Finding **#2** — previously the most impactful — is resolved: environments with the same static
providers, external symbols, and `MathContext` now share cache entries. The remaining open items
are **#1** (singleton lifecycle) and **#4** (immutability contract documentation).
