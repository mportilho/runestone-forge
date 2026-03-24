package com.runestone.expeval2.perf;

import com.runestone.expeval2.environment.ExpressionEnvironment;

import java.math.BigDecimal;
import java.util.Map;

public final class ObjectNavigationBenchmarkSupport {

    private static final int FRAME_COUNT = 256;
    private static final int FRAME_MASK = FRAME_COUNT - 1;
    private static final BigDecimal ONE_POINT_TEN = new BigDecimal("1.10");

    public static final String NESTED_PROPERTY_EXPRESSION =
            "usuario.endereco.bairro.codigo = \"BAT-100\"";
    public static final String METHOD_NO_ARG_EXPRESSION =
            "usuario.pedido.calcularTotal()";
    public static final String METHOD_WITH_ARG_EXPRESSION =
            "usuario.pedido.calcularTotal(taxa)";

    private static final Usuario[] USERS = buildUsers();
    private static final BigDecimal[] TAXES = buildTaxes();
    private static final Map<String, Object>[] USER_ONLY_VALUES = buildUserOnlyValues();
    private static final Map<String, Object>[] USER_AND_RATE_VALUES = buildUserAndRateValues();
    private static final ExpressionEnvironment TYPED_ENVIRONMENT = buildTypedNavigationEnvironment();
    private static final ExpressionEnvironment REFLECTIVE_ENVIRONMENT = buildReflectiveNavigationEnvironment();

    private ObjectNavigationBenchmarkSupport() {
    }

    public static ExpressionEnvironment typedEnvironment() {
        return TYPED_ENVIRONMENT;
    }

    public static ExpressionEnvironment reflectiveEnvironment() {
        return REFLECTIVE_ENVIRONMENT;
    }

    public static ExpressionEnvironment buildTypedNavigationEnvironment() {
        return ExpressionEnvironment.builder()
                .registerTypeHint(Usuario.class)
                .registerTypeHint(Endereco.class)
                .registerTypeHint(Bairro.class)
                .registerTypeHint(Pedido.class)
                .registerExternalSymbol("usuario", USERS[0], true)
                .registerExternalSymbol("taxa", TAXES[0], true)
                .build();
    }

    public static ExpressionEnvironment buildReflectiveNavigationEnvironment() {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol("usuario", USERS[0], true)
                .registerExternalSymbol("taxa", TAXES[0], true)
                .build();
    }

    public static Map<String, Object> userOnlyValues(int index) {
        return USER_ONLY_VALUES[index & FRAME_MASK];
    }

    public static Map<String, Object> userAndRateValues(int index) {
        return USER_AND_RATE_VALUES[index & FRAME_MASK];
    }

    private static Usuario[] buildUsers() {
        Usuario[] users = new Usuario[FRAME_COUNT];
        for (int index = 0; index < FRAME_COUNT; index++) {
            String code = (index & 1) == 0 ? "BAT-100" : "BAT-200";
            Bairro bairro = new Bairro(code);
            Endereco endereco = new Endereco("Cidade-" + (index & 15), bairro);
            BigDecimal valor = BigDecimal.valueOf(10_000L + index, 2);
            Pedido pedido = new Pedido(valor);
            users[index] = new Usuario("Usuario-" + index, endereco, pedido);
        }
        return users;
    }

    private static BigDecimal[] buildTaxes() {
        BigDecimal[] taxes = new BigDecimal[FRAME_COUNT];
        for (int index = 0; index < FRAME_COUNT; index++) {
            taxes[index] = BigDecimal.valueOf(1L + (index % 7L), 2);
        }
        return taxes;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object>[] buildUserOnlyValues() {
        Map<String, Object>[] values = (Map<String, Object>[]) new Map<?, ?>[FRAME_COUNT];
        for (int index = 0; index < FRAME_COUNT; index++) {
            values[index] = Map.of("usuario", USERS[index]);
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object>[] buildUserAndRateValues() {
        Map<String, Object>[] values = (Map<String, Object>[]) new Map<?, ?>[FRAME_COUNT];
        for (int index = 0; index < FRAME_COUNT; index++) {
            values[index] = Map.of("usuario", USERS[index], "taxa", TAXES[index]);
        }
        return values;
    }

    public record Bairro(String codigo) {
    }

    public record Endereco(String cidade, Bairro bairro) {
    }

    public record Usuario(String nome, Endereco endereco, Pedido pedido) {
    }

    public static final class Pedido {

        private final BigDecimal valor;

        public Pedido(BigDecimal valor) {
            this.valor = valor;
        }

        public BigDecimal getValor() {
            return valor;
        }

        public BigDecimal calcularTotal() {
            return valor.multiply(ONE_POINT_TEN);
        }

        public BigDecimal calcularTotal(BigDecimal taxa) {
            return valor.multiply(BigDecimal.ONE.add(taxa));
        }
    }
}
