package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval.internal.compiler.ExpressionCompiler;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import com.runestone.expeval.internal.runtime.ExpressionRuntimeSupport;

import java.util.Objects;

/**
 * Stateful expression engine with an independently managed compilation cache.
 *
 * <p>Use this type when the JVM-wide singleton used by the static expression factories is not
 * appropriate, such as tests that require isolated caches or applications that manage the engine
 * as a dependency-injection component.
 */
public final class ExpressionEngine {

    private final ExpressionCompiler compiler;

    /**
     * Creates an engine using the default cache configuration.
     */
    public ExpressionEngine() {
        this(new ExpressionCompiler());
    }

    /**
     * Creates an engine using explicit cache settings.
     *
     * @param cacheConfig cache settings; must not be {@code null}
     */
    public ExpressionEngine(CacheConfig cacheConfig) {
        this(new ExpressionCompiler(cacheConfig));
    }

    private ExpressionEngine(ExpressionCompiler compiler) {
        this.compiler = Objects.requireNonNull(compiler, "compiler must not be null");
    }

    /**
     * Compiles a math expression with an empty environment.
     */
    public MathExpression compileMath(String source) {
        return compileMath(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Compiles a math expression with the given environment.
     */
    public MathExpression compileMath(String source, ExpressionEnvironment environment) {
        return MathExpression.from(ExpressionRuntimeSupport.compileMath(source, environment, compiler));
    }

    /**
     * Compiles a logical expression with an empty environment.
     */
    public LogicalExpression compileLogical(String source) {
        return compileLogical(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Compiles a logical expression with the given environment.
     */
    public LogicalExpression compileLogical(String source, ExpressionEnvironment environment) {
        return LogicalExpression.from(ExpressionRuntimeSupport.compileLogical(source, environment, compiler));
    }

    /**
     * Compiles an assignment block with an empty environment.
     */
    public AssignmentExpression compileAssignments(String source) {
        return compileAssignments(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Compiles an assignment block with the given environment.
     */
    public AssignmentExpression compileAssignments(String source, ExpressionEnvironment environment) {
        return AssignmentExpression.from(ExpressionRuntimeSupport.compileAssignments(source, environment, compiler));
    }

    /**
     * Validates a math expression with an empty environment.
     */
    public ValidationResult validateMath(String source) {
        return validateMath(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Validates a math expression with the given environment.
     */
    public ValidationResult validateMath(String source, ExpressionEnvironment environment) {
        return ExpressionRuntimeSupport.validate(source, ExpressionResultType.MATH, environment, compiler);
    }

    /**
     * Validates a logical expression with an empty environment.
     */
    public ValidationResult validateLogical(String source) {
        return validateLogical(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Validates a logical expression with the given environment.
     */
    public ValidationResult validateLogical(String source, ExpressionEnvironment environment) {
        return ExpressionRuntimeSupport.validate(source, ExpressionResultType.LOGICAL, environment, compiler);
    }

    /**
     * Validates an assignment block with an empty environment.
     */
    public ValidationResult validateAssignments(String source) {
        return validateAssignments(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Validates an assignment block with the given environment.
     */
    public ValidationResult validateAssignments(String source, ExpressionEnvironment environment) {
        return ExpressionRuntimeSupport.validate(source, ExpressionResultType.ASSIGNMENTS, environment, compiler);
    }

    /**
     * Removes all entries from this engine's compilation cache.
     */
    public void invalidateCache() {
        compiler.invalidateCache();
    }
}
