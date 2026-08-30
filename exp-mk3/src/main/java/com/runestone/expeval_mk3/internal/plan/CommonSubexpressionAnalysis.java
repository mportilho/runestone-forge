package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Map;

/**
 * The result of {@link CommonSubexpressionAnalyzer}: for every AST node that is one occurrence of an
 * eligible Subexpressao Comum Memoizada (issue #121), the frame slot appended past the semantic
 * {@code frameSize} that all of that subtree's occurrences share. Empty in Oraculo Sem Otimizacoes
 * mode, where no memoization ever happens.
 */
record CommonSubexpressionAnalysis(
        Map<NodeId, Integer> memoSlotsByNodeId,
        Map<NodeId, MemoizedOccurrence> occurrencesByNodeId,
        Map<NodeId, int[]> replaySlotsByCalculationNodeId,
        int memoSlotCount,
        int replaySlotCount) {

    static final CommonSubexpressionAnalysis EMPTY =
            new CommonSubexpressionAnalysis(Map.of(), Map.of(), Map.of(), 0, 0);

    CommonSubexpressionAnalysis {
        memoSlotsByNodeId = Map.copyOf(memoSlotsByNodeId);
        occurrencesByNodeId = Map.copyOf(occurrencesByNodeId);
        replaySlotsByCalculationNodeId = Map.copyOf(replaySlotsByCalculationNodeId);
    }
}
