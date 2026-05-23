package com.runestone.expeval.internal.semantic;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.catalog.FunctionCatalog;
import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.internal.ast.ExpressionNode;
import com.runestone.expeval.internal.ast.FunctionCallNode;
import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.UnknownType;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionOverloadResolverTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 1, 1, 0, 1, 1);

    private final Map<NodeId, ResolvedFunctionBinding> functionBindings = new LinkedHashMap<>();
    private final List<IssueCode> issues = new ArrayList<>();

    @Test
    void shouldResolveMatchingFunctionAndRegisterBinding() throws ReflectiveOperationException {
        FunctionDescriptor descriptor = descriptor("calc", List.of(ScalarType.NUMBER), ScalarType.NUMBER, "one");
        FunctionOverloadResolver resolver = resolver(Map.of("calc", List.of(descriptor)));
        FunctionCallNode node = functionCall("calc", 1);

        assertThat(resolver.resolve(node, List.of(ScalarType.NUMBER))).isEqualTo(ScalarType.NUMBER);
        assertThat(functionBindings.get(node.nodeId()).descriptor()).isSameAs(descriptor);
        assertThat(issues).isEmpty();
    }

    @Test
    void shouldReportUnknownFunction() {
        FunctionOverloadResolver resolver = resolver(Map.of());

        assertThat(resolver.resolve(functionCall("missing", 1), List.of(ScalarType.NUMBER)))
                .isEqualTo(UnknownType.INSTANCE);
        assertThat(issues).containsExactly(IssueCode.UNKNOWN_FUNCTION);
    }

    @Test
    void shouldReportInvalidArity() throws ReflectiveOperationException {
        FunctionOverloadResolver resolver = resolver(Map.of(
                "calc",
                List.of(descriptor("calc", List.of(ScalarType.NUMBER), ScalarType.NUMBER, "one"))));

        assertThat(resolver.resolve(functionCall("calc", 2), List.of(ScalarType.NUMBER, ScalarType.NUMBER)))
                .isEqualTo(UnknownType.INSTANCE);
        assertThat(issues).containsExactly(IssueCode.INVALID_FUNCTION_ARITY);
    }

    @Test
    void shouldReportIncompatibleArguments() throws ReflectiveOperationException {
        FunctionOverloadResolver resolver = resolver(Map.of(
                "calc",
                List.of(descriptor("calc", List.of(ScalarType.NUMBER), ScalarType.NUMBER, "one"))));

        assertThat(resolver.resolve(functionCall("calc", 1), List.of(ScalarType.STRING)))
                .isEqualTo(UnknownType.INSTANCE);
        assertThat(issues).containsExactly(IssueCode.INCOMPATIBLE_FUNCTION_ARGUMENTS);
    }

    @Test
    void shouldReportAmbiguousFunction() throws ReflectiveOperationException {
        FunctionDescriptor first = descriptor("calc", List.of(ScalarType.NUMBER), ScalarType.NUMBER, "one");
        FunctionDescriptor second = descriptor("calc", List.of(ScalarType.NUMBER), ScalarType.NUMBER, "one");
        FunctionOverloadResolver resolver = resolver(Map.of("calc", List.of(first, second)));

        assertThat(resolver.resolve(functionCall("calc", 1), List.of(ScalarType.NUMBER)))
                .isEqualTo(UnknownType.INSTANCE);
        assertThat(issues).containsExactly(IssueCode.AMBIGUOUS_FUNCTION);
    }

    private FunctionOverloadResolver resolver(Map<String, List<FunctionDescriptor>> descriptorsByName) {
        return new FunctionOverloadResolver(
                new FunctionCatalog(descriptorsByName),
                functionBindings,
                (code, message, sourceSpan) -> issues.add(code));
    }

    private static FunctionDescriptor descriptor(
            String name,
            List<ResolvedType> parameterTypes,
            ResolvedType returnType,
            String methodName) throws ReflectiveOperationException {
        MethodHandle handle = MethodHandles.lookup().findStatic(
                FunctionOverloadResolverTest.class,
                methodName,
                MethodType.methodType(Object.class, Object.class));
        List<Class<?>> javaParameterTypes = new ArrayList<>(parameterTypes.size());
        for (int index = 0; index < parameterTypes.size(); index++) {
            javaParameterTypes.add(Object.class);
        }
        return new FunctionDescriptor(
                name,
                javaParameterTypes,
                parameterTypes,
                returnType,
                handle);
    }

    private static FunctionCallNode functionCall(String name, int arity) {
        List<ExpressionNode> arguments = new ArrayList<>(arity);
        for (int index = 0; index < arity; index++) {
            arguments.add(new LiteralNode(new NodeId("literal-" + name + '-' + index), SPAN, "1"));
        }
        return new FunctionCallNode(new NodeId("function-" + name + '-' + arity), SPAN, name, arguments);
    }

    @SuppressWarnings("unused")
    private static Object one(Object value) {
        return value;
    }
}
