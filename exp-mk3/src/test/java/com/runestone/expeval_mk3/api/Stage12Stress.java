package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Opt-in sustained Stage 12 contention gate. It runs all resource-heavy work in a constrained Temurin
 * child JVM so the normal Maven process never needs the production safety envelope.
 */
final class Stage12Stress {

    private static final int VIRTUAL_TASKS = 10_000;
    private static final int PLATFORM_COMPILATIONS = 50_000;
    @Test
    void hardeningStressCompletesInAConstrainedTemurinChildJvm() throws Exception {
        Stage12StressSupport.runInConstrainedTemurinChild(
                Stage12Stress.class, Path.of("target", "stage12", "stage12-stress.log"));
    }

    public static void main(String[] arguments) throws Exception {
        Stage12StressSupport.requireTemurin21();
        CompilationResourceStress.runCeilingChecks();
        runSafeValueCeilings();
        runAdditionalHardeningScenarios();
        runSustainedContention();
        System.out.println("Stage 12 stress passed");
    }

    private static void runSafeValueCeilings() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder()
                .maxMaterializedSize(100_000)
                .maxTextLength(8_388_608)
                .maxNumericPrecision(10_000)
                .maxValueDepth(256)
                .build();
        ExpressionEngine engine = ExpressionEngine.builder().build();

        String maximumText = "x".repeat(limits.maxTextLength());
        ExpressionEnvironment textEnvironment = ExpressionEnvironment.builder().trustMode(ExpressionTrustMode.SAFE)
                .resourceLimits(limits)
                .externalSymbol("text", "x", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        ResultExpression text = successful(engine, "text", textEnvironment).asResult();
        assertThat(text.compute(Map.of("text", maximumText))).isEqualTo(maximumText);
        assertThatThrownBy(() -> text.compute(Map.of("text", maximumText + "x")))
                .isInstanceOf(ExpressionExecutionException.class);

        BigDecimal maximumPrecision = new BigDecimal("9".repeat(limits.maxNumericPrecision()));
        ExpressionEnvironment numberEnvironment = ExpressionEnvironment.builder().trustMode(ExpressionTrustMode.SAFE)
                .resourceLimits(limits)
                .externalSymbol("number", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        MathExpression number = successful(engine, "number", numberEnvironment).asMath();
        assertThat(number.compute(Map.of("number", maximumPrecision))).isEqualByComparingTo(maximumPrecision);
        assertThatThrownBy(() -> number.compute(Map.of("number", new BigDecimal("9".repeat(10_001)))))
                .isInstanceOf(ExpressionExecutionException.class);

        List<BigDecimal> maximumValues = Collections.nCopies(limits.maxMaterializedSize(), BigDecimal.ONE);
        ExpressionEnvironment valuesEnvironment = ExpressionEnvironment.builder().trustMode(ExpressionTrustMode.SAFE)
                .resourceLimits(limits)
                .externalSymbol("values", new CollectionType(ScalarType.NUMBER), maximumValues,
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        ResultExpression values = successful(engine, "values", valuesEnvironment).asResult();
        assertThat(values.compute()).isEqualTo(maximumValues);
        assertThatThrownBy(() -> values.compute(Map.of("values",
                Collections.nCopies(limits.maxMaterializedSize() + 1, BigDecimal.ONE))))
                .isInstanceOf(ExpressionExecutionException.class);

        Object nestedAtLimit = nestedValue(limits.maxValueDepth());
        ExpressionEnvironment nestedEnvironment = ExpressionEnvironment.builder().trustMode(ExpressionTrustMode.SAFE)
                .resourceLimits(limits)
                .externalSymbol("nested", nestedCollectionType(limits.maxValueDepth()), nestedAtLimit,
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        ResultExpression nested = successful(engine, "nested", nestedEnvironment).asResult();
        assertThat(nested.compute()).isEqualTo(nestedAtLimit);
        assertThatThrownBy(() -> nested.compute(Map.of("nested", List.of(nestedAtLimit))))
                .isInstanceOf(ExpressionExecutionException.class);
    }

    private static void runAdditionalHardeningScenarios() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder()
                .maxTextLength(8_388_608)
                .maxValueDepth(256)
                .build();
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment textEnvironment = ExpressionEnvironment.builder().trustMode(ExpressionTrustMode.SAFE)
                .resourceLimits(limits)
                .externalSymbol("text", "a", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        String adversarialText = "a".repeat(limits.maxTextLength() / 2) + "!";
        LogicalExpression regex = successful(engine, "text =~ \"(a+)+$\"", textEnvironment).asLogical();
        assertThat(regex.compute(Map.of("text", adversarialText))).isFalse();
        ResultExpression replacement = successful(engine, "replaceAll(text, \"a\", \"bb\")", textEnvironment).asResult();
        String expandedText = (String) replacement.compute(Map.of(
                "text", adversarialText.substring(0, adversarialText.length() - 1)));
        assertThat(expandedText).hasSize(limits.maxTextLength());
        assertThatThrownBy(() -> replacement.compute(Map.of("text", adversarialText)))
                .isInstanceOf(ExpressionExecutionException.class);

        ExpressionEnvironment cacheEnvironment = ExpressionEnvironment.builder()
                .externalSymbol("value", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        ExpressionEngine cacheEngine = ExpressionEngine.builder().build();
        for (int index = 0; index < 2_048; index++) {
            ExpressionCompilationResult result = cacheEngine.compile("value + " + index, cacheEnvironment);
            if (!(result instanceof ExpressionCompilationResult.Success)) {
                throw new AssertionError("cache stress compilation failed at source " + index);
            }
        }
    }

    private static Object nestedValue(int depth) {
        Object value = BigDecimal.ONE;
        for (int index = 0; index < depth; index++) {
            value = List.of(value);
        }
        return value;
    }

    private static ExpressionType nestedCollectionType(int depth) {
        ExpressionType type = ScalarType.NUMBER;
        for (int index = 0; index < depth; index++) {
            type = new CollectionType(type);
        }
        return type;
    }

    private static void runSustainedContention() throws Exception {
        StressProvider provider = new StressProvider();
        ExpressionEnvironment environment = environment(provider);
        RuntimeServices runtimeServices = RuntimeServices.withClock(Clock.systemUTC());
        Workload workload = new Workload(environment, runtimeServices, provider);

        runPlatformCompilations(workload);
        runVirtualPlanExecutions(workload);

        assertThat(workload.compilations.get() + workload.executions.get())
                .as("combined compilation/execution operations")
                .isGreaterThanOrEqualTo(100_000);
        assertThat(provider.memoInvocations()).as("one CSE provider invocation per reached numeric view")
                .isEqualTo(workload.numericExecutions.get());
    }

    private static void runPlatformCompilations(Workload workload) throws Exception {
        int parallelism = Math.min(32, Math.max(4, Runtime.getRuntime().availableProcessors() * 2));
        CountDownLatch ready = new CountDownLatch(parallelism);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>(parallelism);
        try (ExecutorService executor = Executors.newFixedThreadPool(parallelism)) {
            for (int worker = 0; worker < parallelism; worker++) {
                int baseAttempts = PLATFORM_COMPILATIONS / parallelism;
                int remainder = PLATFORM_COMPILATIONS % parallelism;
                int attempts = baseAttempts + (worker < remainder ? 1 : 0);
                int workerOffset = worker * baseAttempts + Math.min(worker, remainder);
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(30, TimeUnit.SECONDS)) {
                        throw new AssertionError("platform stress start barrier timed out");
                    }
                    for (int attempt = 0; attempt < attempts; attempt++) {
                        workload.compileAndExecute(workerOffset + attempt);
                    }
                    return null;
                }));
            }
            if (!ready.await(30, TimeUnit.SECONDS)) {
                throw new AssertionError("platform stress workers did not become ready");
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(60, TimeUnit.SECONDS);
            }
        }
    }

    private static void runVirtualPlanExecutions(Workload workload) throws Exception {
        CountDownLatch ready = new CountDownLatch(VIRTUAL_TASKS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>(VIRTUAL_TASKS);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int task = 0; task < VIRTUAL_TASKS; task++) {
                int taskIndex = task;
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(30, TimeUnit.SECONDS)) {
                        throw new AssertionError("virtual stress start barrier timed out");
                    }
                    workload.executeSharedPlan(taskIndex);
                    return null;
                }));
            }
            if (!ready.await(30, TimeUnit.SECONDS)) {
                throw new AssertionError("virtual stress tasks did not become ready");
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(60, TimeUnit.SECONDS);
            }
        }
    }

    private static ExpressionEnvironment environment(StressProvider provider) {
        return ExpressionEnvironment.builder()
                .trustMode(ExpressionTrustMode.SAFE)
                .externalSymbol("factor", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("target", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("divisor", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("values", new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE, BigDecimal.TWO, BigDecimal.valueOf(3)),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(provider, FunctionPurity.PURE)
                .build();
    }

    private static CompiledExpression successful(ExpressionEngine engine, String source, ExpressionEnvironment environment) {
        ExpressionCompilationResult result = engine.compile(source, environment);
        if (result instanceof ExpressionCompilationResult.Success success) {
            return success.compiledExpression();
        }
        throw new AssertionError("expected compilation success for " + source + ": "
                + ((ExpressionCompilationResult.Failure) result).diagnostics());
    }

    public static final class StressProvider {
        private final AtomicLong memoInvocations = new AtomicLong();

        public BigDecimal memo(BigDecimal value) {
            memoInvocations.incrementAndGet();
            return value;
        }

        public BigDecimal identity(BigDecimal value) {
            return value;
        }

        long memoInvocations() {
            return memoInvocations.get();
        }
    }

    private static final class Workload {
        private static final String NUMERIC_SOURCE = "cse := memo(factor) + memo(factor); "
                + "mapped := values.map(@ -> identity(@) + factor).sum(); cse + mapped";
        private static final String BOOLEAN_SOURCE = "moment := currDateTime; "
                + "matched := values.any(@ -> identity(@) = target); matched and moment = moment";
        private static final String RUNTIME_FAILURE_SOURCE = "value := 10 / divisor; value";
        private static final String PARSE_FAILURE_SOURCE = "1 +";
        private static final String LEXICAL_FAILURE_SOURCE = "1 #";
        private static final String SEMANTIC_FAILURE_SOURCE = "missing";

        private final ExpressionEnvironment environment;
        private final RuntimeServices runtimeServices;
        private final AtomicLong compilations = new AtomicLong();
        private final AtomicLong executions = new AtomicLong();
        private final AtomicLong numericExecutions = new AtomicLong();
        private final CompiledExpression numericPlan;
        private final CompiledExpression booleanPlan;
        private final CompiledExpression runtimeFailurePlan;

        private Workload(ExpressionEnvironment environment, RuntimeServices runtimeServices, StressProvider provider) {
            this.environment = environment;
            this.runtimeServices = runtimeServices;
            ExpressionEngine engine = ExpressionEngine.builder().clock(runtimeServices.clock()).build();
            this.numericPlan = successful(engine, NUMERIC_SOURCE, environment);
            this.booleanPlan = successful(engine, BOOLEAN_SOURCE, environment);
            this.runtimeFailurePlan = successful(engine, RUNTIME_FAILURE_SOURCE, environment);
            if (provider.memoInvocations() != 0) {
                throw new AssertionError("compilation must not invoke the stress provider");
            }
        }

        private void compileAndExecute(int operation) {
            int scenario = Math.floorMod(operation, 20);
            if (scenario == 0) {
                assertCompilationFailure(PARSE_FAILURE_SOURCE);
                return;
            }
            if (scenario == 1) {
                assertCompilationFailure(LEXICAL_FAILURE_SOURCE);
                return;
            }
            if (scenario == 2) {
                assertCompilationFailure(SEMANTIC_FAILURE_SOURCE);
                return;
            }
            if (scenario == 3) {
                assertRuntimeFailure();
                return;
            }

            boolean numeric = scenario % 2 == 0;
            CompiledExpression compiled = compile(numeric ? NUMERIC_SOURCE : BOOLEAN_SOURCE);
            executeView(compiled, numeric, operation);
        }

        private CompiledExpression compile(String source) {
            compilations.incrementAndGet();
            ExpressionCompilationResult result = CompilationPipeline.compile(source, environment, runtimeServices);
            if (result instanceof ExpressionCompilationResult.Success success) {
                return success.compiledExpression();
            }
            throw new AssertionError("expected compilation success for " + source);
        }

        private void assertCompilationFailure(String source) {
            compilations.incrementAndGet();
            ExpressionCompilationResult result = CompilationPipeline.compile(source, environment, runtimeServices);
            if (!(result instanceof ExpressionCompilationResult.Failure)) {
                throw new AssertionError("expected compilation failure for " + source);
            }
        }

        private void assertRuntimeFailure() {
            assertThatThrownBy(() -> runtimeFailurePlan.asResult().compute(Map.of("divisor", BigDecimal.ZERO)))
                    .isInstanceOf(ExpressionExecutionException.class);
            executions.incrementAndGet();
        }

        private void executeSharedPlan(int operation) {
            boolean numeric = operation % 2 == 0;
            executeView(numeric ? numericPlan : booleanPlan, numeric, operation);
        }

        private void executeView(CompiledExpression compiled, boolean numeric, int operation) {
            BigDecimal factor = BigDecimal.valueOf(Math.floorMod(operation, 3) + 1L);
            Map<String, Object> inputs = Map.of(
                    "factor", factor,
                    "target", BigDecimal.valueOf(Math.floorMod(operation, 3) + 1L),
                    "values", List.of(BigDecimal.ONE, BigDecimal.TWO, BigDecimal.valueOf(3)));
            ViewKind view = ViewKind.fromOperation(operation);
            if (numeric) {
                executeNumericView(compiled, view, factor, inputs);
            } else {
                executeBooleanView(compiled, view, inputs);
            }
        }

        private void executeNumericView(CompiledExpression compiled, ViewKind view, BigDecimal factor,
                                        Map<String, Object> inputs) {
            BigDecimal expected = BigDecimal.valueOf(6).add(factor.multiply(BigDecimal.valueOf(5)));
            switch (view) {
                case RESULT -> assertThat(compiled.asResult().compute(inputs)).isEqualTo(expected);
                case RESULT_WITH_MEMORY -> assertThat(compiled.asResult().computeWithMemory(inputs).result())
                        .isEqualTo(expected);
                case TYPED -> assertThat(compiled.asMath().compute(inputs)).isEqualByComparingTo(expected);
                case TYPED_WITH_MEMORY -> assertThat(compiled.asMath().computeWithMemory(inputs).result())
                        .isEqualByComparingTo(expected);
                case ASSIGNMENTS -> assertThat(compiled.asAssignments().compute(inputs))
                        .containsEntry("cse", factor.multiply(BigDecimal.TWO));
                case ASSIGNMENTS_WITH_MEMORY -> assertThat(compiled.asAssignments().computeWithMemory(inputs).result())
                        .containsEntry("mapped", BigDecimal.valueOf(6).add(factor.multiply(BigDecimal.valueOf(3))));
            }
            numericExecutions.incrementAndGet();
            executions.incrementAndGet();
        }

        private void executeBooleanView(CompiledExpression compiled, ViewKind view, Map<String, Object> inputs) {
            switch (view) {
                case RESULT -> assertThat(compiled.asResult().compute(inputs)).isEqualTo(true);
                case RESULT_WITH_MEMORY -> assertThat(compiled.asResult().computeWithMemory(inputs).result()).isEqualTo(true);
                case TYPED -> assertThat(compiled.asLogical().compute(inputs)).isTrue();
                case TYPED_WITH_MEMORY -> assertThat(compiled.asLogical().computeWithMemory(inputs).result()).isTrue();
                case ASSIGNMENTS -> assertThat(compiled.asAssignments().compute(inputs)).containsEntry("matched", true);
                case ASSIGNMENTS_WITH_MEMORY -> assertThat(compiled.asAssignments().computeWithMemory(inputs).result())
                        .containsKey("moment");
            }
            executions.incrementAndGet();
        }
    }

    private enum ViewKind {
        RESULT,
        RESULT_WITH_MEMORY,
        TYPED,
        TYPED_WITH_MEMORY,
        ASSIGNMENTS,
        ASSIGNMENTS_WITH_MEMORY;

        private static final ViewKind[] VALUES = values();

        private static ViewKind fromOperation(int operation) {
            return VALUES[Math.floorMod(operation, VALUES.length)];
        }
    }
}
