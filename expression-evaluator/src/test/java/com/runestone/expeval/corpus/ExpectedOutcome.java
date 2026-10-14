package com.runestone.expeval.corpus;

sealed interface ExpectedOutcome
        permits ExpectedDiagnostics, ExpectedResult, ExpectedRuntimeError, ExpectedRuntimeDiagnostic, NoExpectedOutcome {
}
