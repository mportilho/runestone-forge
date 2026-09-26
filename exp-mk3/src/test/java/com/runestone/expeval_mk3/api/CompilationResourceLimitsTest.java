package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompilationResourceLimitsTest {

    private final ExpressionEngine engine = ExpressionEngine.builder().build();

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void sourceLengthUsesUtf16AndRejectsBeforeCacheAndUncachedPipeline(ExpressionTrustMode mode) {
        var environment = environment(mode, ExpressionResourceLimits.builder().maxSourceLength(5).build());
        for (String source : List.of("\"😀\"", "\"😀x\"", "\"😀xy\"")) {
            var result = engine.compile(source, environment);
            if (mode == ExpressionTrustMode.UNSAFE || source.length() <= 5) {
                assertThat(result).isInstanceOf(ExpressionCompilationResult.Success.class);
            } else {
                diagnostic(result, "COMPILE_SOURCE_LENGTH_EXCEEDED", DiagnosticCategory.PARSE);
                assertThat(engine.compile(source, environment)).isNotSameAs(result);
                diagnostic(CompilationPipeline.compile(source, environment, RuntimeServices.systemDefault()),
                        "COMPILE_SOURCE_LENGTH_EXCEEDED", DiagnosticCategory.PARSE);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void tokensIncludeHiddenWhitespaceButExcludeEofAndSkippedComments(ExpressionTrustMode mode) {
        var environment = environment(mode, ExpressionResourceLimits.builder().maxTokenCount(4).build());
        for (String source : List.of("1+2", "1 +2", "1 + 2")) {
            var result = engine.compile(source, environment);
            if (mode == ExpressionTrustMode.UNSAFE || !source.equals("1 + 2")) {
                assertThat(result).isInstanceOf(ExpressionCompilationResult.Success.class);
            } else {
                assertThat(diagnostic(result, "COMPILE_TOKEN_COUNT_EXCEEDED", DiagnosticCategory.PARSE).primarySpan())
                        .contains(new SourceSpan(4, 5, 1, 5));
            }
        }
        assertThat(engine.compile("/* ignored */1+2", environment))
                .isInstanceOf(ExpressionCompilationResult.Success.class);
    }

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void depthCoversRecursiveAndLeftDeepShapes(ExpressionTrustMode mode) {
        var environment = environment(mode, ExpressionResourceLimits.builder().maxSyntaxDepth(4).build());
        for (int depth = 3; depth <= 5; depth++) {
            for (String source : List.of(
                    "(".repeat(depth - 1) + "1" + ")".repeat(depth - 1),
                    "-".repeat(depth - 1) + "1",
                    "1+".repeat(depth - 1) + "1",
                    "1^".repeat(depth - 1) + "1")) {
                var result = engine.compile(source, environment);
                if (mode == ExpressionTrustMode.UNSAFE || depth <= 4) {
                    assertThat(result).as(source).isInstanceOf(ExpressionCompilationResult.Success.class);
                } else {
                    diagnostic(result, "COMPILE_SYNTAX_DEPTH_EXCEEDED", DiagnosticCategory.PARSE);
                }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void conditionalDepthIncludesBranchNodes(ExpressionTrustMode mode) {
        for (String source : List.of("if true then if true then 1 else 1 endif else 1 endif",
                "if(true,if(true,1,1),1)")) {
            for (int limit = 4; limit <= 6; limit++) {
                var environment = environment(mode, ExpressionResourceLimits.builder().maxSyntaxDepth(limit).build());
                var result = engine.compile(source, environment);
                if (mode == ExpressionTrustMode.UNSAFE || limit >= 5) {
                    assertThat(result).isInstanceOf(ExpressionCompilationResult.Success.class);
                } else {
                    diagnostic(result, "COMPILE_SYNTAX_DEPTH_EXCEEDED", DiagnosticCategory.PARSE);
                }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void nodesIncludeFileAndAreCheckedBeforeSemanticResolution(ExpressionTrustMode mode) {
        var environment = environment(mode, ExpressionResourceLimits.builder().maxAstNodeCount(4).build());
        for (String source : List.of("-1", "--1", "---1")) {
            var result = engine.compile(source, environment);
            if (mode == ExpressionTrustMode.UNSAFE || !source.equals("---1")) {
                assertThat(result).isInstanceOf(ExpressionCompilationResult.Success.class);
            } else {
                diagnostic(result, "COMPILE_AST_NODE_COUNT_EXCEEDED", DiagnosticCategory.SEMANTIC);
            }
        }
        if (mode != ExpressionTrustMode.UNSAFE) {
            diagnostic(engine.compile("[unknown,other,third]", environment),
                    "COMPILE_AST_NODE_COUNT_EXCEEDED", DiagnosticCategory.SEMANTIC);
        }
    }

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void zeroDisablesEachCapacityOnlyInEnforcingModes(ExpressionTrustMode mode) {
        var limits = List.of(
                ExpressionResourceLimits.builder().maxSourceLength(0).build(),
                ExpressionResourceLimits.builder().maxTokenCount(0).build(),
                ExpressionResourceLimits.builder().maxSyntaxDepth(0).build(),
                ExpressionResourceLimits.builder().maxAstNodeCount(0).build());
        var codes = List.of("COMPILE_SOURCE_LENGTH_EXCEEDED", "COMPILE_TOKEN_COUNT_EXCEEDED",
                "COMPILE_SYNTAX_DEPTH_EXCEEDED", "COMPILE_AST_NODE_COUNT_EXCEEDED");
        for (int index = 0; index < limits.size(); index++) {
            var result = engine.compile("1", environment(mode, limits.get(index)));
            if (mode == ExpressionTrustMode.UNSAFE) {
                assertThat(result).isInstanceOf(ExpressionCompilationResult.Success.class);
            } else {
                diagnostic(result, codes.get(index), index == 3 ? DiagnosticCategory.SEMANTIC : DiagnosticCategory.PARSE);
            }
        }
    }

    @Test
    void wideThousandNodeExpressionFitsDefaults() {
        String source = "[" + "1,".repeat(999) + "1]";
        assertThat(engine.compile(source, ExpressionEnvironment.builder().build()))
                .isInstanceOf(ExpressionCompilationResult.Success.class);
    }

    private static ExpressionEnvironment environment(ExpressionTrustMode mode, ExpressionResourceLimits limits) {
        return ExpressionEnvironment.builder().trustMode(mode).resourceLimits(limits).build();
    }

    private static ExpressionDiagnostic diagnostic(ExpressionCompilationResult result, String code, DiagnosticCategory category) {
        assertThat(result).isInstanceOf(ExpressionCompilationResult.Failure.class);
        var diagnostics = ((ExpressionCompilationResult.Failure) result).diagnostics();
        assertThat(diagnostics).hasSize(1);
        var diagnostic = diagnostics.getFirst();
        assertThat(diagnostic.code()).isEqualTo(code);
        assertThat(diagnostic.category()).isEqualTo(category);
        assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.ERROR);
        assertThat(diagnostic.primarySpan()).isPresent();
        assertThat(diagnostic.suggestion()).isEmpty();
        return diagnostic;
    }
}
