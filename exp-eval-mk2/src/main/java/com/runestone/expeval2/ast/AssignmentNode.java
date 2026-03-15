package com.runestone.expeval2.ast;

public sealed interface AssignmentNode extends Node permits DestructuringAssignmentNode, SimpleAssignmentNode {
}
