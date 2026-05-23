package com.runestone.expeval.internal.semantic;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.internal.ast.BinaryOperationNode;
import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.VectorType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OperatorTypeCheckerTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 1, 1, 0, 1, 1);

    private final List<IssueCode> issues = new ArrayList<>();
    private final OperatorTypeChecker checker = new OperatorTypeChecker((code, message, sourceSpan) -> issues.add(code));

    @Test
    void shouldResolveArithmeticOperatorsAsNumber() {
        BinaryOperationNode node = binary(BinaryOperator.ADD);

        assertThat(checker.resolveBinary(node, ScalarType.NUMBER, ScalarType.NUMBER)).isEqualTo(ScalarType.NUMBER);
        assertThat(issues).isEmpty();
    }

    @Test
    void shouldReportRegexOperandTypeMismatch() {
        BinaryOperationNode node = binary(BinaryOperator.REGEX_MATCH);

        assertThat(checker.resolveBinary(node, ScalarType.STRING, ScalarType.NUMBER)).isEqualTo(ScalarType.BOOLEAN);
        assertThat(issues).containsExactly(IssueCode.TYPE_MISMATCH);
    }

    @Test
    void shouldReportInvalidMembershipRightOperand() {
        BinaryOperationNode node = binary(BinaryOperator.IN);

        assertThat(checker.resolveBinary(node, ScalarType.NUMBER, ScalarType.NUMBER)).isEqualTo(ScalarType.BOOLEAN);
        assertThat(issues).containsExactly(IssueCode.INCOMPATIBLE_IN_OPERANDS);
    }

    @Test
    void shouldAcceptVectorMembershipRightOperand() {
        BinaryOperationNode node = binary(BinaryOperator.IN);

        assertThat(checker.resolveBinary(node, ScalarType.NUMBER, VectorType.INSTANCE)).isEqualTo(ScalarType.BOOLEAN);
        assertThat(issues).isEmpty();
    }

    private static BinaryOperationNode binary(BinaryOperator operator) {
        return new BinaryOperationNode(
                new NodeId("binary-" + operator),
                SPAN,
                operator,
                literal("left"),
                literal("right"));
    }

    private static LiteralNode literal(String name) {
        return new LiteralNode(new NodeId("literal-" + name), SPAN, "1");
    }
}
