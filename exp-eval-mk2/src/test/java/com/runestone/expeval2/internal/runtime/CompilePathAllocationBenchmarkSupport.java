package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Supports {@code CompilePathAllocationBenchmark}, isolating three allocation smells
 * in the compile and cache-hit code paths.
 *
 * <h2>Smells under investigation</h2>
 * <ol>
 *   <li><b>RuntimeValueFactory / RuntimeCoercionService per compile() call (Smell A)</b>:
 *       {@link ExpressionRuntimeSupport#from} always creates two stateless wrapper objects —
 *       one {@code RuntimeValueFactory} and one {@code RuntimeCoercionService} — even when the
 *       {@link CompiledExpression} is served from the Caffeine cache.  Both objects hold only a
 *       single reference to the shared {@link com.runestone.converters.DataConversionService};
 *       they are ~24 B each (~48 B total per call).  Fix: store them as final fields in
 *       {@link com.runestone.expeval2.environment.ExpressionEnvironment}, which already owns the
 *       conversion service they wrap.</li>
 *
 *   <li><b>FunctionRef allocated on every FunctionDescriptor.functionRef() call (Smell B)</b>:
 *       {@code FunctionDescriptor.functionRef()} returns {@code new FunctionRef(name, arity())}
 *       on every invocation.  Both {@code name} and {@code arity()} are compile-time-invariant;
 *       the record could be precomputed once in the constructor.  Impact: one 32-B allocation per
 *       function call <em>node</em> in the AST per cache-miss compilation.</li>
 *
 *   <li><b>FunctionCatalog.findExact() stream + list per lookup (Smell C)</b>:
 *       {@code findExact(name, arity)} creates a new {@code Stream} and calls {@code .toList()}
 *       on every semantic resolution of a function call, allocating a stream object and a new
 *       list even when the catalog has only one overload for the given name.  A plain indexed loop
 *       or a direct {@code Map<FunctionRef, FunctionDescriptor>} lookup would allocate nothing.</li>
 * </ol>
 *
 * <p>The cache-hit scenario ({@link #compileSimpleCacheHit()} and {@link #compileFunctionCacheHit()})
 * bypasses the ANTLR + AST + semantic-resolution work; only the {@code ExpressionRuntimeSupport.from()}
 * wrapper allocation (Smell A) and {@code MutableBindings.seedDefaults()} remain.
 *
 * <p>The cache-miss scenario ({@link #compileFunctionCacheMiss()}) exercises the full pipeline,
 * making Smells B and C visible at the cost of also including parser and AST allocation.
 */
public final class CompilePathAllocationBenchmarkSupport {

    /** Simple math expression — no function calls, no external symbols. */
    static final String SIMPLE_MATH_EXPRESSION = "a + b * c - d + e * f - g + h * i - j + k * l";

    /** Expression with 4 calls to an environment-provided function. */
    static final String FUNCTION_CALL_EXPRESSION =
        "ln(a) + ln(b) + ln(c) + ln(d) + sqrt(e) + sqrt(f) + sqrt(g) + sqrt(h)";

    private final ExpressionEnvironment simpleEnv;
    private final ExpressionEnvironment mathEnv;
    private final ExpressionCompiler sharedCompiler;

    public CompilePathAllocationBenchmarkSupport() {
        simpleEnv = ExpressionEnvironmentBuilder.empty();
        mathEnv = new ExpressionEnvironmentBuilder().addMathFunctions().build();
        sharedCompiler = new ExpressionCompiler();
        // Warm the cache so subsequent calls return immediately from Caffeine
        sharedCompiler.compile(SIMPLE_MATH_EXPRESSION, ExpressionResultType.MATH, simpleEnv);
        sharedCompiler.compile(FUNCTION_CALL_EXPRESSION, ExpressionResultType.MATH, mathEnv);
    }

    /**
     * Cache-hit path for a no-function expression.
     * Measures Smell A only: RuntimeValueFactory + RuntimeCoercionService allocation per call.
     */
    public ExpressionRuntimeSupport compileSimpleCacheHit() {
        return ExpressionRuntimeSupport.compile(
            SIMPLE_MATH_EXPRESSION, ExpressionResultType.MATH, simpleEnv, sharedCompiler
        );
    }

    /**
     * Cache-hit path for an expression with 8 function calls.
     * Measures Smell A (factory objects) in the context where the function environment has
     * many symbols seeded via {@code MutableBindings.seedDefaults()}.
     */
    public ExpressionRuntimeSupport compileFunctionCacheHit() {
        return ExpressionRuntimeSupport.compile(
            FUNCTION_CALL_EXPRESSION, ExpressionResultType.MATH, mathEnv, sharedCompiler
        );
    }

    /**
     * Full (cache-miss) compile of the 8-function expression.
     * Measures Smells A + B + C together with full pipeline overhead.
     * Use {@code -prof gc} to isolate allocation by comparing against cache-hit.
     */
    public ExpressionRuntimeSupport compileFunctionCacheMiss() {
        ExpressionCompiler freshCompiler = new ExpressionCompiler();
        return ExpressionRuntimeSupport.compile(
            FUNCTION_CALL_EXPRESSION, ExpressionResultType.MATH, mathEnv, freshCompiler
        );
    }

    /**
     * Evaluates the 8-function expression. Used to isolate compute-path allocation
     * separately from the compile-path allocation measured by the other methods.
     */
    public BigDecimal evaluateFunction(ExpressionRuntimeSupport runtime, BigDecimal value) {
        Map<String, Object> values = new HashMap<>();
        for (String name : new String[]{"a","b","c","d","e","f","g","h"}) {
            values.put(name, value);
        }
        return runtime.computeMath(values);
    }
}
