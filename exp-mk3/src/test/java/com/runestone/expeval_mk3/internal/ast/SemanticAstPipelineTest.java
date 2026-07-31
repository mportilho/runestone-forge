package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        ExpressionFileNode ast = build("first := 1;\nsecond := first;");
        ExpressionFileNode reparsed = build(AstPrettyPrinter.print(ast));

        assertThat(ast.assignments()).hasSize(2);
        assertThat(ast.resultExpression()).isEmpty();
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("first := 1;\nsecond := first;");
        assertThat(reparsed.resultExpression()).isEmpty();
        assertThat(AstStructuralEquality.equals(ast, reparsed)).isTrue();
    }

    @Test
    @DisplayName("literal AST values are materialized independently from the environment")
    void literalAstValuesAreMaterializedIndependentlyFromEnvironment() {
        ExpressionFileNode ast = build("truth := true; text := \"line\\n\\\"quoted\\\"\\\\path\"; "
                + "small := 9223372036854775807; big := 9223372036854775808; "
                + "amount := 001.2300; day := d\"2024-01-02\"; time := t\"10:30\"; "
                + "local := dt\"2024-01-02T10:30:00\"; instant := dt\"2024-01-02T10:30:00+02:00\"; instant");

        assertThat(literalValue(ast.assignments().get(0).expression()))
                .isEqualTo(new BooleanLiteralValue(true));
        assertThat(literalValue(ast.assignments().get(1).expression()))
                .isEqualTo(new StringLiteralValue("line\n\"quoted\"\\path"));
        assertThat(literalValue(ast.assignments().get(2).expression()))
                .isEqualTo(new LongLiteralValue(Long.MAX_VALUE));
        assertThat(literalValue(ast.assignments().get(3).expression()))
                .isEqualTo(new BigIntegerLiteralValue(new BigInteger("9223372036854775808")));
        assertThat(literalValue(ast.assignments().get(4).expression()))
                .isEqualTo(new DecimalLiteralValue(new BigDecimal("001.2300")));
        assertThat(literalValue(ast.assignments().get(5).expression()))
                .isEqualTo(new DateLiteralValue(LocalDate.of(2024, 1, 2)));
        assertThat(literalValue(ast.assignments().get(6).expression()))
                .isEqualTo(new TimeLiteralValue(LocalTime.of(10, 30)));
        assertThat(literalValue(ast.assignments().get(7).expression()))
                .isEqualTo(new LocalDateTimeLiteralValue(LocalDateTime.of(2024, 1, 2, 10, 30)));
        assertThat(literalValue(ast.assignments().get(8).expression()))
                .isEqualTo(new OffsetDateTimeLiteralValue(OffsetDateTime.parse("2024-01-02T10:30:00+02:00")));
        assertThat(ast.resultExpression()).hasValueSatisfying(result ->
                assertThat(result).isInstanceOf(IdentifierNode.class));

        String printed = AstPrettyPrinter.print(ast);
        assertThat(printed).contains("text := \"line\\n\\\"quoted\\\"\\\\path\";");
        assertThat(AstStructuralEquality.equals(ast, build(printed))).isTrue();

        ExpressionFileNode utcOffset = build("instant := dt\"2024-01-02T10:30:00+00:00\"; instant");
        String printedOffset = AstPrettyPrinter.print(utcOffset);
        assertThat(printedOffset).contains("+00:00").doesNotContain("Z");
        assertThat(AstStructuralEquality.equals(utcOffset, build(printedOffset))).isTrue();

        ExpressionFileNode plusTwoOffset = build("instant := dt\"2024-01-02T10:30:00+02:00\"; instant");
        ExpressionFileNode sameInstantUtcOffset = build("instant := dt\"2024-01-02T08:30:00+00:00\"; instant");
        assertThat(AstStructuralEquality.equals(plusTwoOffset, sameInstantUtcOffset)).isFalse();

        ExpressionFileNode zero = build("zero := 0; zero");
        assertThat(literalValue(zero.assignments().getFirst().expression()))
                .isEqualTo(new LongLiteralValue(0));
    }

    @Test
    @DisplayName("current temporal values build dynamic expression nodes")
    void currentTemporalValuesBuildDynamicExpressionNodes() {
        ExpressionFileNode ast = build("day := currDate; time := currTime; instant := currDateTime; instant");

        assertThat(ast.assignments().get(0).expression())
                .isEqualTo(new CurrentTemporalValueNode(
                        ast.assignments().get(0).expression().id(),
                        ast.assignments().get(0).expression().sourceSpan(),
                        CurrentTemporalValueKind.DATE));
        assertThat(ast.assignments().get(1).expression())
                .isEqualTo(new CurrentTemporalValueNode(
                        ast.assignments().get(1).expression().id(),
                        ast.assignments().get(1).expression().sourceSpan(),
                        CurrentTemporalValueKind.TIME));
        assertThat(ast.assignments().get(2).expression())
                .isEqualTo(new CurrentTemporalValueNode(
                        ast.assignments().get(2).expression().id(),
                        ast.assignments().get(2).expression().sourceSpan(),
                        CurrentTemporalValueKind.DATE_TIME));
        assertThat(AstPrettyPrinter.print(ast))
                .isEqualTo("day := currDate;\ntime := currTime;\ninstant := currDateTime;\ninstant");
        assertThat(AstStructuralEquality.equals(ast, build(AstPrettyPrinter.print(ast)))).isTrue();
    }

    @Test
    @DisplayName("explicit grouping and unary operators build source-faithful AST nodes")
    void explicitGroupingAndUnaryOperatorsBuildSourceFaithfulAstNodes() {
        ExpressionFileNode ast = build("grouped := (!!flag); synonym := \u00ACflag; grouped");

        assertThat(ast.assignments().getFirst().expression()).isInstanceOf(GroupedExpressionNode.class);
        GroupedExpressionNode grouped = (GroupedExpressionNode) ast.assignments().getFirst().expression();
        assertThat(grouped.expression()).isInstanceOf(UnaryOperationNode.class);
        UnaryOperationNode outerNot = (UnaryOperationNode) grouped.expression();
        assertThat(outerNot.operator()).isEqualTo(UnaryOperator.LOGICAL_NOT);
        assertThat(outerNot.operand()).isInstanceOf(UnaryOperationNode.class);
        UnaryOperationNode innerNot = (UnaryOperationNode) outerNot.operand();
        assertThat(innerNot.operator()).isEqualTo(UnaryOperator.LOGICAL_NOT);
        assertThat(innerNot.operand()).isInstanceOf(IdentifierNode.class);
        assertThat(ast.assignments().get(1).expression()).isInstanceOf(UnaryOperationNode.class);
        assertThat(((UnaryOperationNode) ast.assignments().get(1).expression()).operator())
                .isEqualTo(UnaryOperator.LOGICAL_NOT);

        String printed = AstPrettyPrinter.print(ast);
        assertThat(printed).isEqualTo("grouped := (~~flag);\nsynonym := ~flag;\ngrouped");
        assertThat(AstStructuralEquality.equals(ast, build(printed))).isTrue();
    }

    @Test
    @DisplayName("binary operators follow grammar associativity")
    void binaryOperatorsFollowGrammarAssociativity() {
        ExpressionFileNode ast = build("power := 2 ^ 3 ^ 4; subtract := a - b - c; rooted := 3 \u221A 27; rooted");

        BinaryOperationNode power = (BinaryOperationNode) ast.assignments().getFirst().expression();
        assertThat(power.operator()).isEqualTo(BinaryOperator.EXPONENTIATE);
        assertThat(power.left()).isInstanceOf(LiteralNode.class);
        assertThat(power.right()).isInstanceOf(BinaryOperationNode.class);
        assertThat(((BinaryOperationNode) power.right()).operator()).isEqualTo(BinaryOperator.EXPONENTIATE);

        BinaryOperationNode subtract = (BinaryOperationNode) ast.assignments().get(1).expression();
        assertThat(subtract.operator()).isEqualTo(BinaryOperator.SUBTRACT);
        assertThat(subtract.left()).isInstanceOf(BinaryOperationNode.class);
        assertThat(((BinaryOperationNode) subtract.left()).operator()).isEqualTo(BinaryOperator.SUBTRACT);

        BinaryOperationNode rooted = (BinaryOperationNode) ast.assignments().get(2).expression();
        assertThat(rooted.operator()).isEqualTo(BinaryOperator.ROOT);

        String printed = AstPrettyPrinter.print(ast);
        assertThat(printed).isEqualTo("power := 2 ^ 3 ^ 4;\nsubtract := a - b - c;\nrooted := 3 root 27;\nrooted");
        assertThat(AstStructuralEquality.equals(ast, build(printed))).isTrue();
    }

    @Test
    @DisplayName("postfix chains preserve operation order and operator spans")
    void postfixChainsPreserveOperationOrderAndOperatorSpans() {
        String source = "value := n%!; value";
        ExpressionFileNode ast = build(source);

        PostfixOperationNode postfix = (PostfixOperationNode) ast.assignments().getFirst().expression();
        assertThat(postfix.operand()).isInstanceOf(IdentifierNode.class);
        assertThat(postfix.operations()).extracting(PostfixOperatorOccurrence::operator)
                .containsExactly(PostfixOperator.PERCENT, PostfixOperator.FACTORIAL);
        assertThat(source.substring(
                postfix.operations().getFirst().sourceSpan().offset(),
                postfix.operations().getFirst().sourceSpan().endOffset())).isEqualTo("%");
        assertThat(source.substring(
                postfix.operations().getLast().sourceSpan().offset(),
                postfix.operations().getLast().sourceSpan().endOffset())).isEqualTo("!");

        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("value := n%!;\nvalue");
        assertThat(AstStructuralEquality.equals(ast, build(AstPrettyPrinter.print(ast)))).isTrue();
    }

    @Test
    @DisplayName("between and membership operators preserve negation metadata and canonical synonyms")
    void betweenAndMembershipOperatorsPreserveNegationMetadataAndCanonicalSynonyms() {
        ExpressionFileNode ast = build("range := x not between low and high; excluded := x nin [1, 2]; "
                + "alsoExcluded := x not in [3, 4]; included := x in []; included");

        BetweenNode range = (BetweenNode) ast.assignments().getFirst().expression();
        assertThat(range.negated()).isTrue();
        assertThat(range.value()).isInstanceOf(IdentifierNode.class);
        assertThat(range.lowerBound()).isInstanceOf(IdentifierNode.class);
        assertThat(range.upperBound()).isInstanceOf(IdentifierNode.class);

        MembershipNode excluded = (MembershipNode) ast.assignments().get(1).expression();
        MembershipNode alsoExcluded = (MembershipNode) ast.assignments().get(2).expression();
        MembershipNode included = (MembershipNode) ast.assignments().get(3).expression();
        assertThat(excluded.negated()).isTrue();
        assertThat(alsoExcluded.negated()).isTrue();
        assertThat(included.negated()).isFalse();
        assertThat(excluded.collection()).isInstanceOf(CollectionLiteralNode.class);
        assertThat(((CollectionLiteralNode) included.collection()).elements()).isEmpty();

        String printed = AstPrettyPrinter.print(ast);
        assertThat(printed).isEqualTo("range := x not between low and high;\n"
                + "excluded := x not in [1, 2];\n"
                + "alsoExcluded := x not in [3, 4];\n"
                + "included := x in [];\n"
                + "included");
        assertThat(AstStructuralEquality.equals(ast, build(printed))).isTrue();
    }

    @Test
    @DisplayName("direct null coalesce chains are variadic without flattening grouped operands")
    void directNullCoalesceChainsAreVariadicWithoutFlatteningGroupedOperands() {
        String source = "chain := a ?? b ?? c; grouped := a ?? (b ?? c); chain";
        ExpressionFileNode ast = build(source);

        NullCoalesceNode chain = (NullCoalesceNode) ast.assignments().getFirst().expression();
        assertThat(chain.operands()).hasSize(3);
        assertThat(chain.operatorSpans()).hasSize(2);
        assertThat(source.substring(chain.operatorSpans().getFirst().offset(), chain.operatorSpans().getFirst().endOffset()))
                .isEqualTo("??");
        assertThat(source.substring(chain.operatorSpans().getLast().offset(), chain.operatorSpans().getLast().endOffset()))
                .isEqualTo("??");

        NullCoalesceNode groupedOuter = (NullCoalesceNode) ast.assignments().get(1).expression();
        assertThat(groupedOuter.operands()).hasSize(2);
        assertThat(groupedOuter.operands().getLast()).isInstanceOf(GroupedExpressionNode.class);
        GroupedExpressionNode groupedOperand = (GroupedExpressionNode) groupedOuter.operands().getLast();
        assertThat(groupedOperand.expression()).isInstanceOf(NullCoalesceNode.class);
        assertThat(((NullCoalesceNode) groupedOperand.expression()).operands()).hasSize(2);

        String printed = AstPrettyPrinter.print(ast);
        assertThat(printed).isEqualTo("chain := a ?? b ?? c;\ngrouped := a ?? (b ?? c);\nchain");
        assertThat(AstStructuralEquality.equals(ast, build(printed))).isTrue();
    }

    @Test
    @DisplayName("navigation from identifiers, calls, and current item builds ordered typed links")
    void navigationFromIdentifiersCallsAndCurrentItemBuildsOrderedTypedLinks() {
        String source = "identifier := account.name?.if(total, @.value)[\"key\"]?.[2][*]; "
                + "call := load().items(1, @)?.size; current := @?.name; current";
        ExpressionFileNode ast = build(source);

        NavigationChainNode identifierChain = (NavigationChainNode) ast.assignments().getFirst().expression();
        assertThat(identifierChain.receiver()).isInstanceOf(IdentifierNode.class);
        assertThat(identifierName(identifierChain.receiver())).isEqualTo("account");
        assertThat(identifierChain.links()).hasSize(5);
        assertThat(identifierChain.links()).extracting(link -> link.getClass().getSimpleName()).containsExactly(
                "PropertyNavigationLink",
                "CallNavigationLink",
                "StringKeySubscriptNavigationLink",
                "IndexSubscriptNavigationLink",
                "WildcardNavigationLink");
        assertThat(identifierChain.links()).allSatisfy(link -> {
            assertThat(link.id()).isNotEqualTo(NodeId.UNASSIGNED);
            assertThat(source.substring(link.sourceSpan().offset(), link.sourceSpan().endOffset())).isNotBlank();
        });
        PropertyNavigationLink property = (PropertyNavigationLink) identifierChain.links().getFirst();
        assertThat(property.memberName().value()).isEqualTo("name");
        assertThat(property.safe()).isFalse();
        CallNavigationLink call = (CallNavigationLink) identifierChain.links().get(1);
        assertThat(call.memberName().value()).isEqualTo("if");
        assertThat(call.safe()).isTrue();
        assertThat(call.arguments()).hasSize(2);
        assertThat(call.arguments().getLast()).isInstanceOf(ExpressionCallArgument.class);
        NavigationChainNode currentArgument = (NavigationChainNode) ((ExpressionCallArgument) call.arguments().getLast()).expression();
        assertThat(currentArgument.receiver()).isInstanceOf(CurrentItemNode.class);
        assertThat(currentArgument.links()).singleElement().satisfies(link -> {
            assertThat(link).isInstanceOf(PropertyNavigationLink.class);
            assertThat(((PropertyNavigationLink) link).memberName().value()).isEqualTo("value");
        });
        StringKeySubscriptNavigationLink key = (StringKeySubscriptNavigationLink) identifierChain.links().get(2);
        assertThat(key.key()).isEqualTo("key");
        assertThat(key.safe()).isFalse();
        IndexSubscriptNavigationLink index = (IndexSubscriptNavigationLink) identifierChain.links().get(3);
        assertThat(index.index()).isEqualTo(new SubscriptIntegerLiteral(2));
        assertThat(index.safe()).isTrue();
        WildcardNavigationLink wildcard = (WildcardNavigationLink) identifierChain.links().getLast();
        assertThat(wildcard.safe()).isFalse();

        NavigationChainNode callChain = (NavigationChainNode) ast.assignments().get(1).expression();
        assertThat(callChain.receiver()).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode receiverCall = (FunctionCallNode) callChain.receiver();
        assertThat(receiverCall.name().value()).isEqualTo("load");
        assertThat(callChain.links()).hasSize(2);
        CallNavigationLink items = (CallNavigationLink) callChain.links().getFirst();
        assertThat(items.memberName().value()).isEqualTo("items");
        assertThat(items.arguments()).hasSize(2);
        assertThat(callChain.links().getLast()).isInstanceOf(PropertyNavigationLink.class);
        assertThat(((PropertyNavigationLink) callChain.links().getLast()).safe()).isTrue();

        NavigationChainNode currentChain = (NavigationChainNode) ast.assignments().get(2).expression();
        assertThat(currentChain.receiver()).isInstanceOf(CurrentItemNode.class);
        assertThat(currentChain.links()).singleElement().satisfies(link -> {
            assertThat(link).isInstanceOf(PropertyNavigationLink.class);
            assertThat(((PropertyNavigationLink) link).safe()).isTrue();
        });

        String printed = AstPrettyPrinter.print(ast);
        assertThat(printed).isEqualTo("identifier := account.name?.if(total, @.value)[\"key\"]?.[2][*];\n"
                + "call := load().items(1, @)?.size;\n"
                + "current := @?.name;\n"
                + "current");
        assertThat(AstStructuralEquality.equals(ast, build(printed))).isTrue();
    }

    @Test
    @DisplayName("subscript links preserve decimal integer values and slice bounds")
    void subscriptLinksPreserveDecimalIntegerValuesAndSliceBounds() {
        String source = "value := data[16][77][-2:20][7:][:-10][*]; value";
        ExpressionFileNode ast = build(source);

        NavigationChainNode chain = (NavigationChainNode) ast.assignments().getFirst().expression();

        assertThat(chain.links()).hasSize(6);
        assertThat(((IndexSubscriptNavigationLink) chain.links().get(0)).index())
                .isEqualTo(new SubscriptIntegerLiteral(16));
        assertThat(((IndexSubscriptNavigationLink) chain.links().get(1)).index())
                .isEqualTo(new SubscriptIntegerLiteral(77));
        SliceSubscriptNavigationLink closedSlice = (SliceSubscriptNavigationLink) chain.links().get(2);
        assertThat(closedSlice.start()).isEqualTo(integerBound(-2));
        assertThat(closedSlice.end()).isEqualTo(integerBound(20));
        SliceSubscriptNavigationLink openEndSlice = (SliceSubscriptNavigationLink) chain.links().get(3);
        assertThat(openEndSlice.start()).isEqualTo(integerBound(7));
        assertThat(openEndSlice.end()).isEqualTo(UnboundedSubscriptSliceBound.INSTANCE);
        SliceSubscriptNavigationLink openStartSlice = (SliceSubscriptNavigationLink) chain.links().get(4);
        assertThat(openStartSlice.start()).isEqualTo(UnboundedSubscriptSliceBound.INSTANCE);
        assertThat(openStartSlice.end()).isEqualTo(integerBound(-10));
        assertThat(chain.links().get(5)).isInstanceOf(WildcardNavigationLink.class);

        String printed = AstPrettyPrinter.print(ast);
        assertThat(printed).isEqualTo("value := data[16][77][-2:20][7:][:-10][*];\nvalue");
        assertThat(AstStructuralEquality.equals(ast, build(printed))).isTrue();
    }

    @Test
    @DisplayName("filters, global calls, and navigated calls share source-faithful call arguments")
    void filtersGlobalCallsAndNavigatedCallsShareSourceFaithfulCallArguments() {
        String source = "filtered := items[?(@.active and @.score > 10)]; "
                + "mapped := filtered.map(@ -> @.score + bonus).sum(); "
                + "nested := groups?.map(@ -> @.items[?(@.active)]); "
                + "paged := mapped.window(0, @ -> @.score, 10); "
                + "global := transform(items, @ -> @.value); standalone := @; paged";
        ExpressionFileNode ast = build(source);

        NavigationChainNode filtered = (NavigationChainNode) ast.assignments().getFirst().expression();
        assertThat(filtered.links()).singleElement().satisfies(link -> {
            assertThat(link).isInstanceOf(FilterNavigationLink.class);
            FilterNavigationLink filter = (FilterNavigationLink) link;
            assertThat(filter.safe()).isFalse();
            assertThat(source.substring(filter.sourceSpan().offset(), filter.sourceSpan().endOffset()))
                    .isEqualTo("[?(@.active and @.score > 10)]");
            assertThat(filter.predicate()).isInstanceOf(BinaryOperationNode.class);
        });

        NavigationChainNode mapped = (NavigationChainNode) ast.assignments().get(1).expression();
        assertThat(mapped.links()).hasSize(2);
        CallNavigationLink map = (CallNavigationLink) mapped.links().getFirst();
        assertThat(map.memberName().value()).isEqualTo("map");
        assertThat(map.arguments()).singleElement().satisfies(argument -> {
            assertThat(argument).isInstanceOf(LambdaCallArgument.class);
            LambdaNode lambda = ((LambdaCallArgument) argument).lambda();
            assertThat(lambda.currentItem()).isInstanceOf(CurrentItemNode.class);
            assertThat(source.substring(lambda.sourceSpan().offset(), lambda.sourceSpan().endOffset()))
                    .isEqualTo("@ -> @.score + bonus");
            assertThat(lambda.body()).isInstanceOf(BinaryOperationNode.class);
        });
        CallNavigationLink sum = (CallNavigationLink) mapped.links().getLast();
        assertThat(sum.memberName().value()).isEqualTo("sum");
        assertThat(sum.arguments()).isEmpty();

        NavigationChainNode nested = (NavigationChainNode) ast.assignments().get(2).expression();
        CallNavigationLink nestedMap = (CallNavigationLink) nested.links().getFirst();
        assertThat(nestedMap.safe()).isTrue();
        LambdaNode nestedLambda = ((LambdaCallArgument) nestedMap.arguments().getFirst()).lambda();
        assertThat(nestedLambda.body()).isInstanceOf(NavigationChainNode.class);
        NavigationChainNode nestedLambdaBody = (NavigationChainNode) nestedLambda.body();
        assertThat(nestedLambdaBody.receiver()).isInstanceOf(CurrentItemNode.class);
        assertThat(nestedLambdaBody.links()).hasSize(2);
        assertThat(nestedLambdaBody.links().getLast()).isInstanceOf(FilterNavigationLink.class);

        NavigationChainNode paged = (NavigationChainNode) ast.assignments().get(3).expression();
        CallNavigationLink window = (CallNavigationLink) paged.links().getFirst();
        assertThat(window.arguments()).hasSize(3);
        assertThat(window.arguments().get(0)).isInstanceOf(ExpressionCallArgument.class);
        assertThat(window.arguments().get(1)).isInstanceOf(LambdaCallArgument.class);
        assertThat(window.arguments().get(2)).isInstanceOf(ExpressionCallArgument.class);
        ExpressionCallArgument firstArgument = (ExpressionCallArgument) window.arguments().getFirst();
        assertThat(firstArgument.expression()).isInstanceOf(LiteralNode.class);

        FunctionCallNode global = (FunctionCallNode) ast.assignments().get(4).expression();
        assertThat(global.arguments().getFirst()).isInstanceOf(ExpressionCallArgument.class);
        assertThat(global.arguments().getLast()).isInstanceOf(LambdaCallArgument.class);
        assertThat(ast.assignments().get(5).expression()).isInstanceOf(CurrentItemNode.class);

        String printed = AstPrettyPrinter.print(ast);
        assertThat(printed).isEqualTo("filtered := items[?(@.active and @.score > 10)];\n"
                + "mapped := filtered.map(@ -> @.score + bonus).sum();\n"
                + "nested := groups?.map(@ -> @.items[?(@.active)]);\n"
                + "paged := mapped.window(0, @ -> @.score, 10);\n"
                + "global := transform(items, @ -> @.value);\n"
                + "standalone := @;\n"
                + "paged");
        assertThat(AstStructuralEquality.equals(ast, build(printed))).isTrue();
    }

    @Test
    @DisplayName("classic and functional conditionals share one semantic AST shape with source syntax metadata")
    void classicAndFunctionalConditionalsShareOneSemanticAstShapeWithSourceSyntaxMetadata() {
        ExpressionFileNode classicAst = build(
                "classic := if flag then [1] elsif fallback then [2] else [] endif; classic");
        ExpressionFileNode functionalAst = build(
                "functional := if(flag, [1], fallback, [2], []); functional");
        ExpressionFileNode functionalSemicolonAst = build(
                "functional := if(flag; [1]; fallback; [2]; []); functional");

        ConditionalNode classic = (ConditionalNode) classicAst.assignments().getFirst().expression();
        ConditionalNode functional = (ConditionalNode) functionalAst.assignments().getFirst().expression();
        ConditionalNode functionalSemicolon = (ConditionalNode) functionalSemicolonAst.assignments().getFirst().expression();

        assertThat(classic.syntax()).isEqualTo(ConditionalSyntax.CLASSIC);
        assertThat(functional.syntax()).isEqualTo(ConditionalSyntax.FUNCTIONAL);
        assertThat(classic.branches()).hasSize(2);
        assertThat(functional.branches()).hasSize(2);
        assertThat(identifierName(classic.branches().getFirst().condition())).isEqualTo("flag");
        assertThat(identifierName(functional.branches().getFirst().condition())).isEqualTo("flag");
        assertThat(identifierName(classic.branches().getLast().condition())).isEqualTo("fallback");
        assertThat(identifierName(functional.branches().getLast().condition())).isEqualTo("fallback");
        assertThat(classic.branches().getFirst().consequence()).isInstanceOf(CollectionLiteralNode.class);
        assertThat(functional.branches().getFirst().consequence()).isInstanceOf(CollectionLiteralNode.class);
        assertThat(classic.elseExpression()).isInstanceOf(CollectionLiteralNode.class);
        assertThat(functional.elseExpression()).isInstanceOf(CollectionLiteralNode.class);
        assertThat(functional.separators()).extracting(ConditionalSeparatorOccurrence::separator)
                .containsExactly(
                        ConditionalSeparator.COMMA,
                        ConditionalSeparator.COMMA,
                        ConditionalSeparator.COMMA,
                        ConditionalSeparator.COMMA);
        assertThat(functionalSemicolon.separators()).extracting(ConditionalSeparatorOccurrence::separator)
                .containsExactly(
                        ConditionalSeparator.SEMICOLON,
                        ConditionalSeparator.SEMICOLON,
                        ConditionalSeparator.SEMICOLON,
                        ConditionalSeparator.SEMICOLON);

        assertThat(AstPrettyPrinter.print(classicAst)).isEqualTo(
                "classic := if flag then [1] elsif fallback then [2] else [] endif;\nclassic");
        assertThat(AstPrettyPrinter.print(functionalAst)).isEqualTo(
                "functional := if(flag, [1], fallback, [2], []);\nfunctional");
        assertThat(AstPrettyPrinter.print(functionalSemicolonAst)).isEqualTo(
                "functional := if(flag; [1]; fallback; [2]; []);\nfunctional");
        assertThat(AstStructuralEquality.equals(classicAst, build(AstPrettyPrinter.print(classicAst)))).isTrue();
        assertThat(AstStructuralEquality.equals(functionalAst, build(AstPrettyPrinter.print(functionalAst)))).isTrue();
        assertThat(AstStructuralEquality.equals(
                functionalSemicolonAst,
                build(AstPrettyPrinter.print(functionalSemicolonAst)))).isTrue();
    }

    @Test
    @DisplayName("conditional branches and else expressions carry node identities and source spans")
    void conditionalBranchesAndElseExpressionsCarryNodeIdentitiesAndSourceSpans() {
        String source = "decision := if flag then [1] elsif fallback then [2] else [] endif; decision";
        ExpressionFileNode ast = build(source);

        ConditionalNode conditional = (ConditionalNode) ast.assignments().getFirst().expression();
        ConditionalBranchNode firstBranch = conditional.branches().getFirst();
        ConditionalBranchNode secondBranch = conditional.branches().getLast();

        assertThat(conditional.id()).isNotEqualTo(NodeId.UNASSIGNED);
        assertThat(firstBranch.id()).isNotEqualTo(NodeId.UNASSIGNED);
        assertThat(secondBranch.id()).isNotEqualTo(NodeId.UNASSIGNED);
        assertThat(conditional.elseExpression().id()).isNotEqualTo(NodeId.UNASSIGNED);
        assertThat(source.substring(conditional.sourceSpan().offset(), conditional.sourceSpan().endOffset()))
                .isEqualTo("if flag then [1] elsif fallback then [2] else [] endif");
        assertThat(source.substring(firstBranch.sourceSpan().offset(), firstBranch.sourceSpan().endOffset()))
                .isEqualTo("flag then [1]");
        assertThat(source.substring(secondBranch.sourceSpan().offset(), secondBranch.sourceSpan().endOffset()))
                .isEqualTo("fallback then [2]");
        assertThat(source.substring(
                conditional.elseExpression().sourceSpan().offset(),
                conditional.elseExpression().sourceSpan().endOffset()))
                .isEqualTo("[]");
    }

    @Test
    @DisplayName("collection literals are immutable full-span nodes and empty collections share the empty element list")
    void collectionLiteralsAreImmutableFullSpanNodesAndEmptyCollectionsShareTheEmptyElementList() {
        String source = "first := []; second := []; numbers := [1, 2]; numbers";
        ExpressionFileNode ast = build(source);

        CollectionLiteralNode first = (CollectionLiteralNode) ast.assignments().getFirst().expression();
        CollectionLiteralNode second = (CollectionLiteralNode) ast.assignments().get(1).expression();
        CollectionLiteralNode numbers = (CollectionLiteralNode) ast.assignments().get(2).expression();

        assertThat(first).isNotSameAs(second);
        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(first.elements()).isSameAs(second.elements());
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> first.elements().add(second));
        assertThat(source.substring(first.sourceSpan().offset(), first.sourceSpan().endOffset())).isEqualTo("[]");
        assertThat(source.substring(second.sourceSpan().offset(), second.sourceSpan().endOffset())).isEqualTo("[]");
        assertThat(source.substring(numbers.sourceSpan().offset(), numbers.sourceSpan().endOffset())).isEqualTo("[1, 2]");
        assertThat(numbers.elements()).hasSize(2);
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("first := [];\nsecond := [];\nnumbers := [1, 2];\nnumbers");
        assertThat(AstStructuralEquality.equals(ast, build(AstPrettyPrinter.print(ast)))).isTrue();
    }

    @Test
    @DisplayName("full scalar precedence tracer builds and round-trips")
    void fullScalarPrecedenceTracerBuildsAndRoundTrips() {
        ExpressionFileNode ast = build("a ?? b or c and d = e xor f || g + h * -i root j ^ k%");

        assertThat(ast.resultExpression()).hasValueSatisfying(result ->
                assertThat(result).isInstanceOf(NullCoalesceNode.class));
        assertThat(AstPrettyPrinter.print(ast))
                .isEqualTo("a ?? b or c and d = e xor f || g + h * -i root j ^ k%");
        assertThat(AstStructuralEquality.equals(ast, build(AstPrettyPrinter.print(ast)))).isTrue();
    }

    @Test
    @DisplayName("invalid local temporal materialization returns stable diagnostics")
    void invalidLocalTemporalMaterializationReturnsStableDiagnostics() {
        ParseResult parseResult = parser.parse("badDate := d\"2024-02-30\"; badDateTime := dt\"2024-02-30T10:15:30\";");
        assertThat(parseResult).isInstanceOf(ParseSuccess.class);

        SemanticAstBuildResult buildResult = astBuilder.build((ParseSuccess) parseResult);

        assertThat(buildResult).isInstanceOf(SemanticAstBuildFailure.class);
        assertThat(((SemanticAstBuildFailure) buildResult).diagnostics()).satisfiesExactly(
                diagnostic -> {
                    assertThat(diagnostic.category()).isEqualTo(DiagnosticCategory.SEMANTIC);
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.AST_INVALID_DATE_LITERAL.name());
                    assertThat(diagnostic.primarySpan().orElseThrow().offset()).isEqualTo(11);
                    assertThat(diagnostic.primarySpan().orElseThrow().endOffset()).isEqualTo(24);
                },
                diagnostic -> {
                    assertThat(diagnostic.category()).isEqualTo(DiagnosticCategory.SEMANTIC);
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.AST_INVALID_DATE_TIME_LITERAL.name());
                    assertThat(diagnostic.primarySpan().orElseThrow().offset()).isEqualTo(41);
                    assertThat(diagnostic.primarySpan().orElseThrow().endOffset()).isEqualTo(64);
                });
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
        SemanticAstBuildResult buildResult = astBuilder.build((ParseSuccess) parseResult);
        assertThat(buildResult).isInstanceOf(SemanticAstBuildSuccess.class);
        return ((SemanticAstBuildSuccess) buildResult).file();
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

    private static String identifierName(ExpressionNode expression) {
        assertThat(expression).isInstanceOf(IdentifierNode.class);
        return ((IdentifierNode) expression).name();
    }

    private static IntegerSubscriptSliceBound integerBound(long value) {
        return new IntegerSubscriptSliceBound(new SubscriptIntegerLiteral(value));
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
            case CurrentTemporalValueNode currentTemporalValue -> new CurrentTemporalValueNode(
                    shift(currentTemporalValue.id(), offset),
                    currentTemporalValue.sourceSpan(),
                    currentTemporalValue.kind());
            case CurrentItemNode currentItem -> new CurrentItemNode(shift(currentItem.id(), offset), currentItem.sourceSpan());
            case FunctionCallNode functionCall -> new FunctionCallNode(
                    shift(functionCall.id(), offset),
                    functionCall.sourceSpan(),
                    functionCall.name(),
                    functionCall.arguments().stream().map(argument -> shiftCallArgument(argument, offset)).toList());
            case ConditionalNode conditional -> new ConditionalNode(
                    shift(conditional.id(), offset),
                    conditional.sourceSpan(),
                    conditional.syntax(),
                    conditional.branches().stream().map(branch -> shiftNodeIds(branch, offset)).toList(),
                    conditional.separators(),
                    shiftNodeIds(conditional.elseExpression(), offset));
            case GroupedExpressionNode grouped -> new GroupedExpressionNode(
                    shift(grouped.id(), offset),
                    grouped.sourceSpan(),
                    shiftNodeIds(grouped.expression(), offset));
            case IdentifierNode identifier -> new IdentifierNode(
                    shift(identifier.id(), offset),
                    identifier.sourceSpan(),
                    identifier.name());
            case LiteralNode literal -> new LiteralNode(shift(literal.id(), offset), literal.sourceSpan(), literal.value());
            case UnaryOperationNode unary -> new UnaryOperationNode(
                    shift(unary.id(), offset),
                    unary.sourceSpan(),
                    unary.operator(),
                    unary.operatorSpan(),
                    shiftNodeIds(unary.operand(), offset));
            case BinaryOperationNode binary -> new BinaryOperationNode(
                    shift(binary.id(), offset),
                    binary.sourceSpan(),
                    shiftNodeIds(binary.left(), offset),
                    binary.operator(),
                    binary.operatorSpan(),
                    shiftNodeIds(binary.right(), offset));
            case PostfixOperationNode postfix -> new PostfixOperationNode(
                    shift(postfix.id(), offset),
                    postfix.sourceSpan(),
                    shiftNodeIds(postfix.operand(), offset),
                    postfix.operations());
            case BetweenNode between -> new BetweenNode(
                    shift(between.id(), offset),
                    between.sourceSpan(),
                    shiftNodeIds(between.value(), offset),
                    between.negated(),
                    between.operatorSpan(),
                    shiftNodeIds(between.lowerBound(), offset),
                    between.lowerSeparatorSpan(),
                    shiftNodeIds(between.upperBound(), offset));
            case MembershipNode membership -> new MembershipNode(
                    shift(membership.id(), offset),
                    membership.sourceSpan(),
                    shiftNodeIds(membership.element(), offset),
                    membership.negated(),
                    membership.operatorSpan(),
                    shiftNodeIds(membership.collection(), offset));
            case NavigationChainNode navigation -> new NavigationChainNode(
                    shift(navigation.id(), offset),
                    navigation.sourceSpan(),
                    shiftNodeIds(navigation.receiver(), offset),
                    navigation.links().stream().map(link -> shiftNodeIds(link, offset)).toList());
            case NullCoalesceNode coalesce -> new NullCoalesceNode(
                    shift(coalesce.id(), offset),
                    coalesce.sourceSpan(),
                    coalesce.operands().stream().map(operand -> shiftNodeIds(operand, offset)).toList(),
                    coalesce.operatorSpans());
            case CollectionLiteralNode collection -> new CollectionLiteralNode(
                    shift(collection.id(), offset),
                    collection.sourceSpan(),
                    collection.elements().stream().map(operand -> shiftNodeIds(operand, offset)).toList());
        };
    }

    private static NavigationLink shiftNodeIds(NavigationLink link, int offset) {
        return switch (link) {
            case CallNavigationLink call -> new CallNavigationLink(
                    shift(call.id(), offset),
                    call.sourceSpan(),
                    call.memberName(),
                    call.safe(),
                    call.arguments().stream().map(argument -> shiftCallArgument(argument, offset)).toList());
            case FilterNavigationLink filter -> new FilterNavigationLink(
                    shift(filter.id(), offset),
                    filter.sourceSpan(),
                    shiftNodeIds(filter.predicate(), offset),
                    filter.safe());
            case IndexSubscriptNavigationLink index -> new IndexSubscriptNavigationLink(
                    shift(index.id(), offset),
                    index.sourceSpan(),
                    index.index(),
                    index.safe());
            case PropertyNavigationLink property -> new PropertyNavigationLink(
                    shift(property.id(), offset),
                    property.sourceSpan(),
                    property.memberName(),
                    property.safe());
            case SliceSubscriptNavigationLink slice -> new SliceSubscriptNavigationLink(
                    shift(slice.id(), offset),
                    slice.sourceSpan(),
                    slice.start(),
                    slice.end(),
                    slice.safe());
            case StringKeySubscriptNavigationLink stringKey -> new StringKeySubscriptNavigationLink(
                    shift(stringKey.id(), offset),
                    stringKey.sourceSpan(),
                    stringKey.key(),
                    stringKey.safe());
            case WildcardNavigationLink wildcard -> new WildcardNavigationLink(
                    shift(wildcard.id(), offset),
                    wildcard.sourceSpan(),
                    wildcard.safe());
        };
    }

    private static CallArgument shiftCallArgument(CallArgument argument, int offset) {
        return switch (argument) {
            case ExpressionCallArgument expression -> new ExpressionCallArgument(
                    shiftNodeIds(expression.expression(), offset));
            case LambdaCallArgument lambda -> new LambdaCallArgument(shiftNodeIds(lambda.lambda(), offset));
        };
    }

    private static LambdaNode shiftNodeIds(LambdaNode lambda, int offset) {
        return new LambdaNode(
                shift(lambda.id(), offset),
                lambda.sourceSpan(),
                new CurrentItemNode(shift(lambda.currentItem().id(), offset), lambda.currentItem().sourceSpan()),
                lambda.arrowSpan(),
                shiftNodeIds(lambda.body(), offset));
    }

    private static ConditionalBranchNode shiftNodeIds(ConditionalBranchNode branch, int offset) {
        return new ConditionalBranchNode(
                shift(branch.id(), offset),
                branch.sourceSpan(),
                shiftNodeIds(branch.condition(), offset),
                shiftNodeIds(branch.consequence(), offset));
    }

    private static NodeId shift(NodeId id, int offset) {
        return new NodeId(id.value() + offset);
    }
}
