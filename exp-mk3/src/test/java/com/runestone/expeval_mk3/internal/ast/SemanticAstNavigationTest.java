package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.source.SourceSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticAstNavigationTest {

    private final ExpressionParser parser = new ExpressionParser();
    private final SemanticAstBuilder astBuilder = new SemanticAstBuilder();

    @Test
    @DisplayName("identifier navigation builds one ordered chain with typed member links")
    void identifierNavigationBuildsOneOrderedChainWithTypedMemberLinks() {
        ExpressionFileNode ast = build("person.if?.then(1, name).*");

        NavigationChainNode chain = resultAs(ast, NavigationChainNode.class);
        IdentifierNode receiver = as(chain.receiver(), IdentifierNode.class);
        PropertyNavigationLink property = as(chain.links().get(0), PropertyNavigationLink.class);
        MethodNavigationLink method = as(chain.links().get(1), MethodNavigationLink.class);
        WildcardNavigationLink wildcard = as(chain.links().get(2), WildcardNavigationLink.class);

        assertThat(receiver.name()).isEqualTo("person");
        assertThat(chain.links()).hasSize(3);
        assertThat(property.memberName()).isEqualTo(new MemberName("if", new SourceSpan(7, 9, 1, 8)));
        assertThat(property.safeNavigation()).isFalse();
        assertThat(property.sourceSpan()).isEqualTo(new SourceSpan(6, 9, 1, 7));
        assertThat(method.memberName()).isEqualTo(new MemberName("then", new SourceSpan(11, 15, 1, 12)));
        assertThat(method.safeNavigation()).isTrue();
        assertThat(method.arguments()).hasSize(2);
        assertThat(wildcard.sourceSpan()).isEqualTo(new SourceSpan(24, 26, 1, 25));
        assertThat(property.id().value()).isGreaterThan(chain.id().value());
        assertThat(method.id().value()).isGreaterThan(property.id().value());
        assertThat(wildcard.id().value()).isGreaterThan(method.id().value());
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("person.if?.then(1, name).*");
        assertRoundTrips(ast);
    }

    @Test
    @DisplayName("function call and current item receivers participate in navigation chains")
    void functionCallAndCurrentItemReceiversParticipateInNavigationChains() {
        ExpressionFileNode functionAst = build("resolve(user)?.value");
        ExpressionFileNode currentItemAst = build("@[\"key\"]");

        NavigationChainNode functionChain = resultAs(functionAst, NavigationChainNode.class);
        FunctionCallNode functionCall = as(functionChain.receiver(), FunctionCallNode.class);
        PropertyNavigationLink safeProperty = as(functionChain.links().getFirst(), PropertyNavigationLink.class);
        NavigationChainNode currentItemChain = resultAs(currentItemAst, NavigationChainNode.class);
        CurrentItemNode currentItem = as(currentItemChain.receiver(), CurrentItemNode.class);
        SubscriptNavigationLink stringKey = as(currentItemChain.links().getFirst(), SubscriptNavigationLink.class);

        assertThat(functionCall.name()).isEqualTo(new MemberName("resolve", new SourceSpan(0, 7, 1, 1)));
        assertThat(functionCall.arguments()).hasSize(1);
        assertThat(safeProperty.safeNavigation()).isTrue();
        assertThat(safeProperty.memberName().value()).isEqualTo("value");
        assertThat(currentItem.sourceSpan()).isEqualTo(new SourceSpan(0, 1, 1, 1));
        assertThat(stringKey.subscript()).isEqualTo(new StringKeySubscript("key"));
        assertThat(AstPrettyPrinter.print(functionAst)).isEqualTo("resolve(user)?.value");
        assertThat(AstPrettyPrinter.print(currentItemAst)).isEqualTo("@[\"key\"]");
        assertRoundTrips(functionAst);
        assertRoundTrips(currentItemAst);
    }

    @Test
    @DisplayName("subscripts build source-faithful typed links and preserve integer literal formats")
    void subscriptsBuildSourceFaithfulTypedLinksAndPreserveIntegerLiteralFormats() {
        ExpressionFileNode ast = build("items[0x0A][-07:10][5:][:-0xF]?.[*]");

        NavigationChainNode chain = resultAs(ast, NavigationChainNode.class);
        SubscriptNavigationLink hexIndexLink = as(chain.links().get(0), SubscriptNavigationLink.class);
        SubscriptNavigationLink octalSliceLink = as(chain.links().get(1), SubscriptNavigationLink.class);
        SubscriptNavigationLink openEndSliceLink = as(chain.links().get(2), SubscriptNavigationLink.class);
        SubscriptNavigationLink openStartSliceLink = as(chain.links().get(3), SubscriptNavigationLink.class);
        SubscriptNavigationLink safeWildcardLink = as(chain.links().get(4), SubscriptNavigationLink.class);

        IndexSubscript hexIndex = as(hexIndexLink.subscript(), IndexSubscript.class);
        SliceSubscript octalSlice = as(octalSliceLink.subscript(), SliceSubscript.class);
        SliceSubscript openEndSlice = as(openEndSliceLink.subscript(), SliceSubscript.class);
        SliceSubscript openStartSlice = as(openStartSliceLink.subscript(), SliceSubscript.class);

        assertThat(hexIndex.index()).isEqualTo(new SignedIntegerLiteral(BigInteger.TEN, IntegerLiteralFormat.HEXADECIMAL));
        assertThat(octalSlice.start()).contains(new SignedIntegerLiteral(BigInteger.valueOf(-7), IntegerLiteralFormat.OCTAL));
        assertThat(octalSlice.end()).contains(new SignedIntegerLiteral(BigInteger.TEN, IntegerLiteralFormat.DECIMAL));
        assertThat(openEndSlice.start()).contains(new SignedIntegerLiteral(BigInteger.valueOf(5), IntegerLiteralFormat.DECIMAL));
        assertThat(openEndSlice.end()).isEmpty();
        assertThat(openStartSlice.start()).isEmpty();
        assertThat(openStartSlice.end()).contains(new SignedIntegerLiteral(BigInteger.valueOf(-15), IntegerLiteralFormat.HEXADECIMAL));
        assertThat(safeWildcardLink.safeNavigation()).isTrue();
        assertThat(safeWildcardLink.subscript()).isEqualTo(new WildcardSubscript());
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("items[0xa][-07:10][5:][:-0xf]?.[*]");
        assertRoundTrips(ast);
    }

    private ExpressionFileNode build(String source) {
        ParseResult parseResult = parser.parse(source);
        assertThat(parseResult).isInstanceOf(ParseSuccess.class);
        SemanticAstResult result = astBuilder.build((ParseSuccess) parseResult);
        assertThat(result).isInstanceOf(SemanticAstSuccess.class);
        return ((SemanticAstSuccess) result).file();
    }

    private void assertRoundTrips(ExpressionFileNode ast) {
        ExpressionFileNode reparsed = build(AstPrettyPrinter.print(ast));

        assertThat(AstStructuralEquality.equals(ast, reparsed)).isTrue();
    }

    private static <T extends ExpressionNode> T resultAs(ExpressionFileNode ast, Class<T> type) {
        assertThat(ast.resultExpression()).isPresent();
        return as(ast.resultExpression().orElseThrow(), type);
    }

    private static <T> T as(Object node, Class<T> type) {
        assertThat(node).isInstanceOf(type);
        return type.cast(node);
    }
}
