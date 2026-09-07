package com.runestone.expeval_mk3.internal.parser;

import com.runestone.expeval_mk3.api.SourceSpan;
import org.antlr.v4.runtime.Token;

import java.util.Objects;

/** Converts ANTLR code-point positions to the UTF-16 units used by the public API. */
public final class AntlrSourcePositions {

    private final int utf16Length;
    private final int[] codePointBoundaries;
    private final int[] lineStarts;
    private final int eofLine;
    private final int eofColumn;

    private AntlrSourcePositions(
            int utf16Length,
            int[] codePointBoundaries,
            int[] lineStarts,
            int eofLine,
            int eofColumn) {
        this.utf16Length = utf16Length;
        this.codePointBoundaries = codePointBoundaries;
        this.lineStarts = lineStarts;
        this.eofLine = eofLine;
        this.eofColumn = eofColumn;
    }

    static AntlrSourcePositions from(String source) {
        Objects.requireNonNull(source, "source");
        int lineCount = 1;
        int lastLineStart = 0;
        int supplementaryCount = 0;
        for (int index = 0; index < source.length(); index++) {
            char current = source.charAt(index);
            if (current == '\n') {
                lineCount++;
                lastLineStart = index + 1;
            } else if (Character.isHighSurrogate(current)
                    && index + 1 < source.length()
                    && Character.isLowSurrogate(source.charAt(index + 1))) {
                supplementaryCount++;
                index++;
            }
        }
        int codePointCount = source.length() - supplementaryCount;
        if (supplementaryCount == 0) {
            return new AntlrSourcePositions(
                    source.length(), null, null, lineCount, source.length() - lastLineStart + 1);
        }
        int[] boundaries = new int[codePointCount + 1];
        int[] starts = new int[lineCount];
        int codePointIndex = 0;
        int lineIndex = 1;
        for (int utf16Index = 0; utf16Index < source.length();) {
            boundaries[codePointIndex] = utf16Index;
            int codePoint = source.codePointAt(utf16Index);
            int width = Character.charCount(codePoint);
            utf16Index += width;
            codePointIndex++;
            if (codePoint == '\n') {
                starts[lineIndex++] = utf16Index;
            }
        }
        boundaries[codePointCount] = source.length();
        return new AntlrSourcePositions(
                source.length(), boundaries, starts, lineCount, source.length() - starts[lineCount - 1] + 1);
    }

    public SourceSpan span(Token token) {
        Objects.requireNonNull(token, "token");
        int start = offset(token.getStartIndex());
        int end = token.getType() == Token.EOF ? start : offset(token.getStopIndex() + 1);
        return new SourceSpan(start, Math.max(start, end), line(token), column(token, start));
    }

    public SourceSpan span(Token startToken, Token stopToken) {
        Objects.requireNonNull(startToken, "startToken");
        Objects.requireNonNull(stopToken, "stopToken");
        int start = offset(startToken.getStartIndex());
        int end = stopToken.getType() == Token.EOF ? start : offset(stopToken.getStopIndex() + 1);
        return new SourceSpan(start, Math.max(start, end), line(startToken), column(startToken, start));
    }

    public SourceSpan insertionSpan(Token token) {
        Objects.requireNonNull(token, "token");
        int offset = offset(token.getStartIndex());
        return new SourceSpan(offset, offset, line(token), column(token, offset));
    }

    public SourceSpan eofSpan() {
        return new SourceSpan(utf16Length, utf16Length, eofLine, eofColumn);
    }

    private int offset(int codePointOffset) {
        int boundedOffset = Math.max(0, codePointOffset);
        if (codePointBoundaries == null) {
            return Math.min(boundedOffset, utf16Length);
        }
        return codePointBoundaries[Math.min(boundedOffset, codePointBoundaries.length - 1)];
    }

    private int column(Token token, int utf16Offset) {
        int line = line(token);
        if (lineStarts != null && line <= lineStarts.length) {
            return utf16Offset - lineStarts[line - 1] + 1;
        }
        return Math.max(1, token.getCharPositionInLine() + 1);
    }

    private static int line(Token token) {
        return Math.max(1, token.getLine());
    }

}
