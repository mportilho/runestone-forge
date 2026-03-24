package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import com.runestone.expeval2.types.ObjectType;
import com.runestone.expeval2.types.ScalarType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Object navigation - execution plan")
class ObjectNavigationPlanTest {

    private final ExpressionCompiler compiler = new ExpressionCompiler();

    @Test
    @DisplayName("property chain with registered type hints uses precomputed MethodHandles")
    void shouldBuildStaticPropertyChainStepsWhenTypeHintsAreAvailable() {
        var usuario = new UsuarioComPedido(new Pedido(new BigDecimal("100")));
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerTypeHint(UsuarioComPedido.class)
                .registerTypeHint(Pedido.class)
                .registerExternalSymbol("usuario", usuario, true)
                .registerExternalSymbol("taxa", new BigDecimal("0.05"), true)
                .build();

        CompiledExpression compiled = compiler.compile(
                "usuario.pedido.calcularTotal(taxa)",
                ExpressionResultType.MATH,
                environment
        );

        assertThat(compiled.executionPlan().resultExpression()).isInstanceOf(ExecutablePropertyChain.class);
        ExecutablePropertyChain chain = (ExecutablePropertyChain) compiled.executionPlan().resultExpression();
        assertThat(chain.chain()).hasSize(2);

        assertThat(chain.chain().get(0)).isInstanceOf(ExecutablePropertyChain.ExecutableFieldGet.class);
        ExecutablePropertyChain.ExecutableFieldGet propertyStep =
                (ExecutablePropertyChain.ExecutableFieldGet) chain.chain().get(0);
        assertThat(propertyStep.name()).isEqualTo("pedido");
        assertThat(propertyStep.getter()).isNotNull();
        assertThat(propertyStep.resolvedType()).isEqualTo(new ObjectType(Pedido.class));

        assertThat(chain.chain().get(1)).isInstanceOf(ExecutablePropertyChain.ExecutableMethodInvoke.class);
        ExecutablePropertyChain.ExecutableMethodInvoke methodStep =
                (ExecutablePropertyChain.ExecutableMethodInvoke) chain.chain().get(1);
        assertThat(methodStep.name()).isEqualTo("calcularTotal");
        assertThat(methodStep.handle()).isNotNull();
        assertThat(methodStep.parameterTypes()).containsExactly(BigDecimal.class);
        assertThat(methodStep.returnType()).isEqualTo(ScalarType.NUMBER);
    }

    @Test
    @DisplayName("property chain without registered type hint keeps reflective fallback steps")
    void shouldKeepReflectiveFallbackWhenTypeHintsAreMissing() {
        var usuario = new Usuario(new BigDecimal("42"));
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerExternalSymbol("usuario", usuario, true)
                .build();

        CompiledExpression compiled = compiler.compile(
                "usuario.pontos",
                ExpressionResultType.MATH,
                environment
        );

        assertThat(compiled.executionPlan().resultExpression()).isInstanceOf(ExecutablePropertyChain.class);
        ExecutablePropertyChain chain = (ExecutablePropertyChain) compiled.executionPlan().resultExpression();
        assertThat(chain.chain()).singleElement().isInstanceOf(ExecutablePropertyChain.ReflectivePropertyAccess.class);
    }

    record Usuario(BigDecimal pontos) {
    }

    record UsuarioComPedido(Pedido pedido) {
    }

    record Pedido(BigDecimal valor) {
        public BigDecimal calcularTotal(BigDecimal taxa) {
            return valor.multiply(BigDecimal.ONE.add(taxa));
        }
    }
}
