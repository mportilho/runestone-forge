package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ObjectType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.runtime.CollectionOperationExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.CollectionOperationExecutors;
import com.runestone.expeval_mk3.internal.runtime.RegisteredPropertyExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.CollectionOperationBinding;
import com.runestone.expeval_mk3.internal.semantics.NavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.RegisteredPropertyNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import com.runestone.expeval_mk3.support.EnvironmentConfigurations;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves issue #95's "no runtime lookup" rule: a migrated navigation/collection-operation node carries
 * the exact {@code MethodHandle}/{@code CollectionOperationExecutor} instance semantic resolution (or
 * the executor table, resolved once at plan-build time) already selected, rather than re-resolving a
 * member or operation identity during {@code execute()}. Identity ({@code ==}) is the right assertion
 * here — an accidental re-lookup would still produce an equal-by-value handle/executor most of the
 * time, so only reference identity distinguishes "reused the resolved binding" from "looked it up again".
 */
class NavigationExecutionUsesResolvedBindingsTest {

    @Test
    void registeredPropertyNodeReusesTheSetupResolvedAccessorHandleInstance() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "customer",
                        new ObjectType(EnvironmentConfigurations.CustomerProfile.class.getName()),
                        new EnvironmentConfigurations.CustomerProfile("Ana", BigDecimal.TEN),
                        ExternalSymbolOverwritePolicy.FIXED)
                .registerJavaType(EnvironmentConfigurations.CustomerProfile.class)
                .build();
        ExpressionFileNode ast = ast("customer.name");
        SemanticModel model = resolve(ast, environment);
        NavigationBinding binding = model.navigationBindings().values().iterator().next();
        RegisteredPropertyNavigationBinding propertyBinding = (RegisteredPropertyNavigationBinding) binding;

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);
        RegisteredPropertyExecutableNode node = (RegisteredPropertyExecutableNode) plan.resultExpression();

        assertThat(node.binding().accessorHandle()).isSameAs(propertyBinding.accessorHandle());
    }

    @Test
    void collectionOperationNodeReusesTheExecutorResolvedOnceAtPlanBuildTime() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("items", new CollectionType(ScalarType.NUMBER), List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("items.sum()");
        SemanticModel model = resolve(ast, environment);
        NavigationBinding binding = model.navigationBindings().values().iterator().next();
        CollectionOperationBinding operationBinding = (CollectionOperationBinding) binding;

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);
        CollectionOperationExecutableNode node = (CollectionOperationExecutableNode) plan.resultExpression();

        assertThat(node.executor())
                .isSameAs(CollectionOperationExecutors.executorFor(operationBinding.identity()));
        assertThat(operationBinding.identity()).isEqualTo(CollectionOperationCatalog.OperationIdentity.SUM);
    }

    private static SemanticModel resolve(ExpressionFileNode ast, ExpressionEnvironment environment) {
        SemanticResolutionSuccess result = (SemanticResolutionSuccess) new SemanticResolver().resolve(ast, environment);
        return result.model();
    }

    private static ExpressionFileNode ast(String source) {
        ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(source);
        return ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
    }
}
