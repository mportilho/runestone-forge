package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.regex.LinearRegex;
import com.runestone.expeval_mk3.internal.regex.PreparedRegexCall;
import com.runestone.expeval_mk3.internal.runtime.ConstantExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutableBranch;
import com.runestone.expeval_mk3.internal.runtime.ExecutableLambda;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNodeRetainedPayload;
import com.runestone.expeval_mk3.internal.runtime.ExecutableOperationArguments;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Conservative unit estimator for the plan portion of a cache entry. The calibrated units intentionally
 * describe admission pressure rather than exact heap bytes: fixed units cover immutable node layouts and
 * recursive units cover source-controlled values downloaded into the plan. It never traverses bindings,
 * descriptors, runtime services, or external defaults because those belong to trusted shared environments.
 * Shared immutable payload reached by multiple plan edges is conservatively charged per edge; executable
 * plans are acyclic, so this avoids miss-path identity tables without risking unbounded traversal.
 */
final class PlanRetainedWeight {

    static final int PLAN_UNITS = 256;
    static final int EXECUTABLE_NODE_UNITS = 128;
    static final int ASSIGNMENT_UNITS = 96;
    static final int ASSIGNED_SYMBOL_UNITS = 64;
    static final int FOLDED_READ_UNITS = 64;
    private static final int EXTERNAL_BINDING_UNITS = 48;
    private static final int COLLECTION_UNITS = 48;
    private static final int MAP_UNITS = 64;
    private static final int SET_UNITS = 64;
    private static final int ARRAY_UNITS = 32;
    private static final int REFERENCE_UNITS = 8;
    private static final int STRING_UNITS = 48;
    private static final int BIG_NUMBER_UNITS = 64;
    private static final int TEMPORAL_UNITS = 48;
    private static final int OTHER_CONSTANT_UNITS = 48;

    private PlanRetainedWeight() {
    }

    static int estimate(ExecutionPlan plan) {
        Estimator estimator = new Estimator(plan.foldedVariableReads());
        estimator.add(PLAN_UNITS);
        estimator.planOwnedContainers(plan);
        estimator.node(plan.resultExpression());
        for (AssignmentExecutable assignment : plan.assignments()) {
            estimator.add(ASSIGNMENT_UNITS);
            estimator.intArray(assignment.frameSlotCount());
            estimator.node(assignment.expression());
        }
        for (AssignedSymbol symbol : plan.assignedSymbolsInCreationOrder()) {
            estimator.add(ASSIGNED_SYMBOL_UNITS);
            estimator.value(symbol.name());
        }
        for (FoldedRead foldedRead : plan.foldedVariableReads()) {
            estimator.add(FOLDED_READ_UNITS);
            estimator.value(foldedRead.symbolName());
        }
        estimator.add(plan.fullCalculationMemorySchema().estimatedRetainedWeight());
        estimator.add(plan.assignmentCalculationMemorySchema().estimatedRetainedWeight());
        return estimator.result();
    }

    private static final class Estimator implements ExecutableNodeRetainedPayload.Visitor {

        private final Set<NodeId> environmentFoldedReadIds;
        private final Set<Object> environmentFoldedValues;
        private long units;

        private Estimator(List<FoldedRead> foldedReads) {
            if (foldedReads.isEmpty()) {
                environmentFoldedReadIds = Set.of();
                environmentFoldedValues = Set.of();
                return;
            }
            Set<NodeId> ids = Collections.newSetFromMap(new IdentityHashMap<>());
            for (FoldedRead foldedRead : foldedReads) {
                ids.add(foldedRead.nodeId());
            }
            environmentFoldedReadIds = ids;
            Set<Object> values = Collections.newSetFromMap(new IdentityHashMap<>());
            for (FoldedRead foldedRead : foldedReads) {
                values.add(foldedRead.foldedValue());
            }
            environmentFoldedValues = values;
        }

        @Override
        public void node(ExecutableNode node) {
            if (node == null) {
                return;
            }
            add(EXECUTABLE_NODE_UNITS);
            ExecutableNodeRetainedPayload.visit(node, this);
        }

        @Override
        public void value(Object value) {
            if (value == null || environmentFoldedValues.contains(value) || value instanceof NodeId
                    || value instanceof SourceSpan || value.getClass().isEnum()) {
                return;
            }
            if (value instanceof ExecutableNode node) {
                node(node);
                return;
            }
            if (value instanceof ExecutableBranch branch) {
                add(OTHER_CONSTANT_UNITS);
                node(branch.condition());
                node(branch.consequence());
                return;
            }
            if (value instanceof ExecutableLambda lambda) {
                add(OTHER_CONSTANT_UNITS);
                node(lambda.body());
                return;
            }
            if (value instanceof ExecutableOperationArguments arguments) {
                add(OTHER_CONSTANT_UNITS);
                value(arguments.valueArguments());
                value(arguments.lambdaArguments());
                return;
            }
            if (value instanceof LinearRegex regex) {
                add(regex.estimatedRetainedWeight());
                return;
            }
            if (value instanceof PreparedRegexCall preparedRegexCall) {
                add(preparedRegexCall.estimatedRetainedWeight());
                return;
            }
            if (value instanceof String string) {
                add((long) STRING_UNITS + (long) string.length() * Character.BYTES);
            } else if (value instanceof BigDecimal decimal) {
                add(BIG_NUMBER_UNITS);
                value(decimal.unscaledValue());
            } else if (value instanceof BigInteger integer) {
                add(BIG_NUMBER_UNITS + Integer.BYTES * ((integer.bitLength() + 31L) / 32L));
            } else if (value instanceof Temporal) {
                add(TEMPORAL_UNITS);
            } else if (value instanceof Collection<?> collection) {
                add((long) COLLECTION_UNITS + (long) collection.size() * REFERENCE_UNITS);
                collection.forEach(this::value);
            } else if (value instanceof Map<?, ?> map) {
                add((long) MAP_UNITS + (long) map.size() * REFERENCE_UNITS * 2);
                map.forEach((key, mappedValue) -> {
                    value(key);
                    value(mappedValue);
                });
            } else if (value instanceof Object[] array) {
                add((long) ARRAY_UNITS + (long) array.length * REFERENCE_UNITS);
                for (Object element : array) {
                    value(element);
                }
            } else if (value instanceof int[] array) {
                add((long) ARRAY_UNITS + (long) array.length * Integer.BYTES);
            } else if (value instanceof long[] array) {
                add((long) ARRAY_UNITS + (long) array.length * Long.BYTES);
            } else if (value instanceof byte[] array) {
                add((long) ARRAY_UNITS + array.length);
            } else if (value instanceof char[] array) {
                add((long) ARRAY_UNITS + (long) array.length * Character.BYTES);
            } else if (!(value instanceof Number) && !(value instanceof Boolean) && !(value instanceof Character)) {
                // A source-prepared value whose exact implementation is intentionally opaque to this estimator.
                add(OTHER_CONSTANT_UNITS);
            }
        }

        @Override
        public void constant(ConstantExecutableNode constant) {
            if (!environmentFoldedReadIds.contains(constant.id())) {
                constant.retainedValues().forEach(this::value);
            }
        }

        private void add(long additionalUnits) {
            units = Math.min(Integer.MAX_VALUE, units + Math.max(0, additionalUnits));
        }

        private void planOwnedContainers(ExecutionPlan plan) {
            int assignmentCount = plan.assignments().size();
            int externalBindingCount = plan.externalBindingCount();
            int declaredSymbolCount = plan.declaredSymbolCount();
            list(assignmentCount);
            list(externalBindingCount);
            add((long) externalBindingCount * EXTERNAL_BINDING_UNITS);
            map(externalBindingCount);
            list(declaredSymbolCount);
            set(declaredSymbolCount);
            list(plan.assignedSymbolsInCreationOrder().size());
            list(plan.foldedVariableReads().size());
            objectArray(plan.frameTemplateLength());
        }

        private void list(int size) {
            add((long) COLLECTION_UNITS + (long) size * REFERENCE_UNITS);
        }

        private void map(int size) {
            add((long) MAP_UNITS + (long) size * REFERENCE_UNITS * 2);
        }

        private void set(int size) {
            add((long) SET_UNITS + (long) size * REFERENCE_UNITS);
        }

        private void objectArray(int length) {
            add((long) ARRAY_UNITS + (long) length * REFERENCE_UNITS);
        }

        private void intArray(int length) {
            add((long) ARRAY_UNITS + (long) length * Integer.BYTES);
        }

        private int result() {
            return (int) Math.max(1, units);
        }
    }
}
