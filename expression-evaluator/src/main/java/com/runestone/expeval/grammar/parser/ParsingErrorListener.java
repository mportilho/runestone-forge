package com.runestone.expeval.grammar.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.Interval;

import java.util.BitSet;
import java.util.Locale;

/**
 * This class is used to throw an exception when a parsing error occurs.
 *
 * @author Marcelo Portilho
 */
public class ParsingErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg,
                            RecognitionException recognitionException) {
        throw new IllegalStateException(String.format(Locale.ROOT, "%s, on line %s and position %s", msg, line, charPositionInLine),
                recognitionException);
    }

    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        String text = recognizer.getContext().getStart().getInputStream().getText(new Interval(startIndex, stopIndex));
        throw new IllegalStateException(String.format("Ambiguity found near the segment '%s'", text));
    }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
    }

}
