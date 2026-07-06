package com.runestone.expeval_mk3.corpus;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class Etapa3CorpusGateTest {

    @Test
    @DisplayName("corpus includes representative Etapa 3 environment configurations")
    void corpusIncludesRepresentativeEtapa3EnvironmentConfigurations() {
        List<ExpressionCase> environmentCases = ExpressionCaseLoader.loadAll().stream()
                .filter(expressionCase -> expressionCase.id().startsWith("semantics.environment."))
                .toList();

        assertThat(environmentCases)
                .extracting(ExpressionCase::id)
                .contains(
                        "semantics.environment.standard-decimal.001",
                        "semantics.environment.fast-strict-java.001");
        assertThat(environmentCases)
                .allSatisfy(expressionCase -> {
                    assertThat(expressionCase.phase()).isEqualTo(CasePhase.SEMANTICS);
                    assertThat(expressionCase.kind()).isEqualTo(CaseKind.VALID);
                    assertThat(expressionCase.root().hasNonNull("environment"))
                            .as("environment field in %s", expressionCase.id())
                            .isTrue();
                });
    }

    @Test
    @DisplayName("standard decimal environment case declares default Etapa 3 policies")
    void standardDecimalEnvironmentCaseDeclaresDefaultEtapa3Policies() {
        JsonNode standard = environment("semantics.environment.standard-decimal.001");

        assertThat(standard.path("numericMode").textValue()).isEqualTo("DECIMAL");
        assertThat(standard.path("zoneId").textValue()).isEqualTo("UTC");
        assertThat(standard.path("standardBuiltIns").booleanValue()).isTrue();
        assertThat(standard.path("symbols").has("amount")).isTrue();
        assertThat(standard.path("symbols").has("businessDate")).isTrue();
        assertThat(standard.path("functions")).isNotEmpty();
    }

    @Test
    @DisplayName("fast strict environment case declares Java type and custom policy configurations")
    void fastStrictEnvironmentCaseDeclaresJavaTypeAndCustomPolicyConfigurations() {
        JsonNode fastStrict = environment("semantics.environment.fast-strict-java.001");

        assertThat(fastStrict.path("numericMode").textValue()).isEqualTo("FAST");
        assertThat(fastStrict.path("zoneId").textValue()).isEqualTo("America/Sao_Paulo");
        assertThat(fastStrict.path("strictMode").booleanValue()).isTrue();
        assertThat(fastStrict.path("limits").path("maxCurrentItemDepth").intValue()).isPositive();
        assertThat(fastStrict.path("limits").path("materializationLimit").intValue()).isPositive();
        assertThat(fastStrict.path("mathContext").path("precision").intValue()).isPositive();
        assertThat(fastStrict.path("transcendentalMathContext").path("precision").intValue()).isPositive();
        assertThat(fastStrict.path("conversionProfile").textValue()).isEqualTo("strict-bank-profile-v1");
        assertThat(fastStrict.path("javaTypes")).isNotEmpty();
    }

    private static JsonNode environment(String id) {
        return ExpressionCaseLoader.loadAll().stream()
                .filter(expressionCase -> expressionCase.id().equals(id))
                .findFirst()
                .orElseThrow()
                .root()
                .get("environment");
    }
}
