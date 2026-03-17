package com.runestone.expeval2.internal.ast;

public sealed interface AssignmentNode extends Node permits DestructuringAssignmentNode, SimpleAssignmentNode {
}
