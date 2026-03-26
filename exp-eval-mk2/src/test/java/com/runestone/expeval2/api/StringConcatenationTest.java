package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.internal.grammar.ParsingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Operador || de concatenação de strings")
class StringConcatenationTest {

    // -------------------------------------------------------------------------
    // Fixtures de funções para testes com catálogo
    // -------------------------------------------------------------------------

    public static final class StringFunctions {

        private StringFunctions() {}

        public static String teste() {
            return "!";
        }

        public static String prefixar(String valor) {
            return ">>>" + valor;
        }
    }

    // -------------------------------------------------------------------------
    // Literais
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Concatenação com literais")
    class Literals {

        @Test
        @DisplayName("dois literais concatenados retornam a string unida")
        void twoLiteralsShouldConcatenate() {
            assertThat(AssignmentExpression.compile("x = \"hello\" || \" world\";")
                    .compute().get("x"))
                    .isEqualTo("hello world");
        }

        @Test
        @DisplayName("três literais encadeados")
        void chainOfThreeLiterals() {
            assertThat(AssignmentExpression.compile("x = \"a\" || \"b\" || \"c\";")
                    .compute().get("x"))
                    .isEqualTo("abc");
        }

        @Test
        @DisplayName("literal vazio à direita não altera o resultado")
        void emptyLiteralOnRight() {
            assertThat(AssignmentExpression.compile("x = \"hello\" || \"\";")
                    .compute().get("x"))
                    .isEqualTo("hello");
        }

        @Test
        @DisplayName("literal vazio à esquerda não altera o resultado")
        void emptyLiteralOnLeft() {
            assertThat(AssignmentExpression.compile("x = \"\" || \"hello\";")
                    .compute().get("x"))
                    .isEqualTo("hello");
        }

        @Test
        @DisplayName("ambos literais vazios resultam em string vazia")
        void bothEmptyLiterals() {
            assertThat(AssignmentExpression.compile("x = \"\" || \"\";")
                    .compute().get("x"))
                    .isEqualTo("");
        }

        @Test
        @DisplayName("escape de aspas dentro de literal é preservado")
        void escapedQuoteInLiteral() {
            assertThat(AssignmentExpression.compile("x = \"diz: \\\"oi\\\"\" || \"!\";")
                    .compute().get("x"))
                    .isEqualTo("diz: \"oi\"!");
        }

        @ParameterizedTest(name = "\"{0}\" || \"{1}\" = \"{2}\"")
        @CsvSource({
            "abc,def,abcdef",
            "'foo',' bar','foo bar'",
            "' ','',' '",
            "'Java',' 21','Java 21'"
        })
        @DisplayName("pares de literais — comportamento uniforme")
        void literalPairs(String left, String right, String expected) {
            String expr = "x = \"" + left + "\" || \"" + right + "\";";
            assertThat(AssignmentExpression.compile(expr).compute().get("x"))
                    .isEqualTo(expected);
        }
    }

    // -------------------------------------------------------------------------
    // Referências sem type-hint (a1 = b1 || "teste" / a3 = "b3" || teste)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Referências sem type-hint explícito")
    class UntypedReferences {

        @Test
        @DisplayName("a1 = b1 || \"teste\" — variável untyped à esquerda")
        void untypedReferenceOnLeft() {
            // b1 sem <text>: tipo desconhecido em compile-time, resolvido em runtime
            assertThat(AssignmentExpression.compile("a1 = b1 || \"teste\";")
                    .compute(Map.of("b1", "valor"))
                    .get("a1"))
                    .isEqualTo("valorteste");
        }

        @Test
        @DisplayName("a3 = \"b3\" || teste — variável untyped à direita")
        void untypedReferenceOnRight() {
            assertThat(AssignmentExpression.compile("a3 = \"b3\" || teste;")
                    .compute(Map.of("teste", "valor"))
                    .get("a3"))
                    .isEqualTo("b3valor");
        }

        @Test
        @DisplayName("a2 = \"b2\" || \"teste\" — dois literais com atribuição simples")
        void literalAssignment() {
            assertThat(AssignmentExpression.compile("a2 = \"b2\" || \"teste\";")
                    .compute().get("a2"))
                    .isEqualTo("b2teste");
        }

        @Test
        @DisplayName("variável untyped com valor vazio produz prefixo sozinho")
        void untypedReferenceWithEmptyValue() {
            assertThat(AssignmentExpression.compile("x = \"prefix_\" || sufixo;")
                    .compute(Map.of("sufixo", ""))
                    .get("x"))
                    .isEqualTo("prefix_");
        }
    }

    // -------------------------------------------------------------------------
    // Decisão funcional como operando (a4 = if(true, "a", "b") || "c")
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Decisão funcional como operando de ||")
    class FunctionalDecision {

        @Test
        @DisplayName("a4 = if(true, \"a\", \"b\") || \"c\" — ramo verdadeiro concatena")
        void functionalDecisionTrueBranch() {
            assertThat(AssignmentExpression.compile("a4 = if(true, \"a\", \"b\") || \"c\";")
                    .compute().get("a4"))
                    .isEqualTo("ac");
        }

        @Test
        @DisplayName("if(false, ...) || literal usa ramo falso")
        void functionalDecisionFalseBranch() {
            assertThat(AssignmentExpression.compile("a4 = if(false, \"a\", \"b\") || \"c\";")
                    .compute().get("a4"))
                    .isEqualTo("bc");
        }

        @Test
        @DisplayName("literal || if(...) — decisão funcional à direita")
        void functionalDecisionOnRight() {
            assertThat(AssignmentExpression.compile("x = \"c\" || if(true, \"a\", \"b\");")
                    .compute().get("x"))
                    .isEqualTo("ca");
        }

        @Test
        @DisplayName("decisão funcional com variável externa no ramo selecionado")
        void functionalDecisionWithExternalVariable() {
            assertThat(AssignmentExpression.compile("x = if(true, <text>prefix, \"nope\") || \"-sufixo\";")
                    .compute(Map.of("prefix", "valor"))
                    .get("x"))
                    .isEqualTo("valor-sufixo");
        }
    }

    // -------------------------------------------------------------------------
    // Chamada de função como operando (a5 = "c" || if(...) || teste())
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Chamada de função como operando de ||")
    class FunctionCallOperand {

        private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder()
                .registerStaticProvider(StringFunctions.class)
                .build();

        @Test
        @DisplayName("a5 = \"c\" || if(true, \"a\", \"b\") || teste() — cadeia com função")
        void chainWithFunctionCall() {
            // "c" || "a" (ramo true) || "!" (retorno de teste()) = "ca!"
            assertThat(AssignmentExpression.compile(
                    "a5 = \"c\" || if(true, \"a\", \"b\") || teste();", ENV)
                    .compute().get("a5"))
                    .isEqualTo("ca!");
        }

        @Test
        @DisplayName("função que recebe argumento produz string concatenada")
        void functionWithArgumentInChain() {
            assertThat(AssignmentExpression.compile(
                    "x = \"resultado: \" || prefixar(<text>nome);", ENV)
                    .compute(Map.of("nome", "Alice"))
                    .get("x"))
                    .isEqualTo("resultado: >>>Alice");
        }

        @Test
        @DisplayName("função no meio de uma cadeia de três operandos")
        void functionInMiddleOfChain() {
            // "inicio-" || "!" || "-fim" = "inicio-!-fim"
            assertThat(AssignmentExpression.compile(
                    "x = \"inicio-\" || teste() || \"-fim\";", ENV)
                    .compute().get("x"))
                    .isEqualTo("inicio-!-fim");
        }
    }

    // -------------------------------------------------------------------------
    // Bloco multi-atribuição com todas as expressões de referência
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Bloco multi-atribuição com expressões de referência")
    class MultiAssignmentBlock {

        private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder()
                .registerStaticProvider(StringFunctions.class)
                .build();

        @Test
        @DisplayName("todas as expressões de referência avaliadas em um único bloco")
        void allReferenceExpressionsInOneBlock() {
            var result = AssignmentExpression.compile("""
                    a1 = b1 || "teste";
                    a2 = "b2" || "teste";
                    a3 = "b3" || sufixo;
                    a4 = if(true, "a", "b") || "c";
                    a5 = "c" || if(true, "a", "b") || teste();
                    """, ENV)
                    .compute(Map.of("b1", "valor", "sufixo", "valor"));

            assertThat(result.get("a1")).isEqualTo("valorteste");
            assertThat(result.get("a2")).isEqualTo("b2teste");
            assertThat(result.get("a3")).isEqualTo("b3valor");
            assertThat(result.get("a4")).isEqualTo("ac");
            assertThat(result.get("a5")).isEqualTo("ca!");
        }

        @Test
        @DisplayName("variáveis internas de atribuição podem ser usadas como operandos de ||")
        void internalAssignmentUsedAsOperand() {
            var result = AssignmentExpression.compile("""
                    nome = "Alice";
                    saudacao = "Olá, " || nome || "!";
                    """)
                    .compute();

            assertThat(result.get("saudacao")).isEqualTo("Olá, Alice!");
        }
    }

    // -------------------------------------------------------------------------
    // Variáveis externas com type-hint
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Concatenação com variáveis tipadas")
    class Variables {

        @Test
        @DisplayName("duas variáveis tipadas concatenadas")
        void twoTypedVariablesShouldConcatenate() {
            assertThat(AssignmentExpression.compile("x = <text>first || \" \" || <text>last;")
                    .compute(Map.of("first", "John", "last", "Doe"))
                    .get("x"))
                    .isEqualTo("John Doe");
        }

        @Test
        @DisplayName("variável tipada concatenada com literal à direita")
        void typedVariablePlusLiteral() {
            assertThat(AssignmentExpression.compile("x = <text>name || \"!\";")
                    .compute(Map.of("name", "World"))
                    .get("x"))
                    .isEqualTo("World!");
        }
    }

    // -------------------------------------------------------------------------
    // Comparação lógica com resultado de concatenação
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Concatenação em comparação lógica")
    class Comparison {

        @Test
        @DisplayName("concat seguido de comparação retorna true quando igual")
        void concatComparisonTrue() {
            assertThat(LogicalExpression.compile("\"hello\" || \" world\" = \"hello world\"")
                    .compute())
                    .isTrue();
        }

        @Test
        @DisplayName("concat seguido de comparação retorna false quando diferente")
        void concatComparisonFalse() {
            assertThat(LogicalExpression.compile("\"a\" || \"b\" = \"x\"")
                    .compute())
                    .isFalse();
        }

        @Test
        @DisplayName("concat em ambos os lados da comparação")
        void concatOnBothSidesOfComparison() {
            assertThat(LogicalExpression.compile("\"ab\" || \"c\" = \"a\" || \"bc\"")
                    .compute())
                    .isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // Branches de decisão (if-then-else)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Concatenação em expressões condicionais if-then-else")
    class Conditionals {

        @Test
        @DisplayName("concat no ramo then de uma decisão")
        void concatInThenBranch() {
            assertThat(AssignmentExpression.compile(
                    "x = if true then \"hello\" || <text>suffix else \"bye\" endif;")
                    .compute(Map.of("suffix", " world"))
                    .get("x"))
                    .isEqualTo("hello world");
        }

        @Test
        @DisplayName("concat no ramo else de uma decisão")
        void concatInElseBranch() {
            assertThat(AssignmentExpression.compile(
                    "x = if false then \"a\" else \"bye\" || <text>suffix endif;")
                    .compute(Map.of("suffix", "!"))
                    .get("x"))
                    .isEqualTo("bye!");
        }

        @Test
        @DisplayName("concat envolve toda a decisão if-then-else")
        void concatSurroundsDecision() {
            // "prefix-" || (decision) || "-suffix"
            assertThat(AssignmentExpression.compile(
                    "x = \"[\" || if true then \"sim\" else \"nao\" endif || \"]\";")
                    .compute().get("x"))
                    .isEqualTo("[sim]");
        }
    }

    // -------------------------------------------------------------------------
    // Idempotência — mesma expressão compilada, múltiplas avaliações
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Idempotência de expressão compilada")
    class Idempotency {

        @Test
        @DisplayName("mesma expressão avaliada com inputs diferentes produz resultados corretos")
        void compiledExpressionEvaluatedMultipleTimes() {
            var expr = AssignmentExpression.compile("x = <text>a || \"-\" || <text>b;");

            assertThat(expr.compute(Map.of("a", "foo", "b", "bar")).get("x")).isEqualTo("foo-bar");
            assertThat(expr.compute(Map.of("a", "alpha", "b", "beta")).get("x")).isEqualTo("alpha-beta");
            assertThat(expr.compute(Map.of("a", "", "b", "only")).get("x")).isEqualTo("-only");
        }
    }

    // -------------------------------------------------------------------------
    // Erros de tipo
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Erros de tipo")
    class TypeError {

        @Test
        @DisplayName("número sem cast em operando de || resulta em erro de parsing")
        void numericOperandShouldFailParsing() {
            // A gramática restringe || a contexto de stringEntity; número literal não é aceito
            assertThatThrownBy(() -> AssignmentExpression.compile("x = 1 || \"b\";"))
                    .isInstanceOf(ParsingException.class);
        }

        @Test
        @DisplayName("booleano em operando de || resulta em erro de parsing")
        void booleanOperandShouldFailParsing() {
            // true é literal lógico; não pertence ao contexto de stringConcatExpression
            assertThatThrownBy(() -> LogicalExpression.compile("true || \"b\" = \"trueb\""))
                    .isInstanceOf(ParsingException.class);
        }
    }
}
