package com.runestone.expeval_mk3.api;

import java.util.List;

/**
 * Aggregated, deterministically ordered report of every function-provider configuration problem
 * discovered while building an {@link ExpressionEnvironment}. An invalid provider fails the whole
 * build; this exception carries every independently discoverable problem rather than only the
 * first one encountered.
 */
public final class EnvironmentConfigurationException extends IllegalArgumentException {

    private final List<ProviderConfigurationProblem> problems;

    private EnvironmentConfigurationException(String message, Throwable cause, List<ProviderConfigurationProblem> problems) {
        super(message, cause);
        this.problems = problems;
    }

    static EnvironmentConfigurationException of(List<ProviderConfigurationProblem> problems) {
        List<ProviderConfigurationProblem> sorted = problems.stream()
                .sorted(ProviderConfigurationProblem.ORDER)
                .toList();
        String message = "invalid function provider configuration: " + sorted.stream()
                .map(ProviderConfigurationProblem::message)
                .distinct()
                .reduce((first, second) -> first + "; " + second)
                .orElseThrow();
        EnvironmentConfigurationException exception = new EnvironmentConfigurationException(
                message, sorted.getFirst().cause(), sorted);
        for (int index = 1; index < sorted.size(); index++) {
            RuntimeException cause = sorted.get(index).cause();
            if (cause != null) {
                exception.addSuppressed(cause);
            }
        }
        return exception;
    }

    /**
     * Every independently discoverable problem, sorted by provider exposure type, Java method
     * signature, and failure category, regardless of reflection or registration order.
     */
    public List<ProviderConfigurationProblem> problems() {
        return problems;
    }
}
