package com.runestone.expeval_mk3.corpus;

import com.fasterxml.jackson.databind.JsonNode;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CorpusStructureTest {

    private static final Set<String> REMOVED_ENVIRONMENT_FIELDS = Set.of(
            "numericMode",
            "strict",
            "strictMode",
            "maxFilterDepth");

    @Test
    @DisplayName("corpus contains at least one hundred structurally valid expression cases")
    void corpusContainsAtLeastOneHundredCases() {
        assertThat(ExpressionCaseLoader.loadAll()).hasSizeGreaterThanOrEqualTo(100);
    }

    @Test
    @DisplayName("expression case ids are unique")
    void expressionCaseIdsAreUnique() {
        Set<String> ids = new HashSet<>();

        for (ExpressionCase expressionCase : ExpressionCaseLoader.loadAll()) {
            assertThat(ids.add(expressionCase.id()))
                    .as("duplicate expression case id in %s", expressionCase.path())
                    .isTrue();
        }
    }

    @Test
    @DisplayName("initial corpus covers every controlled coverage tag")
    void initialCorpusCoversEveryControlledCoverageTag() {
        Set<CoverageTag> coveredTags = new HashSet<>();
        for (ExpressionCase expressionCase : ExpressionCaseLoader.loadAll()) {
            coveredTags.addAll(expressionCase.coverage());
        }

        assertThat(coveredTags).containsExactlyInAnyOrder(CoverageTag.values());
    }

    @Test
    @DisplayName("executable corpus diagnostics use stable codes")
    void executableCorpusDiagnosticsUseStableCodes() {
        for (ExpressionCase expressionCase : ExpressionCaseLoader.loadAll()) {
            if ((expressionCase.phase() != CasePhase.SEMANTIC && expressionCase.phase() != CasePhase.RUNTIME)
                    || !(expressionCase.expectedOutcome() instanceof ExpectedDiagnostic expected)) {
                continue;
            }

            assertThat(expected.code())
                    .as("%s", expressionCase.path())
                    .isNotEqualTo("TBD");
        }
    }

    @Test
    @DisplayName("corpus environments follow the Etapa 3.5 contract")
    void corpusEnvironmentsFollowEtapa35Contract() {
        for (ExpressionCase expressionCase : ExpressionCaseLoader.loadAll()) {
            JsonNode environment = expressionCase.root().get("environment");
            if (environment == null) {
                continue;
            }

            Set<String> environmentFields = new HashSet<>();
            environment.fieldNames().forEachRemaining(environmentFields::add);
            assertThat(environmentFields)
                    .as("%s", expressionCase.path())
                    .doesNotContainAnyElementsOf(REMOVED_ENVIRONMENT_FIELDS);
            assertNonNegativeInteger(environment, "maxMaterializedSize", expressionCase);
            assertNonNegativeInteger(environment, "maxFactorialInput", expressionCase);
            assertExternalSymbolsFollowCurrentContract(environment.get("symbols"), expressionCase);
            JsonNode inputs = expressionCase.root().get("inputs");
            if (inputs != null) {
                assertThat(inputs.isObject()).as("%s inputs", expressionCase.path()).isTrue();
            }
            assertThat(containsNull(inputs))
                    .as("%s", expressionCase.path())
                    .isFalse();
        }
    }

    private static void assertNonNegativeInteger(JsonNode environment, String field, ExpressionCase expressionCase) {
        JsonNode value = environment.get(field);
        assertThat(value).as("%s field %s", expressionCase.path(), field).isNotNull();
        assertThat(value.isIntegralNumber()).as("%s field %s", expressionCase.path(), field).isTrue();
        assertThat(value.canConvertToInt()).as("%s field %s", expressionCase.path(), field).isTrue();
        assertThat(value.intValue()).as("%s field %s", expressionCase.path(), field).isNotNegative();
    }

    private static void assertExternalSymbolsFollowCurrentContract(JsonNode symbols, ExpressionCase expressionCase) {
        if (symbols == null) {
            return;
        }
        assertThat(symbols.isObject()).as("%s symbols", expressionCase.path()).isTrue();
        symbols.properties().forEach(entry -> {
            JsonNode symbol = entry.getValue();
            assertThat(symbol.isObject()).as("%s symbol %s", expressionCase.path(), entry.getKey()).isTrue();
            assertThat(symbol.hasNonNull("default"))
                    .as("%s symbol %s", expressionCase.path(), entry.getKey())
                    .isTrue();
            assertThat(symbol.hasNonNull("overwritePolicy"))
                    .as("%s symbol %s", expressionCase.path(), entry.getKey())
                    .isTrue();
            JsonNode overwritePolicy = symbol.get("overwritePolicy");
            assertThat(overwritePolicy.isTextual())
                    .as("%s symbol %s", expressionCase.path(), entry.getKey())
                    .isTrue();
            assertThat(overwritePolicy.textValue())
                    .as("%s symbol %s", expressionCase.path(), entry.getKey())
                    .isIn(
                            ExternalSymbolOverwritePolicy.FIXED.name(),
                            ExternalSymbolOverwritePolicy.OVERRIDABLE.name());
            assertThat(containsNull(symbol.get("default")))
                    .as("%s symbol %s", expressionCase.path(), entry.getKey())
                    .isFalse();
        });
    }

    private static boolean containsNull(JsonNode node) {
        if (node == null) {
            return false;
        }
        if (node.isNull()) {
            return true;
        }
        for (JsonNode child : node) {
            if (containsNull(child)) {
                return true;
            }
        }
        return false;
    }
}
