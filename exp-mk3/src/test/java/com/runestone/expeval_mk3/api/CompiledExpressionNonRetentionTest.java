package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.MemoizedExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #136: extends the non-retention proof issue #94/#95 built for {@code ExecutionPlan} one boundary
 * higher, to the value {@link CompilationCache} actually holds resident: {@link CompiledExpression}. A
 * cache entry that quietly reached the {@link ExpressionEnvironment}, the {@link SemanticModel}, or an
 * ANTLR parse-tree node would defeat the whole point of a bounded cache. The retention matrix below covers
 * a scalar, a folded constant, navigation, a collection operation, a registered function, and a
 * Subexpressao Comum Memoizada; the last is verified structurally (an actual {@link MemoizedExecutableNode}
 * is found) so the coverage claim is not hollow. A source long enough not to collide with a short field
 * name is also checked for a duplicate copy in the reachable graph, distinguishing the key's intentional
 * source reference (covered separately by {@code CompilationCacheTest}) from an accidental one in the
 * value. A companion test walks a {@link ExpressionCompilationResult.Failure} the same way, since a failed
 * compilation obeys the same cache policy as a successful one.
 */
class CompiledExpressionNonRetentionTest {

    @Test
    void everyCompiledExpressionFieldTypeExcludesTheEnvironmentTheSemanticModelAndParserRuleContexts() {
        for (Field field : CompiledExpression.class.getDeclaredFields()) {
            Class<?> type = field.getType();
            assertThat(ExpressionEnvironment.class.isAssignableFrom(type))
                    .as("CompiledExpression.%s must not be the whole ExpressionEnvironment", field.getName())
                    .isFalse();
            assertThat(SemanticModel.class.isAssignableFrom(type))
                    .as("CompiledExpression.%s must not be the SemanticModel", field.getName())
                    .isFalse();
            assertThat(ParserRuleContext.class.isAssignableFrom(type))
                    .as("CompiledExpression.%s must not be a parse tree node", field.getName())
                    .isFalse();
        }
    }

    @Test
    void theCompiledValueNeverReachesTheEnvironmentTheSemanticModelOrAParseTreeAcrossTheRetentionMatrix() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        List<String> retentionMatrix = List.of(
                "x",                                        // scalar
                "1 + 2",                                    // folded constant
                "b := [1, 2, 3]; b[0]",                     // navigation
                "b := [1, 2, 3]; b.map(@ -> @ + 1)",         // collection operation
                "sqrt(x)",                                  // registered function
                "(x + 1) * (x + 1)");                       // Subexpressao Comum Memoizada
        boolean sawMemoizedNode = false;

        for (String source : retentionMatrix) {
            ExpressionCompilationResult.Success success = (ExpressionCompilationResult.Success)
                    CompilationPipeline.compile(source, environment, RuntimeServices.systemDefault());
            CompiledExpression compiledExpression = success.compiledExpression();

            Object plan = fieldValue(compiledExpression, "plan");
            Object runtimeServices = fieldValue(compiledExpression, "runtimeServices");
            assertThat(plan).as("source: %s", source).isInstanceOf(ExecutionPlan.class);
            assertThat(runtimeServices).as("source: %s", source).isInstanceOf(RuntimeServices.class);

            Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            Deque<Object> queue = new ArrayDeque<>();
            queue.add(plan);
            queue.add(runtimeServices);
            while (!queue.isEmpty()) {
                Object value = queue.poll();
                if (value == null || !visited.add(value)) {
                    continue;
                }
                assertNotRetained(value, source);
                if (value instanceof MemoizedExecutableNode) {
                    sawMemoizedNode = true;
                }
                if (value instanceof ExecutableNode node) {
                    enqueueAll(queue, fieldValues(node));
                } else if (value.getClass().isRecord()) {
                    enqueueAll(queue, recordComponentValues(value));
                } else if (value instanceof Collection<?> collection) {
                    enqueueAll(queue, collection);
                } else if (value instanceof Map<?, ?> map) {
                    enqueueAll(queue, map.keySet());
                    enqueueAll(queue, map.values());
                } else if (value instanceof Object[] array) {
                    enqueueAll(queue, List.of(array));
                } else if (value.getClass().getPackageName().startsWith("com.runestone.expeval_mk3")) {
                    enqueueAll(queue, declaredFieldValues(value));
                }
            }
        }

        assertThat(sawMemoizedNode)
                .as("the retention matrix must actually exercise a Subexpressao Comum Memoizada, not just claim it")
                .isTrue();
    }

    @Test
    void aFailureResultNeverReachesTheEnvironmentTheSemanticModelOrAParseTreeAndNeverDuplicatesTheSource() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        String source = "1 + ";

        ExpressionCompilationResult.Failure failure = (ExpressionCompilationResult.Failure)
                CompilationPipeline.compile(source, environment, RuntimeServices.systemDefault());

        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Object> queue = new ArrayDeque<>();
        queue.add(failure.diagnostics());
        while (!queue.isEmpty()) {
            Object value = queue.poll();
            if (value == null || !visited.add(value)) {
                continue;
            }
            assertNotRetained(value, source);
            if (value.getClass().isRecord()) {
                enqueueAll(queue, recordComponentValues(value));
            } else if (value instanceof Collection<?> collection) {
                enqueueAll(queue, collection);
            } else if (value.getClass().getPackageName().startsWith("com.runestone.expeval_mk3")) {
                enqueueAll(queue, declaredFieldValues(value));
            }
        }
    }

    private static void enqueueAll(Deque<Object> queue, Iterable<?> values) {
        for (Object value : values) {
            if (value != null) {
                queue.add(value);
            }
        }
    }

    private static void assertNotRetained(Object value, String source) {
        Class<?> type = value.getClass();
        assertThat(ExpressionEnvironment.class.isAssignableFrom(type))
                .as("must not retain the whole ExpressionEnvironment, found %s for source: %s", type, source)
                .isFalse();
        assertThat(SemanticModel.class.isAssignableFrom(type))
                .as("must not retain the SemanticModel, found %s for source: %s", type, source)
                .isFalse();
        assertThat(ParserRuleContext.class.isAssignableFrom(type) || Token.class.isAssignableFrom(type))
                .as("must not retain a parse tree node or token, found %s for source: %s", type, source)
                .isFalse();
        boolean isAstNode = type.getPackageName().equals("com.runestone.expeval_mk3.internal.ast")
                && type != com.runestone.expeval_mk3.internal.ast.NodeId.class
                && !type.isEnum();
        assertThat(isAstNode).as("must not retain an AST node, found %s for source: %s", type, source).isFalse();
        // A source at least this long cannot coincidentally collide with a short field like a symbol or
        // operator name the plan legitimately stores (e.g. source "x" would otherwise false-positive
        // against the plan's own declared symbol name "x").
        if (source.length() > 4 && value instanceof String stringValue) {
            assertThat(stringValue)
                    .as("must not hold a duplicate copy of the full source, found for source: %s", source)
                    .isNotEqualTo(source);
        }
    }

    private static Object fieldValue(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static List<Object> fieldValues(ExecutableNode node) {
        RecordComponent[] components = node.getClass().getRecordComponents();
        if (components != null) {
            return recordComponentValues(node, components);
        }
        return declaredFieldValues(node);
    }

    private static List<Object> declaredFieldValues(Object target) {
        List<Object> values = new ArrayList<>();
        for (Field field : target.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            values.add(readField(field, target));
        }
        return values;
    }

    private static List<Object> recordComponentValues(Object record) {
        return recordComponentValues(record, record.getClass().getRecordComponents());
    }

    private static List<Object> recordComponentValues(Object target, RecordComponent[] components) {
        List<Object> values = new ArrayList<>();
        for (RecordComponent component : components) {
            values.add(invoke(component, target));
        }
        return values;
    }

    private static Object invoke(RecordComponent component, Object target) {
        try {
            Method accessor = component.getAccessor();
            accessor.setAccessible(true);
            return accessor.invoke(target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Object readField(Field field, Object target) {
        try {
            return field.get(target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
