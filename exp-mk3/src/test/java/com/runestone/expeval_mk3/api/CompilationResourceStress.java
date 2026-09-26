package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildFailure;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** Opt-in ceiling gate, isolated from Maven's heap and stack. */
class CompilationResourceStress {

    @Test
    void ceilingsSurviveConstrainedChildJvm() throws Exception {
        Path output = Path.of("target", "stage12", "compilation-resource-stress.log");
        Files.createDirectories(output.getParent());
        Process child = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Xms512m", "-Xmx512m", "-Xss1m", "-cp", System.getProperty("java.class.path"),
                CompilationResourceStress.class.getName())
                .redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertThat(child.waitFor(90, TimeUnit.SECONDS)).as("stress child timeout; see %s", output).isTrue();
            assertThat(child.exitValue()).as("stress child; see %s", output).isZero();
        } finally {
            child.destroyForcibly();
        }
    }

    public static void main(String[] arguments) {
        System.out.println(System.getProperty("java.runtime.version"));
        for (ExpressionTrustMode mode : new ExpressionTrustMode[] {ExpressionTrustMode.TRUSTED, ExpressionTrustMode.SAFE}) {
            var limits = ExpressionResourceLimits.builder().maxSourceLength(262_144).maxTokenCount(65_536)
                    .maxSyntaxDepth(256).maxAstNodeCount(65_536).maxMaterializedSize(100_000).build();
            var environment = ExpressionEnvironment.builder().trustMode(mode).resourceLimits(limits).build();
            var engine = ExpressionEngine.builder().build();
            success(engine, environment, "1" + " ".repeat(262_143));
            failure(engine, environment, "1" + " ".repeat(262_144), "COMPILE_SOURCE_LENGTH_EXCEEDED");
            // 32,767 elements: 65,535 visible tokens plus one hidden whitespace token.
            success(engine, environment, "[" + "1,".repeat(32_766) + "1] ");
            failure(engine, environment, "[" + "1,".repeat(32_767) + "1]", "COMPILE_TOKEN_COUNT_EXCEEDED");
            for (int depth : new int[] {255, 256, 257}) {
                for (String source : new String[] {
                        "(".repeat(depth - 1) + "1" + ")".repeat(depth - 1),
                        "-".repeat(depth - 1) + "1",
                        "1+".repeat(depth - 1) + "1",
                        "1^".repeat(depth - 1) + "1",
                        "abs(".repeat(depth - 1) + "1" + ")".repeat(depth - 1),
                        "if true then ".repeat((depth - 1) / 2) + "-".repeat((depth - 1) % 2)
                                + "1" + " else 1 endif".repeat((depth - 1) / 2),
                        "if(true,".repeat((depth - 1) / 2) + "-".repeat((depth - 1) % 2)
                                + "1" + ",1)".repeat((depth - 1) / 2)}) {
                    if (depth <= 256) {
                        success(engine, environment, source);
                    } else {
                        failure(engine, environment, source, "COMPILE_SYNTAX_DEPTH_EXCEEDED");
                    }
                }
                // These syntax shapes are independently valid even when their nested types
                // are outside the language's semantic contracts. Exercise parser and AST.
                for (String source : new String[] {
                        "[".repeat(depth - 1) + "1" + "]".repeat(depth - 1),
                        "f(@ -> ".repeat((depth - 1) / 2) + "-".repeat((depth - 1) % 2)
                                + "1" + ")".repeat((depth - 1) / 2),
                        "a[?(".repeat((depth - 1) / 2) + "-".repeat((depth - 1) % 2)
                                + "1" + ")]".repeat((depth - 1) / 2)}) {
                    var parsed = new ExpressionParser().parse(source, limits);
                    if (depth <= 256) {
                        assertThat(parsed).isInstanceOf(ParseSuccess.class);
                        assertThat(new SemanticAstBuilder().build((ParseSuccess) parsed, limits.maxAstNodeCount()))
                                .isInstanceOf(SemanticAstBuildSuccess.class);
                    } else {
                        assertThat(parsed).isInstanceOf(com.runestone.expeval_mk3.internal.parser.ParseFailure.class);
                    }
                }
            }
            var shallow = ExpressionEnvironment.builder().trustMode(mode).resourceLimits(
                    ExpressionResourceLimits.builder().maxSourceLength(262_144).maxTokenCount(65_536)
                            .maxSyntaxDepth(4).build()).build();
            failure(engine, shallow, "if (true) and true then ".repeat(1_000) + "1"
                    + " else 1 endif".repeat(1_000), "COMPILE_SYNTAX_DEPTH_EXCEEDED");
            failure(engine, shallow, "[a?.[0],".repeat(1_000) + "1" + "]".repeat(1_000),
                    "COMPILE_SYNTAX_DEPTH_EXCEEDED");
            // Each assignment contributes four nodes (assignment, target, unary, literal),
            // followed by file and result nodes. Use a separate token budget-safe node ceiling.
            String assignments = java.util.stream.IntStream.range(0, 10_000)
                    .mapToObj(index -> "v" + index + ":=-1;").collect(java.util.stream.Collectors.joining()) + "1";
            success(engine, environment, assignments);
            success(engine, environment, "1");
        }
        // Isolate the AST ceiling: a source with this many nodes naturally needs more
        // tokens than the independent token ceiling. The unbounded parser is a test seam.
        String assignments = java.util.stream.IntStream.range(0, 21_844)
                .mapToObj(index -> "v" + index + ":=1;").collect(java.util.stream.Collectors.joining());
        for (String suffix : new String[] {"-1", "--1", "---1"}) {
            var parsed = (ParseSuccess) new ExpressionParser().parse(assignments + suffix);
            var result = new SemanticAstBuilder().build(parsed, 65_536);
            if (suffix.equals("---1")) {
                assertThat(result).isInstanceOf(SemanticAstBuildFailure.class);
                assertThat(((SemanticAstBuildFailure) result).diagnostics())
                        .extracting(ExpressionDiagnostic::code).containsExactly("COMPILE_AST_NODE_COUNT_EXCEEDED");
            } else {
                assertThat(result).isInstanceOf(SemanticAstBuildSuccess.class);
            }
        }
        System.out.println("Compilation resource ceilings passed");
    }

    private static void success(ExpressionEngine engine, ExpressionEnvironment environment, String source) {
        assertThat(engine.compile(source, environment)).isInstanceOf(ExpressionCompilationResult.Success.class);
    }

    private static void failure(ExpressionEngine engine, ExpressionEnvironment environment, String source, String code) {
        var result = engine.compile(source, environment);
        assertThat(result).isInstanceOf(ExpressionCompilationResult.Failure.class);
        assertThat(((ExpressionCompilationResult.Failure) result).diagnostics())
                .extracting(ExpressionDiagnostic::code).containsExactly(code);
    }
}
