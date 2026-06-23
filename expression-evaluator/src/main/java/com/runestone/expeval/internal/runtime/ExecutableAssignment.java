package com.runestone.expeval.internal.runtime;

sealed interface ExecutableAssignment permits ExecutableSimpleAssignment, ExecutableDestructuringAssignment {
}
