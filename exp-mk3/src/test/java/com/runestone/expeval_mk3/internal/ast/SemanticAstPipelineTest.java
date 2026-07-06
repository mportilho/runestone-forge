package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SemanticAstPipelineTest {

    private final ExpressionParser parser = new ExpressionParser();
    private final SemanticAstBuilder astBuilder = new SemanticAstBuilder();

    @Test
    @DisplayName("simple source with assignments and result builds a deterministic semantic AST")
    void simpleSourceWithAssignmentsAndResultBuildsDeterministicSemanticAst() {
        String source = "total:=42; label := \"ok\";\nready := true; total";

        ExpressionFileNode ast = build(source);

        assertThat(ast.assignments()).hasSize(3);
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> ast.assignments().add(ast.assignments().getFirst()));
        assertThat(ast.resultExpression()).hasValueSatisfying(result ->
                assertThat(result).isEqualTo(new IdentifierNode(new NodeId(10), result.sourceSpan(), "total")));
        assertThat(nodeIds(ast)).containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("total := 42;\nlabel := \"ok\";\nready := true;\ntotal");
    }

    @Test
    @DisplayName("assignments-only source builds an expression file with an empty result expression")
    void assignmentsOnlySourceBuildsExpressionFileWithEmptyResultExpression() {
        ExpressionFileNode ast = build("first := null;\nsecond := first;");
        ExpressionFileNode reparsed = build(AstPrettyPrinter.print(ast));

        assertThat(ast.assignments()).hasSize(2);
        assertThat(ast.resultExpression()).isEmpty();
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("first := null;\nsecond := first;");
        assertThat(reparsed.resultExpression()).isEmpty();
        assertThat(AstStructuralEquality.equals(ast, reparsed)).isTrue();
    }

    @Test
    @DisplayName("literal AST values are materialized independently from the environment")
    void literalAstValuesAreMaterializedIndependentlyFromEnvironment() {
        ExpressionFileNode ast = build("day := d\"2024-01-02\"; time := t\"10:30\"; "
                + "instant := dt\"2024-01-02T10:30:00+02:00\"; instant");

        assertThat(literalValue(ast.assignments().get(0).expression()))
                .isEqualTo(new DateLiteralValue(LocalDate.of(2024, 1, 2)));
        assertThat(literalValue(ast.assignments().get(1).expression()))
                .isEqualTo(new TimeLiteralValue(LocalTime.of(10, 30)));
        assertThat(literalValue(ast.assignments().get(2).expression()))
                .isEqualTo(new OffsetDateTimeLiteralValue(OffsetDateTime.parse("2024-01-02T10:30:00+02:00")));
        assertThat(ast.resultExpression()).hasValueSatisfying(result ->
                assertThat(result).isInstanceOf(IdentifierNode.class));

        ExpressionFileNode utcOffset = build("instant := dt\"2024-01-02T10:30:00+00:00\"; instant");
        String printed = AstPrettyPrinter.print(utcOffset);
        assertThat(printed).contains("+00:00").doesNotContain("Z");
        assertThat(AstStructuralEquality.equals(utcOffset, build(printed))).isTrue();

        ExpressionFileNode decimalWithLeadingZero = build("leading := 018; leading");
        assertThat(literalValue(decimalWithLeadingZero.assignments().getFirst().expression()))
                .isEqualTo(new LongLiteralValue(18));
    }

    @Test
    @DisplayName("pretty-printed tracer AST reparses to a structurally equal tree")
    void prettyPrintedTracerAstReparsesToStructurallyEqualTree() {
        ExpressionFileNode original = build("x:=1; y := x; y");

        ExpressionFileNode reparsed = build(AstPrettyPrinter.print(original));
        ExpressionFileNode reidentified = shiftNodeIds(original, 100);

        assertThat(AstStructuralEquality.equals(original, reparsed)).isTrue();
        assertThat(reidentified).isNotEqualTo(original);
        assertThat(AstStructuralEquality.equals(original, reidentified)).isTrue();
    }

    @Test
    @DisplayName("only the root AST record uses Optional for the result expression")
    void onlyRootAstRecordUsesOptionalForResultExpression() {
        List<RecordComponent> optionalComponents = astRecordTypes().stream()
                .flatMap(type -> List.of(type.getRecordComponents()).stream())
                .filter(component -> component.getType().equals(Optional.class))
                .toList();

        assertThat(optionalComponents).singleElement().satisfies(component -> {
            assertThat(component.getDeclaringRecord()).isEqualTo(ExpressionFileNode.class);
            assertThat(component.getName()).isEqualTo("resultExpression");
        });
    }

    private ExpressionFileNode build(String source) {
        ParseResult parseResult = parser.parse(source);
        assertThat(parseResult).isInstanceOf(ParseSuccess.class);
        SemanticAstResult result = astBuilder.build((ParseSuccess) parseResult);
        assertThat(result).isInstanceOf(SemanticAstSuccess.class);
        return ((SemanticAstSuccess) result).file();
    }

    private static List<Integer> nodeIds(ExpressionFileNode file) {
        List<Integer> ids = new ArrayList<>();
        ids.add(file.id().value());
        for (AssignmentNode assignment : file.assignments()) {
            ids.add(assignment.id().value());
            ids.add(assignment.target().id().value());
            ids.add(assignment.expression().id().value());
        }
        file.resultExpression().ifPresent(expression -> ids.add(expression.id().value()));
        return ids;
    }

    private static List<Class<?>> astRecordTypes() {
        List<Class<?>> recordTypes = new ArrayList<>();
        collectAstRecordTypes(AstNode.class, recordTypes);
        return recordTypes;
    }

    private static void collectAstRecordTypes(Class<?> type, List<Class<?>> recordTypes) {
        if (type.isRecord()) {
            recordTypes.add(type);
        }
        Class<?>[] permittedSubclasses = type.getPermittedSubclasses();
        if (permittedSubclasses == null) {
            return;
        }
        for (Class<?> permittedSubclass : permittedSubclasses) {
            collectAstRecordTypes(permittedSubclass, recordTypes);
        }
    }

    private static LiteralValue literalValue(ExpressionNode expression) {
        assertThat(expression).isInstanceOf(LiteralNode.class);
        return ((LiteralNode) expression).value();
    }

    private static ExpressionFileNode shiftNodeIds(ExpressionFileNode file, int offset) {
        return new ExpressionFileNode(
                shift(file.id(), offset),
                file.sourceSpan(),
                file.assignments().stream().map(assignment -> shiftNodeIds(assignment, offset)).toList(),
                file.resultExpression().map(expression -> shiftNodeIds(expression, offset)));
    }

    private static AssignmentNode shiftNodeIds(AssignmentNode assignment, int offset) {
        return new AssignmentNode(
                shift(assignment.id(), offset),
                assignment.sourceSpan(),
                shiftNodeIds(assignment.target(), offset),
                shiftNodeIds(assignment.expression(), offset));
    }

    private static AssignmentTargetNode shiftNodeIds(AssignmentTargetNode target, int offset) {
        if (target instanceof IdentifierAssignmentTargetNode identifier) {
            return new IdentifierAssignmentTargetNode(
                    shift(identifier.id(), offset),
                    identifier.sourceSpan(),
                    identifier.name());
        }
        throw new IllegalArgumentException("Unsupported target: " + target.getClass().getName());
    }

    private static ExpressionNode shiftNodeIds(ExpressionNode expression, int offset) {
        return switch (expression) {
            case BetweenNode between -> new BetweenNode(
                    shift(between.id(), offset),
                    between.sourceSpan(),
                    shiftNodeIds(between.value(), offset),
                    between.operatorSpan(),
                    between.negated(),
                    shiftNodeIds(between.lowerBound(), offset),
                    shiftNodeIds(between.upperBound(), offset));
            case BinaryOperationNode binary -> new BinaryOperationNode(
                    shift(binary.id(), offset),
                    binary.sourceSpan(),
                    shiftNodeIds(binary.left(), offset),
                    binary.operator(),
                    binary.operatorSpan(),
                    shiftNodeIds(binary.right(), offset));
            case ConditionalNode conditional -> new ConditionalNode(
                    shift(conditional.id(), offset),
                    conditional.sourceSpan(),
                    conditional.sourceForm(),
                    conditional.branches().stream().map(branch -> shiftNodeIds(branch, offset)).toList(),
                    shiftNodeIds(conditional.elseExpression(), offset));
            case CurrentItemNode currentItem -> new CurrentItemNode(
                    shift(currentItem.id(), offset),
                    currentItem.sourceSpan());
            case IdentifierNode identifier -> new IdentifierNode(
                    shift(identifier.id(), offset),
                    identifier.sourceSpan(),
                    identifier.name());
            case CurrentTemporalValueNode currentTemporalValue -> new CurrentTemporalValueNode(
                    shift(currentTemporalValue.id(), offset),
                    currentTemporalValue.sourceSpan(),
                    currentTemporalValue.kind());
            case FunctionCallNode functionCall -> new FunctionCallNode(
                    shift(functionCall.id(), offset),
                    functionCall.sourceSpan(),
                    functionCall.name(),
                    functionCall.arguments().stream().map(argument -> shiftNodeIds(argument, offset)).toList());
            case GroupedExpressionNode grouped -> new GroupedExpressionNode(
                    shift(grouped.id(), offset),
                    grouped.sourceSpan(),
                    shiftNodeIds(grouped.expression(), offset));
            case LiteralNode literal -> new LiteralNode(shift(literal.id(), offset), literal.sourceSpan(), literal.value());
            case MembershipNode membership -> new MembershipNode(
                    shift(membership.id(), offset),
                    membership.sourceSpan(),
                    shiftNodeIds(membership.value(), offset),
                    membership.operatorSpan(),
                    membership.negated(),
                    shiftNodeIds(membership.candidates(), offset));
            case NavigationChainNode navigationChain -> new NavigationChainNode(
                    shift(navigationChain.id(), offset),
                    navigationChain.sourceSpan(),
                    shiftNodeIds(navigationChain.receiver(), offset),
                    navigationChain.links().stream().map(link -> shiftNodeIds(link, offset)).toList());
            case NullCoalescenceNode nullCoalescence -> new NullCoalescenceNode(
                    shift(nullCoalescence.id(), offset),
                    nullCoalescence.sourceSpan(),
                    nullCoalescence.operands().stream().map(operand -> shiftNodeIds(operand, offset)).toList(),
                    nullCoalescence.operatorSpans());
            case PostfixOperationNode postfix -> new PostfixOperationNode(
                    shift(postfix.id(), offset),
                    postfix.sourceSpan(),
                    shiftNodeIds(postfix.operand(), offset),
                    postfix.operators());
            case UnaryOperationNode unary -> new UnaryOperationNode(
                    shift(unary.id(), offset),
                    unary.sourceSpan(),
                    unary.operator(),
                    unary.operatorSpan(),
                    shiftNodeIds(unary.operand(), offset));
            case VectorLiteralNode vectorLiteral -> new VectorLiteralNode(
                    shift(vectorLiteral.id(), offset),
                    vectorLiteral.sourceSpan(),
                    vectorLiteral.elements().stream().map(element -> shiftNodeIds(element, offset)).toList());
        };
    }

    private static NavigationLink shiftNodeIds(NavigationLink link, int offset) {
        return switch (link) {
            case CollectionOperationNavigationLink collectionOperation -> new CollectionOperationNavigationLink(
                    shift(collectionOperation.id(), offset),
                    collectionOperation.sourceSpan(),
                    collectionOperation.operationName(),
                    collectionOperation.arguments().stream().map(argument -> shiftNodeIds(argument, offset)).toList());
            case FilterNavigationLink filter -> new FilterNavigationLink(
                    shift(filter.id(), offset),
                    filter.sourceSpan(),
                    shiftNodeIds(filter.predicate(), offset),
                    filter.safeNavigation());
            case MethodNavigationLink method -> new MethodNavigationLink(
                    shift(method.id(), offset),
                    method.sourceSpan(),
                    method.memberName(),
                    method.safeNavigation(),
                    method.arguments().stream().map(argument -> shiftNodeIds(argument, offset)).toList());
            case PropertyNavigationLink property -> new PropertyNavigationLink(
                    shift(property.id(), offset),
                    property.sourceSpan(),
                    property.memberName(),
                    property.safeNavigation());
            case SubscriptNavigationLink subscript -> new SubscriptNavigationLink(
                    shift(subscript.id(), offset),
                    subscript.sourceSpan(),
                    subscript.subscript(),
                    subscript.safeNavigation());
            case WildcardNavigationLink wildcard -> new WildcardNavigationLink(
                    shift(wildcard.id(), offset),
                    wildcard.sourceSpan());
        };
    }

    private static CollectionOperationArgument shiftNodeIds(CollectionOperationArgument argument, int offset) {
        return switch (argument) {
            case LambdaCollectionOperationArgument lambda -> new LambdaCollectionOperationArgument(
                    shift(lambda.id(), offset),
                    lambda.sourceSpan(),
                    shiftNodeIds(lambda.lambda(), offset));
            case PositionalCollectionOperationArgument positional -> new PositionalCollectionOperationArgument(
                    shift(positional.id(), offset),
                    positional.sourceSpan(),
                    shiftNodeIds(positional.expression(), offset));
        };
    }

    private static LambdaNode shiftNodeIds(LambdaNode lambda, int offset) {
        return new LambdaNode(
                shift(lambda.id(), offset),
                lambda.sourceSpan(),
                lambda.currentItemSpan(),
                lambda.arrowSpan(),
                shiftNodeIds(lambda.body(), offset));
    }

    private static ConditionalBranchNode shiftNodeIds(ConditionalBranchNode branch, int offset) {
        return new ConditionalBranchNode(
                shift(branch.id(), offset),
                branch.sourceSpan(),
                shiftNodeIds(branch.condition(), offset),
                shiftNodeIds(branch.resultExpression(), offset));
    }

    private static NodeId shift(NodeId id, int offset) {
        return new NodeId(id.value() + offset);
    }
}
