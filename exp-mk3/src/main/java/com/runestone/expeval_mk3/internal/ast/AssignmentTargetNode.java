package com.runestone.expeval_mk3.internal.ast;

public sealed interface AssignmentTargetNode extends AstNode permits DestructuringAssignmentTargetNode,
        IdentifierAssignmentTargetNode {
}
