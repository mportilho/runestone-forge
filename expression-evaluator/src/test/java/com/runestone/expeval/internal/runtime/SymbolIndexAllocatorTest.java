package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.ast.ExpressionFileNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.ast.SourceSpan;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SymbolIndexAllocatorTest {

    @Test
    void assignsDeterministicAlphabeticalIndicesBySymbolKind() {
        SymbolRef internalB = new SymbolRef("b", SymbolKind.INTERNAL);
        SymbolRef internalA = new SymbolRef("a", SymbolKind.INTERNAL);
        SymbolRef externalY = new SymbolRef("y", SymbolKind.EXTERNAL);
        SymbolRef externalX = new SymbolRef("x", SymbolKind.EXTERNAL);

        SemanticModel model = semanticModel(
                Map.of("b", internalB, "a", internalA),
                Map.of("y", externalY, "x", externalX));

        SymbolIndexAllocator.assignIndices(model);

        assertThat(internalA.index()).isZero();
        assertThat(internalB.index()).isEqualTo(1);
        assertThat(externalX.index()).isZero();
        assertThat(externalY.index()).isEqualTo(1);
    }

    private static SemanticModel semanticModel(Map<String, SymbolRef> internalSymbolsByName,
                                               Map<String, SymbolRef> externalSymbolsByName) {
        return new SemanticModel(
                new ExpressionFileNode(new NodeId("file"), new SourceSpan(0, 0, 1, 0, 1, 0), List.of(), null),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                new LinkedHashMap<>(externalSymbolsByName),
                new LinkedHashMap<>(internalSymbolsByName),
                Map.of(),
                List.of());
    }
}
