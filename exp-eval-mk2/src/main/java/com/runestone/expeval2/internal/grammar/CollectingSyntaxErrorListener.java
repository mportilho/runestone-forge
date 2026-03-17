package com.runestone.expeval2.internal.grammar;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

final class CollectingSyntaxErrorListener extends BaseErrorListener {

    private final List<SyntaxError> errors = new ArrayList<>();

    @Override
    public void syntaxError(
        Recognizer<?, ?> recognizer,
        Object offendingSymbol,
        int line,
        int charPositionInLine,
        String msg,
        RecognitionException e
    ) {
        errors.add(new SyntaxError(line, charPositionInLine, msg));
    }

    boolean hasErrors() {
        return !errors.isEmpty();
    }

    List<SyntaxError> errors() {
        return List.copyOf(errors);
    }
}
