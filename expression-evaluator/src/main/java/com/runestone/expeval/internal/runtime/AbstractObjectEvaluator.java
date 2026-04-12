package com.runestone.expeval.internal.runtime;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.api.CompilationPosition;
import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.ast.SourceSpan;

import com.runestone.expeval.internal.navigation.FilterContext;
import com.runestone.expeval.internal.navigation.MapProjectionKind;
import com.runestone.expeval.internal.navigation.TypeIntrospectionSupport;
import com.runestone.expeval.internal.navigation.VectorAggregationKind;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Expression evaluator that carries all sub-expression results as {@code Object} values
 * ({@link BigDecimal}, {@link Boolean}, {@link LocalDate}, etc.) with no intermediate boxing.
 *
 * <h2>Scope contract</h2>
 * <ul>
 *   <li><strong>Scope read</strong> — variables are looked up via
 *       {@link ExecutionScope#find}, which avoids both {@link java.util.Optional} and wrapper
 *       allocation.</li>
 *   <li><strong>Scope write (assignments)</strong> — the result is stored directly via
 *       {@link ExecutionScope#assign}.  The scope handles null-value sentinels internally.</li>
 *   <li><strong>Function arguments</strong> — coerced via
 *       {@link RuntimeServices#coerce}, which fast-paths same-type arguments.</li>
 * </ul>
 */
abstract class AbstractObjectEvaluator<T> implements Evaluator<T> {

    /** Sentinel name used for {@code @} (current element) in filter predicates. */
    private static final String CURRENT_ELEMENT_REF = "@";

    /**
     * Per-thread stack of filter contexts to support nested filter predicates such as
     * {@code list[?(@.items[?(@.active)])]}. Thread-local because compiled expression
     * objects are shared and may be invoked concurrently.
     */
    private static final ThreadLocal<Deque<FilterContext>> FILTER_CTX =
            ThreadLocal.withInitial(ArrayDeque::new);

    private final CompiledExpression compiledExpression;
    private final RuntimeServices runtimeServices;
    private final MathContext mathContext;

    protected AbstractObjectEvaluator(CompiledExpression compiledExpression,
                                      RuntimeServices runtimeServices,
                                      MathContext mathContext) {
        this.compiledExpression = Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices must not be null");
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext must not be null");
    }

    @Override
    public final T evaluate(ExecutionScope scope) {
        Objects.requireNonNull(scope, "scope must not be null");
        ExecutionPlan plan = compiledExpression.executionPlan();
        for (ExecutableAssignment assignment : plan.assignments()) {
            executeAssignment(assignment, scope);
        }
        return convertResult(evaluateExpr(plan.resultExpression(), scope));
    }

    /**
     * Converts the final result to the evaluator's declared return type.
     */
    protected abstract T convertResult(Object value);

    @Override
    public final Map<String, Object> evaluateAssignments(ExecutionScope scope) {
        Objects.requireNonNull(scope, "scope must not be null");
        ExecutionPlan plan = compiledExpression.executionPlan();
        for (ExecutableAssignment assignment : plan.assignments()) {
            executeAssignment(assignment, scope);
        }
        List<ExecutableAssignment> assignments = plan.assignments();
        Map<String, Object> result = new LinkedHashMap<>(assignments.size());
        for (ExecutableAssignment assignment : assignments) {
            switch (assignment) {
                case ExecutableSimpleAssignment s -> {
                    Object value = scope.find(s.target());
                    result.put(s.target().name(), value == ExecutionScope.UNBOUND ? null : value);
                }
                case ExecutableDestructuringAssignment d -> {
                    for (SymbolRef target : d.targets()) {
                        Object value = scope.find(target);
                        result.put(target.name(), value == ExecutionScope.UNBOUND ? null : value);
                    }
                }
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Assignment execution
    // -------------------------------------------------------------------------

    private void executeAssignment(ExecutableAssignment assignment, ExecutionScope scope) {
        switch (assignment) {
            case ExecutableSimpleAssignment s -> {
                Object value = evaluateExpr(s.value(), scope);
                scope.assign(s.target(), value);
                AuditCollector audit = scope.audit();
                if (audit != null) {
                    audit.record(new AuditEvent.AssignmentEvent(s.target().name(), value));
                }
            }
            case ExecutableDestructuringAssignment d -> {
                @SuppressWarnings("unchecked")
                List<Object> elements = (List<Object>) evaluateExpr(d.value(), scope);
                AuditCollector audit = scope.audit();
                List<SymbolRef> targets = d.targets();
                for (int index = 0; index < targets.size(); index++) {
                    SymbolRef target = targets.get(index);
                    Object element = index < elements.size() ? elements.get(index) : null;
                    scope.assign(target, element);
                    if (audit != null) {
                        audit.record(new AuditEvent.AssignmentEvent(target.name(), element));
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Expression evaluation — returns Object, no boxing
    // -------------------------------------------------------------------------

    private Object evaluateExpr(ExecutableNode node, ExecutionScope scope) {
        return switch (node) {
            case ExecutableLiteral lit -> lit.precomputed();
            case ExecutableDynamicLiteral dyn -> {
                Object value = scope.resolveDynamic(dyn.kind());
                AuditCollector audit = scope.audit();
                if (audit != null) {
                    audit.record(new AuditEvent.VariableRead(dyn.kind().canonicalName(), true, value));
                }
                yield value;
            }
            case ExecutableIdentifier id -> {
                if (CURRENT_ELEMENT_REF.equals(id.ref().name())) {
                    FilterContext ctx = FILTER_CTX.get().peek();
                    if (ctx == null) {
                        throw new ExpressionEvaluationException(compiledExpression.source(),
                                "INVALID_CURRENT_ELEMENT",
                                "'@' used outside of a filter predicate context", null);
                    }
                    yield ctx.isMapContext() ? ctx.mapValue() : ctx.element();
                }
                Object value = scope.find(id.ref());
                if (value == ExecutionScope.UNBOUND) {
                    throw unboundVariableException(id);
                }
                AuditCollector audit = scope.audit();
                if (audit != null) {
                    audit.record(new AuditEvent.VariableRead(id.ref().name(), false, value));
                }
                yield value;
            }
            case ExecutablePropertyChain chain -> evaluatePropertyChain(chain, scope);
            case ExecutableFunctionCall f -> evaluateFunctionCall(f, scope);
            case ExecutableConditional c -> evaluateConditional(c, scope);
            case ExecutableSimpleConditional sc -> evaluateSimpleConditional(sc, scope);
            case ExecutableUnaryOp u -> evaluateUnary(u, scope);
            case ExecutableBinaryOp b -> evaluateBinary(b, scope);
            case ExecutableTernaryOp t -> evaluateTernary(t, scope);
            case ExecutablePostfixOp p -> evaluatePostfix(p, scope);
            case ExecutableVectorLiteral v -> evaluateVector(v, scope);
            case ExecutableNullCoalesce nc -> {
                Object leftVal = evaluateExpr(nc.left(), scope);
                yield leftVal != null ? leftVal : evaluateExpr(nc.right(), scope);
            }
            case ExecutableRegexOp r -> evaluateRegex(r, scope);
        };
    }

    // -------------------------------------------------------------------------
    // Node-specific evaluators
    // -------------------------------------------------------------------------

    private Object evaluateFunctionCall(ExecutableFunctionCall node, ExecutionScope scope) {
        if (node.isFolded()) {
            Object result = runtimeServices.coerceToResolvedType(node.foldedResult(), node.binding().returnType());
            AuditCollector audit = scope.audit();
            if (audit != null) {
                audit.record(new AuditEvent.FunctionCall(
                        node.binding().descriptor().name(),
                        node.foldedArgs(),
                        result
                ));
            }
            return result;
        }

        FunctionDescriptor descriptor = node.binding().descriptor();
        int arity = descriptor.arity();
        AuditCollector audit = scope.audit();

        List<ExecutableNode> argsNodes = node.arguments();
        List<Class<?>> paramTypes = descriptor.parameterTypes();
        return switch (arity) {
            case 0 -> runtimeServices.coerceToResolvedType(descriptor.invoke(), node.binding().returnType());
            case 1 -> {
                Object a1 = evaluateExpr(argsNodes.getFirst(), scope);
                a1 = runtimeServices.coerce(a1, paramTypes.getFirst());
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, a1);
                yield result;
            }
            case 2 -> {
                Object a1 = evaluateExpr(argsNodes.get(0), scope);
                a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                Object a2 = evaluateExpr(argsNodes.get(1), scope);
                a2 = runtimeServices.coerce(a2, paramTypes.get(1));
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, a1, a2);
                yield result;
            }
            case 3 -> {
                Object a1 = evaluateExpr(argsNodes.get(0), scope);
                a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                Object a2 = evaluateExpr(argsNodes.get(1), scope);
                a2 = runtimeServices.coerce(a2, paramTypes.get(1));
                Object a3 = evaluateExpr(argsNodes.get(2), scope);
                a3 = runtimeServices.coerce(a3, paramTypes.get(2));
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, a1, a2, a3);
                yield result;
            }
            case 4 -> {
                Object a1 = evaluateExpr(argsNodes.get(0), scope);
                a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                Object a2 = evaluateExpr(argsNodes.get(1), scope);
                a2 = runtimeServices.coerce(a2, paramTypes.get(1));
                Object a3 = evaluateExpr(argsNodes.get(2), scope);
                a3 = runtimeServices.coerce(a3, paramTypes.get(2));
                Object a4 = evaluateExpr(argsNodes.get(3), scope);
                a4 = runtimeServices.coerce(a4, paramTypes.get(3));
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, a1, a2, a3, a4);
                yield result;
            }
            case 5 -> {
                Object a1 = evaluateExpr(argsNodes.get(0), scope);
                a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                Object a2 = evaluateExpr(argsNodes.get(1), scope);
                a2 = runtimeServices.coerce(a2, paramTypes.get(1));
                Object a3 = evaluateExpr(argsNodes.get(2), scope);
                a3 = runtimeServices.coerce(a3, paramTypes.get(2));
                Object a4 = evaluateExpr(argsNodes.get(3), scope);
                a4 = runtimeServices.coerce(a4, paramTypes.get(3));
                Object a5 = evaluateExpr(argsNodes.get(4), scope);
                a5 = runtimeServices.coerce(a5, paramTypes.get(4));
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4, a5), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, a1, a2, a3, a4, a5);
                yield result;
            }
            case 6 -> {
                Object a1 = evaluateExpr(argsNodes.get(0), scope);
                a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                Object a2 = evaluateExpr(argsNodes.get(1), scope);
                a2 = runtimeServices.coerce(a2, paramTypes.get(1));
                Object a3 = evaluateExpr(argsNodes.get(2), scope);
                a3 = runtimeServices.coerce(a3, paramTypes.get(2));
                Object a4 = evaluateExpr(argsNodes.get(3), scope);
                a4 = runtimeServices.coerce(a4, paramTypes.get(3));
                Object a5 = evaluateExpr(argsNodes.get(4), scope);
                a5 = runtimeServices.coerce(a5, paramTypes.get(4));
                Object a6 = evaluateExpr(argsNodes.get(5), scope);
                a6 = runtimeServices.coerce(a6, paramTypes.get(5));
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4, a5, a6), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, a1, a2, a3, a4, a5, a6);
                yield result;
            }
            default -> {
                Object[] args = new Object[arity];
                for (int i = 0; i < arity; i++) {
                    Object evaluated = evaluateExpr(argsNodes.get(i), scope);
                    args[i] = runtimeServices.coerce(evaluated, paramTypes.get(i));
                }
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(args), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, args);
                yield result;
            }
        };
    }

    private void auditFunctionCall(AuditCollector audit, FunctionDescriptor descriptor, Object result, Object... args) {
        audit.record(new AuditEvent.FunctionCall(descriptor.name(), args, result));
    }

    private Object evaluateConditional(ExecutableConditional node, ExecutionScope scope) {
        List<ExecutableNode> conditions = node.conditions();
        for (int index = 0; index < conditions.size(); index++) {
            if (asBoolean(evaluateExpr(conditions.get(index), scope))) {
                return evaluateExpr(node.results().get(index), scope);
            }
        }
        return evaluateExpr(node.elseExpression(), scope);
    }

    private Object evaluateSimpleConditional(ExecutableSimpleConditional node, ExecutionScope scope) {
        if (asBoolean(evaluateExpr(node.condition(), scope))) {
            return evaluateExpr(node.thenExpression(), scope);
        }
        return evaluateExpr(node.elseExpression(), scope);
    }

    private Object evaluateUnary(ExecutableUnaryOp node, ExecutionScope scope) {
        Object operand = evaluateExpr(node.operand(), scope);
        return OperatorEvaluator.evaluateUnary(node.operator(), operand, runtimeServices, mathContext);
    }

    private Object evaluateBinary(ExecutableBinaryOp node, ExecutionScope scope) {
        Object left = evaluateExpr(node.left(), scope);
        BinaryOperator operator = node.operator();
        // Short-circuit evaluation for logical operators
        if (operator == BinaryOperator.AND || operator == BinaryOperator.NAND) {
            boolean leftBool = asBoolean(left);
            if (!leftBool) return operator == BinaryOperator.NAND;
        } else if (operator == BinaryOperator.OR || operator == BinaryOperator.NOR) {
            boolean leftBool = asBoolean(left);
            if (leftBool) return operator == BinaryOperator.OR;
        }
        Object right = evaluateExpr(node.right(), scope);
        return OperatorEvaluator.evaluateBinary(operator, left, right, runtimeServices, mathContext);
    }

    private Object evaluateTernary(ExecutableTernaryOp node, ExecutionScope scope) {
        Object value = evaluateExpr(node.first(), scope);
        Object lower = evaluateExpr(node.second(), scope);
        Object upper = evaluateExpr(node.third(), scope);
        return OperatorEvaluator.evaluateTernary(node.operator(), value, lower, upper, runtimeServices);
    }

    private Object evaluateRegex(ExecutableRegexOp node, ExecutionScope scope) {
        String subject = asString(evaluateExpr(node.subject(), scope));
        boolean matches = node.pattern().matcher(subject).find();
        return node.negate() != matches;
    }

    private Object evaluatePostfix(ExecutablePostfixOp node, ExecutionScope scope) {
        Object operand = evaluateExpr(node.operand(), scope);
        return OperatorEvaluator.evaluatePostfix(node.operator(), operand, runtimeServices, mathContext);
    }

    private List<Object> evaluateVector(ExecutableVectorLiteral node, ExecutionScope scope) {
        if (node.isFolded()) {
            return node.foldedValue();
        }
        List<Object> elements = new ArrayList<>(node.elements().size());
        for (ExecutableNode element : node.elements()) {
            elements.add(evaluateExpr(element, scope));
        }
        return elements;
    }

    private Object evaluatePropertyChain(ExecutablePropertyChain node, ExecutionScope scope) {
        if (node.legacyOnly()) {
            return evaluateLegacyPropertyChain(node, scope);
        }

        Object current = evaluateExpr(node.root(), scope);
        for (ExecutablePropertyChain.ExecutableAccess access : node.chain()) {
            if (current == null) {
                if (isSafeAccess(access)) {
                    return null;
                }
                throw new ExpressionEvaluationException(
                        compiledExpression.source(), "NULL_IN_CHAIN",
                        "null value encountered navigating '" + rootName(node.root()) + "'", null);
            }
            current = switch (access) {
                // ---- typed -------------------------------------------------------
                case ExecutablePropertyChain.ExecutableFieldGet fieldGet ->
                        invokeGetter(node, current, fieldGet);
                case ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke ->
                        invokeMethod(node, scope, current, methodInvoke);
                // ---- reflective --------------------------------------------------
                case ExecutablePropertyChain.ReflectivePropertyAccess propertyAccess ->
                        resolvePropertyReflective(compiledExpression.source(), current, propertyAccess.name());
                case ExecutablePropertyChain.ReflectiveMethodInvoke reflectiveMethodInvoke -> {
                    Object[] args = new Object[reflectiveMethodInvoke.arguments().size()];
                    for (int index = 0; index < reflectiveMethodInvoke.arguments().size(); index++) {
                        args[index] = evaluateExpr(reflectiveMethodInvoke.arguments().get(index), scope);
                    }
                    yield invokeMethodReflective(compiledExpression.source(), current,
                            reflectiveMethodInvoke.name(), args);
                }
                // ---- collection navigation ---------------------------------------
                case ExecutablePropertyChain.ExecutableIndexAccess ia ->
                        applyIndex(current, (int) asBigDecimalStrict(evaluateExpr(ia.index(), scope)).longValue());
                case ExecutablePropertyChain.ExecutableMapKeyAccess mka ->
                        applyMapKey(current, mka.key());
                case ExecutablePropertyChain.ExecutableSliceAccess sa -> {
                    Integer start = sa.start() == null ? null
                            : (int) asBigDecimalStrict(evaluateExpr(sa.start(), scope)).longValue();
                    Integer end = sa.end() == null ? null
                            : (int) asBigDecimalStrict(evaluateExpr(sa.end(), scope)).longValue();
                    yield applySlice(current, start, end);
                }
                case ExecutablePropertyChain.ExecutableWildcard ignored ->
                        applyWildcard(current);
                case ExecutablePropertyChain.ExecutableFilterPredicate fp ->
                        applyFilter(current, fp.predicate(), scope);
                case ExecutablePropertyChain.ExecutableDeepScan ds ->
                        applyDeepScan(current, ds.propertyName(), scope);
                case ExecutablePropertyChain.ExecutableVectorAggregation va ->
                        applyAggregation(current, va.kind());
                case ExecutablePropertyChain.ExecutableMapProjection mp ->
                        applyMapProjection(current, mp.kind());
                case ExecutablePropertyChain.ExecutableCollectionFunction cf ->
                        applyCollectionFunction(current, cf, scope);
            };
        }
        return current;
    }

    private Object evaluateLegacyPropertyChain(ExecutablePropertyChain node, ExecutionScope scope) {
        Object current = evaluateExpr(node.root(), scope);
        for (ExecutablePropertyChain.ExecutableAccess access : node.chain()) {
            if (current == null) {
                if (isSafeAccess(access)) {
                    return null;
                }
                throw new ExpressionEvaluationException(
                        compiledExpression.source(), "NULL_IN_CHAIN",
                        "null value encountered navigating '" + rootName(node.root()) + "'", null);
            }
            current = switch (access) {
                case ExecutablePropertyChain.ExecutableFieldGet fieldGet ->
                        invokeGetter(node, current, fieldGet);
                case ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke ->
                        invokeMethod(node, scope, current, methodInvoke);
                case ExecutablePropertyChain.ReflectivePropertyAccess propertyAccess ->
                        resolvePropertyReflective(compiledExpression.source(), current, propertyAccess.name());
                case ExecutablePropertyChain.ReflectiveMethodInvoke reflectiveMethodInvoke -> {
                    Object[] args = new Object[reflectiveMethodInvoke.arguments().size()];
                    for (int index = 0; index < reflectiveMethodInvoke.arguments().size(); index++) {
                        args[index] = evaluateExpr(reflectiveMethodInvoke.arguments().get(index), scope);
                    }
                    yield invokeMethodReflective(
                            compiledExpression.source(), current, reflectiveMethodInvoke.name(), args);
                }
                default -> throw new IllegalStateException("legacy property chain contains unsupported access: " + access);
            };
        }
        return current;
    }

    private static String rootName(ExecutableNode root) {
        if (root instanceof ExecutableIdentifier id) {
            return id.ref().name();
        }
        return "[constant]";
    }

    private static boolean isSafeAccess(ExecutablePropertyChain.ExecutableAccess access) {
        return switch (access) {
            case ExecutablePropertyChain.ExecutableFieldGet fieldGet -> fieldGet.safe();
            case ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke -> methodInvoke.safe();
            case ExecutablePropertyChain.ReflectivePropertyAccess propertyAccess -> propertyAccess.safe();
            case ExecutablePropertyChain.ReflectiveMethodInvoke reflectiveMethodInvoke -> reflectiveMethodInvoke.safe();
            // Collection navigation steps — never null-safe (they propagate nulls differently)
            default -> false;
        };
    }

    // -------------------------------------------------------------------------
    // Collection / map navigation helpers
    // -------------------------------------------------------------------------

    /** {@code [n]} — single index access; supports negative indices. */
    @SuppressWarnings("unchecked")
    private Object applyIndex(Object collection, int index) {
        List<Object> list = requireList(collection, "index");
        int effective = index < 0 ? list.size() + index : index;
        if (effective < 0 || effective >= list.size()) {
            throw new ExpressionEvaluationException(compiledExpression.source(), "INDEX_OUT_OF_BOUNDS",
                    "index " + index + " is out of bounds for collection of size " + list.size(), null);
        }
        return list.get(effective);
    }

    /** {@code ["key"]} — key lookup in a {@link Map}. */
    @SuppressWarnings("unchecked")
    private static Object applyMapKey(Object map, String key) {
        if (!(map instanceof Map<?, ?> m)) {
            throw new IllegalStateException("map-key access requires a Map but got: "
                    + map.getClass().getName());
        }
        return ((Map<String, Object>) m).get(key);
    }

    /** {@code [start:end]} — Python-style slice. */
    @SuppressWarnings("unchecked")
    private List<Object> applySlice(Object collection, Integer start, Integer end) {
        List<Object> list = requireList(collection, "slice");
        int size = list.size();
        int from = start == null ? 0 : (start < 0 ? Math.max(0, size + start) : Math.min(start, size));
        int to   = end   == null ? size : (end < 0 ? Math.max(0, size + end)   : Math.min(end,   size));
        if (from >= to) return List.of();
        return List.copyOf(list.subList(from, to));
    }

    /** {@code [*]} or {@code .*} — all elements or all map values. */
    @SuppressWarnings("unchecked")
    private static List<Object> applyWildcard(Object current) {
        if (current instanceof List<?> list) {
            return (List<Object>) list;
        }
        if (current instanceof Map<?, ?> map) {
            return new ArrayList<>((Collection<Object>) map.values());
        }
        return List.of(current);
    }

    /** {@code [?(<predicate>)]} — element-wise filter on a list or map. */
    @SuppressWarnings("unchecked")
    private List<Object> applyFilter(Object current, ExecutableNode predicate, ExecutionScope scope) {
        Deque<FilterContext> stack = FILTER_CTX.get();
        if (current instanceof Map<?, ?> map) {
            // Map filter: retain entries where predicate is truthy; result is a list of values
            List<Object> result = new ArrayList<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                FilterContext ctx = FilterContext.ofMapEntry(entry.getKey(), entry.getValue());
                stack.push(ctx);
                try {
                    if (asBoolean(evaluateExpr(predicate, scope))) {
                        result.add(entry.getValue());
                    }
                } finally {
                    stack.pop();
                }
            }
            return result;
        }
        List<Object> list = requireList(current, "filter");
        List<Object> result = new ArrayList<>();
        for (Object element : list) {
            FilterContext ctx = FilterContext.ofElement(element);
            stack.push(ctx);
            try {
                if (asBoolean(evaluateExpr(predicate, scope))) {
                    result.add(element);
                }
            } finally {
                stack.pop();
            }
        }
        return result;
    }

    /**
     * {@code ..name} or {@code ..*} — BFS recursive deep scan.
     * Collects the named property (or all values for wildcard) from every reachable node.
     */
    private List<Object> applyDeepScan(Object root, String propertyName, ExecutionScope scope) {
        List<Object> results = new ArrayList<>();
        // identity-based deduplication to break cycles
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Object> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Object node = queue.poll();
            if (node == null || !visited.add(node)) continue;
            if (propertyName == null) {
                // Wildcard deep scan — collect all reachable leaf values
                if (node instanceof List<?> list) {
                    queue.addAll(list);
                } else if (node instanceof Map<?, ?> map) {
                    for (Object v : map.values()) {
                        results.add(v);
                        if (v instanceof List<?> || v instanceof Map<?, ?>) queue.add(v);
                    }
                } else {
                    results.add(node);
                }
            } else {
                // Named deep scan — collect that property wherever found
                if (node instanceof List<?> list) {
                    queue.addAll(list);
                } else if (node instanceof Map<?, ?> map) {
                    @SuppressWarnings("unchecked")
                    Object val = ((Map<String, Object>) map).get(propertyName);
                    if (val != null) results.add(val);
                    for (Object v : map.values()) {
                        if (v instanceof List<?> || v instanceof Map<?, ?>) queue.add(v);
                    }
                } else {
                    // Reflective property access on plain objects
                    MethodHandle handle = TypeIntrospectionSupport.cachedProperty(node.getClass(), propertyName);
                    if (handle != null) {
                        try {
                            Object val = handle.invoke(node);
                            if (val != null) results.add(val);
                        } catch (Throwable ignored) {
                            // best-effort deep scan — skip inaccessible properties
                        }
                    }
                }
            }
        }
        return results;
    }

    /** {@code ..sum()}, {@code ..avg()}, etc. — numeric aggregations over a list. */
    private Object applyAggregation(Object current, VectorAggregationKind kind) {
        List<?> list = requireList(current, "aggregation");
        if (kind == VectorAggregationKind.COUNT) {
            return BigDecimal.valueOf(list.size());
        }
        if (list.isEmpty()) return null;
        BigDecimal acc = asBigDecimal(list.getFirst());
        switch (kind) {
            case SUM -> {
                for (int i = 1; i < list.size(); i++) acc = acc.add(asBigDecimal(list.get(i)));
                return acc;
            }
            case AVG -> {
                for (int i = 1; i < list.size(); i++) acc = acc.add(asBigDecimal(list.get(i)));
                return acc.divide(BigDecimal.valueOf(list.size()), mathContext);
            }
            case MIN -> {
                for (int i = 1; i < list.size(); i++) {
                    BigDecimal v = asBigDecimal(list.get(i));
                    if (v.compareTo(acc) < 0) acc = v;
                }
                return acc;
            }
            case MAX -> {
                for (int i = 1; i < list.size(); i++) {
                    BigDecimal v = asBigDecimal(list.get(i));
                    if (v.compareTo(acc) > 0) acc = v;
                }
                return acc;
            }
            default -> throw new IllegalStateException("Unhandled aggregation kind: " + kind);
        }
    }

    /** {@code ..keys()} or {@code ..values()} — map projection. */
    @SuppressWarnings("unchecked")
    private static List<Object> applyMapProjection(Object current, MapProjectionKind kind) {
        if (!(current instanceof Map<?, ?> map)) {
            throw new IllegalStateException("map projection requires a Map but got: "
                    + current.getClass().getName());
        }
        Map<String, Object> typed = (Map<String, Object>) map;
        return kind == MapProjectionKind.KEYS
                ? new ArrayList<>(typed.keySet())
                : new ArrayList<>(typed.values());
    }

    /**
     * {@code ..funcName(args)} — invoke a catalog function with the current
     * collection/map as the implicit first argument.
     */
    private Object applyCollectionFunction(Object current, ExecutablePropertyChain.ExecutableCollectionFunction cf,
                                           ExecutionScope scope) {
        ResolvedFunctionBinding binding = cf.binding();
        if (binding == null || binding.descriptor() == null) {
            throw new ExpressionEvaluationException(compiledExpression.source(), "UNRESOLVED_COLLECTION_FUNCTION",
                    "collection function could not be resolved", null);
        }
        var descriptor = binding.descriptor();
        List<ExecutableNode> extraArgNodes = cf.arguments();
        int totalArity = 1 + extraArgNodes.size();
        List<Class<?>> paramTypes = descriptor.parameterTypes();

        Object[] args = new Object[totalArity];
        args[0] = runtimeServices.coerce(current, paramTypes.getFirst());
        for (int i = 0; i < extraArgNodes.size(); i++) {
            Object evaluated = evaluateExpr(extraArgNodes.get(i), scope);
            args[i + 1] = runtimeServices.coerce(evaluated, paramTypes.get(i + 1));
        }
        Object result = descriptor.invoke(args);
        return runtimeServices.coerceToResolvedType(result, binding.returnType());
    }

    @SuppressWarnings("unchecked")
    private List<Object> requireList(Object value, String operation) {
        if (value instanceof List<?> list) return (List<Object>) list;
        throw new ExpressionEvaluationException(compiledExpression.source(), "TYPE_MISMATCH",
                operation + " requires a List but got: "
                + (value == null ? "null" : value.getClass().getName()), null);
    }

    /** Strict number coercion for index/slice operations — rejects non-numeric values early. */
    private BigDecimal asBigDecimalStrict(Object value) {
        if (value instanceof BigDecimal bd) return bd;
        return runtimeServices.asNumber(value);
    }

    private Object invokeGetter(
            ExecutablePropertyChain node,
            Object current,
            ExecutablePropertyChain.ExecutableFieldGet fieldGet) {
        try {
            Object result = fieldGet.getter().invoke(current);
            return runtimeServices.coerceToResolvedType(result, fieldGet.resolvedType());
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            ExpressionEvaluationException exception = new ExpressionEvaluationException(
                    compiledExpression.source(),
                    "PROPERTY_ACCESS_ERROR",
                    "error accessing '" + fieldGet.name() + "' while navigating '" + rootName(node.root())
                    + "': " + throwable.getMessage(),
                    null
            );
            exception.initCause(throwable);
            throw exception;
        }
    }

    private Object invokeMethod(
            ExecutablePropertyChain node,
            ExecutionScope scope,
            Object current,
            ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke) {
        int arity = methodInvoke.arguments().size();
        List<ExecutableNode> arguments = methodInvoke.arguments();
        List<Class<?>> parameterTypes = methodInvoke.parameterTypes();
        try {
            Object result = switch (arity) {
                case 0 -> methodInvoke.handle().invoke(current);
                case 1 -> {
                    Object a1 = evaluateExpr(arguments.get(0), scope);
                    a1 = runtimeServices.coerce(a1, parameterTypes.get(0));
                    yield methodInvoke.handle().invoke(current, a1);
                }
                case 2 -> {
                    Object a1 = evaluateExpr(arguments.get(0), scope);
                    a1 = runtimeServices.coerce(a1, parameterTypes.get(0));
                    Object a2 = evaluateExpr(arguments.get(1), scope);
                    a2 = runtimeServices.coerce(a2, parameterTypes.get(1));
                    yield methodInvoke.handle().invoke(current, a1, a2);
                }
                case 3 -> {
                    Object a1 = evaluateExpr(arguments.get(0), scope);
                    a1 = runtimeServices.coerce(a1, parameterTypes.get(0));
                    Object a2 = evaluateExpr(arguments.get(1), scope);
                    a2 = runtimeServices.coerce(a2, parameterTypes.get(1));
                    Object a3 = evaluateExpr(arguments.get(2), scope);
                    a3 = runtimeServices.coerce(a3, parameterTypes.get(2));
                    yield methodInvoke.handle().invoke(current, a1, a2, a3);
                }
                case 4 -> {
                    Object a1 = evaluateExpr(arguments.get(0), scope);
                    a1 = runtimeServices.coerce(a1, parameterTypes.get(0));
                    Object a2 = evaluateExpr(arguments.get(1), scope);
                    a2 = runtimeServices.coerce(a2, parameterTypes.get(1));
                    Object a3 = evaluateExpr(arguments.get(2), scope);
                    a3 = runtimeServices.coerce(a3, parameterTypes.get(2));
                    Object a4 = evaluateExpr(arguments.get(3), scope);
                    a4 = runtimeServices.coerce(a4, parameterTypes.get(3));
                    yield methodInvoke.handle().invoke(current, a1, a2, a3, a4);
                }
                case 5 -> {
                    Object a1 = evaluateExpr(arguments.get(0), scope);
                    a1 = runtimeServices.coerce(a1, parameterTypes.get(0));
                    Object a2 = evaluateExpr(arguments.get(1), scope);
                    a2 = runtimeServices.coerce(a2, parameterTypes.get(1));
                    Object a3 = evaluateExpr(arguments.get(2), scope);
                    a3 = runtimeServices.coerce(a3, parameterTypes.get(2));
                    Object a4 = evaluateExpr(arguments.get(3), scope);
                    a4 = runtimeServices.coerce(a4, parameterTypes.get(3));
                    Object a5 = evaluateExpr(arguments.get(4), scope);
                    a5 = runtimeServices.coerce(a5, parameterTypes.get(4));
                    yield methodInvoke.handle().invoke(current, a1, a2, a3, a4, a5);
                }
                case 6 -> {
                    Object a1 = evaluateExpr(arguments.get(0), scope);
                    a1 = runtimeServices.coerce(a1, parameterTypes.get(0));
                    Object a2 = evaluateExpr(arguments.get(1), scope);
                    a2 = runtimeServices.coerce(a2, parameterTypes.get(1));
                    Object a3 = evaluateExpr(arguments.get(2), scope);
                    a3 = runtimeServices.coerce(a3, parameterTypes.get(2));
                    Object a4 = evaluateExpr(arguments.get(3), scope);
                    a4 = runtimeServices.coerce(a4, parameterTypes.get(3));
                    Object a5 = evaluateExpr(arguments.get(4), scope);
                    a5 = runtimeServices.coerce(a5, parameterTypes.get(4));
                    Object a6 = evaluateExpr(arguments.get(5), scope);
                    a6 = runtimeServices.coerce(a6, parameterTypes.get(5));
                    yield methodInvoke.handle().invoke(current, a1, a2, a3, a4, a5, a6);
                }
                default -> {
                    Object[] args = new Object[arity + 1];
                    args[0] = current;
                    for (int index = 0; index < arity; index++) {
                        Object evaluated = evaluateExpr(arguments.get(index), scope);
                        args[index + 1] = runtimeServices.coerce(evaluated, parameterTypes.get(index));
                    }
                    yield methodInvoke.handle().invokeWithArguments(args);
                }
            };
            return runtimeServices.coerceToResolvedType(result, methodInvoke.returnType());
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            ExpressionEvaluationException exception = new ExpressionEvaluationException(
                    compiledExpression.source(),
                    "METHOD_INVOKE_ERROR",
                    "error invoking '" + methodInvoke.name() + "' while navigating '" + rootName(node.root())
                    + "': " + throwable.getMessage(),
                    null
            );
            exception.initCause(throwable);
            throw exception;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object resolvePropertyReflective(String source, Object target, String name) {
        // For Map targets, a dot-notation property access degrades to a key lookup
        if (target instanceof Map<?, ?> map) {
            return ((Map<String, Object>) map).get(name);
        }
        Class<?> cls = target.getClass();
        MethodHandle handle = TypeIntrospectionSupport.cachedProperty(cls, name);
        if (handle == null) {
            throw new ExpressionEvaluationException(source, "UNKNOWN_PROPERTY",
                    "property '" + name + "' not found on " + cls.getSimpleName(), null);
        }
        try {
            return handle.invoke(target);
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            ExpressionEvaluationException exception = new ExpressionEvaluationException(
                    source, "PROPERTY_ACCESS_ERROR",
                    "error accessing '" + name + "': " + throwable.getMessage(), null);
            exception.initCause(throwable);
            throw exception;
        }
    }

    private static Object invokeMethodReflective(String source, Object target, String name, Object[] args) {
        Class<?> cls = target.getClass();
        MethodHandle handle = TypeIntrospectionSupport.cachedMethod(cls, name, args.length);
        if (handle == null) {
            throw new ExpressionEvaluationException(source, "UNKNOWN_METHOD",
                    "method '" + name + "' with " + args.length + " argument(s) not found on " + cls.getSimpleName(), null);
        }
        try {
            return switch (args.length) {
                case 0 -> handle.invoke(target);
                case 1 -> handle.invoke(target, args[0]);
                case 2 -> handle.invoke(target, args[0], args[1]);
                case 3 -> handle.invoke(target, args[0], args[1], args[2]);
                case 4 -> handle.invoke(target, args[0], args[1], args[2], args[3]);
                case 5 -> handle.invoke(target, args[0], args[1], args[2], args[3], args[4]);
                case 6 -> handle.invoke(target, args[0], args[1], args[2], args[3], args[4], args[5]);
                default -> {
                    Object[] fullArgs = new Object[args.length + 1];
                    fullArgs[0] = target;
                    System.arraycopy(args, 0, fullArgs, 1, args.length);
                    yield handle.invokeWithArguments(fullArgs);
                }
            };
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            ExpressionEvaluationException exception = new ExpressionEvaluationException(
                    source, "METHOD_INVOKE_ERROR",
                    "error invoking '" + name + "': " + throwable.getMessage(), null);
            exception.initCause(throwable);
            throw exception;
        }
    }

    // -------------------------------------------------------------------------
    // Comparison helpers
    // -------------------------------------------------------------------------

    private int compare(Object left, Object right) {
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            return asBigDecimal(left).compareTo(asBigDecimal(right));
        }
        if (left instanceof LocalDate || right instanceof LocalDate) {
            return asLocalDate(left).compareTo(asLocalDate(right));
        }
        if (left instanceof LocalTime || right instanceof LocalTime) {
            return asLocalTime(left).compareTo(asLocalTime(right));
        }
        if (left instanceof LocalDateTime || right instanceof LocalDateTime) {
            return asLocalDateTime(left).compareTo(asLocalDateTime(right));
        }
        if (left instanceof String || right instanceof String) {
            return asString(left).compareTo(asString(right));
        }
        if (left instanceof Boolean || right instanceof Boolean) {
            return Boolean.compare(asBoolean(left), asBoolean(right));
        }
        throw new IllegalStateException("unsupported comparison between values");
    }

    private List<?> asList(Object value) {
        if (value instanceof List<?> list) return list;
        throw new IllegalStateException(
                "expected a List but found: " + (value == null ? "null" : value.getClass().getName()));
    }

    private boolean compareEquality(Object left, Object right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            return asBigDecimal(left).compareTo(asBigDecimal(right)) == 0;
        }
        if (left instanceof List<?> leftList && right instanceof List<?> rightList) {
            if (leftList.size() != rightList.size()) return false;
            for (int i = 0; i < leftList.size(); i++) {
                if (!compareEquality(leftList.get(i), rightList.get(i))) return false;
            }
            return true;
        }
        return Objects.equals(left, right);
    }

    // -------------------------------------------------------------------------
    // Type helpers — fast-path instanceof casts, fallback via RuntimeServices
    // -------------------------------------------------------------------------

    private BigDecimal asBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) return bd;
        return runtimeServices.asNumber(value);
    }

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean b) return b;
        return runtimeServices.asBoolean(value);
    }

    private String asString(Object value) {
        if (value instanceof String s) return s;
        return runtimeServices.asString(value);
    }

    private LocalDate asLocalDate(Object value) {
        if (value instanceof LocalDate d) return d;
        return runtimeServices.asDate(value);
    }

    private LocalTime asLocalTime(Object value) {
        if (value instanceof LocalTime t) return t;
        return runtimeServices.asTime(value);
    }

    private LocalDateTime asLocalDateTime(Object value) {
        if (value instanceof LocalDateTime dt) return dt;
        return runtimeServices.asDateTime(value);
    }

    // -------------------------------------------------------------------------
    // Math helpers
    // -------------------------------------------------------------------------

    private BigDecimal pow(BigDecimal base, BigDecimal exponent) {
        BigDecimal normalized = exponent.stripTrailingZeros();
        if (normalized.scale() <= 0 && normalized.precision() <= 9) {
            return base.pow(normalized.intValue(), mathContext);
        }
        return BigDecimalMath.pow(base, exponent, mathContext);
    }

    // -------------------------------------------------------------------------
    // Error helpers
    // -------------------------------------------------------------------------

    private ExpressionEvaluationException unboundVariableException(ExecutableIdentifier id) {
        SourceSpan span = id.sourceSpan();
        CompilationPosition position = new CompilationPosition(span.startLine(), span.startColumn(), span.endColumn());
        String message = "variable '" + id.ref().name() + "' has no value; call setValue(\""
                         + id.ref().name() + "\", ...) before compute()";
        return new ExpressionEvaluationException(compiledExpression.source(), "UNBOUND_VARIABLE", message, position);
    }
}
