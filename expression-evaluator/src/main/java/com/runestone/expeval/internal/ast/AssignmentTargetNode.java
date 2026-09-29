package com.runestone.expeval.internal.ast;

public sealed interface AssignmentTargetNode extends AstNode permits DestructuringAssignmentTargetNode,
        IdentifierAssignmentTargetNode {
}
