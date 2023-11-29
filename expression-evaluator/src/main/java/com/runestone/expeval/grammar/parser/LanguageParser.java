package com.runestone.expeval.grammar.parser;

import com.runestone.expeval.grammar.language.ExpressionEvaluatorParser;

/**
 * Interface for parsing a textual expression based on the language grammar.
 */
public interface LanguageParser {

    LanguageData parse(ExpressionEvaluatorParser.StartContext startContext);

}
