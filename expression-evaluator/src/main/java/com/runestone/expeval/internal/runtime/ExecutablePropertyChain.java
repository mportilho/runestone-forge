package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.types.ResolvedType;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Objects;

/**
 * Executable representation of a dot-chain navigation expression such as
 * {@code usuario.nome} or {@code pedido.calcularTotal(taxa)}.
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
            ReflectiveMethodInvoke {
    }

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
}
