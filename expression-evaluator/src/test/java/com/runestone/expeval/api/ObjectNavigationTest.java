package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Object navigation via dot-chain expressions")
class ObjectNavigationTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    record Endereco(String cidade) {}

    record Usuario(String nome, BigDecimal pontos, Endereco endereco) {}

    record Pedido(BigDecimal valor) {
        public BigDecimal calcularTotal() {
            return valor.multiply(BigDecimal.valueOf(1.1));
        }
        public BigDecimal calcularTotal(BigDecimal taxa) {
            return valor.multiply(BigDecimal.ONE.add(taxa));
        }
    }

    static class UsuarioPojo {
        private final String nome;
        private final BigDecimal pontos;
        UsuarioPojo(String nome, BigDecimal pontos) {
            this.nome = nome;
            this.pontos = pontos;
        }
        public String getNome() { return nome; }
        public BigDecimal getPontos() { return pontos; }
    }

    static class RegistroPublico {
        public String codigo = "REG-001";
    }

    static class MembroNaoPublico {
        String codigo = "PRIV-001";
        BigDecimal calcularTotal() {
            return BigDecimal.TEN;
        }
    }

    record UsuarioComPedido(String nome, Pedido pedido) {}

    // -------------------------------------------------------------------------
    // Happy path — property access
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("property access on Java record returns the field value")
    void shouldAccessRecordPropertyDirectly() {
        var usuario = new Usuario("Alice", new BigDecimal("150"), null);
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(Usuario.class)
                .registerExternalSymbol("usuario", usuario, true)
                .build();

        String result = LogicalExpression.compile("usuario.nome = \"Alice\"", env)
                .compute(Map.of("usuario", usuario))
                ? "Alice" : "miss";

        assertThat(result).isEqualTo("Alice");
    }

    @Test
    @DisplayName("property access evaluates numeric field in arithmetic expression")
    void shouldUseRecordNumericPropertyInArithmetic() {
        var usuario = new Usuario("Bob", new BigDecimal("50"), null);
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(Usuario.class)
                .registerExternalSymbol("usuario", usuario, true)
                .build();

        BigDecimal result = MathExpression.compile("usuario.pontos * 2", env)
                .compute(Map.of("usuario", usuario));

        assertThat(result).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("nested property access navigates two hops correctly")
    void shouldNavigateTwoLevelsOfPropertyAccess() {
        var usuario = new Usuario("Carol", BigDecimal.ZERO, new Endereco("Belém"));
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(Usuario.class)
                .registerTypeHint(Endereco.class)
                .registerExternalSymbol("usuario", usuario, true)
                .build();

        boolean result = LogicalExpression.compile("usuario.endereco.cidade = \"Belém\"", env)
                .compute(Map.of("usuario", usuario));

        assertThat(result).isTrue();
    }

    // -------------------------------------------------------------------------
    // Happy path — method call in chain
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("no-arg method call in chain returns the method result")
    void shouldInvokeNoArgMethodInChain() {
        var pedido = new Pedido(new BigDecimal("100"));
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(Pedido.class)
                .registerExternalSymbol("pedido", pedido, true)
                .build();

        BigDecimal result = MathExpression.compile("pedido.calcularTotal()", env)
                .compute(Map.of("pedido", pedido));

        assertThat(result).isEqualByComparingTo("110.0");
    }

    @Test
    @DisplayName("method call with argument in chain uses the supplied variable")
    void shouldInvokeMethodWithArgumentInChain() {
        var pedido = new Pedido(new BigDecimal("200"));
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(Pedido.class)
                .registerExternalSymbol("pedido", pedido, true)
                .registerExternalSymbol("taxa", new BigDecimal("0.05"), true)
                .build();

        BigDecimal result = MathExpression.compile("pedido.calcularTotal(taxa)", env)
                .compute(Map.of("pedido", pedido, "taxa", new BigDecimal("0.05")));

        assertThat(result).isEqualByComparingTo("210.00");
    }

    @Test
    @DisplayName("chained property access through two object levels")
    void shouldAccessPropertyThroughObjectChain() {
        var pedido = new Pedido(new BigDecimal("300"));
        var usuario = new UsuarioComPedido("Diana", pedido);
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(UsuarioComPedido.class)
                .registerTypeHint(Pedido.class)
                .registerExternalSymbol("usuario", usuario, true)
                .build();

        BigDecimal result = MathExpression.compile("usuario.pedido.valor", env)
                .compute(Map.of("usuario", usuario));

        assertThat(result).isEqualByComparingTo("300");
    }

    // -------------------------------------------------------------------------
    // POJO getters and public fields
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POJO getter getNome() is discovered and maps to property 'nome'")
    void shouldDiscoverPojoGetterAsProperty() {
        var pojo = new UsuarioPojo("Eve", new BigDecimal("75"));
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(UsuarioPojo.class)
                .registerExternalSymbol("usuario", pojo, true)
                .build();

        boolean result = LogicalExpression.compile("usuario.nome = \"Eve\"", env)
                .compute(Map.of("usuario", pojo));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("public field is discoverable as fallback property")
    void shouldAccessPublicFieldAsFallback() {
        var reg = new RegistroPublico();
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(RegistroPublico.class)
                .registerExternalSymbol("reg", reg, true)
                .build();

        boolean result = LogicalExpression.compile("reg.codigo = \"REG-001\"", env)
                .compute(Map.of("reg", reg));

        assertThat(result).isTrue();
    }

    // -------------------------------------------------------------------------
    // Tolerance — no type hint registered
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("expression compiles and evaluates without type hint registration")
    void shouldEvaluateWithoutTypeHintRegistration() {
        var usuario = new Usuario("Frank", new BigDecimal("99"), null);
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerExternalSymbol("usuario", usuario, true)
                .build();

        BigDecimal result = MathExpression.compile("usuario.pontos", env)
                .compute(Map.of("usuario", usuario));

        assertThat(result).isEqualByComparingTo("99");
    }

    // -------------------------------------------------------------------------
    // Error — null in chain
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("null intermediate value in navigation chain throws ExpressionEvaluationException")
    void shouldThrowWhenIntermediateValueIsNull() {
        var usuario = new Usuario("Grace", BigDecimal.ZERO, null); // endereco == null
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(Usuario.class)
                .registerTypeHint(Endereco.class)
                .registerExternalSymbol("usuario", usuario, true)
                .build();
        LogicalExpression expr = LogicalExpression.compile("usuario.endereco.cidade = \"Belém\"", env);

        assertThatThrownBy(() -> expr.compute(Map.of("usuario", usuario)))
                .isInstanceOf(ExpressionEvaluationException.class)
                .hasMessageContaining("null");
    }

    // -------------------------------------------------------------------------
    // Static validation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("validate returns valid when type is registered and property exists")
    void shouldPassStaticValidationWhenTypeHintCoversExpression() {
        var usuario = new Usuario("Heidi", new BigDecimal("10"), null);
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(Usuario.class)
                .registerExternalSymbol("usuario", usuario, true)
                .build();

        var validation = MathExpression.validate("usuario.pontos * 2", env);

        assertThat(validation.valid()).isTrue();
    }

    @Test
    @DisplayName("statically typed property chain still enforces math result type validation")
    void shouldFailWhenTypedPropertyChainDoesNotMatchMathResultType() {
        var usuario = new Usuario("Heidi", new BigDecimal("10"), null);
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(Usuario.class)
                .registerExternalSymbol("usuario", usuario, true)
                .build();

        var validation = MathExpression.validate("usuario.nome", env);

        assertThat(validation.valid()).isFalse();
        assertThat(validation.issues()).extracting(CompilationIssue::code)
                .contains("RESULT_TYPE_MISMATCH");
    }

    @Test
    @DisplayName("non-public field is not exposed as property by type hint discovery")
    void shouldIgnoreNonPublicFieldDuringTypeHintDiscovery() {
        var value = new MembroNaoPublico();
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(MembroNaoPublico.class)
                .registerExternalSymbol("obj", value, true)
                .build();

        var validation = LogicalExpression.validate("obj.codigo = \"PRIV-001\"", env);

        assertThat(validation.valid()).isFalse();
        assertThat(validation.issues()).extracting(CompilationIssue::code)
                .contains("UNKNOWN_PROPERTY");
    }

    @Test
    @DisplayName("non-public method is not exposed for method-call navigation")
    void shouldIgnoreNonPublicMethodDuringTypeHintDiscovery() {
        var value = new MembroNaoPublico();
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerTypeHint(MembroNaoPublico.class)
                .registerExternalSymbol("obj", value, true)
                .build();

        var validation = MathExpression.validate("obj.calcularTotal()", env);

        assertThat(validation.valid()).isFalse();
        assertThat(validation.issues()).extracting(CompilationIssue::code)
                .contains("UNKNOWN_METHOD");
    }

    // -------------------------------------------------------------------------
    // Cache key — environment ID
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("environments with different type hints generate distinct environment IDs")
    void shouldProduceDifferentEnvironmentIdsForDifferentTypeHints() {
        var usuario = new Usuario("Ivan", BigDecimal.ONE, null);

        ExpressionEnvironment envWithHint = ExpressionEnvironment.builder()
                .registerTypeHint(Usuario.class)
                .registerExternalSymbol("usuario", usuario, true)
                .build();

        ExpressionEnvironment envWithoutHint = ExpressionEnvironment.builder()
                .registerExternalSymbol("usuario", usuario, true)
                .build();

        assertThat(envWithHint.environmentId())
                .isNotEqualTo(envWithoutHint.environmentId());
    }

    // -------------------------------------------------------------------------
    // Validation — null guard
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("registerTypeHint(null) throws NullPointerException")
    void shouldRejectNullTypeHint() {
        ExpressionEnvironmentBuilder builder = ExpressionEnvironment.builder();

        assertThatThrownBy(() -> builder.registerTypeHint(null))
                .isInstanceOf(NullPointerException.class);
    }
}
