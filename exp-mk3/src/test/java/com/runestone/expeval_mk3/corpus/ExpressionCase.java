package com.runestone.expeval_mk3.corpus;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;
import java.util.Set;

record ExpressionCase(
        String id,
        CasePhase phase,
        CaseKind kind,
        String source,
        Set<CoverageTag> coverage,
        ExpectedOutcome expectedOutcome,
        JsonNode root,
        Path path) {
}
