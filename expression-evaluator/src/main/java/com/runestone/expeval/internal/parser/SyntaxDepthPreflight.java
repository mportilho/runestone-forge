package com.runestone.expeval.internal.parser;

import com.runestone.expeval.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval.internal.diagnostics.ExpressionDiagnostics;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

import static com.runestone.expeval.internal.grammar.ExpressionEvaluatorLexer.*;

/**
 * Iterative, conservative projection, before either recursive parser pass. Within an
 * expression segment every operator adds one level, irrespective of precedence.
 * Sibling arguments/statements/branches take the maximum, rather than accumulating.
 * The leaf has depth one; delimiters and classic conditionals each add a level.
 */
final class SyntaxDepthPreflight {

    private SyntaxDepthPreflight() {
    }

    static void check(List<Token> tokens, int limit, AntlrSourcePositions positions) {
        List<Frame> frames = new ArrayList<>();
        frames.add(new Frame(0, 0));
        int previous = 0;
        boolean operandExpected = true;
        boolean functionalConditional = false;
        for (int index = 0; index < tokens.size(); index++) {
            Token token = tokens.get(index);
            int type = token.getType();
            if (token.getChannel() != Token.DEFAULT_CHANNEL || type == Token.EOF) {
                continue;
            }
            Frame current = frames.getLast();
            boolean member = (previous == PERIOD || previous == SAFE_NAV) && memberName(type);
            if (!member) {
                switch (type) {
                    case LPAREN, LBRACKET -> {
                        frames.add(new Frame(type, functionalConditional ? 1 : 0));
                        functionalConditional = false;
                    }
                    case IF -> {
                        // Functional IF has a top-level argument separator inside its
                        // parentheses. A classic condition may continue after a group.
                        functionalConditional = functionalIf(tokens, index + 1);
                        if (!functionalConditional) {
                            frames.add(new Frame(IF, 1));
                        }
                    }
                    case RPAREN, RBRACKET, ENDIF -> {
                        int opener = type == RPAREN ? LPAREN : type == RBRACKET ? LBRACKET : IF;
                        if (frames.size() > 1 && current.opener == opener) {
                            frames.removeLast();
                            frames.getLast().childDepth = Math.max(frames.getLast().childDepth, current.depth(0));
                        }
                    }
                    case COMMA, SEMI, THEN, ELSE, ELSEIF, ASSIGN -> current.nextSegment();
                    case PLUS, MINUS, MULT, DIV, MODULO, ROOT, EXPONENTIATION,
                         NOT, AND, OR, XOR, XNOR, NAND, NOR,
                         CONCAT, GT, GE, LT, LE, EQ, NEQ, IN, NIN, BETWEEN,
                         REGEX_MATCH, REGEX_NOT_MATCH, ARROW -> current.operators++;
                    case NULLCOALESCE -> current.coalesce = 1;
                    case PERIOD, SAFE_NAV -> current.navigation = 1;
                    case PERCENT -> current.postfix = 1;
                    case EXCLAMATION -> {
                        if (operandExpected) {
                            current.operators++;
                        } else {
                            current.postfix = 1;
                        }
                    }
                    default -> { }
                }
            }
            int depth = 0;
            for (int frameIndex = frames.size() - 1; frameIndex >= 0; frameIndex--) {
                depth = frames.get(frameIndex).depth(depth);
            }
            if (depth > limit) {
                throw new CompilationLimitException(ExpressionDiagnostics.create(
                        DiagnosticCode.COMPILE_SYNTAX_DEPTH_EXCEEDED,
                        "Projected syntax depth exceeds the compilation limit", positions.span(token)));
            }
            operandExpected = !member && switch (type) {
                case IDENTIFIER, AT, INT, FLOAT, STRING, DATE, TIME, DATETIME,
                     TRUE, FALSE, NOW_DATE, NOW_TIME, NOW_DATETIME,
                     RPAREN, RBRACKET, ENDIF, PERCENT -> false;
                case EXCLAMATION -> operandExpected;
                default -> true;
            };
            previous = type;
        }
    }

    private static boolean memberName(int type) {
        return switch (type) {
            case IDENTIFIER, IF, THEN, ELSE, ELSEIF, ENDIF, AND, OR, XOR, XNOR,
                 NAND, NOR, TRUE, FALSE, IN, NIN, NOT_KW, BETWEEN, MODULO,
                 ROOT, NOW_DATE, NOW_TIME, NOW_DATETIME -> true;
            default -> false;
        };
    }

    private static boolean functionalIf(List<Token> tokens, int index) {
        while (index < tokens.size() && tokens.get(index).getChannel() != Token.DEFAULT_CHANNEL) {
            index++;
        }
        if (index == tokens.size() || tokens.get(index).getType() != LPAREN) {
            return false;
        }
        int nesting = 0;
        for (; index < tokens.size(); index++) {
            int type = tokens.get(index).getType();
            if (type == LPAREN || type == LBRACKET) {
                nesting++;
            } else if ((type == RPAREN || type == RBRACKET) && --nesting == 0) {
                return false;
            } else if (nesting == 1 && (type == COMMA || type == SEMI)) {
                return true;
            }
        }
        return false;
    }

    private static final class Frame {
        private final int opener;
        private final int branchDepth;
        private int operators;
        private int childDepth;
        private int siblingDepth;
        private int coalesce;
        private int navigation;
        private int postfix;

        private Frame(int opener, int branchDepth) {
            this.opener = opener;
            this.branchDepth = branchDepth;
        }

        private int depth(int activeChildDepth) {
            return Math.max(siblingDepth, 1 + branchDepth + operators + coalesce + navigation + postfix
                    + Math.max(childDepth, activeChildDepth));
        }

        private void nextSegment() {
            siblingDepth = depth(0);
            operators = 0;
            childDepth = 0;
            coalesce = 0;
            navigation = 0;
            postfix = 0;
        }
    }
}
