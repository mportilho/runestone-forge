package com.runestone.expeval_mk3.corpus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CorpusStructureTest {

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
}
