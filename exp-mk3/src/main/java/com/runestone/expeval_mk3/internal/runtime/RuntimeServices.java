package com.runestone.expeval_mk3.internal.runtime;

import java.time.Clock;
import java.util.Objects;

/**
 * Runtime-only services associated with a compiled expression, kept out of the Plano Imutavel and out of
 * the Ambiente de Expressao identity. Public compilation uses {@link #systemDefault()}; {@link #withClock}
 * is the internal seam that lets tests inject a fixed or counting {@link Clock}.
 */
public final class RuntimeServices {

    private static final RuntimeServices DEFAULT = new RuntimeServices(Clock.systemUTC());

    private final Clock clock;

    private RuntimeServices(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public static RuntimeServices systemDefault() {
        return DEFAULT;
    }

    public static RuntimeServices withClock(Clock clock) {
        return new RuntimeServices(clock);
    }

    public Clock clock() {
        return clock;
    }
}
