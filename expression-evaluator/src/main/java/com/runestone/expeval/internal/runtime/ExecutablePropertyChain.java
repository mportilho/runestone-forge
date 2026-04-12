package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.navigation.MapProjectionKind;
import com.runestone.expeval.internal.navigation.NavigationMode;
import com.runestone.expeval.internal.navigation.VectorAggregationKind;
import com.runestone.expeval.types.ResolvedType;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Objects;

/**
 * Executable representation of a navigation expression such as
 * {@code store.book[*].author} or {@code store..price..sum()}.
 *
 * <p>When type hints are available the chain carries precomputed {@link MethodHandle}s.
 * For dynamic chains without hints, runtime reflection remains as a fallback.
 */
record ExecutablePropertyChain(
        ExecutableNode root,
        List<ExecutableAccess> chain
) implements ExecutableNode {

    ExecutablePropertyChain {
        Objects.requireNonNull(root, "root must not be null");
        chain = List.copyOf(Objects.requireNonNull(chain, "chain must not be null"));
    }

    sealed interface ExecutableAccess permits
            ExecutableFieldGet,
            ExecutableMethodInvoke,
            ReflectivePropertyAccess,
            ReflectiveMethodInvoke,
            ExecutableIndexAccess,
            ExecutableMapKeyAccess,
            ExecutableSliceAccess,
            ExecutableWildcard,
            ExecutableFilterPredicate,
            ExecutableDeepScan,
            ExecutableCollectionFunction,
            ExecutableMapProjection,
            ExecutableVectorAggregation {
    }

    // -------------------------------------------------------------------------
    // Existing typed accesses (dot-notation, method calls)
    // -------------------------------------------------------------------------

    record ExecutableFieldGet(
            String name,
            MethodHandle getter,
            ResolvedType resolvedType,
            boolean safe
    ) implements ExecutableAccess {

        ExecutableFieldGet {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(getter, "getter must not be null");
            Objects.requireNonNull(resolvedType, "resolvedType must not be null");
        }
    }

    record ExecutableMethodInvoke(
            String name,
            MethodHandle handle,
            List<ExecutableNode> arguments,
            List<Class<?>> parameterTypes,
            ResolvedType returnType,
            boolean safe
    ) implements ExecutableAccess {

        ExecutableMethodInvoke {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(handle, "handle must not be null");
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
            parameterTypes = List.copyOf(Objects.requireNonNull(parameterTypes, "parameterTypes must not be null"));
            Objects.requireNonNull(returnType, "returnType must not be null");
            if (arguments.size() != parameterTypes.size()) {
                throw new IllegalArgumentException("arguments size does not match parameterTypes size");
            }
        }
    }

    record ReflectivePropertyAccess(String name, boolean safe) implements ExecutableAccess {
        ReflectivePropertyAccess {
            Objects.requireNonNull(name, "name must not be null");
        }
    }

    record ReflectiveMethodInvoke(String name, List<ExecutableNode> arguments, boolean safe) implements ExecutableAccess {
        ReflectiveMethodInvoke {
            Objects.requireNonNull(name, "name must not be null");
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
        }
    }

    // -------------------------------------------------------------------------
    // New collection / map navigation steps
    // -------------------------------------------------------------------------

    /** {@code [n]} or {@code [-n]} — single index access. */
    record ExecutableIndexAccess(ExecutableNode index) implements ExecutableAccess {
        ExecutableIndexAccess {
            Objects.requireNonNull(index, "index must not be null");
        }
    }

    /** {@code ["key"]} — literal key lookup in a {@code Map<String, ?>}. */
    record ExecutableMapKeyAccess(String key) implements ExecutableAccess {
        ExecutableMapKeyAccess {
            Objects.requireNonNull(key, "key must not be null");
        }
    }

    /** {@code [start:end]}, {@code [:end]}, {@code [start:]} — slice. */
    record ExecutableSliceAccess(
            @Nullable ExecutableNode start,
            @Nullable ExecutableNode end
    ) implements ExecutableAccess {}

    /** {@code [*]} or {@code .*} — wildcard, returns all elements/values. */
    record ExecutableWildcard() implements ExecutableAccess {}

    /** {@code [?(<predicate>)]} — filter; evaluated element-by-element. */
    record ExecutableFilterPredicate(ExecutableNode predicate) implements ExecutableAccess {
        ExecutableFilterPredicate {
            Objects.requireNonNull(predicate, "predicate must not be null");
        }
    }

    /** {@code ..name} or {@code ..*} — recursive deep scan. */
    record ExecutableDeepScan(@Nullable String propertyName) implements ExecutableAccess {}

    /**
     * {@code ..funcName(args)} — invoke a {@link com.runestone.expeval.catalog.FunctionCatalog}
     * function with the current collection/map as the implicit first argument.
     */
    record ExecutableCollectionFunction(
            ResolvedFunctionBinding binding,
            List<ExecutableNode> arguments,
            NavigationMode resultMode
    ) implements ExecutableAccess {
        ExecutableCollectionFunction {
            Objects.requireNonNull(binding, "binding must not be null");
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
            Objects.requireNonNull(resultMode, "resultMode must not be null");
        }
    }

    /** {@code ..keys()} or {@code ..values()}. */
    record ExecutableMapProjection(MapProjectionKind kind) implements ExecutableAccess {
        ExecutableMapProjection {
            Objects.requireNonNull(kind, "kind must not be null");
        }
    }

    /** {@code ..sum()}, {@code ..count()}, etc. */
    record ExecutableVectorAggregation(VectorAggregationKind kind) implements ExecutableAccess {
        ExecutableVectorAggregation {
            Objects.requireNonNull(kind, "kind must not be null");
        }
    }
}
