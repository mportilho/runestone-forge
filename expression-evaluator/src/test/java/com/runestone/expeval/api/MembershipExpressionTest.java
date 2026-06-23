package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Operadores 'in' e 'not in' de pertencimento a vetor")
class MembershipExpressionTest {

    // -------------------------------------------------------------------------
    // Sujeito literal — in
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Sujeito literal — in")
    class LiteralSubjectIn {

        @Test
        @DisplayName("número encontrado no vetor retorna true")
        void numberFoundInVectorReturnsTrue() {
            assertThat(LogicalExpression.compile("1 in [1, 2, 3]").compute()).isTrue();
        }

        @Test
        @DisplayName("número não encontrado no vetor retorna false")
        void numberNotFoundInVectorReturnsFalse() {
            assertThat(LogicalExpression.compile("4 in [1, 2, 3]").compute()).isFalse();
        }

        @Test
        @DisplayName("string encontrada no vetor retorna true")
        void stringFoundInVectorReturnsTrue() {
            assertThat(LogicalExpression.compile("\"a\" in [\"a\", \"b\", \"c\"]").compute()).isTrue();
        }

        @Test
        @DisplayName("string não encontrada no vetor retorna false")
        void stringNotFoundInVectorReturnsFalse() {
            assertThat(LogicalExpression.compile("\"z\" in [\"a\", \"b\", \"c\"]").compute()).isFalse();
        }

        @Test
        @DisplayName("variável null não encontrada em vetor não-nulo retorna false")
        void nullVariableNotFoundInNonNullVectorReturnsFalse() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("v", BigDecimal.ZERO, true)
                    .build();
            Map<String, Object> bindings = new HashMap<>();
            bindings.put("v", null);
            assertThat(LogicalExpression.compile("v in [1, 2, 3]", env)
                    .compute(bindings)).isFalse();
        }

        @ParameterizedTest(name = "[{index}] {0} in [1,2,3] -> {1}")
        @DisplayName("vários valores numéricos contra vetor literal")
        @CsvSource({
            "1, true",
            "2, true",
            "3, true",
            "0, false",
            "4, false",
        })
        void numericMembership(int value, boolean expected) {
            String expr = value + " in [1, 2, 3]";
            assertThat(LogicalExpression.compile(expr).compute()).isEqualTo(expected);
        }
    }

    // -------------------------------------------------------------------------
    // Sujeito literal — not in
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Sujeito literal — not in")
    class LiteralSubjectNotIn {

        @Test
        @DisplayName("número ausente do vetor retorna true")
        void numberAbsentFromVectorReturnsTrue() {
            assertThat(LogicalExpression.compile("4 not in [1, 2, 3]").compute()).isTrue();
        }

        @Test
        @DisplayName("número presente no vetor retorna false")
        void numberPresentInVectorReturnsFalse() {
            assertThat(LogicalExpression.compile("1 not in [1, 2, 3]").compute()).isFalse();
        }

        @Test
        @DisplayName("string ausente do vetor retorna true")
        void stringAbsentFromVectorReturnsTrue() {
            assertThat(LogicalExpression.compile("\"z\" not in [\"a\", \"b\"]").compute()).isTrue();
        }

        @Test
        @DisplayName("in e not in são complementares para o mesmo valor")
        void inAndNotInAreComplementary() {
            boolean inResult = LogicalExpression.compile("1 in [1, 2, 3]").compute();
            boolean notInResult = LogicalExpression.compile("1 not in [1, 2, 3]").compute();
            assertThat(inResult).isNotEqualTo(notInResult);
        }
    }

    // -------------------------------------------------------------------------
    // Sujeito variável
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Sujeito variável")
    class VariableSubject {

        @Test
        @DisplayName("variável numérica encontrada no vetor literal retorna true")
        void numericVariableFoundInLiteralVector() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("x", BigDecimal.ZERO, true)
                    .build();
            assertThat(LogicalExpression.compile("x in [1, 2, 3]", env)
                    .compute(Map.of("x", BigDecimal.valueOf(2)))).isTrue();
        }

        @Test
        @DisplayName("variável numérica ausente do vetor retorna false")
        void numericVariableAbsentFromVector() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("x", BigDecimal.ZERO, true)
                    .build();
            assertThat(LogicalExpression.compile("x in [1, 2, 3]", env)
                    .compute(Map.of("x", BigDecimal.valueOf(9)))).isFalse();
        }

        @Test
        @DisplayName("variável de string encontrada no vetor literal retorna true")
        void stringVariableFoundInLiteralVector() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("status", "", true)
                    .build();
            assertThat(LogicalExpression.compile("<text>status in [\"ativo\", \"pendente\"]", env)
                    .compute(Map.of("status", "ativo"))).isTrue();
        }

        @Test
        @DisplayName("variável not in com vetor literal retorna true quando ausente")
        void variableNotInLiteralVector() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("x", BigDecimal.ZERO, true)
                    .build();
            assertThat(LogicalExpression.compile("x not in [1, 2, 3]", env)
                    .compute(Map.of("x", BigDecimal.valueOf(9)))).isTrue();
        }

        @Test
        @DisplayName("variável com vetor como símbolo externo")
        void variableCheckedAgainstExternalVector() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("valor", BigDecimal.ZERO, true)
                    .registerExternalSymbol("lista", List.of(), true)
                    .build();
            assertThat(LogicalExpression.compile("valor in <vector>lista", env)
                    .compute(Map.of("valor", BigDecimal.valueOf(2),
                                   "lista", List.of(BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3)))))
                    .isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // Null dentro do vetor
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Null dentro do vetor literal")
    class NullInVectorLiteral {

        @Test
        @DisplayName("número presente em vetor que contém null retorna true")
        void numberFoundInVectorContainingNullReturnsTrue() {
            assertThat(LogicalExpression.compile("1 in [1, null, 3]").compute()).isTrue();
        }

        @Test
        @DisplayName("número ausente de vetor que contém null retorna false")
        void numberAbsentFromVectorContainingNullReturnsFalse() {
            assertThat(LogicalExpression.compile("4 in [1, null, 3]").compute()).isFalse();
        }

        @Test
        @DisplayName("not in com número ausente em vetor que contém null retorna true")
        void numberNotInVectorContainingNullReturnsTrue() {
            assertThat(LogicalExpression.compile("4 not in [1, null, 3]").compute()).isTrue();
        }

        @Test
        @DisplayName("not in com número presente em vetor que contém null retorna false")
        void numberPresentNotInVectorContainingNullReturnsFalse() {
            assertThat(LogicalExpression.compile("1 not in [1, null, 3]").compute()).isFalse();
        }

        @Test
        @DisplayName("variável null encontrada em vetor com elemento null retorna true")
        void nullVariableFoundInVectorWithNullElementReturnsTrue() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("v", BigDecimal.ZERO, true)
                    .build();
            Map<String, Object> bindings = new HashMap<>();
            bindings.put("v", null);
            assertThat(LogicalExpression.compile("v in [1, null, 3]", env)
                    .compute(bindings)).isTrue();
        }

        @Test
        @DisplayName("variável não-null encontrada em vetor que também contém null retorna true")
        void nonNullVariableFoundInVectorAlsoContainingNullReturnsTrue() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("x", BigDecimal.ZERO, true)
                    .build();
            assertThat(LogicalExpression.compile("x in [null, 2, 3]", env)
                    .compute(Map.of("x", BigDecimal.valueOf(2)))).isTrue();
        }

        @Test
        @DisplayName("número em vetor com apenas null retorna false")
        void numberInVectorOfOnlyNullReturnsFalse() {
            assertThat(LogicalExpression.compile("1 in [null]").compute()).isFalse();
        }
    }

    // -------------------------------------------------------------------------
    // Expressões compostas
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Expressões compostas")
    class ComposedExpressions {

        @Test
        @DisplayName("in combinado com and retorna true quando ambos verdadeiros")
        void inCombinedWithAndBothTrue() {
            assertThat(LogicalExpression.compile("(1 in [1, 2]) and (3 in [3, 4])").compute()).isTrue();
        }

        @Test
        @DisplayName("in combinado com and retorna false quando um é falso")
        void inCombinedWithAndOneFalse() {
            assertThat(LogicalExpression.compile("(1 in [1, 2]) and (9 in [3, 4])").compute()).isFalse();
        }

        @Test
        @DisplayName("in em expressão condicional if-then-else")
        void inInsideConditional() {
            assertThat(MathExpression.compile("if 1 in [1, 2, 3] then 10 else 0 endif").compute())
                    .isEqualByComparingTo(BigDecimal.TEN);
        }

        @Test
        @DisplayName("not in em expressão condicional if-then-else")
        void notInInsideConditional() {
            assertThat(MathExpression.compile("if 9 not in [1, 2, 3] then 10 else 0 endif").compute())
                    .isEqualByComparingTo(BigDecimal.TEN);
        }

        @Test
        @DisplayName("resultado de in usado como operando de or")
        void inResultUsedInOr() {
            assertThat(LogicalExpression.compile("(5 in [1, 2]) or (3 in [3, 4])").compute()).isTrue();
        }
    }
}
