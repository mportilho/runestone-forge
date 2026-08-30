package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.CalculationMemory;
import com.runestone.expeval_mk3.api.CompiledExpression;
import com.runestone.expeval_mk3.api.ExpressionEngine;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.internal.memory.CalculationMemorySchema;
import com.runestone.expeval_mk3.internal.memory.CalculationRecorder;
import com.runestone.expeval_mk3.internal.memory.DefaultCalculationMemory;
import com.runestone.expeval_mk3.internal.memory.VariableMemorySchema;
import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.runtime.CurrentTemporalExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;
import com.runestone.expeval_mk3.internal.runtime.FunctionCallExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.MemoizedExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.OracleRegisteredMethodExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.OracleRegisteredPropertyExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.RegisteredMethodExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.RegisteredPropertyExecutableNode;
import com.runestone.expeval_mk3.support.DeterministicObjectGraph;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;

/** Reproducible JOL report for issue #145's production plan and memory layouts. */
public final class CalculationMemoryProductionLayoutReport {

    private static final int SHARING_SAMPLE_SIZE = 32;

    private CalculationMemoryProductionLayoutReport() {
    }

    public static void main(String[] args) {
        System.setProperty("jol.magicFieldOffset", "true");
        printEnvironment();
        printShallowLayouts();
        printPlanFootprints();
        printMemoryFootprints();
        printSharingFootprints();
    }

    private static void printEnvironment() {
        System.out.println("== environment ==");
        System.out.println(VM.current().details());
        System.out.println("java.version=" + System.getProperty("java.version"));
        System.out.println("java.vm.name=" + System.getProperty("java.vm.name"));
        System.out.println("java.vm.version=" + System.getProperty("java.vm.version"));
        System.out.println("sun.arch.data.model=" + System.getProperty("sun.arch.data.model"));
        System.out.println("jvm.arguments=" + ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    private static void printShallowLayouts() {
        System.out.println("== shallow layouts ==");
        for (Class<?> type : new Class<?>[] {
                FunctionCallExecutableNode.class,
                RegisteredPropertyExecutableNode.class,
                OracleRegisteredPropertyExecutableNode.class,
                RegisteredMethodExecutableNode.class,
                OracleRegisteredMethodExecutableNode.class,
                CurrentTemporalExecutableNode.class,
                MemoizedExecutableNode.class,
                ExecutionScope.class,
                CalculationRecorder.class,
                CalculationMemorySchema.class,
                VariableMemorySchema.class,
                DefaultCalculationMemory.class
        }) {
            System.out.printf("%s,%d%n", type.getSimpleName(), ClassLayout.parseClass(type).instanceSize());
        }
        printPackagePrivateLayout(
                "com.runestone.expeval_mk3.internal.runtime.StaticCalculationConstantExecutableNode");
        printPackagePrivateLayout("com.runestone.expeval_mk3.internal.runtime.StaticCalculationGroup");
    }

    private static void printPlanFootprints() {
        System.out.println("== plan footprints ==");
        System.out.println("targetNodes,actualNodes,instances,bytes");
        for (int targetNodes : new int[] {10, 100, 1_000}) {
            ExecutionPlan plan = planWithNodeCount(targetNodes);
            long actualNodes = DeterministicObjectGraph.from(plan).objects().stream()
                    .filter(ExecutableNode.class::isInstance)
                    .count();
            GraphLayout layout = GraphLayout.parseInstance(plan);
            System.out.printf("%d,%d,%d,%d%n", targetNodes, actualNodes, layout.totalCount(), layout.totalSize());
        }
    }

    private static void printMemoryFootprints() {
        ExpressionEnvironment environment = environment();
        ExpressionEngine engine = ExpressionEngine.builder().build();
        CalculationMemory empty = engine.compileOrThrow("1 + 2", environment).asMath().computeWithMemory().memory();
        CalculationMemory dense = engine.compileOrThrow("mark(1) + mark(2) + mark(3)", environment)
                .asMath().computeWithMemory().memory();
        CalculationMemory prefix = engine.compileOrThrow("markBoolean(false) and markBoolean(true)", environment)
                .asLogical().computeWithMemory().memory();
        CalculationMemory gapped = engine.compileOrThrow(
                        "if markBoolean(false) then mark(1) else mark(2) endif", environment)
                .asMath().computeWithMemory().memory();

        System.out.println("== memory footprints ==");
        System.out.println("shape,variables,calculations,instances,bytes");
        printMemory("empty", empty);
        printMemory("dense", dense);
        printMemory("prefix", prefix);
        printMemory("gapped", gapped);
    }

    private static void printSharingFootprints() {
        ExpressionEnvironment environment = environment();
        CalculationMemory[] distinctPlanMemories = new CalculationMemory[SHARING_SAMPLE_SIZE];
        for (int index = 0; index < distinctPlanMemories.length; index++) {
            distinctPlanMemories[index] = ExpressionEngine.builder().build()
                    .compileOrThrow("mark(amount)", environment())
                    .asMath()
                    .computeWithMemory(Map.of("amount", BigDecimal.valueOf(index + 1)))
                    .memory();
        }

        CalculationMemory[] sharedPlanMemories = new CalculationMemory[SHARING_SAMPLE_SIZE];
        var sharedExpression = ExpressionEngine.builder().build()
                .compileOrThrow("mark(amount)", environment)
                .asMath();
        for (int index = 0; index < sharedPlanMemories.length; index++) {
            sharedPlanMemories[index] = sharedExpression.computeWithMemory(
                    Map.of("amount", BigDecimal.valueOf(index + 1))).memory();
        }

        GraphLayout distinct = GraphLayout.parseInstance((Object[]) distinctPlanMemories);
        GraphLayout shared = GraphLayout.parseInstance((Object[]) sharedPlanMemories);
        System.out.println("== key sharing ==");
        System.out.println("shape,count,instances,bytes");
        System.out.printf("one-memory-per-plan,%d,%d,%d%n",
                SHARING_SAMPLE_SIZE, distinct.totalCount(), distinct.totalSize());
        System.out.printf("many-memories-one-plan,%d,%d,%d%n",
                SHARING_SAMPLE_SIZE, shared.totalCount(), shared.totalSize());
    }

    private static ExecutionPlan planWithNodeCount(int targetNodes) {
        int terms = (targetNodes + 1) / 3;
        String joined = String.join(" + ", java.util.stream.IntStream.range(0, terms)
                .mapToObj(index -> "mark(" + (index + 1) + ")")
                .toList());
        String source = "-(-(" + joined + "))";
        CompiledExpression compiled = ExpressionEngine.builder().build().compileOrThrow(source, environment());
        ExecutionPlan plan = fieldValue(compiled, "plan", ExecutionPlan.class);
        long actualNodes = DeterministicObjectGraph.from(plan).objects().stream()
                .filter(ExecutableNode.class::isInstance)
                .count();
        if (actualNodes != targetNodes) {
            throw new IllegalStateException("expected " + targetNodes + " executable nodes but built " + actualNodes);
        }
        return plan;
    }

    private static ExpressionEnvironment environment() {
        return ExpressionEnvironment.builder()
                .externalSymbol("amount", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(LayoutFunctions.class, FunctionPurity.IMPURE)
                .build();
    }

    private static void printMemory(String shape, CalculationMemory memory) {
        GraphLayout layout = GraphLayout.parseInstance(memory);
        System.out.printf("%s,%d,%d,%d,%d%n", shape, memory.variableCount(), memory.calculationCount(),
                layout.totalCount(), layout.totalSize());
    }

    private static void printPackagePrivateLayout(String className) {
        try {
            Class<?> type = Class.forName(className);
            System.out.printf("%s,%d%n", type.getSimpleName(), ClassLayout.parseClass(type).instanceSize());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static <T> T fieldValue(Object target, String name, Class<T> type) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return type.cast(field.get(target));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static final class LayoutFunctions {
        private LayoutFunctions() {
        }

        public static BigDecimal mark(BigDecimal value) {
            return value;
        }

        public static boolean markBoolean(boolean value) {
            return value;
        }
    }
}
