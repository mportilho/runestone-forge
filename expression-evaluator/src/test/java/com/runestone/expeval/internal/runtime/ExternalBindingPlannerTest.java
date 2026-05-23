package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.execution.plan.ExternalBindingPlan;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.catalog.ExternalSymbolDescriptor;
import com.runestone.expeval.internal.ast.ExpressionFileNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.internal.semantic.SemanticModel;
import com.runestone.expeval.internal.semantic.SymbolKind;
import com.runestone.expeval.internal.semantic.SymbolIndexAllocator;
import com.runestone.expeval.internal.semantic.SymbolRef;
import com.runestone.expeval.types.ScalarType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalBindingPlannerTest {

    @Test
    void seedsDefaultsByAssignedExternalSymbolIndex() {
        SymbolRef amount = externalSymbol("amount");
        SymbolRef enabled = externalSymbol("enabled");
        SemanticModel model = semanticModel(Map.of(
                "amount", amount,
                "enabled", enabled));
        ExternalSymbolCatalog catalog = new ExternalSymbolCatalog(Map.of(
                "amount", new ExternalSymbolDescriptor("amount", ScalarType.NUMBER, "42.50", true),
                "enabled", new ExternalSymbolDescriptor("enabled", ScalarType.BOOLEAN, "true", true)));

        Object[] defaults = ExternalBindingPlanner.seedDefaults(model, catalog, runtimeServices());

        assertThat(defaults).containsExactly(new BigDecimal("42.50"), true);
    }

    @Test
    void leavesUnknownExternalDefaultsUnbound() {
        SymbolRef known = externalSymbol("known");
        SymbolRef unknown = externalSymbol("unknown");
        SemanticModel model = semanticModel(Map.of(
                "known", known,
                "unknown", unknown));
        ExternalSymbolCatalog catalog = new ExternalSymbolCatalog(Map.of(
                "known", new ExternalSymbolDescriptor("known", ScalarType.STRING, "value", true)));

        Object[] defaults = ExternalBindingPlanner.seedDefaults(model, catalog, runtimeServices());

        assertThat(defaults).containsExactly("value", ExecutionScope.UNBOUND);
    }

    @Test
    void buildsBindingPlansFromCatalogMetadata() {
        SymbolRef fixed = externalSymbol("fixed");
        SymbolRef open = externalSymbol("open");
        SymbolRef unknown = externalSymbol("unknown");
        SemanticModel model = semanticModel(Map.of(
                "fixed", fixed,
                "open", open,
                "unknown", unknown));
        ExternalSymbolCatalog catalog = new ExternalSymbolCatalog(Map.of(
                "fixed", new ExternalSymbolDescriptor("fixed", ScalarType.NUMBER, BigDecimal.ONE, false),
                "open", new ExternalSymbolDescriptor("open", ScalarType.STRING, "default", true)));

        Map<String, ExternalBindingPlan> plans = ExternalBindingPlanner.seedBindingPlans(model, catalog);

        assertThat(plans).containsOnlyKeys("fixed", "open", "unknown");
        assertThat(plans.get("fixed").symbolRef()).isSameAs(fixed);
        assertThat(plans.get("fixed").declaredType()).isEqualTo(ScalarType.NUMBER);
        assertThat(plans.get("fixed").overridable()).isFalse();
        assertThat(plans.get("open").declaredType()).isEqualTo(ScalarType.STRING);
        assertThat(plans.get("open").overridable()).isTrue();
        assertThat(plans.get("unknown").declaredType()).isNull();
        assertThat(plans.get("unknown").overridable()).isTrue();
    }

    @Test
    void seedsOnlyNonOverridableConstantsForCompileTimeFolding() {
        SymbolRef fixed = externalSymbol("fixed");
        SymbolRef overridable = externalSymbol("overridable");
        SemanticModel model = semanticModel(Map.of(
                "fixed", fixed,
                "overridable", overridable));
        ExternalSymbolCatalog catalog = new ExternalSymbolCatalog(Map.of(
                "fixed", new ExternalSymbolDescriptor("fixed", ScalarType.NUMBER, "10", false),
                "overridable", new ExternalSymbolDescriptor("overridable", ScalarType.NUMBER, "20", true)));

        Map<SymbolRef, Object> constants = ExternalBindingPlanner.seedNonOverridableConstants(
                model,
                catalog,
                runtimeServices());

        assertThat(constants).containsOnlyKeys(fixed);
        assertThat(constants.get(fixed)).isEqualTo(new BigDecimal("10"));
    }

    private static SymbolRef externalSymbol(String name) {
        return new SymbolRef(name, SymbolKind.EXTERNAL);
    }

    private static RuntimeServices runtimeServices() {
        return new RuntimeServices(new DefaultDataConversionService());
    }

    private static SemanticModel semanticModel(Map<String, SymbolRef> externalSymbolsByName) {
        SemanticModel model = new SemanticModel(
                new ExpressionFileNode(new NodeId("file"), new SourceSpan(0, 0, 1, 0, 1, 0), List.of(), null),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                new LinkedHashMap<>(externalSymbolsByName),
                Map.of(),
                Map.of(),
                List.of());
        SymbolIndexAllocator.assignIndices(model);
        return model;
    }
}
