package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the planning seam behind issue #91: {@code ExecutionPlanBuilder} reads a navigation link's
 * meaning exclusively from {@code SemanticModel.navigationBindings()}. If that binding is absent,
 * planning must fail loudly instead of falling back to inspecting the {@code NavigationLink} AST
 * shape or the receiver's runtime value.
 */
class ExecutionPlanBuilderNavigationSeamTest {

    @Test
    void planBuildsSuccessfullyWhenEveryNavigationLinkHasABinding() {
        ExpressionFileNode ast = ast("items[0]");
        SemanticModel model = resolve(ast);

        assertThat(model.navigationBindings()).isNotEmpty();
        assertThat(new ExecutionPlanBuilder().build(model, environment())).isNotNull();
    }

    @Test
    void planBuildingFailsInsteadOfRederivingAMissingNavigationBinding() throws Exception {
        ExpressionFileNode ast = ast("items[0]");
        SemanticModel model = resolve(ast);
        clearNavigationBindings(model);

        assertThatThrownBy(() -> new ExecutionPlanBuilder().build(model, environment()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("navigation binding");
    }

    private static SemanticModel resolve(ExpressionFileNode ast) {
        SemanticResolutionSuccess result =
                (SemanticResolutionSuccess) new SemanticResolver().resolve(ast, environment());
        return result.model();
    }

    private static ExpressionEnvironment environment() {
        return ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();
    }

    @Test
    void planBuildingFailsInsteadOfRederivingMissingRuntimeNullability() throws Exception {
        ExpressionFileNode ast = ast("items[0]");
        SemanticModel model = resolve(ast);
        clearRuntimeNullability(model);

        assertThatThrownBy(() -> new ExecutionPlanBuilder().build(model, environment()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("runtime nullability");
    }

    private static void clearNavigationBindings(SemanticModel model) throws Exception {
        Field field = SemanticModel.class.getDeclaredField("navigationBindings");
        field.setAccessible(true);
        field.set(model, Map.of());
    }

    private static void clearRuntimeNullability(SemanticModel model) throws Exception {
        Field field = SemanticModel.class.getDeclaredField("runtimeNullability");
        field.setAccessible(true);
        field.set(model, Map.of());
    }

    private static ExpressionFileNode ast(String source) {
        ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(source);
        return ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
    }
}
