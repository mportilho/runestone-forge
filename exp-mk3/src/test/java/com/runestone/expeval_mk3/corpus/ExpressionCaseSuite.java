package com.runestone.expeval_mk3.corpus;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

enum ExpressionCaseSuite {
    PARSER(expressionCase -> expressionCase.phase() == CasePhase.PARSER),
    SEMANTIC(expressionCase -> expressionCase.phase() == CasePhase.SEMANTIC),
    RUNTIME(expressionCase -> expressionCase.phase() == CasePhase.RUNTIME);

    private final Predicate<ExpressionCase> selector;

    ExpressionCaseSuite(Predicate<ExpressionCase> selector) {
        this.selector = selector;
    }

    boolean includes(ExpressionCase expressionCase) {
        return selector.test(expressionCase);
    }

    static List<ExpressionCaseSuite> matching(ExpressionCase expressionCase) {
        return Arrays.stream(values())
                .filter(suite -> suite.includes(expressionCase))
                .toList();
    }
}
