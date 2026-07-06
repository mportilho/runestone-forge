package com.runestone.expeval_mk3.internal.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

final class AstNodeIdAssigner {

    private int nextId;

    ExpressionFileNode assign(ExpressionFileNode file) {
        Objects.requireNonNull(file, "file");
        nextId = 0;
        NodeId fileId = next();
        List<AssignmentNode> assignments = new ArrayList<>(file.assignments().size());
        for (AssignmentNode assignment : file.assignments()) {
            assignments.add(assignAssignment(assignment));
        }
        Optional<ExpressionNode> resultExpression = file.resultExpression().map(this::assignExpression);
        return new ExpressionFileNode(fileId, file.sourceSpan(), assignments, resultExpression);
    }

    private AssignmentNode assignAssignment(AssignmentNode assignment) {
        NodeId assignmentId = next();
        AssignmentTargetNode target = assignTarget(assignment.target());
        ExpressionNode expression = assignExpression(assignment.expression());
        return new AssignmentNode(assignmentId, assignment.sourceSpan(), target, expression);
    }

    private AssignmentTargetNode assignTarget(AssignmentTargetNode target) {
        if (target instanceof IdentifierAssignmentTargetNode identifier) {
            return new IdentifierAssignmentTargetNode(next(), identifier.sourceSpan(), identifier.name());
        }
        throw new IllegalArgumentException("Unsupported assignment target node: " + target.getClass().getName());
    }

    private ExpressionNode assignExpression(ExpressionNode expression) {
        return switch (expression) {
            case CurrentTemporalValueNode currentTemporalValue -> new CurrentTemporalValueNode(
                    next(),
                    currentTemporalValue.sourceSpan(),
                    currentTemporalValue.kind());
            case IdentifierNode identifier -> new IdentifierNode(next(), identifier.sourceSpan(), identifier.name());
            case LiteralNode literal -> new LiteralNode(next(), literal.sourceSpan(), literal.value());
        };
    }

    private NodeId next() {
        return new NodeId(nextId++);
    }
}
