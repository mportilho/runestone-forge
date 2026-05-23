package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.internal.ast.ExpressionNode;
import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.ast.PropertyChainNode;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.UnknownType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTypeValidatorTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 1, 1, 0, 1, 1);

    private final List<IssueCode> issues = new ArrayList<>();
    private final ResultTypeValidator validator = new ResultTypeValidator((code, message, sourceSpan) -> issues.add(code));

    @Test
    void shouldReportMathResultTypeMismatch() {
        validator.validate(ExpressionResultType.MATH, literal(), ScalarType.STRING);

        assertThat(issues).containsExactly(IssueCode.RESULT_TYPE_MISMATCH);
    }

    @Test
    void shouldAcceptLogicalBooleanResult() {
        validator.validate(ExpressionResultType.LOGICAL, literal(), ScalarType.BOOLEAN);

        assertThat(issues).isEmpty();
    }

    @Test
    void shouldTolerateUnknownPropertyChainResult() {
        ExpressionNode propertyChain = new PropertyChainNode(
                new NodeId("property-chain"),
                SPAN,
                "unknownRoot",
                List.of(new PropertyChainNode.PropertyAccess("value")));

        validator.validate(ExpressionResultType.MATH, propertyChain, UnknownType.INSTANCE);

        assertThat(issues).isEmpty();
    }

    private static LiteralNode literal() {
        return new LiteralNode(new NodeId("literal"), SPAN, "1");
    }
}
