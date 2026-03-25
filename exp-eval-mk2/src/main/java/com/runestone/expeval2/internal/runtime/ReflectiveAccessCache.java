package com.runestone.expeval2.internal.runtime;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JVM-level cache for reflective property and method handles.
 *
 * <p>On the first access for a given class, {@link TypeIntrospectionSupport} discovers all
 * accessible property and method handles and stores them in a {@link ConcurrentHashMap}.
 * Subsequent accesses for the same class are O(1) map lookups with no reflection overhead.
 *
 * <p>Handles are stored as {@link MethodHandle} objects resolved once via
 * {@code setAccessible(true)} + {@code MethodHandles.lookup().unreflect(...)}, eliminating
 * the per-call cost of {@code getDeclaredMethods}, {@code getRecordComponents},
 * {@code Method.copy}, and {@code setAccessible} that dominated the reflective fallback path.
 *
 * <p>The cache is unbounded and never evicts entries. Class objects have the same lifetime as
 * their classloader; eviction would require a {@code WeakReference} strategy that adds complexity
 * without benefit for the typical single-classloader use-case of this runtime.
 *
 * <p>Thread safety: {@link ConcurrentHashMap#computeIfAbsent} guarantees safe publication of
 * {@link ReflectiveMeta}. Under contention on a cold type, discovery may run more than once;
 * the result is idempotent, so no coordination is needed.
 *
 * <p>Package-private — not part of the public API.
 */
final class ReflectiveAccessCache {

    private static final ConcurrentHashMap<Class<?>, ReflectiveMeta> CACHE = new ConcurrentHashMap<>();

    private ReflectiveAccessCache() {}

    /**
     * Returns the cached {@link MethodHandle} for {@code propertyName} on {@code type},
     * or {@code null} if no accessible property with that name exists.
     */
    static MethodHandle property(Class<?> type, String propertyName) {
        return metaFor(type).properties().get(propertyName);
    }

    /**
     * Returns the cached {@link MethodHandle} for {@code methodName} with {@code arity}
     * parameters on {@code type}, or {@code null} if no such method exists.
     */
    static MethodHandle method(Class<?> type, String methodName, int arity) {
        Map<Integer, MethodHandle> byArity = metaFor(type).methods().get(methodName);
        return byArity == null ? null : byArity.get(arity);
    }

    private static ReflectiveMeta metaFor(Class<?> type) {
        return CACHE.computeIfAbsent(type, t -> new ReflectiveMeta(
                TypeIntrospectionSupport.discoverPropertyHandles(t),
                TypeIntrospectionSupport.discoverMethodHandles(t)));
    }

    private record ReflectiveMeta(
            Map<String, MethodHandle> properties,
            Map<String, Map<Integer, MethodHandle>> methods
    ) {}
}
