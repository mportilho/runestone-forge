package com.runestone.dynafilter.core.statemachine;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record StateMachineDefinition(
        TechnicalStateMachine name,
        String initialState,
        Set<String> terminalStates,
        List<StateTransition> transitions
) {

    public StateMachineDefinition {
        name = Objects.requireNonNull(name, "name must not be null");
        initialState = Objects.requireNonNull(initialState, "initialState must not be null");
        terminalStates = terminalStates == null ? Set.of() : Set.copyOf(terminalStates);
        transitions = transitions == null ? List.of() : List.copyOf(transitions);
    }

    public boolean hasTransition(String origin, String destination) {
        return transitions.stream()
                .anyMatch(transition -> transition.origin().equals(origin) && transition.destination().equals(destination));
    }
}
