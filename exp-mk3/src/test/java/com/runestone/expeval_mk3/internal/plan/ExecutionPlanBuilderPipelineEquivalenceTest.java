package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.runtime.ExecutableBranch;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the internal pipeline required by issue #94: skipping the (currently empty) transformation
 * step via {@code buildUnoptimized} produces the same plan shape as the public {@code build} entry
 * point, both for the assignments and for the result expression. This is the equivalence oracle future
 * optimization phases build on, so the comparison walks node identity/source-span/family rather than
 * only checking the computed value.
 */
class ExecutionPlanBuilderPipelineEquivalenceTest {

    private static final String SOURCE =
            "a := 1; b := [1, 2, 3]; c := a!; d := (a ?? 2) + c; e := -a; "
                    + "(d > 0 and a between 0 and 10) or (a = 1) or (a ^ 2 = 1) or (sqrt(a) > 0) "
                    + "or (a in b) or (e < 0) or (if(a > 0, true, false))";

    @Test
    void buildAndBuildUnoptimizedProduceStructurallyIdenticalPlans() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("dummy", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve(SOURCE, environment);
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

        ExecutionPlan viaPublicEntryPoint = builder.build(model, environment);
        ExecutionPlan viaUnoptimizedPath = builder.buildUnoptimized(model, environment);

        assertThat(viaPublicEntryPoint.hasResult()).isTrue();
        assertThat(dump(viaPublicEntryPoint.resultExpression()))
                .isEqualTo(dump(viaUnoptimizedPath.resultExpression()));
        assertThat(assignmentDumps(viaPublicEntryPoint)).isEqualTo(assignmentDumps(viaUnoptimizedPath));
        assertThat(viaPublicEntryPoint.compute(java.util.Map.of(), java.time.Clock.systemUTC()))
                .isEqualTo(viaUnoptimizedPath.compute(java.util.Map.of(), java.time.Clock.systemUTC()));
    }

    private static List<String> assignmentDumps(ExecutionPlan plan) {
        return plan.assignments().stream()
                .map(assignment -> assignment.id() + "@" + assignment.sourceSpan())
                .collect(Collectors.toList());
    }

    /** Structural dump: family class, node identity, source span, and children, in evaluation order. */
    private static String dump(ExecutableNode node) {
        if (node == null) {
            return "<none>";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(node.getClass().getSimpleName())
                .append('[').append(node.id()).append('@').append(node.sourceSpan()).append(']');
        for (ExecutableNode child : children(node)) {
            builder.append('(').append(dump(child)).append(')');
        }
        return builder.toString();
    }

    private static List<ExecutableNode> children(ExecutableNode node) {
        List<ExecutableNode> children = new java.util.ArrayList<>();
        for (var recordComponent : node.getClass().getRecordComponents() != null
                ? node.getClass().getRecordComponents() : new java.lang.reflect.RecordComponent[0]) {
            Object value = invoke(recordComponent, node);
            collect(value, children);
        }
        if (node.getClass().getRecordComponents() == null) {
            for (var field : node.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                collect(readField(field, node), children);
            }
        }
        return children;
    }

    private static void collect(Object value, List<ExecutableNode> children) {
        if (value instanceof ExecutableNode child) {
            children.add(child);
        } else if (value instanceof ExecutableBranch branch) {
            children.add(branch.condition());
            children.add(branch.consequence());
        } else if (value instanceof List<?> list) {
            for (Object element : list) {
                collect(element, children);
            }
        }
    }

    private static Object invoke(java.lang.reflect.RecordComponent component, Object target) {
        try {
            return component.getAccessor().invoke(target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Object readField(java.lang.reflect.Field field, Object target) {
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
