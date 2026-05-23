package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.ast.SourceSpan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEventEstimatorTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 0, 1, 0, 1, 0);

    @Test
    void estimatesAssignmentsResultExpressionAndFoldedVariableReads() {
        SymbolRef target = new SymbolRef("target", SymbolKind.INTERNAL);
        ExecutableAssignment assignment = new ExecutableSimpleAssignment(target, identifier("input"));

        int count = AuditEventEstimator.estimate(List.of(assignment), identifier("result"), 2);

        assertThat(count).isEqualTo(5);
    }

    @Test
    void estimatesOnlyMostExpensiveConditionalBranch() {
        ExecutableConditional conditional = new ExecutableConditional(
                List.of(identifier("firstCondition"), identifier("secondCondition")),
                List.of(new ExecutableLiteral("short"), binaryIdentifiers("left", "right")),
                identifier("fallback"));

        int count = AuditEventEstimator.estimate(List.of(), conditional, 0);

        assertThat(count).isEqualTo(4);
    }

    @Test
    void includesPropertyChainRootAndMethodArguments() {
        ExecutablePropertyChain chain = new ExecutablePropertyChain(
                identifier("root"),
                List.of(
                        new ExecutablePropertyChain.ReflectivePropertyAccess("name", false),
                        new ExecutablePropertyChain.ReflectiveMethodInvoke(
                                "matches",
                                List.of(identifier("pattern"), new ExecutableLiteral(true)),
                                false)));

        int count = AuditEventEstimator.estimate(List.of(), chain, 0);

        assertThat(count).isEqualTo(2);
    }

    private static ExecutableBinaryOp binaryIdentifiers(String left, String right) {
        return new ExecutableBinaryOp(BinaryOperator.ADD, identifier(left), identifier(right));
    }

    private static ExecutableIdentifier identifier(String name) {
        return new ExecutableIdentifier(new SymbolRef(name, SymbolKind.EXTERNAL), SPAN);
    }
}
