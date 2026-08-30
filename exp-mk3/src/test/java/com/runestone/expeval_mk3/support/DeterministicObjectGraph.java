package com.runestone.expeval_mk3.support;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/** Deterministic, identity-based graph inspection for non-retention tests. */
public final class DeterministicObjectGraph {

    private final List<ReachedObject> reached;

    private DeterministicObjectGraph(List<ReachedObject> reached) {
        this.reached = List.copyOf(reached);
    }

    public static DeterministicObjectGraph from(Object... roots) {
        Deque<ReachedObject> pending = new ArrayDeque<>();
        for (int index = 0; index < roots.length; index++) {
            if (roots[index] != null) {
                pending.addLast(new ReachedObject(roots[index], "root[" + index + "]"));
            }
        }

        Map<Object, Boolean> visited = new IdentityHashMap<>();
        List<ReachedObject> reached = new ArrayList<>();
        while (!pending.isEmpty()) {
            ReachedObject current = pending.removeFirst();
            if (visited.put(current.value(), Boolean.TRUE) != null) {
                continue;
            }
            reached.add(current);
            enqueueChildren(current, pending);
        }
        return new DeterministicObjectGraph(reached);
    }

    public boolean containsIdentity(Object expected) {
        return pathToIdentity(expected).isPresent();
    }

    public Optional<String> pathToIdentity(Object expected) {
        return reached.stream()
                .filter(item -> item.value() == expected)
                .map(ReachedObject::path)
                .findFirst();
    }

    public Optional<String> pathToType(Class<?> type) {
        return reached.stream()
                .filter(item -> type.isInstance(item.value()))
                .map(ReachedObject::path)
                .findFirst();
    }

    public List<Object> objects() {
        return reached.stream().map(ReachedObject::value).toList();
    }

    private static void enqueueChildren(ReachedObject current, Deque<ReachedObject> pending) {
        Object value = current.value();
        Class<?> type = value.getClass();
        if (type.isEnum()) {
            return;
        }
        if (type.isArray()) {
            if (!type.componentType().isPrimitive()) {
                for (int index = 0; index < Array.getLength(value); index++) {
                    enqueue(pending, Array.get(value, index), current.path() + "[" + index + "]");
                }
            }
            return;
        }
        if (value instanceof List<?> list) {
            for (int index = 0; index < list.size(); index++) {
                enqueue(pending, list.get(index), current.path() + "[" + index + "]");
            }
            return;
        }
        if (value instanceof Collection<?> collection) {
            int index = 0;
            for (Object element : collection) {
                enqueue(pending, element, current.path() + "[" + index++ + "]");
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            List<Map.Entry<?, ?>> entries = new ArrayList<>(map.entrySet());
            entries.sort(Comparator.comparing(entry -> String.valueOf(entry.getKey())));
            for (int index = 0; index < entries.size(); index++) {
                Map.Entry<?, ?> entry = entries.get(index);
                enqueue(pending, entry.getKey(), current.path() + ".key[" + index + "]");
                enqueue(pending, entry.getValue(), current.path() + ".value[" + index + "]");
            }
            return;
        }
        if (value instanceof Optional<?> optional) {
            optional.ifPresent(element -> enqueue(pending, element, current.path() + ".value"));
            return;
        }
        if (value instanceof AtomicReference<?> reference) {
            enqueue(pending, reference.get(), current.path() + ".value");
            return;
        }
        if (type.getModule().isNamed()) {
            return;
        }

        fields(type).stream()
                .filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.getType().isPrimitive())
                .forEach(field -> enqueue(pending, read(field, value), current.path() + "." + field.getName()));
    }

    private static List<Field> fields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type;
                current != null && current != Object.class && !current.getModule().isNamed();
                current = current.getSuperclass()) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
        }
        fields.sort(Comparator.comparing(field -> field.getDeclaringClass().getName() + "#" + field.getName()));
        return fields;
    }

    private static Object read(Field field, Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot inspect " + field, exception);
        }
    }

    private static void enqueue(Deque<ReachedObject> pending, Object value, String path) {
        if (value != null) {
            pending.addLast(new ReachedObject(value, path));
        }
    }

    private record ReachedObject(Object value, String path) {
    }
}
