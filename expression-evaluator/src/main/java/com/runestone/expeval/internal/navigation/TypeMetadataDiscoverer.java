package com.runestone.expeval.internal.navigation;

import com.runestone.expeval.catalog.MethodDescriptor;
import com.runestone.expeval.catalog.PropertyDescriptor;
import com.runestone.expeval.catalog.TypeMetadata;
import com.runestone.expeval.types.ObjectType;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ResolvedTypes;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Centralizes Java member discovery policies for typed metadata and runtime fallback access.
 *
 * <p>Type-hint metadata intentionally exposes only public Java members. Runtime fallback keeps the
 * historical declared-member policy so untyped expressions can still navigate accessible object
 * graphs through cached method handles.
 */
public final class TypeMetadataDiscoverer {

    private TypeMetadataDiscoverer() {
    }

    public static TypeMetadata discoverPublicMetadata(Class<?> type, Collection<Class<?>> registeredTypes) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(registeredTypes, "registeredTypes must not be null");

        Set<Class<?>> registeredTypeSet = new LinkedHashSet<>(registeredTypes);
        Map<String, PropertyDescriptor> properties = new LinkedHashMap<>();
        Map<String, List<MethodDescriptor>> methods = new LinkedHashMap<>();

        discoverRecordPropertyDescriptors(type, properties, registeredTypeSet);
        discoverPublicGetterPropertyDescriptors(type, properties, registeredTypeSet);
        discoverPublicFieldPropertyDescriptors(type, properties, registeredTypeSet);
        discoverPublicMethodDescriptors(type, methods, registeredTypeSet);

        return new TypeMetadata(type, properties, methods);
    }

    static Map<String, MethodHandle> discoverRuntimePropertyHandles(Class<?> type) {
        Objects.requireNonNull(type, "type must not be null");

        Map<String, MethodHandle> result = new LinkedHashMap<>();
        discoverRecordPropertyHandles(type, result);
        discoverDeclaredGetterPropertyHandles(type, result);
        discoverDeclaredFieldPropertyHandles(type, result);
        return Map.copyOf(result);
    }

    static Map<String, Map<Integer, MethodHandle>> discoverRuntimeMethodHandles(Class<?> type) {
        Objects.requireNonNull(type, "type must not be null");

        Map<String, Map<Integer, MethodHandle>> result = new LinkedHashMap<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()) || method.isSynthetic() || method.isBridge()) {
                    continue;
                }
                result.computeIfAbsent(method.getName(), ignored -> new LinkedHashMap<>())
                        .putIfAbsent(method.getParameterCount(), unreflect(method));
            }
        }
        Map<String, Map<Integer, MethodHandle>> copy = new LinkedHashMap<>(result.size());
        result.forEach((name, byArity) -> copy.put(name, Map.copyOf(byArity)));
        return Map.copyOf(copy);
    }

    private static void discoverRecordPropertyDescriptors(
            Class<?> type,
            Map<String, PropertyDescriptor> properties,
            Set<Class<?>> registeredTypes) {
        if (!type.isRecord()) {
            return;
        }
        for (RecordComponent component : type.getRecordComponents()) {
            properties.putIfAbsent(component.getName(), new PropertyDescriptor(
                    component.getName(),
                    unreflect(component.getAccessor()),
                    resolveDeclaredType(component.getType(), registeredTypes)
            ));
        }
    }

    private static void discoverPublicGetterPropertyDescriptors(
            Class<?> type,
            Map<String, PropertyDescriptor> properties,
            Set<Class<?>> registeredTypes) {
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class
                    || method.isSynthetic()
                    || method.isBridge()
                    || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            String propertyName = propertyNameFromGetter(method);
            if (propertyName == null) {
                continue;
            }
            properties.putIfAbsent(propertyName, new PropertyDescriptor(
                    propertyName,
                    unreflect(method),
                    resolveDeclaredType(method.getReturnType(), registeredTypes)
            ));
        }
    }

    private static void discoverPublicFieldPropertyDescriptors(
            Class<?> type,
            Map<String, PropertyDescriptor> properties,
            Set<Class<?>> registeredTypes) {
        for (Field field : type.getFields()) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            properties.putIfAbsent(field.getName(), new PropertyDescriptor(
                    field.getName(),
                    unreflectGetter(field),
                    resolveDeclaredType(field.getType(), registeredTypes)
            ));
        }
    }

    private static void discoverPublicMethodDescriptors(
            Class<?> type,
            Map<String, List<MethodDescriptor>> methods,
            Set<Class<?>> registeredTypes) {
        Set<String> seenSignatures = new LinkedHashSet<>();
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class
                    || method.isSynthetic()
                    || method.isBridge()
                    || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            String signature = method.getName() + List.of(method.getParameterTypes());
            if (!seenSignatures.add(signature)) {
                continue;
            }
            List<Class<?>> parameterTypes = List.of(method.getParameterTypes());
            methods.computeIfAbsent(method.getName(), ignored -> new ArrayList<>())
                    .add(new MethodDescriptor(
                            method.getName(),
                            unreflect(method),
                            parameterTypes,
                            resolveDeclaredType(method.getReturnType(), registeredTypes)
                    ));
        }
    }

    private static void discoverRecordPropertyHandles(Class<?> type, Map<String, MethodHandle> result) {
        if (!type.isRecord()) {
            return;
        }
        for (RecordComponent component : type.getRecordComponents()) {
            result.putIfAbsent(component.getName(), unreflect(component.getAccessor()));
        }
    }

    private static void discoverDeclaredGetterPropertyHandles(Class<?> type, Map<String, MethodHandle> result) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() != 0
                        || method.getReturnType() == void.class
                        || Modifier.isStatic(method.getModifiers())
                        || method.isSynthetic()) {
                    continue;
                }
                String propertyName = propertyNameFromGetter(method);
                if (propertyName != null) {
                    result.putIfAbsent(propertyName, unreflect(method));
                }
            }
        }
    }

    private static void discoverDeclaredFieldPropertyHandles(Class<?> type, Map<String, MethodHandle> result) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                result.putIfAbsent(field.getName(), unreflectGetter(field));
            }
        }
    }

    private static String propertyNameFromGetter(Method method) {
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

    private static String decapitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        if (value.length() > 1 && Character.isUpperCase(value.charAt(0)) && Character.isUpperCase(value.charAt(1))) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private static MethodHandle unreflect(Method method) {
        try {
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("failed to create method handle for " + method, exception);
        }
    }

    private static MethodHandle unreflectGetter(Field field) {
        try {
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectGetter(field);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("failed to create field getter handle for " + field, exception);
        }
    }

    private static ResolvedType resolveDeclaredType(Class<?> javaType, Set<Class<?>> registeredTypes) {
        return registeredTypes.contains(javaType) ? new ObjectType(javaType) : ResolvedTypes.fromJavaType(javaType);
    }
}
