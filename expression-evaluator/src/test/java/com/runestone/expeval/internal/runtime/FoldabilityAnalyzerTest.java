package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.SourceSpan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FoldabilityAnalyzerTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 0, 1, 0, 1, 0);

    @Test
    void treatsLiteralsAsFoldableAndDynamicLiteralsAsRuntimeOnly() {
        assertThat(FoldabilityAnalyzer.isFoldableNode(new ExecutableLiteral("value"), false)).isTrue();
        assertThat(FoldabilityAnalyzer.isFoldableNode(
                new ExecutableDynamicLiteral(DynamicInstant.CURR_DATE), false)).isFalse();
    }

    @Test
    void currentElementIdentifierIsFoldableOnlyInsideFilterContext() {
        ExecutableIdentifier currentElement = new ExecutableIdentifier(
                new SymbolRef(LanguageSymbols.CURRENT_ELEMENT, SymbolKind.EXTERNAL), SPAN);

        assertThat(FoldabilityAnalyzer.isFoldableNode(currentElement, false)).isFalse();
        assertThat(FoldabilityAnalyzer.isFoldableNode(currentElement, true)).isTrue();
    }

    @Test
    void reflectiveMethodAndDeepScanAreFoldabilityBarriers() {
        assertThat(FoldabilityAnalyzer.isFoldableAccess(
                new ExecutablePropertyChain.ReflectiveMethodInvoke("size", List.of(), false))).isFalse();
        assertThat(FoldabilityAnalyzer.isFoldableAccess(
                new ExecutablePropertyChain.ExecutableDeepScan("price"))).isFalse();
    }

    @Test
    void filterPredicateAllowsCurrentElementReferences() {
        ExecutableIdentifier currentElement = new ExecutableIdentifier(
                new SymbolRef(LanguageSymbols.CURRENT_ELEMENT, SymbolKind.EXTERNAL), SPAN);

        assertThat(FoldabilityAnalyzer.isFoldableAccess(
                new ExecutablePropertyChain.ExecutableFilterPredicate(currentElement))).isTrue();
    }
}
