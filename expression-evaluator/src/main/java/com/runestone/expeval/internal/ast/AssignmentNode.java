package com.runestone.expeval.internal.ast;

public sealed interface AssignmentNode extends Node permits DestructuringAssignmentNode, SimpleAssignmentNode {
}
