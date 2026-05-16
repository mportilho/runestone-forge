package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;

import java.util.Map;

public interface StatementGenerator {

    StatementWrapper generateStatements(AnnotationStatementInput filterInputs, Map<String, Object> filterParameters);
}
