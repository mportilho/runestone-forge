package com.runestone.expeval_mk3.internal.runtime;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/** Test double counting how many times {@link #instant()} was consulted. */
public final class CountingClock extends Clock {

    private final Clock delegate;
    private final AtomicInteger callCount = new AtomicInteger();

    public CountingClock(Clock delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    public int callCount() {
        return callCount.get();
    }

    @Override
    public ZoneId getZone() {
        return delegate.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new CountingClock(delegate.withZone(zone));
    }

    @Override
    public Instant instant() {
        callCount.incrementAndGet();
        return delegate.instant();
    }
}
