package com.runestone.dynafilter.core.statemachine;

import java.util.Objects;

public record StateTransition(String origin, String destination, String trigger) {

    public StateTransition {
        origin = Objects.requireNonNull(origin, "origin must not be null");
        destination = Objects.requireNonNull(destination, "destination must not be null");
        trigger = Objects.requireNonNull(trigger, "trigger must not be null");
    }
}
