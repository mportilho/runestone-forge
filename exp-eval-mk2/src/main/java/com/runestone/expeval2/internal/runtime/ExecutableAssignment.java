package com.runestone.expeval2.internal.runtime;

sealed interface ExecutableAssignment permits ExecutableSimpleAssignment, ExecutableDestructuringAssignment {
}
