package com.runestone.expeval_mk3.corpus;

sealed interface ExpectedOutcome
        permits ExpectedDiagnostics, ExpectedResult, ExpectedRuntimeError, ExpectedRuntimeDiagnostic, NoExpectedOutcome {
}
