package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Provide;
import net.jqwik.api.Property;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticAstPropertyRoundTripTest {

    private static final NodeId ID = NodeId.UNASSIGNED;
    private static final SourceSpan SPAN = new SourceSpan(0, 1, 1, 1);

    @Property(tries = 100)
    void boundedValidSemanticAstPrettyPrintsReparsesAndRebuildsStructurallyEqual(
            @ForAll("semanticTrees") ExpressionFileNode ast) {
        ExpressionFileNode reparsed = AstTestSupport.build(AstPrettyPrinter.print(ast));

        assertThat(AstStructuralEquality.equals(ast, reparsed))
                .as(AstPrettyPrinter.print(ast))
                .isTrue();
    }

    @Provide
    Arbitrary<ExpressionFileNode> semanticTrees() {
        return expressionFiles(3).map(file -> new AstNodeIdAssigner().assign(file));
    }

    private Arbitrary<ExpressionFileNode> expressionFiles(int depth) {
        Arbitrary<ExpressionNode> expression = expressions(depth);
        return Arbitraries.oneOf(
                expression.map(result -> new ExpressionFileNode(ID, SPAN, List.of(), Optional.of(result))),
                expression.map(value -> new ExpressionFileNode(
                        ID,
                        SPAN,
                        List.of(new AssignmentNode(ID, SPAN, identifierTarget("x"), value)),
                        Optional.of(identifier("x")))),
                expression.map(value -> new ExpressionFileNode(
                        ID,
                        SPAN,
                        List.of(new AssignmentNode(ID, SPAN, destructuringTarget(), vector(value, literal(1L)))),
                        Optional.of(identifier("a")))),
                Combinators.combine(expression, expression)
                        .as((first, second) -> new ExpressionFileNode(
                                ID,
                                SPAN,
                                List.of(
                                        new AssignmentNode(ID, SPAN, identifierTarget("x"), first),
                                        new AssignmentNode(ID, SPAN, identifierTarget("y"), second)),
                                Optional.of(identifier("y")))));
    }

    private Arbitrary<ExpressionNode> expressions(int depth) {
        Arbitrary<ExpressionNode> atom = Arbitraries.oneOf(
                Arbitraries.of(
                        literal(0L),
                        literal(1L),
                        literal(true),
                        literal(false),
                        new LiteralNode(ID, SPAN, new NullLiteralValue()),
                        identifier("x"),
                        new CurrentTemporalValueNode(ID, SPAN, CurrentTemporalValueKind.DATE),
                        new CurrentTemporalValueNode(ID, SPAN, CurrentTemporalValueKind.TIME),
                        new CurrentTemporalValueNode(ID, SPAN, CurrentTemporalValueKind.DATETIME),
                        new LiteralNode(ID, SPAN, new DateLiteralValue(LocalDate.of(2024, 2, 29))),
                        new LiteralNode(ID, SPAN, new TimeLiteralValue(LocalTime.of(10, 30, 45))),
                        new LiteralNode(ID, SPAN, new LocalDateTimeLiteralValue(
                                LocalDateTime.of(2024, 2, 29, 10, 30, 45)))),
                Arbitraries.strings()
                        .alpha()
                        .ofMinLength(0)
                        .ofMaxLength(8)
                        .map(value -> new LiteralNode(ID, SPAN, new StringLiteralValue(value))),
                Arbitraries.of(vector(), vector(literal(1L)), vector(literal(1L), literal(2L))));
        if (depth == 0) {
            return atom;
        }

        Arbitrary<ExpressionNode> nested = expressions(depth - 1);
        return Arbitraries.oneOf(
                atom,
                nested.map(SemanticAstPropertyRoundTripTest::group),
                unaryExpressions(nested),
                binaryExpressions(nested),
                coalescenceExpressions(nested),
                functionCalls(nested),
                conditionalExpressions(nested),
                membershipExpressions(nested),
                navigationExpressions(nested),
                postfixExpressions(nested));
    }

    private static Arbitrary<ExpressionNode> unaryExpressions(Arbitrary<ExpressionNode> nested) {
        return Combinators.combine(Arbitraries.of(UnaryOperator.NEGATE, UnaryOperator.LOGICAL_NOT), nested)
                .as((operator, operand) -> new UnaryOperationNode(ID, SPAN, operator, SPAN, group(operand)));
    }

    private static Arbitrary<ExpressionNode> binaryExpressions(Arbitrary<ExpressionNode> nested) {
        return Combinators.combine(
                        nested,
                        Arbitraries.of(
                                BinaryOperator.ADD,
                                BinaryOperator.SUBTRACT,
                                BinaryOperator.MULTIPLY,
                                BinaryOperator.DIVIDE,
                                BinaryOperator.CONCAT,
                                BinaryOperator.LOGICAL_AND,
                                BinaryOperator.LOGICAL_OR,
                                BinaryOperator.LOGICAL_XOR,
                                BinaryOperator.EQUAL,
                                BinaryOperator.NOT_EQUAL,
                                BinaryOperator.GREATER_THAN,
                                BinaryOperator.LESS_THAN),
                        nested)
                .as((left, operator, right) -> new BinaryOperationNode(
                        ID,
                        SPAN,
                        group(left),
                        operator,
                        SPAN,
                        group(right)));
    }

    private static Arbitrary<ExpressionNode> coalescenceExpressions(Arbitrary<ExpressionNode> nested) {
        return Combinators.combine(nested, nested, nested)
                .as((first, second, third) -> new NullCoalescenceNode(
                        ID,
                        SPAN,
                        List.of(group(first), group(second), group(third)),
                        List.of(SPAN, SPAN)));
    }

    private static Arbitrary<ExpressionNode> functionCalls(Arbitrary<ExpressionNode> nested) {
        return Combinators.combine(Arbitraries.of("sum", "max", "min"), nested, nested)
                .as((name, first, second) -> new FunctionCallNode(
                        ID,
                        SPAN,
                        member(name),
                        List.of(first, second)));
    }

    private static Arbitrary<ExpressionNode> conditionalExpressions(Arbitrary<ExpressionNode> nested) {
        return Combinators.combine(nested, nested, nested)
                .as((condition, trueResult, falseResult) -> new ConditionalNode(
                        ID,
                        SPAN,
                        ConditionalSourceForm.FUNCTIONAL,
                        List.of(new ConditionalBranchNode(ID, SPAN, condition, trueResult)),
                        falseResult));
    }

    private static Arbitrary<ExpressionNode> membershipExpressions(Arbitrary<ExpressionNode> nested) {
        return Combinators.combine(Arbitraries.of("in", "not-in", "between"), nested, nested, nested)
                .as((operator, value, lower, upper) -> switch (operator) {
                    case "in" -> new MembershipNode(ID, SPAN, group(value), SPAN, false, vector(literal(1L), literal(2L)));
                    case "not-in" -> new MembershipNode(ID, SPAN, group(value), SPAN, true, vector(literal(1L), literal(2L)));
                    case "between" -> new BetweenNode(ID, SPAN, group(value), SPAN, false, group(lower), group(upper));
                    default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
                });
    }

    private static Arbitrary<ExpressionNode> navigationExpressions(Arbitrary<ExpressionNode> nested) {
        return Combinators.combine(
                        Arbitraries.of("property", "safe-property", "method", "index", "safe-index", "filter", "sum", "map"),
                        nested)
                .as((kind, argument) -> new NavigationChainNode(
                        ID,
                        SPAN,
                        identifier("x"),
                        List.of(navigationLink(kind, argument))));
    }

    private static NavigationLink navigationLink(String kind, ExpressionNode argument) {
        return switch (kind) {
            case "property" -> new PropertyNavigationLink(ID, SPAN, member("name"), false);
            case "safe-property" -> new PropertyNavigationLink(ID, SPAN, member("name"), true);
            case "method" -> new MethodNavigationLink(ID, SPAN, member("method"), false, List.of(argument));
            case "index" -> new SubscriptNavigationLink(ID, SPAN, indexSubscript(), false);
            case "safe-index" -> new SubscriptNavigationLink(ID, SPAN, indexSubscript(), true);
            case "filter" -> new FilterNavigationLink(ID, SPAN, literal(true), false);
            case "sum" -> new CollectionOperationNavigationLink(ID, SPAN, member("sum"), List.of());
            case "map" -> new CollectionOperationNavigationLink(ID, SPAN, member("map"), List.of(lambdaArgument()));
            default -> throw new IllegalArgumentException("Unsupported navigation kind: " + kind);
        };
    }

    private static Arbitrary<ExpressionNode> postfixExpressions(Arbitrary<ExpressionNode> nested) {
        return Combinators.combine(nested, Arbitraries.of(PostfixOperator.PERCENT, PostfixOperator.FACTORIAL))
                .as((operand, operator) -> new PostfixOperationNode(
                        ID,
                        SPAN,
                        group(operand),
                        List.of(new PostfixOperatorOccurrence(operator, SPAN))));
    }

    private static ExpressionNode group(ExpressionNode expression) {
        return new GroupedExpressionNode(ID, SPAN, expression);
    }

    private static IdentifierNode identifier(String name) {
        return new IdentifierNode(ID, SPAN, name);
    }

    private static AssignmentTargetNode identifierTarget(String name) {
        return new IdentifierAssignmentTargetNode(ID, SPAN, name);
    }

    private static DestructuringAssignmentTargetNode destructuringTarget() {
        return new DestructuringAssignmentTargetNode(
                ID,
                SPAN,
                List.of(
                        new IdentifierAssignmentTargetNode(ID, SPAN, "a"),
                        new IdentifierAssignmentTargetNode(ID, SPAN, "b")));
    }

    private static LiteralNode literal(long value) {
        return new LiteralNode(ID, SPAN, new LongLiteralValue(value));
    }

    private static LiteralNode literal(boolean value) {
        return new LiteralNode(ID, SPAN, new BooleanLiteralValue(value));
    }

    private static VectorLiteralNode vector(ExpressionNode... elements) {
        return new VectorLiteralNode(ID, SPAN, List.of(elements));
    }

    private static MemberName member(String value) {
        return new MemberName(value, SPAN);
    }

    private static IndexSubscript indexSubscript() {
        return new IndexSubscript(new SignedIntegerLiteral(BigInteger.ZERO, IntegerLiteralFormat.DECIMAL));
    }

    private static LambdaCollectionOperationArgument lambdaArgument() {
        LambdaNode lambda = new LambdaNode(ID, SPAN, SPAN, SPAN, new CurrentItemNode(ID, SPAN));
        return new LambdaCollectionOperationArgument(ID, SPAN, lambda);
    }
}
