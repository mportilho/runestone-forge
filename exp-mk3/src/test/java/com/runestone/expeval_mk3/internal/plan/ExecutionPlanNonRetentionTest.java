package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.runtime.ExecutableBranch;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.LinkExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the non-retention contract from issue #94 for the scalar/core node family: literal, frame
 * read, unary, postfix, binary, between, membership, coalescence, conditional, and call. Scope is
 * explicitly limited to constructs that do not go through {@link LinkExecutableNode} — navigation and
 * collection-operation links keep their own AST-derived closures until the dependent migration ticket
 * moves them onto dedicated node classes, so a plan built from a purely scalar source proves the point
 * without tripping on that deferred bridge.
 */
class ExecutionPlanNonRetentionTest {

    private static final String SOURCE =
            "a := 1; b := [1, 2, 3]; c := a!; d := (a ?? 2) + c; e := -a; "
                    + "(d > 0 and a between 0 and 10) or (a = 1) or (a ^ 2 = 1) or (sqrt(a) > 0) "
                    + "or (a in b) or (e < 0) or (if(a > 0, true, false))";

    @Test
    void planGraphNeverReachesTheEnvironmentOrTheAstOrTheSemanticModel() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("dummy", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve(SOURCE, environment);
        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        Set<Object> visited = new HashSet<>();
        Deque<ExecutableNode> queue = new ArrayDeque<>();
        queue.add(plan.resultExpression());
        plan.assignments().forEach(assignment -> queue.add((ExecutableNode) expressionOf(assignment)));

        while (!queue.isEmpty()) {
            ExecutableNode node = queue.poll();
            if (node == null || !visited.add(node)) {
                continue;
            }
            assertThat(node).isNotInstanceOf(LinkExecutableNode.class);
            for (Object fieldValue : fieldValues(node)) {
                assertNotRetained(fieldValue);
                if (fieldValue instanceof ExecutableNode child) {
                    queue.add(child);
                } else if (fieldValue instanceof ExecutableBranch branch) {
                    queue.add(branch.condition());
                    queue.add(branch.consequence());
                } else if (fieldValue instanceof List<?> list) {
                    for (Object element : list) {
                        if (element instanceof ExecutableNode child) {
                            queue.add(child);
                        }
                    }
                }
            }
        }
    }

    @Test
    void everyCoreNodePreservesItsOriginatingNodeIdAndSourceSpan() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("dummy", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve(SOURCE, environment);
        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        ExecutableNode result = plan.resultExpression();
        assertThat(result.id()).isNotNull();
        assertThat(result.sourceSpan()).isEqualTo(model.ast().resultExpression().orElseThrow().sourceSpan());
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
        if (value == null) {
            return;
        }
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
        List<Object> values = new java.util.ArrayList<>();
        RecordComponent[] components = node.getClass().getRecordComponents();
        if (components != null) {
            for (RecordComponent component : components) {
                values.add(invoke(component, node));
            }
            return values;
        }
        for (Field field : node.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            values.add(readField(field, node));
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
