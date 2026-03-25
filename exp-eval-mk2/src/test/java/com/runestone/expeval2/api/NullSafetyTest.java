package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
        @DisplayName("null em atribuição resulta em variável com valor null")
        void shouldAssignNullLiteralToVariable() {
            Map<String, Object> result = AssignmentExpression.compile("x = null;").compute();

            assertThat(result).containsEntry("x", null);
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
        @DisplayName("?? com literal null no lado esquerdo retorna fallback")
        void shouldReturnFallbackForNullLiteralOnLeft() {
            Map<String, Object> result = AssignmentExpression.compile("y = null ?? \"fallback\";")
                    .compute();

            assertThat(result).containsEntry("y", "fallback");
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
    // Fixture auxiliar
    // -------------------------------------------------------------------------

    public static final class NullableFixture {
        private NullableFixture() {}

        public static Object identity(Object value) {
            return value;
        }
    }
}
