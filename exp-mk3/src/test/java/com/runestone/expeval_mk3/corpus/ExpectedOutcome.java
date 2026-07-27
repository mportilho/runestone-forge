package com.runestone.expeval_mk3.corpus;

sealed interface ExpectedOutcome permits ExpectedDiagnostic, ExpectedResult, ExpectedRuntimeError, NoExpectedOutcome {
}
