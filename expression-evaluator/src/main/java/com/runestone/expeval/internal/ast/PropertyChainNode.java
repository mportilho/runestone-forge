package com.runestone.expeval.internal.ast;

import com.runestone.expeval.internal.navigation.MapProjectionKind;
import com.runestone.expeval.internal.navigation.VectorAggregationKind;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record PropertyChainNode(
        NodeId nodeId,
        SourceSpan sourceSpan,
        String rootIdentifier,
        List<MemberAccess> chain
) implements ExpressionNode {

    public PropertyChainNode {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        Objects.requireNonNull(sourceSpan, "sourceSpan must not be null");
        Objects.requireNonNull(rootIdentifier, "rootIdentifier must not be null");
        chain = List.copyOf(Objects.requireNonNull(chain, "chain must not be null"));
    }

    public sealed interface MemberAccess permits
            PropertyAccess, SafePropertyAccess, MethodCallAccess, SafeMethodCallAccess,
            CollectionIndexStep, MapKeyStep, CollectionSliceStep, WildcardStep,
            FilterPredicateStep, DeepScanStep, CollectionFunctionStep, MapProjectionStep,
            VectorAggregationStep {}

    // -------------------------------------------------------------------------
    // Existing accesses (dot-notation, safe-nav, method calls)
    // -------------------------------------------------------------------------

    public record PropertyAccess(String name) implements MemberAccess {
        public PropertyAccess {
            Objects.requireNonNull(name, "name must not be null");
        }
    }

    public record SafePropertyAccess(String name) implements MemberAccess {
        public SafePropertyAccess {
            Objects.requireNonNull(name, "name must not be null");
        }
    }

    public record MethodCallAccess(String name, List<ExpressionNode> arguments) implements MemberAccess {
        public MethodCallAccess {
            Objects.requireNonNull(name, "name must not be null");
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
        }
    }

    public record SafeMethodCallAccess(String name, List<ExpressionNode> arguments) implements MemberAccess {
        public SafeMethodCallAccess {
            Objects.requireNonNull(name, "name must not be null");
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
        }
    }

    // -------------------------------------------------------------------------
    // New collection / map navigation steps
    // -------------------------------------------------------------------------

    /** {@code [n]} or {@code [-n]} — zero-based index access into a list. */
    public record CollectionIndexStep(ExpressionNode index) implements MemberAccess {
        public CollectionIndexStep {
            Objects.requireNonNull(index, "index must not be null");
        }
    }

    /** {@code ["key"]} — literal key lookup in a {@code Map<String, ?>}. */
    public record MapKeyStep(String key) implements MemberAccess {
        public MapKeyStep {
            Objects.requireNonNull(key, "key must not be null");
        }
    }

    /**
     * {@code [start:end]}, {@code [:end]}, or {@code [start:]} — Python-style slice.
     * A {@code null} start means "from the beginning"; a {@code null} end means "to the end".
     */
    public record CollectionSliceStep(
            @Nullable ExpressionNode start,
            @Nullable ExpressionNode end
    ) implements MemberAccess {}

    /** {@code [*]} or {@code .*} — wildcard; returns all elements / values. */
    public record WildcardStep() implements MemberAccess {}

    /** {@code [?(<expr>)]} — filter predicate; retains only matching elements. */
    public record FilterPredicateStep(ExpressionNode predicate) implements MemberAccess {
        public FilterPredicateStep {
            Objects.requireNonNull(predicate, "predicate must not be null");
        }
    }

    /**
     * {@code ..name} or {@code ..*} — recursive deep scan.
     * A {@code null} propertyName means wildcard ({@code ..*}).
     */
    public record DeepScanStep(@Nullable String propertyName) implements MemberAccess {}

    /** {@code ..funcName(args)} — invoke a {@link com.runestone.expeval.catalog.FunctionCatalog} function
     *  with the current collection/map as the implicit first argument. */
    public record CollectionFunctionStep(String name, List<ExpressionNode> arguments) implements MemberAccess {
        public CollectionFunctionStep {
            Objects.requireNonNull(name, "name must not be null");
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
        }
    }

    /** {@code ..keys()} or {@code ..values()} — built-in map projection. */
    public record MapProjectionStep(MapProjectionKind kind) implements MemberAccess {
        public MapProjectionStep {
            Objects.requireNonNull(kind, "kind must not be null");
        }
    }

    /** {@code ..sum()}, {@code ..avg()}, {@code ..min()}, {@code ..max()}, {@code ..count()} etc. */
    public record VectorAggregationStep(VectorAggregationKind kind) implements MemberAccess {
        public VectorAggregationStep {
            Objects.requireNonNull(kind, "kind must not be null");
        }
    }
}
