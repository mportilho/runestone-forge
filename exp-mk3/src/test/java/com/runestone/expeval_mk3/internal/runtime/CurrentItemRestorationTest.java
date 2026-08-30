package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentItemRestorationTest {

    private static final int CURRENT_ITEM_SLOT = 0;
    private static final SourceSpan SPAN = new SourceSpan(0, 1, 1, 1);

    @Test
    void executableLambdaRestoresThePreviousItemAfterSuccessAndFailure() {
        ExecutionScope scope = scopeWith("outer");
        ExecutableLambda successful = new ExecutableLambda(node(current -> current.read(CURRENT_ITEM_SLOT)), CURRENT_ITEM_SLOT);
        ExecutableLambda failing = new ExecutableLambda(node(current -> {
            throw new ExpectedFailure();
        }), CURRENT_ITEM_SLOT);

        assertThat(successful.execute(scope, "inner")).isEqualTo("inner");
        assertThat(scope.read(CURRENT_ITEM_SLOT)).isEqualTo("outer");
        assertThatThrownBy(() -> failing.execute(scope, "failing")).isInstanceOf(ExpectedFailure.class);
        assertThat(scope.read(CURRENT_ITEM_SLOT)).isEqualTo("outer");
    }

    @Test
    void filteringRestoresThePreviousItemAfterEveryItemAndAfterFailure() {
        ExecutionScope scope = scopeWith("outer");
        ExecutableNode accepting = node(current -> current.read(CURRENT_ITEM_SLOT).equals("keep"));

        Object result = ExpressionRuntime.filteredValues(
                List.of("drop", "keep"), false, accepting, CURRENT_ITEM_SLOT, scope, 10, SPAN);

        assertThat(result).isEqualTo(List.of("keep"));
        assertThat(scope.read(CURRENT_ITEM_SLOT)).isEqualTo("outer");

        ExecutableNode failing = node(current -> {
            throw new ExpectedFailure();
        });
        assertThatThrownBy(() -> ExpressionRuntime.filteredValues(
                List.of("failing"), false, failing, CURRENT_ITEM_SLOT, scope, 10, SPAN))
                .isInstanceOf(ExpectedFailure.class);
        assertThat(scope.read(CURRENT_ITEM_SLOT)).isEqualTo("outer");
    }

    private static ExecutionScope scopeWith(Object currentItem) {
        ExecutionScope scope = new ExecutionScope(ExecutionScope.blankFrame(1), ZoneOffset.UTC, Clock.systemUTC());
        scope.write(CURRENT_ITEM_SLOT, currentItem);
        return scope;
    }

    private static ExecutableNode node(Function<ExecutionScope, Object> execution) {
        return new ExecutableNode() {
            @Override
            public NodeId id() {
                return new NodeId(0);
            }

            @Override
            public SourceSpan sourceSpan() {
                return SPAN;
            }

            @Override
            public Object execute(ExecutionScope scope) {
                return execution.apply(scope);
            }
        };
    }

    private static final class ExpectedFailure extends RuntimeException {
    }
}
