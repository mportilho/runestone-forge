package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.ReflectedFunctionImporter;
import com.runestone.expeval_mk3.api.ScalarType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class FunctionCatalogConstructionBenchmark {

    private List<FunctionDescriptor> customFunctions;
    private ReflectedFunctionImporter.ImportPlan importedFunctions;

    @Setup
    public void setup() throws NoSuchMethodException {
        List<FunctionDescriptor> descriptors = new ArrayList<>();
        for (Method method : CustomFunctions.class.getDeclaredMethods()) {
            descriptors.add(FunctionDescriptor.fromMethod(
                    method.getName(),
                    method,
                    List.of(ScalarType.STRING),
                    ScalarType.STRING,
                    FunctionPurity.PURE));
        }
        customFunctions = List.copyOf(descriptors);
        importedFunctions = ReflectedFunctionImporter.importAll(ImportedFunctions.class, FunctionPurity.PURE);
    }

    @Benchmark
    public ExpressionEnvironment successfulMixedCatalog() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder();
        for (FunctionDescriptor descriptor : customFunctions) {
            builder.function(descriptor);
        }
        return builder.functions(importedFunctions).build();
    }

    public static final class CustomFunctions {

        public static String custom0(String value) { return value; }
        public static String custom1(String value) { return value; }
        public static String custom2(String value) { return value; }
        public static String custom3(String value) { return value; }
        public static String custom4(String value) { return value; }
        public static String custom5(String value) { return value; }
        public static String custom6(String value) { return value; }
        public static String custom7(String value) { return value; }
    }

    public static final class ImportedFunctions {

        public static String imported0(String value) { return value; }
        public static String imported1(String value) { return value; }
        public static String imported2(String value) { return value; }
        public static String imported3(String value) { return value; }
        public static String imported4(String value) { return value; }
        public static String imported5(String value) { return value; }
        public static String imported6(String value) { return value; }
        public static String imported7(String value) { return value; }
    }
}
