package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

record ExpressionFileNode(
        NodeId id,
        SourceSpan sourceSpan,
        List<AssignmentNode> assignments,
        Optional<ExpressionNode> resultExpression) implements AstNode, ExpressionSemanticTree {

    ExpressionFileNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(assignments, "assignments");
        assignments = List.copyOf(assignments);
        Objects.requireNonNull(resultExpression, "resultExpression");
    }

    @Override
    public SourceSpan sourceSpan() {
        return sourceSpan;
    }

    @Override
    public boolean isEmptyFile() {
        return assignments.isEmpty() && resultExpression.isEmpty();
    }

    @Override
    public List<AssignedSymbolSource> assignedSymbols() {
        if (assignments.isEmpty()) {
            return List.of();
        }
        List<AssignedSymbolSource> symbols = new ArrayList<>();
        for (AssignmentNode assignment : assignments) {
            collectAssignedSymbols(assignment.target(), symbols);
        }
        return List.copyOf(symbols);
    }

    private static void collectAssignedSymbols(AssignmentTargetNode target, List<AssignedSymbolSource> symbols) {
        switch (target) {
            case IdentifierAssignmentTargetNode identifier -> symbols.add(new AssignedSymbolSource(
                    identifier.name(),
                    identifier.sourceSpan()));
            case DestructuringAssignmentTargetNode destructuring -> {
                for (IdentifierAssignmentTargetNode element : destructuring.elements()) {
                    symbols.add(new AssignedSymbolSource(element.name(), element.sourceSpan()));
                }
            }
        }
    }
}
