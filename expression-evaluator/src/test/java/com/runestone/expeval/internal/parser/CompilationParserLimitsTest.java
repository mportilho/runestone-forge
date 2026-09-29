package com.runestone.expeval.internal.parser;

import com.runestone.expeval.api.ExpressionResourceLimits;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CompilationParserLimitsTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "(1)|2", "[1]|2", "f(1)|2", "a[0]|2", "a[?(true)]|3",
            "f(@ -> 1)|3", "f(@ -> f(@ -> 1))|5",
            "if (true) then 1 else 1 endif|4", "if(true,1,2)|3",
            "if (true) and true then 1 else 1 endif|5",
            "a.b.c.d.e|2", "a??b??c??d|2", "(((1)))+1|5",
            "a?.[0]|3", "a?.if?.then?.else?.endif|2", "a?.[?(@=1)]|5",
            "[1,2,3,4,5,6]|2", "f(1,2,3,4,5)|2",
            "\"((((----^^^^\"|1", "/* ((( ---- ^^^ */ 1|1", "1%%%%%%%%%%|2", "1!!!!!|2",
            "!!!!!true|6", "f(@ -> if true then @ else 1 endif)|5"
    })
    void projectsEachStructuralFamilyAndResetsSiblingSegments(String source, int depth) {
        var parser = new ExpressionParser();
        for (int limit = depth - 1; limit <= depth + 1; limit++) {
            var result = parser.parse(source, ExpressionResourceLimits.builder().maxSyntaxDepth(limit).build());
            if (limit < depth) {
                assertThat(result).isInstanceOf(ParseFailure.class);
                assertThat(((ParseFailure) result).diagnostics()).extracting(diagnostic -> diagnostic.code())
                        .containsExactly("COMPILE_SYNTAX_DEPTH_EXCEEDED");
            } else {
                assertThat(result).isInstanceOf(ParseSuccess.class);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"(", "[", "f(", "f(@ -> ", "if true then ", "-", "1^", "1+",
            "a[?(", "if(true,"})
    void rejectsMalformedDeepSourcesBeforeParsingAndReusesContext(String fragment) {
        var parser = new ExpressionParser();
        var limits = ExpressionResourceLimits.builder().maxSyntaxDepth(8).build();
        var result = parser.parse(fragment.repeat(30), limits);
        assertThat(result).isInstanceOf(ParseFailure.class);
        assertThat(((ParseFailure) result).diagnostics()).extracting(diagnostic -> diagnostic.code())
                .containsExactly("COMPILE_SYNTAX_DEPTH_EXCEEDED");
        assertThat(parser.parse("1", limits)).isInstanceOf(ParseSuccess.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"if (true) and true then ", "if ([1,2]=[1,2]) then ",
            "if (if(true,true,false)) or false then "})
    void groupedClassicConditionsCannotHideNestedBranches(String prefix) {
        var parser = new ExpressionParser();
        var limits = ExpressionResourceLimits.builder().maxTokenCount(65_536).maxSyntaxDepth(8).build();
        var result = parser.parse(prefix.repeat(100) + "1" + " else 1 endif".repeat(100), limits);
        assertThat(result).isInstanceOf(ParseFailure.class);
        assertThat(((ParseFailure) result).diagnostics()).extracting(diagnostic -> diagnostic.code())
                .containsExactly("COMPILE_SYNTAX_DEPTH_EXCEEDED");
        assertThat(parser.parse("1", limits)).isInstanceOf(ParseSuccess.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"[a?.[0],", "[a?.[?(@=1)],", "[a?.if[0],"})
    void safeSubscriptClosersCannotPopEnclosingCollections(String prefix) {
        var parser = new ExpressionParser();
        var limits = ExpressionResourceLimits.builder().maxSyntaxDepth(8).build();
        var result = parser.parse(prefix.repeat(100) + "1" + "]".repeat(100), limits);
        assertThat(result).isInstanceOf(ParseFailure.class);
        assertThat(((ParseFailure) result).diagnostics()).extracting(diagnostic -> diagnostic.code())
                .containsExactly("COMPILE_SYNTAX_DEPTH_EXCEEDED");
        assertThat(parser.parse("1", limits)).isInstanceOf(ParseSuccess.class);
    }
}
