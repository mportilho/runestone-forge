package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ObjectType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import com.runestone.expeval_mk3.support.EnvironmentConfigurations;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the non-retention contract from issues #94 and #95 across every migrated node family: the
 * scalar/core nodes (literal, frame read, unary, postfix, binary, between, membership, coalescence,
 * conditional, call) and the navigation/collection-operation nodes (index/slice/map-key subscript,
 * filter, wildcard, contextual member, registered property, and collection operation). Every navigation
 * kind now has its own dedicated {@link ExecutableNode} class instead of the AST-closing
 * {@code LinkExecutableNode} bridge issue #95 removed; the walk below recurses into any retained record
 * (not just executable children) so a resolved-descriptor record that still points back at an AST node
 * — e.g. a {@code CollectionOperationBinding} carrying a {@code LambdaBinding.lambda()} — is caught too.
 */
class ExecutionPlanNonRetentionTest {

    private static final String SOURCE =
            "a := 1; b := [1, 2, 3]; c := a!; d := (a ?? 2) + c; e := -a; "
                    + "f := b[0]; g := b[0:2]; h := m[\"A\"]; i := b[?(@ > 1)]; j := b[*]; k := m[*]; "
                    + "l := b.map(@ -> @ + 1); n := b.sum(); o := b.count(); p := m.keys(); q := m.values(); "
                    + "r := b.any(@ -> @ > 2); s := b.all(@ -> @ > 0); t := b.avg(); "
                    + "u := b.reduce(0, @ -> @.accumulator + @.item); v := b.sortBy(@ -> @, \"asc\"); "
                    + "w := customer.name; x := customer.score; "
                    + "(d > 0 and a between 0 and 10) or (a = 1) or (a ^ 2 = 1) or (sqrt(a) > 0) "
                    + "or (a in b) or (e < 0) or (if(a > 0, true, false))";

    @Test
    void planGraphNeverReachesTheEnvironmentOrTheAstOrTheSemanticModel() {
        ExpressionEnvironment environment = environment();
        SemanticModel model = resolve(SOURCE, environment);
        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Object> queue = new ArrayDeque<>();
        enqueue(queue, plan.resultExpression());
        plan.assignments().forEach(assignment -> enqueue(queue, expressionOf(assignment)));

        while (!queue.isEmpty()) {
            Object value = queue.poll();
            if (!visited.add(value)) {
                continue;
            }
            assertNotRetained(value);
            if (value instanceof ExecutableNode node) {
                assertThat(node.id()).as("every node preserves its NodeId, found %s", node.getClass()).isNotNull();
                assertThat(node.sourceSpan())
                        .as("every node preserves its SourceSpan, found %s", node.getClass())
                        .isNotNull();
                fieldValues(node).forEach(fieldValue -> enqueue(queue, fieldValue));
            } else if (value.getClass().isRecord()) {
                recordComponentValues(value).forEach(fieldValue -> enqueue(queue, fieldValue));
            } else if (value instanceof List<?> list) {
                list.forEach(element -> enqueue(queue, element));
            }
        }
    }

    @Test
    void everyCoreNodePreservesItsOriginatingNodeIdAndSourceSpan() {
        ExpressionEnvironment environment = environment();
        SemanticModel model = resolve(SOURCE, environment);
        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        ExecutableNode result = plan.resultExpression();
        assertThat(result.id()).isNotNull();
        assertThat(result.sourceSpan()).isEqualTo(model.ast().resultExpression().orElseThrow().sourceSpan());
    }

    private static ExpressionEnvironment environment() {
        return ExpressionEnvironment.builder()
                .externalSymbol("dummy", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("A", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol(
                        "customer",
                        new ObjectType(EnvironmentConfigurations.CustomerProfile.class.getName()),
                        new EnvironmentConfigurations.CustomerProfile("Ana", BigDecimal.TEN),
                        ExternalSymbolOverwritePolicy.FIXED)
                .registerJavaType(EnvironmentConfigurations.CustomerProfile.class)
                .build();
    }

    private static void enqueue(Deque<Object> queue, Object value) {
        if (value != null) {
            queue.add(value);
        }
    }

    private static Object expressionOf(AssignmentExecutable assignment) {
        try {
            Field field = AssignmentExecutable.class.getDeclaredField("expression");
            field.setAccessible(true);
            return field.get(assignment);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void assertNotRetained(Object value) {
        Class<?> type = value.getClass();
        assertThat(ExpressionEnvironment.class.isAssignableFrom(type))
                .as("must not retain the whole ExpressionEnvironment, found %s", type)
                .isFalse();
        assertThat(SemanticModel.class.isAssignableFrom(type))
                .as("must not retain the SemanticModel, found %s", type)
                .isFalse();
        // Small resolved-operator enums (BinaryOperator, UnaryOperator, PostfixOperator, ...) are exactly
        // what the plan is allowed to keep; only tree/model-shaped AST node types are forbidden.
        boolean isAstNode = type.getPackageName().equals("com.runestone.expeval_mk3.internal.ast")
                && type != NodeId.class
                && !type.isEnum();
        assertThat(isAstNode).as("must not retain an AST node, found %s", type).isFalse();
    }

    private static List<Object> fieldValues(ExecutableNode node) {
        RecordComponent[] components = node.getClass().getRecordComponents();
        if (components != null) {
            return recordComponentValues(node, components);
        }
        List<Object> values = new java.util.ArrayList<>();
        for (Field field : node.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            values.add(readField(field, node));
        }
        return values;
    }

    private static List<Object> recordComponentValues(Object record) {
        return recordComponentValues(record, record.getClass().getRecordComponents());
    }

    private static List<Object> recordComponentValues(Object target, RecordComponent[] components) {
        List<Object> values = new java.util.ArrayList<>();
        for (RecordComponent component : components) {
            values.add(invoke(component, target));
        }
        return values;
    }

    private static Object invoke(RecordComponent component, Object target) {
        try {
            return component.getAccessor().invoke(target);
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

    private static SemanticModel resolve(String source, ExpressionEnvironment environment) {
        ExpressionFileNode ast = ast(source);
        SemanticResolutionSuccess result = (SemanticResolutionSuccess) new SemanticResolver().resolve(ast, environment);
        return result.model();
    }

    private static ExpressionFileNode ast(String source) {
        ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(source);
        return ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
    }
}
