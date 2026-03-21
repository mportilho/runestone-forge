package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.api.AuditResult;
import com.runestone.expeval2.api.CacheConfig;
import com.runestone.expeval2.api.CompilationIssue;
import com.runestone.expeval2.api.CompilationPosition;
import com.runestone.expeval2.api.ExpressionCompilationException;
import com.runestone.expeval2.api.ValidationResult;
import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.internal.ast.SourceSpan;
import com.runestone.expeval2.internal.ast.mapping.SemanticAstBuilder;
import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2ParserFacade;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import com.runestone.expeval2.internal.grammar.ParsingException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ExpressionRuntimeSupport {

    private static volatile ExpressionCompiler COMPILER;

    /**
     * Configures the JVM-wide expression compiler before the first compilation.
     *
     * <p>This method must be called <strong>before</strong> any call to
     * {@code compileMath}, {@code compileLogical}, {@code compileAssignments}, or {@code compile}.
     * Once the compiler is initialized — either by an explicit call to this method or lazily on the
     * first compilation — subsequent calls have no effect and the supplied config is silently ignored.
     *
     * <p>Typical usage (application startup, {@code main} or Spring {@code @Bean}):
     * <pre>{@code
     * ExpressionRuntimeSupport.configure(new CacheConfig(4_096, Duration.ofHours(1)));
     * }</pre>
     *
     * @param cacheConfig cache settings to apply; must not be {@code null}
     */
    public static void configure(CacheConfig cacheConfig) {
        Objects.requireNonNull(cacheConfig, "cacheConfig must not be null");
        if (COMPILER == null) {
            synchronized (ExpressionRuntimeSupport.class) {
                if (COMPILER == null) {
                    COMPILER = new ExpressionCompiler(
                            new ExpressionEvaluatorV2ParserFacade(),
                            new SemanticAstBuilder(),
                            new SemanticResolver(),
                            new ExecutionPlanBuilder(),
                            cacheConfig
                    );
                }
            }
        }
    }

    private static ExpressionCompiler getCompiler() {
        ExpressionCompiler c = COMPILER;
        if (c == null) {
            synchronized (ExpressionRuntimeSupport.class) {
                c = COMPILER;
                if (c == null) {
                    COMPILER = c = new ExpressionCompiler();
                }
            }
        }
        return c;
    }

    private final CompiledExpression compiledExpression;
    private final MutableBindings bindings;
    private final MathEvaluator mathEvaluator;
    private final LogicalEvaluator logicalEvaluator;
    private final boolean hasAssignments;
    private final int internalSymbolCount;
    private final int maxAuditEvents;

    private ExpressionRuntimeSupport(CompiledExpression compiledExpression, MutableBindings bindings,
                                     RuntimeValueFactory runtimeValueFactory, RuntimeCoercionService runtimeCoercionService,
                                     java.math.MathContext mathContext) {
        this.compiledExpression = Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");
        this.bindings = Objects.requireNonNull(bindings, "bindings must not be null");
        Objects.requireNonNull(runtimeValueFactory, "runtimeValueFactory must not be null");
        Objects.requireNonNull(runtimeCoercionService, "runtimeCoercionService must not be null");
        Objects.requireNonNull(mathContext, "mathContext must not be null");
        this.mathEvaluator = new MathEvaluator(compiledExpression, runtimeValueFactory, runtimeCoercionService, mathContext);
        this.logicalEvaluator = new LogicalEvaluator(compiledExpression, runtimeValueFactory, runtimeCoercionService, mathContext);
        this.hasAssignments = !compiledExpression.executionPlan().assignments().isEmpty();
        this.internalSymbolCount = compiledExpression.semanticModel().internalSymbolsByName().size();
        this.maxAuditEvents = countMaxAuditEvents(compiledExpression.executionPlan());
    }

    public static ExpressionRuntimeSupport compileMath(String source, ExpressionEnvironment environment) {
        return compile(source, ExpressionResultType.MATH, environment);
    }

    public static ExpressionRuntimeSupport compileLogical(String source, ExpressionEnvironment environment) {
        return compile(source, ExpressionResultType.LOGICAL, environment);
    }

    public static ExpressionRuntimeSupport compileAssignments(String source, ExpressionEnvironment environment) {
        return compile(source, ExpressionResultType.ASSIGNMENTS, environment);
    }

    public static ValidationResult validateMath(String source, ExpressionEnvironment environment) {
        return validate(source, ExpressionResultType.MATH, environment);
    }

    public static ValidationResult validateLogical(String source, ExpressionEnvironment environment) {
        return validate(source, ExpressionResultType.LOGICAL, environment);
    }

    public static ValidationResult validateAssignments(String source, ExpressionEnvironment environment) {
        return validate(source, ExpressionResultType.ASSIGNMENTS, environment);
    }

    public static ValidationResult validate(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        try {
            compile(source, resultType, environment);
            return ValidationResult.ok(source);
        } catch (ExpressionCompilationException e) {
            return ValidationResult.failed(source, e.issues());
        } catch (ParsingException e) {
            List<CompilationIssue> issues = e.errors().stream()
                .map(err -> new CompilationIssue(
                    "SYNTAX_ERROR",
                    err.message(),
                    new CompilationPosition(err.line(), err.charPositionInLine(), err.charPositionInLine() + 1)
                ))
                .toList();
            return ValidationResult.failed(
                source,
                issues.isEmpty() ? List.of(new CompilationIssue("SYNTAX_ERROR", "syntax error")) : issues
            );
        }
    }

    public static ExpressionRuntimeSupport compile(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        try {
            CompiledExpression compiled = getCompiler().compile(source, resultType, environment);
            return from(compiled, environment);
        } catch (SemanticResolutionException e) {
            List<CompilationIssue> issues = e.issues().stream()
                .map(issue -> {
                    SourceSpan span = issue.sourceSpan();
                    CompilationPosition position = new CompilationPosition(span.startLine(), span.startColumn(), span.endColumn());
                    return new CompilationIssue(issue.code(), issue.message(), position);
                })
                .toList();
            throw new ExpressionCompilationException(source, issues, e);
        }
    }

    static ExpressionRuntimeSupport from(CompiledExpression compiledExpression, ExpressionEnvironment environment) {
        Objects.requireNonNull(environment, "environment must not be null");
        RuntimeValueFactory runtimeValueFactory = new RuntimeValueFactory(environment.getDataConversionService());
        RuntimeCoercionService runtimeCoercionService = new RuntimeCoercionService(environment.getDataConversionService());
        return new ExpressionRuntimeSupport(
                compiledExpression,
                MutableBindings.from(
                        compiledExpression.semanticModel(),
                        environment.externalSymbolCatalog(),
                        runtimeValueFactory
                ),
                runtimeValueFactory,
                runtimeCoercionService,
                environment.mathContext()
        );
    }

    public void setValue(String symbolName, Object rawValue) {
        bindings.setValue(symbolName, rawValue);
    }

    ExecutionScope createExecutionScope() {
        if (hasAssignments) {
            return ExecutionScope.from(bindings.valuesReadOnly(), internalSymbolCount);
        }
        return ExecutionScope.readOnly(bindings.valuesReadOnly());
    }

    private ExecutionScope createAuditedExecutionScope(AuditCollector collector) {
        if (hasAssignments) {
            return ExecutionScope.fromWithAudit(bindings.valuesReadOnly(), internalSymbolCount, collector);
        }
        return ExecutionScope.readOnlyWithAudit(bindings.valuesReadOnly(), collector);
    }

    public BigDecimal computeMath() {
        return mathEvaluator.evaluate(createExecutionScope());
    }

    public boolean computeLogical() {
        return logicalEvaluator.evaluate(createExecutionScope());
    }

    public AuditResult<BigDecimal> computeMathWithAudit() {
        AuditCollector collector = new AuditCollector(maxAuditEvents);
        BigDecimal result = mathEvaluator.evaluate(createAuditedExecutionScope(collector));
        return new AuditResult<>(result, collector.buildTrace());
    }

    public AuditResult<Boolean> computeLogicalWithAudit() {
        AuditCollector collector = new AuditCollector(maxAuditEvents);
        boolean result = logicalEvaluator.evaluate(createAuditedExecutionScope(collector));
        return new AuditResult<>(result, collector.buildTrace());
    }

    public Map<String, Object> computeAssignments() {
        return mathEvaluator.evaluateAssignments(createExecutionScope());
    }

    public AuditResult<Map<String, Object>> computeAssignmentsWithAudit() {
        AuditCollector collector = new AuditCollector(maxAuditEvents);
        Map<String, Object> result = mathEvaluator.evaluateAssignments(createAuditedExecutionScope(collector));
        return new AuditResult<>(result, collector.buildTrace());
    }

    private static int countMaxAuditEvents(ExecutionPlan plan) {
        int count = 0;
        for (ExecutableAssignment assignment : plan.assignments()) {
            count += switch (assignment) {
                case ExecutableSimpleAssignment s -> 1 + countNodeEvents(s.value());
                case ExecutableDestructuringAssignment d -> d.targets().size() + countNodeEvents(d.value());
            };
        }
        if (plan.resultExpression() != null) {
            count += countNodeEvents(plan.resultExpression());
        }
        return count;
    }

    private static int countNodeEvents(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral ignored -> 0;
            case ExecutableDynamicLiteral ignored -> 1;
            case ExecutableIdentifier ignored -> 1;
            case ExecutableFunctionCall f -> {
                int sum = 1;
                for (ExecutableNode arg : f.arguments()) {
                    sum += countNodeEvents(arg);
                }
                yield sum;
            }
            case ExecutableBinaryOp b -> countNodeEvents(b.left()) + countNodeEvents(b.right());
            case ExecutableUnaryOp u -> countNodeEvents(u.operand());
            case ExecutablePostfixOp p -> countNodeEvents(p.operand());
            case ExecutableConditional c -> {
                // Upper bound: count all branches; only one condition+result path runs at runtime.
                int sum = 0;
                for (ExecutableNode cond : c.conditions()) {
                    sum += countNodeEvents(cond);
                }
                for (ExecutableNode res : c.results()) {
                    sum += countNodeEvents(res);
                }
                yield sum + countNodeEvents(c.elseExpression());
            }
            case ExecutableVectorLiteral v -> {
                int sum = 0;
                for (ExecutableNode el : v.elements()) {
                    sum += countNodeEvents(el);
                }
                yield sum;
            }
        };
    }

    public CompiledExpression getCompiledExpression() {
        return compiledExpression;
    }
}
