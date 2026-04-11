package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval.internal.grammar.ParsingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Operadores =~ e !~ de correspondência com regex")
class StringRegexTest {

    // -------------------------------------------------------------------------
    // Sujeito literal
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Sujeito literal")
    class LiteralSubject {

        @Test
        @DisplayName("literal corresponde ao padrão parcial")
        void literalMatchesPartialPattern() {
            assertThat(LogicalExpression.compile("\"abc123\" =~ \"\\\\d+\"").compute())
                    .isTrue();
        }

        @Test
        @DisplayName("literal não corresponde ao padrão")
        void literalDoesNotMatchPattern() {
            assertThat(LogicalExpression.compile("\"abc\" =~ \"\\\\d+\"").compute())
                    .isFalse();
        }

        @Test
        @DisplayName("correspondência com início de string usando âncora ^")
        void anchoredStartMatch() {
            assertThat(LogicalExpression.compile("\"abc\" =~ \"^abc\"").compute())
                    .isTrue();
        }

        @Test
        @DisplayName("não corresponde quando âncora ^ não satisfeita")
        void anchoredStartNoMatch() {
            assertThat(LogicalExpression.compile("\"xabc\" =~ \"^abc\"").compute())
                    .isFalse();
        }

        @Test
        @DisplayName("padrão vazio sempre corresponde")
        void emptyPatternAlwaysMatches() {
            assertThat(LogicalExpression.compile("\"qualquer\" =~ \"\"").compute())
                    .isTrue();
        }

        @Test
        @DisplayName("sujeito vazio não corresponde a padrão não-vazio")
        void emptySubjectDoesNotMatchNonEmptyPattern() {
            assertThat(LogicalExpression.compile("\"\" =~ \"\\\\d+\"").compute())
                    .isFalse();
        }

        @Test
        @DisplayName("sujeito vazio corresponde a padrão vazio")
        void emptySubjectMatchesEmptyPattern() {
            assertThat(LogicalExpression.compile("\"\" =~ \"\"").compute())
                    .isTrue();
        }

        @ParameterizedTest(name = "[{index}] \"{0}\" =~ \"{1}\" -> {2}")
        @DisplayName("correspondência parcial por find() — padrão não precisa cobrir toda a string")
        // Padrões sem backslash para evitar camadas de escape no CsvSource.
        // Verificação central: find() bate em qualquer posição, não apenas na string inteira.
        @CsvSource({
            "abc123, [0-9]+,    true",
            "abc,    [0-9]+,    false",
            "123abc, [0-9]+,    true",
            "hello,  ^hello$,   true",
            "hello!, ^hello$,   false",
        })
        void partialMatchSemantics(String subject, String pattern, boolean expected) {
            String expr = "\"" + subject + "\" =~ \"" + pattern + "\"";
            assertThat(LogicalExpression.compile(expr).compute()).isEqualTo(expected);
        }
    }

    // -------------------------------------------------------------------------
    // Sujeito variável, padrão literal
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Sujeito como variável externa")
    class VariableSubject {

        @Test
        @DisplayName("variável de string corresponde ao padrão literal")
        void variableSubjectMatchesLiteralPattern() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("texto", "", true)
                    .build();

            assertThat(LogicalExpression.compile("<text>texto =~ \"[0-9]+\"", env)
                    .compute(Map.of("texto", "abc123")))
                    .isTrue();
        }

        @Test
        @DisplayName("variável de string sem type-hint detectada como STRING por coerção")
        void untypedVariableSubjectMatches() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("texto", "qualquer", true)
                    .build();

            assertThat(LogicalExpression.compile("<text>texto =~ \"qual\"", env)
                    .compute(Map.of("texto", "qualquer")))
                    .isTrue();
        }

        @Test
        @DisplayName("variável não corresponde ao padrão")
        void variableSubjectDoesNotMatch() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("texto", "", true)
                    .build();

            assertThat(LogicalExpression.compile("<text>texto =~ \"^\\\\d+$\"", env)
                    .compute(Map.of("texto", "abc")))
                    .isFalse();
        }
    }

    // -------------------------------------------------------------------------
    // Negação (!~)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Operador de negação !~")
    class Negation {

        @Test
        @DisplayName("!~ retorna true quando padrão não corresponde")
        void notMatchReturnsTrueWhenNoMatch() {
            assertThat(LogicalExpression.compile("\"abc\" !~ \"\\\\d+\"").compute())
                    .isTrue();
        }

        @Test
        @DisplayName("!~ retorna false quando padrão corresponde")
        void notMatchReturnsFalseWhenMatch() {
            assertThat(LogicalExpression.compile("\"abc123\" !~ \"\\\\d+\"").compute())
                    .isFalse();
        }

        @Test
        @DisplayName("=~ e !~ são complementares para o mesmo input")
        void matchAndNotMatchAreComplementary() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("v", "", true)
                    .build();
            var matchExpr = LogicalExpression.compile("<text>v =~ \"^[A-Z]\"", env);
            var noMatchExpr = LogicalExpression.compile("<text>v !~ \"^[A-Z]\"", env);

            assertThat(matchExpr.compute(Map.of("v", "Hello"))).isTrue();
            assertThat(noMatchExpr.compute(Map.of("v", "Hello"))).isFalse();

            assertThat(matchExpr.compute(Map.of("v", "hello"))).isFalse();
            assertThat(noMatchExpr.compute(Map.of("v", "hello"))).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // Idempotência — Pattern compilado uma vez, reutilizado em avaliações múltiplas
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Idempotência e reuso do Pattern compilado")
    class Idempotency {

        @Test
        @DisplayName("mesma expressão compilada avaliada múltiplas vezes retorna resultados corretos")
        void compiledExpressionEvaluatedMultipleTimes() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("s", "", true)
                    .build();

            var expr = LogicalExpression.compile("<text>s =~ \"^\\\\d{3}-\\\\d{4}$\"", env);

            assertThat(expr.compute(Map.of("s", "123-4567"))).isTrue();
            assertThat(expr.compute(Map.of("s", "abc-defg"))).isFalse();
            assertThat(expr.compute(Map.of("s", "999-0000"))).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // Uso em expressões compostas
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Uso em expressões lógicas compostas")
    class ComposedExpressions {

        @Test
        @DisplayName("=~ combinado com and")
        void regexCombinedWithAnd() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("a", "", true)
                    .registerExternalSymbol("b", "", true)
                    .build();

            assertThat(LogicalExpression.compile("<text>a =~ \"\\\\d\" and <text>b =~ \"[a-z]\"", env)
                    .compute(Map.of("a", "x1y", "b", "abc")))
                    .isTrue();
        }

        @Test
        @DisplayName("sujeito como expressão de concatenação")
        void concatenatedSubjectIsSupported() {
            // O lado esquerdo de =~ é stringConcatExpression, não apenas um literal.
            // Valida que a gramática aceita e avalia corretamente uma concatenação como sujeito.
            assertThat(LogicalExpression.compile("\"hello\" || \" world\" =~ \"world\"").compute())
                    .isTrue();
        }

        @Test
        @DisplayName("=~ em condicional if-then-else")
        void regexInConditional() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("cep", "", true)
                    .build();

            assertThat(AssignmentExpression.compile("ok = if <text>cep =~ \"^\\\\d{5}-\\\\d{3}$\" then \"valido\" else \"invalido\" endif;", env)
                    .compute(Map.of("cep", "12345-678"))
                    .get("ok"))
                    .isEqualTo("valido");
        }
    }

    // -------------------------------------------------------------------------
    // Erros
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Erros")
    class Errors {

        @Test
        @DisplayName("regex inválido lança PatternSyntaxException na compilação, não na avaliação")
        void invalidRegexThrowsAtCompileTime() {
            assertThatThrownBy(() -> LogicalExpression.compile("\"texto\" =~ \"[abc\""))
                    .isInstanceOf(PatternSyntaxException.class);
        }

        @Test
        @DisplayName("operando numérico em =~ resulta em erro de parsing")
        void numericSubjectFailsParsing() {
            assertThatThrownBy(() -> LogicalExpression.compile("42 =~ \"\\\\d+\""))
                    .isInstanceOf(ParsingException.class);
        }

        @Test
        @DisplayName("variável como padrão é proibida pela gramática")
        void variableAsPatternFailsParsing() {
            // A gramática restringe o lado direito de =~ ao token STRING.
            // Qualquer referência a variável deve falhar no parse.
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("p", "", true)
                    .build();
            assertThatThrownBy(() -> LogicalExpression.compile("<text>s =~ <text>p", env))
                    .isInstanceOf(ParsingException.class);
        }

        @Test
        @DisplayName("variável numérica sem type-hint como sujeito produz erro semântico")
        void numericExternalSymbolAsSubjectFailsSemantically() {
            // n é registrada como BigDecimal; sem type-hint, a gramática aceita n como
            // stringEntity, mas o SemanticResolver rejeita NUMBER como sujeito de =~.
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("n", BigDecimal.ZERO, true)
                    .build();
            assertThatThrownBy(() -> LogicalExpression.compile("n =~ \"\\\\d+\"", env))
                    .isInstanceOf(ExpressionCompilationException.class);
        }

        @Test
        @DisplayName("sujeito nulo em runtime lança IllegalStateException")
        void nullSubjectAtRuntimeThrows() {
            // asString(null) lança IllegalStateException — comportamento consistente
            // com todos os outros operadores do sistema que rejeitam null.
            // Map.of() não aceita valores null; usamos HashMap para injetar null explicitamente.
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("s", "", true)
                    .build();
            var expr = LogicalExpression.compile("s =~ \".*\"", env);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("s", null);
            assertThatThrownBy(() -> expr.compute(inputs))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
