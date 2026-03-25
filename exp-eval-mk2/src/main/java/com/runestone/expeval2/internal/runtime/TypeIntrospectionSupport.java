package com.runestone.expeval2.internal.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovers property and method handles for a given class using the same lookup semantics
 * as the runtime reflective fallback: record components first, then a {@code getDeclaredMethods}
 * hierarchy walk for getters, then a declared-field walk.
 *
 * <p>Results are consumed by {@link ReflectiveAccessCache} and cached for the lifetime of the JVM.
 * The low-level utilities ({@link #unreflect}, {@link #unreflectGetter},
 * {@link #propertyNameFromGetter}, {@link #decapitalize}) are also used by
 * {@code ExpressionEnvironmentBuilder} for the typed-hint discovery path.
 *
 * <p>Although {@code public}, this class belongs to the {@code internal.runtime} package and is
 * not part of the library's public API. No compatibility guarantees are provided.
 */
public final class TypeIntrospectionSupport {

    private TypeIntrospectionSupport() {}

    /**
     * Discovers all accessible property handles for {@code type}.
     *
     * <p>Discovery order (first match wins per property name):
     * <ol>
     *   <li>Record components</li>
     *   <li>Zero-arg instance getters: {@code getXxx()} and {@code isXxx()} (boolean)</li>
     *   <li>Declared instance fields</li>
     * </ol>
     *
     * <p>Getter discovery walks the class hierarchy via {@code getDeclaredMethods}, which covers
     * non-public classes (e.g. package-private inner records) that {@code getMethods()} would miss.
     *
     * @return unmodifiable map of property name → {@link MethodHandle}
     */
    static Map<String, MethodHandle> discoverPropertyHandles(Class<?> type) {
        Map<String, MethodHandle> result = new LinkedHashMap<>();
        discoverRecordProperties(type, result);
        discoverGetterProperties(type, result);
        discoverFieldProperties(type, result);
        return Map.copyOf(result);
    }

    /**
     * Discovers all accessible instance method handles for {@code type}.
     *
     * <p>Walks the class hierarchy via {@code getDeclaredMethods}. The key is
     * {@code (name, arity)}; the first method found in the hierarchy wins (most-specific class first).
     * This matches the lookup order of the original reflective fallback.
     *
     * @return unmodifiable map of method name → (arity → {@link MethodHandle})
     */
    static Map<String, Map<Integer, MethodHandle>> discoverMethodHandles(Class<?> type) {
        Map<String, Map<Integer, MethodHandle>> result = new LinkedHashMap<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers()) || m.isSynthetic() || m.isBridge()) {
                    continue;
                }
                result.computeIfAbsent(m.getName(), ignored -> new LinkedHashMap<>())
                      .putIfAbsent(m.getParameterCount(), unreflect(m));
            }
        }
        Map<String, Map<Integer, MethodHandle>> copy = new LinkedHashMap<>(result.size());
        result.forEach((name, byArity) -> copy.put(name, Map.copyOf(byArity)));
        return Map.copyOf(copy);
    }

    // -------------------------------------------------------------------------
    // Property discovery helpers
    // -------------------------------------------------------------------------

    private static void discoverRecordProperties(Class<?> type, Map<String, MethodHandle> result) {
        if (!type.isRecord()) {
            return;
        }
        for (RecordComponent comp : type.getRecordComponents()) {
            result.putIfAbsent(comp.getName(), unreflect(comp.getAccessor()));
        }
    }

    private static void discoverGetterProperties(Class<?> type, Map<String, MethodHandle> result) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getParameterCount() != 0
                        || m.getReturnType() == void.class
                        || Modifier.isStatic(m.getModifiers())
                        || m.isSynthetic()) {
                    continue;
                }
                String propertyName = propertyNameFromGetter(m);
                if (propertyName != null) {
                    result.putIfAbsent(propertyName, unreflect(m));
                }
            }
        }
    }

    private static void discoverFieldProperties(Class<?> type, Map<String, MethodHandle> result) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.isSynthetic() || Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                result.putIfAbsent(f.getName(), unreflectGetter(f));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Low-level utilities — also used by ExpressionEnvironmentBuilder
    // -------------------------------------------------------------------------

    public static String propertyNameFromGetter(Method method) {
        if (method.getParameterCount() != 0 || method.getReturnType() == void.class) {
            return null;
        }
        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
            return decapitalize(name.substring(3));
        }
        if ((method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)
                && name.startsWith("is") && name.length() > 2) {
            return decapitalize(name.substring(2));
        }
        return null;
    }

    public static String decapitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        if (value.length() > 1 && Character.isUpperCase(value.charAt(0)) && Character.isUpperCase(value.charAt(1))) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    public static MethodHandle unreflect(Method method) {
        try {
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("failed to create method handle for " + method, e);
        }
    }

    public static MethodHandle unreflectGetter(Field field) {
        try {
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("failed to create field getter handle for " + field, e);
        }
    }
}
