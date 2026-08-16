package com.runestone.expeval_mk3.internal.parser;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs an {@link ExpressionParser}'s ATN/DFA warm-up exactly once per process, no matter how many
 * threads race to trigger it. {@code ExpressionEngine} calls {@link #ensureWarmedUp()} when it builds
 * its first instance, default or isolated; later engines observe the warmed-up state and repeat no work.
 *
 * <p>{@link #shared()} warms up a throwaway {@link ExpressionParser} instead of reaching into the
 * pipeline's own parser instance: ANTLR generates {@code decisionToDFA} and the {@code ATN} as
 * {@code static} members of the lexer/parser classes, so every instance shares the same cache and
 * priming any one of them benefits all of them, including whichever instance the pipeline uses.
 *
 * <p>A failing warm-up source is an initialization bug, not a diagnostic on user input: the underlying
 * {@link ExpressionParser#warmUp()} failure (an {@link IllegalStateException}) propagates as-is and the
 * gate stays open for a later retry, since a failed attempt never reflects real work having happened.
 */
public final class ParserWarmUp {

    private static final ParserWarmUp SHARED = new ParserWarmUp(new ExpressionParser()::warmUp);

    private final Runnable warmUpAction;
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final Object lock = new Object();

    ParserWarmUp(Runnable warmUpAction) {
        this.warmUpAction = warmUpAction;
    }

    public static ParserWarmUp shared() {
        return SHARED;
    }

    public void ensureWarmedUp() {
        if (completed.get()) {
            return;
        }
        synchronized (lock) {
            if (completed.get()) {
                return;
            }
            warmUpAction.run();
            completed.set(true);
        }
    }
}
