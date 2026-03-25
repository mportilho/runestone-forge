package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that property-chain navigation handles circular object graphs correctly.
 *
 * <p>Because the expression chain is fixed at compile time, navigating through
 * a cyclic object graph always terminates — the number of hops is bounded by the
 * expression length, not the graph structure. Tests cover both the typed path
 * (type hints registered, {@code MethodHandle} resolved at compile time) and the
 * reflective path (no type hints, {@link com.runestone.expeval2.internal.runtime.ReflectiveAccessCache}
 * used at runtime).
 *
 * <p>Scenarios:
 * <ul>
 *   <li>Self-referential object (A.proximo = A)</li>
 *   <li>Mutually referential objects (A.proximo = B, B.proximo = A)</li>
 *   <li>Method returning {@code this}</li>
 *   <li>Numeric property in arithmetic through circular reference</li>
 * </ul>
 */
@DisplayName("Object navigation with circular object references")
class ObjectNavigationCircularReferenceTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    static class No {
        private final String valor;
        private No proximo;

        No(String valor) {
            this.valor = valor;
        }

        void setProximo(No proximo) {
            this.proximo = proximo;
        }

        public String getValor()  { return valor; }
        public No     getProximo() { return proximo; }
        public No     self()      { return this; }
    }

    static class Conta {
        private final BigDecimal saldo;
        private Conta vinculada;

        Conta(BigDecimal saldo) {
            this.saldo = saldo;
        }

        void setVinculada(Conta vinculada) {
            this.vinculada = vinculada;
        }

        public BigDecimal getSaldo()    { return saldo; }
        public Conta      getVinculada() { return vinculada; }
    }

    // -------------------------------------------------------------------------
    // Typed path (type hints registered)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Typed path — precomputed MethodHandle")
    class TypedPath {

        @Test
        @DisplayName("self-referential property — one hop returns the root value")
        void shouldNavigateOneSelfHop() {
            var no = new No("raiz");
            no.setProximo(no);

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerTypeHint(No.class)
                    .registerExternalSymbol("no", no, true)
                    .build();

            boolean result = LogicalExpression.compile("no.proximo.valor = \"raiz\"", env)
                    .compute(Map.of("no", no));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("self-referential property — two hops still resolves the same value")
        void shouldNavigateTwoSelfHops() {
            var no = new No("raiz");
            no.setProximo(no);

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerTypeHint(No.class)
                    .registerExternalSymbol("no", no, true)
                    .build();

            boolean result = LogicalExpression.compile("no.proximo.proximo.valor = \"raiz\"", env)
                    .compute(Map.of("no", no));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("mutually referential objects — navigates from A to B correctly")
        void shouldNavigateFromAToB() {
            var a = new No("A");
            var b = new No("B");
            a.setProximo(b);
            b.setProximo(a);

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerTypeHint(No.class)
                    .registerExternalSymbol("no", a, true)
                    .build();

            boolean result = LogicalExpression.compile("no.proximo.valor = \"B\"", env)
                    .compute(Map.of("no", a));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("mutually referential objects — two hops return to the original node")
        void shouldNavigateTwoHopsBackToRoot() {
            var a = new No("A");
            var b = new No("B");
            a.setProximo(b);
            b.setProximo(a);

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerTypeHint(No.class)
                    .registerExternalSymbol("no", a, true)
                    .build();

            boolean result = LogicalExpression.compile("no.proximo.proximo.valor = \"A\"", env)
                    .compute(Map.of("no", a));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("method returning this — chain terminates at the fixed expression depth")
        void shouldInvokeMethodReturningThisAndContinueChain() {
            var no = new No("raiz");

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerTypeHint(No.class)
                    .registerExternalSymbol("no", no, true)
                    .build();

            boolean result = LogicalExpression.compile("no.self().valor = \"raiz\"", env)
                    .compute(Map.of("no", no));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("method returning this chained twice — both hops resolve correctly")
        void shouldInvokeMethodReturningThisTwice() {
            var no = new No("raiz");

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerTypeHint(No.class)
                    .registerExternalSymbol("no", no, true)
                    .build();

            boolean result = LogicalExpression.compile("no.self().self().valor = \"raiz\"", env)
                    .compute(Map.of("no", no));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("circular numeric property evaluates correctly in arithmetic")
        void shouldEvaluateCircularNumericPropertyInArithmetic() {
            var conta = new Conta(new BigDecimal("100"));
            conta.setVinculada(conta);

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerTypeHint(Conta.class)
                    .registerExternalSymbol("conta", conta, true)
                    .build();

            BigDecimal result = MathExpression.compile("conta.vinculada.saldo * 2", env)
                    .compute(Map.of("conta", conta));

            assertThat(result).isEqualByComparingTo("200");
        }

        @Test
        @DisplayName("mutually circular accounts — cross-reference arithmetic resolves correctly")
        void shouldEvaluateCrossReferencedArithmetic() {
            var principal = new Conta(new BigDecimal("500"));
            var vinculada  = new Conta(new BigDecimal("300"));
            principal.setVinculada(vinculada);
            vinculada.setVinculada(principal);

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerTypeHint(Conta.class)
                    .registerExternalSymbol("conta", principal, true)
                    .build();

            // conta.vinculada.saldo = 300 (vinculada), conta.vinculada.vinculada.saldo = 500 (volta a principal)
            BigDecimal linkedSaldo = MathExpression.compile("conta.vinculada.saldo", env)
                    .compute(Map.of("conta", principal));
            BigDecimal backSaldo = MathExpression.compile("conta.vinculada.vinculada.saldo", env)
                    .compute(Map.of("conta", principal));

            assertThat(linkedSaldo).isEqualByComparingTo("300");
            assertThat(backSaldo).isEqualByComparingTo("500");
        }
    }

    // -------------------------------------------------------------------------
    // Reflective path (no type hints)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Reflective path — ReflectiveAccessCache")
    class ReflectivePath {

        @Test
        @DisplayName("self-referential property — one hop returns the root value")
        void shouldNavigateOneSelfHopReflectively() {
            var no = new No("raiz");
            no.setProximo(no);

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("no", no, true)
                    .build();

            boolean result = LogicalExpression.compile("no.proximo.valor = \"raiz\"", env)
                    .compute(Map.of("no", no));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("self-referential property — two hops still resolves the same value")
        void shouldNavigateTwoSelfHopsReflectively() {
            var no = new No("raiz");
            no.setProximo(no);

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("no", no, true)
                    .build();

            boolean result = LogicalExpression.compile("no.proximo.proximo.valor = \"raiz\"", env)
                    .compute(Map.of("no", no));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("mutually referential objects — navigates from A to B correctly")
        void shouldNavigateFromAToBReflectively() {
            var a = new No("A");
            var b = new No("B");
            a.setProximo(b);
            b.setProximo(a);

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("no", a, true)
                    .build();

            boolean result = LogicalExpression.compile("no.proximo.valor = \"B\"", env)
                    .compute(Map.of("no", a));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("mutually referential objects — two hops return to the original node")
        void shouldNavigateTwoHopsBackToRootReflectively() {
            var a = new No("A");
            var b = new No("B");
            a.setProximo(b);
            b.setProximo(a);

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("no", a, true)
                    .build();

            boolean result = LogicalExpression.compile("no.proximo.proximo.valor = \"A\"", env)
                    .compute(Map.of("no", a));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("method returning this — chain terminates at the fixed expression depth")
        void shouldInvokeMethodReturningThisReflectively() {
            var no = new No("raiz");

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("no", no, true)
                    .build();

            boolean result = LogicalExpression.compile("no.self().valor = \"raiz\"", env)
                    .compute(Map.of("no", no));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("method returning this chained twice — both hops resolve correctly")
        void shouldInvokeMethodReturningThisTwiceReflectively() {
            var no = new No("raiz");

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("no", no, true)
                    .build();

            boolean result = LogicalExpression.compile("no.self().self().valor = \"raiz\"", env)
                    .compute(Map.of("no", no));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("circular numeric property evaluates correctly in arithmetic")
        void shouldEvaluateCircularNumericPropertyInArithmeticReflectively() {
            var conta = new Conta(new BigDecimal("100"));
            conta.setVinculada(conta);

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("conta", conta, true)
                    .build();

            BigDecimal result = MathExpression.compile("conta.vinculada.saldo * 2", env)
                    .compute(Map.of("conta", conta));

            assertThat(result).isEqualByComparingTo("200");
        }

        @Test
        @DisplayName("mutually circular accounts — cross-reference arithmetic resolves correctly")
        void shouldEvaluateCrossReferencedArithmeticReflectively() {
            var principal = new Conta(new BigDecimal("500"));
            var vinculada  = new Conta(new BigDecimal("300"));
            principal.setVinculada(vinculada);
            vinculada.setVinculada(principal);

            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("conta", principal, true)
                    .build();

            BigDecimal linkedSaldo = MathExpression.compile("conta.vinculada.saldo", env)
                    .compute(Map.of("conta", principal));
            BigDecimal backSaldo = MathExpression.compile("conta.vinculada.vinculada.saldo", env)
                    .compute(Map.of("conta", principal));

            assertThat(linkedSaldo).isEqualByComparingTo("300");
            assertThat(backSaldo).isEqualByComparingTo("500");
        }
    }
}
