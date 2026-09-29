package com.runestone.expeval.api;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs the jqwik hardening property in the same constrained child JVM as the stress workload. */
final class Stage12HardeningProperties {

    @Test
    void tenThousandHardeningTrialsRunInAConstrainedTemurinChildJvm() throws Exception {
        Stage12StressSupport.runInConstrainedTemurinChild(
                Stage12HardeningProperties.class,
                Path.of("target", "stage12", "stage12-hardening-properties.log"));
    }

    public static void main(String[] arguments) {
        Stage12StressSupport.requireTemurin21();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherFactory.create().execute(LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(Stage12HardeningPropertyCheck.class))
                .build(), listener);
        var summary = listener.getSummary();
        summary.printFailuresTo(new PrintWriter(System.out, true));
        assertThat(summary.getTestsFoundCount()).isPositive();
        assertThat(summary.getTestsSkippedCount()).isZero();
        assertThat(summary.getTotalFailureCount()).isZero();
    }
}
