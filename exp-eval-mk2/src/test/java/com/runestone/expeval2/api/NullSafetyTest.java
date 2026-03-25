package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.internal.grammar.ParsingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Null-safety: literal null, operador ?? e navegação segura ?.")
class NullSafetyTest {

    // -------------------------------------------------------------------------
    // Fixtures para navegação de objetos
    // -------------------------------------------------------------------------

    record Endereco(String cidade) {}

    record Usuario(String nome, Endereco endereco) {}

    // -------------------------------------------------------------------------
    // Literal null
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Literal null")
    class LiteralNull {

        @Test
        @DisplayName("null em atribuição é inválido na gramática")
        void shouldRejectNullLiteralInAssignment() {
            assertThatThrownBy(() -> AssignmentExpression.compile("x = null;"))
                    .isInstanceOf(ParsingException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("null como argumento de função é permitido na gramática")
        void shouldParseNullAsArgument() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerStaticProvider(NullableFixture.class)
                    .build();

            String result = AssignmentExpression.compile("x = identity(null);", env)
                    .compute()
                    .get("x")
                    == null ? "foi_nulo" : "nao_nulo";

            assertThat(result).isEqualTo("foi_nulo");
        }
    }

    // -------------------------------------------------------------------------
    // Operador ?? (null coalescing)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Operador ?? (null coalescing)")
    class NullCoalescing {

        @Test
        @DisplayName("?? retorna lado direito quando variável é null")
        void shouldReturnFallbackWhenLeftIsNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("x", null);

            Map<String, Object> result = AssignmentExpression.compile("y = x ?? \"padrão\";")
                    .compute(vars);

            assertThat(result).containsEntry("y", "padrão");
        }

        @Test
        @DisplayName("?? retorna lado esquerdo quando variável tem valor")
        void shouldReturnLeftWhenLeftIsNotNull() {
            Map<String, Object> result = AssignmentExpression.compile("y = x ?? \"padrão\";")
                    .compute(Map.of("x", "presente"));

            assertThat(result).containsEntry("y", "presente");
        }

        @Test
        @DisplayName("literal null no lado esquerdo de ?? é inválido na gramática")
        void shouldRejectNullLiteralOnLeftOfCoalesce() {
            assertThatThrownBy(() -> AssignmentExpression.compile("y = null ?? \"fallback\";"))
                    .isInstanceOf(ParsingException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("?? com valor numérico retorna o número quando não é null")
        void shouldReturnNumericValueWhenNotNull() {
            Map<String, Object> result = AssignmentExpression.compile("y = x ?? 0;")
                    .compute(Map.of("x", new BigDecimal("42")));

            assertThat(result.get("y")).isEqualTo(new BigDecimal("42"));
        }

        @Test
        @DisplayName("?? com variável numérica nula retorna o fallback numérico")
        void shouldReturnNumericFallbackWhenNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("x", null);

            Map<String, Object> result = AssignmentExpression.compile("y = x ?? 0;")
                    .compute(vars);

            assertThat(result.get("y")).isEqualTo(new BigDecimal("0"));
        }
    }

    // -------------------------------------------------------------------------
    // Navegação segura ?.
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Navegação segura ?.")
    class SafeNavigation {

        @Test
        @DisplayName("?. retorna null quando raiz é null (sem type hint)")
        void shouldReturnNullWhenRootIsNullWithoutTypeHint() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("obj", null);

            Map<String, Object> result = AssignmentExpression.compile("y = obj?.nome;")
                    .compute(vars);

            assertThat(result).containsEntry("y", null);
        }

        @Test
        @DisplayName("?. navega normalmente quando objeto não é null (sem type hint)")
        void shouldNavigateWhenObjectIsNotNullWithoutTypeHint() {
            var usuario = new Usuario("Alice", null);

            Map<String, Object> result = AssignmentExpression.compile("y = obj?.nome;")
                    .compute(Map.of("obj", usuario));

            assertThat(result).containsEntry("y", "Alice");
        }

        @Test
        @DisplayName("?. retorna null quando raiz é null (com type hint)")
        void shouldReturnNullWhenRootIsNullWithTypeHint() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerTypeHint(Usuario.class)
                    .registerExternalSymbol("usuario", new Usuario("default", null), true)
                    .build();
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("usuario", null);

            Map<String, Object> result = AssignmentExpression.compile("y = usuario?.nome;", env)
                    .compute(vars);

            assertThat(result).containsEntry("y", null);
        }

        @Test
        @DisplayName("?. navega normalmente quando objeto não é null (com type hint)")
        void shouldNavigateWhenObjectIsNotNullWithTypeHint() {
            var usuario = new Usuario("Bob", null);
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerTypeHint(Usuario.class)
                    .registerExternalSymbol("usuario", usuario, true)
                    .build();

            Map<String, Object> result = AssignmentExpression.compile("y = usuario?.nome;", env)
                    .compute(Map.of("usuario", usuario));

            assertThat(result).containsEntry("y", "Bob");
        }

        @Test
        @DisplayName("chain seguro a?.b?.c retorna null quando a é null")
        void shouldReturnNullForSafeChainWhenFirstHopIsNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("usuario", null);

            Map<String, Object> result = AssignmentExpression.compile("y = usuario?.endereco?.cidade;")
                    .compute(vars);

            assertThat(result).containsEntry("y", null);
        }

        @Test
        @DisplayName("chain seguro a?.b?.c retorna null quando b é null")
        void shouldReturnNullForSafeChainWhenIntermediateHopIsNull() {
            var usuario = new Usuario("Carol", null); // endereco == null
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("usuario", usuario);

            Map<String, Object> result = AssignmentExpression.compile("y = usuario?.endereco?.cidade;")
                    .compute(vars);

            assertThat(result).containsEntry("y", null);
        }

        @Test
        @DisplayName("navegação não segura lança exceção quando intermediário é null")
        void shouldThrowWhenUnsafeChainEncountersNull() {
            var usuario = new Usuario("Dave", null);
            AssignmentExpression expr = AssignmentExpression.compile("y = usuario.endereco.cidade;");

            assertThatThrownBy(() -> expr.compute(Map.of("usuario", usuario)))
                    .isInstanceOf(ExpressionEvaluationException.class)
                    .hasMessageContaining("null");
        }
    }

    // -------------------------------------------------------------------------
    // Combinação: ?. com ??
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Combinação: ?. com ??")
    class SafeNavigationWithCoalescing {

        @Test
        @DisplayName("obj?.campo ?? fallback retorna fallback quando obj é null")
        void shouldReturnFallbackWhenObjectIsNullInCombination() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("obj", null);

            Map<String, Object> result = AssignmentExpression.compile("y = obj?.nome ?? \"anônimo\";")
                    .compute(vars);

            assertThat(result).containsEntry("y", "anônimo");
        }

        @Test
        @DisplayName("obj?.campo ?? fallback retorna o campo quando obj não é null")
        void shouldReturnFieldWhenObjectIsNotNullInCombination() {
            var usuario = new Usuario("Eve", null);

            Map<String, Object> result = AssignmentExpression.compile("y = obj?.nome ?? \"anônimo\";")
                    .compute(Map.of("obj", usuario));

            assertThat(result).containsEntry("y", "Eve");
        }

        @Test
        @DisplayName("chain seguro com ?? retorna fallback quando intermediário é null")
        void shouldReturnFallbackForNullIntermediateInChain() {
            var usuario = new Usuario("Frank", null); // endereco == null
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("obj", usuario);

            Map<String, Object> result = AssignmentExpression.compile("y = obj?.endereco?.cidade ?? \"desconhecido\";")
                    .compute(vars);

            assertThat(result).containsEntry("y", "desconhecido");
        }
    }

    // -------------------------------------------------------------------------
    // ?? por tipo: boolean, date, time, datetime
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Operador ?? por tipo")
    class CoalesceByType {

        @Test
        @DisplayName("boolean: ?? retorna fallback quando variável é null")
        void shouldReturnBooleanFallbackWhenNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("b", null);

            Map<String, Object> result = AssignmentExpression.compile("y = b ?? true;").compute(vars);

            assertThat(result).containsEntry("y", Boolean.TRUE);
        }

        @Test
        @DisplayName("boolean: ?? retorna o valor quando variável é false")
        void shouldReturnFalseNotTriggerCoalesce() {
            Map<String, Object> result = AssignmentExpression.compile("y = b ?? true;")
                    .compute(Map.of("b", Boolean.FALSE));

            assertThat(result).containsEntry("y", Boolean.FALSE);
        }

        @Test
        @DisplayName("date: ?? retorna fallback quando variável é null")
        void shouldReturnDateFallbackWhenNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("d", null);

            Map<String, Object> result = AssignmentExpression.compile("y = d ?? 2024-01-01;").compute(vars);

            assertThat(result).containsEntry("y", LocalDate.of(2024, 1, 1));
        }

        @Test
        @DisplayName("date: ?? retorna o valor quando variável tem data")
        void shouldReturnDateWhenNotNull() {
            LocalDate today = LocalDate.of(2025, 6, 15);

            Map<String, Object> result = AssignmentExpression.compile("y = d ?? 2024-01-01;")
                    .compute(Map.of("d", today));

            assertThat(result).containsEntry("y", today);
        }

        @Test
        @DisplayName("time: ?? retorna fallback quando variável é null")
        void shouldReturnTimeFallbackWhenNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("t", null);

            Map<String, Object> result = AssignmentExpression.compile("y = t ?? 10:00;").compute(vars);

            assertThat(result).containsEntry("y", LocalTime.of(10, 0));
        }

        @Test
        @DisplayName("datetime: ?? retorna fallback quando variável é null")
        void shouldReturnDatetimeFallbackWhenNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("dt", null);

            Map<String, Object> result = AssignmentExpression.compile("y = dt ?? 2024-01-01T10:00;").compute(vars);

            assertThat(result).containsEntry("y", LocalDateTime.of(2024, 1, 1, 10, 0));
        }
    }

    // -------------------------------------------------------------------------
    // Casos de fronteira do ??
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Casos de fronteira do operador ??")
    class CoalesceBoundary {

        @Test
        @DisplayName("zero não é null: ?? não ativa com valor 0")
        void shouldNotCoalesceForZero() {
            Map<String, Object> result = AssignmentExpression.compile("y = x ?? 99;")
                    .compute(Map.of("x", BigDecimal.ZERO));

            assertThat(result.get("y")).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("string vazia não é null: ?? não ativa com \"\"")
        void shouldNotCoalesceForEmptyString() {
            Map<String, Object> result = AssignmentExpression.compile("y = x ?? \"padrão\";")
                    .compute(Map.of("x", ""));

            assertThat(result).containsEntry("y", "");
        }

        @Test
        @DisplayName("ambos os lados null em tempo de execução resulta em null")
        void shouldReturnNullWhenBothSidesAreNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("a", null);
            vars.put("b", null);

            Map<String, Object> result = AssignmentExpression.compile("y = a ?? b;").compute(vars);

            assertThat(result).containsEntry("y", null);
        }

        @Test
        @DisplayName("encadeamento a ?? b ?? c: retorna c quando a e b são null")
        void shouldReturnThirdInChainWhenFirstTwoAreNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("a", null);
            vars.put("b", null);

            Map<String, Object> result = AssignmentExpression.compile("y = a ?? b ?? \"último\";")
                    .compute(vars);

            assertThat(result).containsEntry("y", "último");
        }

        @Test
        @DisplayName("encadeamento a ?? b ?? c: retorna b quando a é null mas b tem valor")
        void shouldReturnSecondInChainWhenFirstIsNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("a", null);
            vars.put("b", "segundo");

            Map<String, Object> result = AssignmentExpression.compile("y = a ?? b ?? \"último\";")
                    .compute(vars);

            assertThat(result).containsEntry("y", "segundo");
        }
    }

    // -------------------------------------------------------------------------
    // Short-circuit: lado direito não avaliado se esquerdo não é null
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Short-circuit do operador ??")
    class CoalesceShortCircuit {

        @Test
        @DisplayName("lado direito não é avaliado quando esquerdo tem valor")
        void shouldNotEvaluateRightWhenLeftIsNotNull() {
            AtomicInteger callCount = new AtomicInteger();
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerStaticProvider(CountingFixture.class)
                    .build();
            CountingFixture.reset(callCount);

            AssignmentExpression.compile("y = x ?? counted();", env)
                    .compute(Map.of("x", "presente"));

            assertThat(callCount).hasValue(0);
        }

        @Test
        @DisplayName("lado direito é avaliado quando esquerdo é null")
        void shouldEvaluateRightWhenLeftIsNull() {
            AtomicInteger callCount = new AtomicInteger();
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerStaticProvider(CountingFixture.class)
                    .build();
            CountingFixture.reset(callCount);
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("x", null);

            AssignmentExpression.compile("y = x ?? counted();", env).compute(vars);

            assertThat(callCount).hasValue(1);
        }
    }

    // -------------------------------------------------------------------------
    // ?? em MathExpression (resultado direto BigDecimal)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("?? em MathExpression")
    class CoalesceInMathExpression {

        @Test
        @DisplayName("retorna fallback numérico quando variável é null")
        void shouldReturnFallbackWhenNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("a", null);

            BigDecimal result = MathExpression.compile("a ?? 3").compute(vars);

            assertThat(result).isEqualByComparingTo("3");
        }

        @Test
        @DisplayName("retorna o valor da variável quando não é null")
        void shouldReturnVariableWhenNotNull() {
            BigDecimal result = MathExpression.compile("a ?? 3")
                    .compute(Map.of("a", new BigDecimal("7")));

            assertThat(result).isEqualByComparingTo("7");
        }

        @Test
        @DisplayName("zero não é null: ?? não ativa com valor 0")
        void shouldNotCoalesceForZero() {
            BigDecimal result = MathExpression.compile("a ?? 3")
                    .compute(Map.of("a", BigDecimal.ZERO));

            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("?? pode ser usada em subexpressão aritmética")
        void shouldAllowCoalesceInsideArithmeticExpression() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("a", null);

            BigDecimal result = MathExpression.compile("(a ?? 10) * 2").compute(vars);

            assertThat(result).isEqualByComparingTo("20");
        }
    }

    // -------------------------------------------------------------------------
    // ?? em LogicalExpression (resultado direto boolean)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("?? em LogicalExpression")
    class CoalesceInLogicalExpression {

        @Test
        @DisplayName("retorna fallback true quando variável é null")
        void shouldReturnTrueFallbackWhenNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("b", null);

            boolean result = LogicalExpression.compile("b ?? true").compute(vars);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("retorna false quando variável é false (false não é null)")
        void shouldReturnFalseWhenVariableIsFalse() {
            boolean result = LogicalExpression.compile("b ?? true")
                    .compute(Map.of("b", Boolean.FALSE));

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("retorna true quando variável é true")
        void shouldReturnTrueWhenVariableIsTrue() {
            boolean result = LogicalExpression.compile("b ?? false")
                    .compute(Map.of("b", Boolean.TRUE));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("?? pode ser operando de expressão lógica composta")
        void shouldAllowCoalesceInsideLogicalExpression() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("flag", null);

            boolean result = LogicalExpression.compile("(flag ?? false) and true").compute(vars);

            assertThat(result).isFalse();
        }
    }

    // -------------------------------------------------------------------------
    // Literais no lado esquerdo de ?? são inválidos na gramática
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Rejeição gramatical: literal no lado esquerdo de ??")
    class CoalesceInvalidLeftSide {

        @Test
        @DisplayName("string literal no lado esquerdo de ?? é inválido")
        void shouldRejectStringLiteralOnLeft() {
            assertThatThrownBy(() -> AssignmentExpression.compile("y = \"valor\" ?? \"fallback\";"))
                    .isInstanceOf(ParsingException.class);
        }

        @Test
        @DisplayName("número literal no lado esquerdo de ?? é inválido")
        void shouldRejectNumericLiteralOnLeft() {
            assertThatThrownBy(() -> AssignmentExpression.compile("y = 42 ?? 0;"))
                    .isInstanceOf(ParsingException.class);
        }

        @Test
        @DisplayName("booleano literal no lado esquerdo de ?? é inválido")
        void shouldRejectBooleanLiteralOnLeft() {
            assertThatThrownBy(() -> AssignmentExpression.compile("y = true ?? false;"))
                    .isInstanceOf(ParsingException.class);
        }
    }

    // -------------------------------------------------------------------------
    // Fixtures auxiliares
    // -------------------------------------------------------------------------

    public static final class NullableFixture {
        private NullableFixture() {}

        public static Object identity(Object value) {
            return value;
        }
    }

    public static final class CountingFixture {
        private static AtomicInteger counter;

        private CountingFixture() {}

        static void reset(AtomicInteger newCounter) {
            counter = newCounter;
        }

        public static String counted() {
            counter.incrementAndGet();
            return "contado";
        }
    }
}
