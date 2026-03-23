package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AssignmentExpression — assignment-only block returning Map<String, Object>")
class AssignmentExpressionTest {

    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().addMathFunctions().build();

    // -------------------------------------------------------------------------
    // Happy Path
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("happy path")
    class HappyPath {

        @Test
        @DisplayName("single numeric assignment returns one entry")
        void singleNumericAssignment() {
            Map<String, Object> result = AssignmentExpression.compile("x = 42;").compute();

            assertThat(result).containsOnlyKeys("x");
            assertThat((BigDecimal) result.get("x")).isEqualByComparingTo("42");
        }

        @Test
        @DisplayName("chained assignments — later variable depends on earlier one")
        void chainedAssignments() {
            Map<String, Object> result = AssignmentExpression.compile("""
                    taxa = 0.05;
                    principal = 1000;
                    juros = principal * taxa;
                    total = principal + juros;
                    """).compute();

            assertThat(result).containsOnlyKeys("taxa", "principal", "juros", "total");
            assertThat((BigDecimal) result.get("total")).isEqualByComparingTo("1050");
            assertThat((BigDecimal) result.get("juros")).isEqualByComparingTo("50");
        }

        @Test
        @DisplayName("result map preserves source-order of assignments")
        void resultPreservesDeclarationOrder() {
            Map<String, Object> result = AssignmentExpression.compile("c = 3; a = 1; b = 2;").compute();

            assertThat(result.keySet()).containsExactly("c", "a", "b");
        }

        @Test
        @DisplayName("string assignment returned as String")
        void stringAssignment() {
            Map<String, Object> result = AssignmentExpression.compile("nome = \"Alice\";").compute();

            assertThat(result.get("nome")).isEqualTo("Alice");
        }

        @Test
        @DisplayName("boolean assignment returned as Boolean")
        void booleanAssignment() {
            Map<String, Object> result = AssignmentExpression.compile("ativo = true;").compute();

            assertThat(result.get("ativo")).isEqualTo(true);
        }

        @Test
        @DisplayName("date assignment returned as LocalDate")
        void dateAssignment() {
            Map<String, Object> result = AssignmentExpression.compile("vencimento = 2025-12-31;").compute();

            assertThat(result.get("vencimento")).isEqualTo(LocalDate.of(2025, 12, 31));
        }

        @Test
        @DisplayName("mixed-type assignments all present in result")
        void mixedTypeAssignments() {
            Map<String, Object> result = AssignmentExpression.compile("""
                    numero = 10;
                    texto = "ola";
                    flag = false;
                    """).compute();

            assertThat(result).containsOnlyKeys("numero", "texto", "flag");
            assertThat((BigDecimal) result.get("numero")).isEqualByComparingTo("10");
            assertThat(result.get("texto")).isEqualTo("ola");
            assertThat(result.get("flag")).isEqualTo(false);
        }

        @Test
        @DisplayName("external symbol used in assignment is resolved")
        void externalSymbolUsedInAssignment() {
            Map<String, Object> result = AssignmentExpression.compile("dobro = extVal * 2;")
                    .compute(Map.of("extVal", new BigDecimal("7")));

            assertThat((BigDecimal) result.get("dobro")).isEqualByComparingTo("14");
        }

        @Test
        @DisplayName("function call result assigned via math function catalog")
        void functionCallAssignment() {
            Map<String, Object> result = AssignmentExpression.compile("raiz = sqrt(144);", ENV).compute();

            assertThat((BigDecimal) result.get("raiz")).isEqualByComparingTo("12");
        }

        @Test
        @DisplayName("destructuring assignment unpacks vector into named variables")
        void destructuringAssignment() {
            Map<String, Object> result = AssignmentExpression.compile("[a, b, c] = [10, 20, 30];").compute();

            assertThat(result).containsOnlyKeys("a", "b", "c");
            assertThat((BigDecimal) result.get("a")).isEqualByComparingTo("10");
            assertThat((BigDecimal) result.get("b")).isEqualByComparingTo("20");
            assertThat((BigDecimal) result.get("c")).isEqualByComparingTo("30");
        }

        @Test
        @DisplayName("conditional assignment resolves correct branch")
        void conditionalAssignment() {
            Map<String, Object> result = AssignmentExpression.compile(
                    "desconto = if true then 10 else 0 endif;").compute();

            assertThat((BigDecimal) result.get("desconto")).isEqualByComparingTo("10");
        }
    }

    // -------------------------------------------------------------------------
    // Boundary / Edge Cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("boundary cases")
    class Boundary {

        @Test
        @DisplayName("single assignment — minimal valid input")
        void singleAssignment() {
            Map<String, Object> result = AssignmentExpression.compile("x = 1;").compute();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("assignment of zero")
        void assignmentOfZero() {
            Map<String, Object> result = AssignmentExpression.compile("z = 0;").compute();

            assertThat((BigDecimal) result.get("z")).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("assignment of negative number via unary minus")
        void assignmentOfNegativeNumber() {
            Map<String, Object> result = AssignmentExpression.compile("negativo = -99;").compute();

            assertThat((BigDecimal) result.get("negativo")).isEqualByComparingTo("-99");
        }

        @Test
        @DisplayName("destructuring with fewer source elements — excess targets receive null")
        void destructuringExcessTargetsAreNull() {
            Map<String, Object> result = AssignmentExpression.compile("[a, b, c] = [1, 2];").compute();

            assertThat(result.get("c")).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // Invalid Input / Validation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("invalid input")
    class InvalidInput {

        @Test
        @DisplayName("missing semicolon causes parse error")
        void missingSemicolon() {
            assertThatThrownBy(() -> AssignmentExpression.compile("x = 1"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("source with only whitespace is rejected")
        void blankSource() {
            assertThatThrownBy(() -> AssignmentExpression.compile("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("unknown function reference causes compilation error")
        void unknownFunction() {
            assertThatThrownBy(() -> AssignmentExpression.compile("x = naoExiste(1);", ENV))
                    .isInstanceOf(ExpressionCompilationException.class);
        }
    }

    // -------------------------------------------------------------------------
    // Null / Empty
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("null and empty inputs")
    class NullAndEmpty {

        @Test
        @DisplayName("null source throws NullPointerException")
        void nullSource() {
            assertThatNullPointerException()
                    .isThrownBy(() -> AssignmentExpression.compile(null));
        }

        @Test
        @DisplayName("null environment throws NullPointerException")
        void nullEnvironment() {
            assertThatNullPointerException()
                    .isThrownBy(() -> AssignmentExpression.compile("x = 1;", null));
        }
    }

    // -------------------------------------------------------------------------
    // Idempotency / Retry
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("idempotency")
    class Idempotency {

        @Test
        @DisplayName("compute() called twice yields the same result")
        void computeIsIdempotent() {
            AssignmentExpression expr = AssignmentExpression.compile("x = 5; y = x * 2;");

            Map<String, Object> first = expr.compute();
            Map<String, Object> second = expr.compute();

            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("compiled expression can be reused with different variable values")
        void reusableWithDifferentValues() {
            AssignmentExpression expr = AssignmentExpression.compile("resultado = base * 10;");

            Map<String, Object> r1 = expr.compute(Map.of("base", new BigDecimal("3")));
            Map<String, Object> r2 = expr.compute(Map.of("base", new BigDecimal("5")));

            assertThat((BigDecimal) r1.get("resultado")).isEqualByComparingTo("30");
            assertThat((BigDecimal) r2.get("resultado")).isEqualByComparingTo("50");
        }
    }

    // -------------------------------------------------------------------------
    // Audit
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("audit trail")
    class AuditTrail {

        @Test
        @DisplayName("computeWithAudit() returns the same values as compute()")
        void auditResultMatchesCompute() {
            AssignmentExpression expr = AssignmentExpression.compile("a = 10; b = a + 5;");

            AuditResult<Map<String, Object>> audited = expr.computeWithAudit();

            assertThat((BigDecimal) audited.value().get("a")).isEqualByComparingTo("10");
            assertThat((BigDecimal) audited.value().get("b")).isEqualByComparingTo("15");
        }

        @Test
        @DisplayName("computeWithAudit() produces a non-empty audit trace")
        void auditTraceIsNonEmpty() {
            AuditResult<Map<String, Object>> audited =
                    AssignmentExpression.compile("x = 7;").computeWithAudit();

            assertThat(audited.trace().events()).isNotEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // compile(String) convenience overload
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("compile overloads")
    class CompileOverloads {

        @Test
        @DisplayName("compile(String) without environment compiles successfully")
        void compileWithoutEnvironment() {
            Map<String, Object> result = AssignmentExpression.compile("v = 99;").compute();

            assertThat((BigDecimal) result.get("v")).isEqualByComparingTo("99");
        }

        @Test
        @DisplayName("compile(String, ExpressionEnvironment) uses provided environment for functions")
        void compileWithEnvironmentProvidesFunction() {
            Map<String, Object> result = AssignmentExpression.compile("resultado = ln(1);", ENV).compute();

            assertThat((BigDecimal) result.get("resultado")).isEqualByComparingTo("0");
        }
    }
}
