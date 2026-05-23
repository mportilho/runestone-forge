package com.runestone.expeval.internal.semantic;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.catalog.ExternalSymbolDescriptor;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.AssignmentNode;
import com.runestone.expeval.internal.ast.IdentifierNode;
import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.internal.ast.SimpleAssignmentNode;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.UnknownType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticSymbolResolverTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 1, 1, 0, 1, 1);

    private final Map<NodeId, SymbolRef> symbolByNodeId = new LinkedHashMap<>();
    private final Map<SymbolRef, List<IdentifierNode>> identifierUsages = new LinkedHashMap<>();
    private final Map<SymbolRef, List<AssignmentNode>> assignmentUsages = new LinkedHashMap<>();
    private final Map<String, SymbolRef> externalSymbolsByName = new LinkedHashMap<>();
    private final Map<String, SymbolRef> internalSymbolsByName = new LinkedHashMap<>();
    private final Map<SymbolRef, ResolvedType> internalTypes = new LinkedHashMap<>();
    private final List<IssueCode> issues = new ArrayList<>();

    @Test
    void shouldRegisterAssignmentsAndResolveInternalIdentifiers() {
        SemanticSymbolResolver resolver = resolver(Map.of());
        SimpleAssignmentNode assignment = new SimpleAssignmentNode(
                nodeId("assignment"),
                SPAN,
                "fee",
                literal("value"));

        resolver.collectInternalSymbols(List.of(assignment));
        resolver.resolveSimpleAssignment(assignment, ScalarType.NUMBER);
        IdentifierNode identifier = identifier("fee");

        assertThat(resolver.resolveIdentifier(identifier, null)).isEqualTo(ScalarType.NUMBER);
        SymbolRef fee = internalSymbolsByName.get("fee");
        assertThat(fee.kind()).isEqualTo(SymbolKind.INTERNAL);
        assertThat(symbolByNodeId).containsEntry(assignment.nodeId(), fee).containsEntry(identifier.nodeId(), fee);
        assertThat(assignmentUsages.get(fee)).containsExactly(assignment);
        assertThat(identifierUsages.get(fee)).containsExactly(identifier);
    }

    @Test
    void shouldResolveExternalIdentifierDeclaredType() {
        SemanticSymbolResolver resolver = resolver(Map.of(
                "principal",
                new ExternalSymbolDescriptor("principal", ScalarType.NUMBER, null, true)));

        IdentifierNode identifier = identifier("principal");

        assertThat(resolver.resolveIdentifier(identifier, null)).isEqualTo(ScalarType.NUMBER);
        SymbolRef principal = externalSymbolsByName.get("principal");
        assertThat(principal.kind()).isEqualTo(SymbolKind.EXTERNAL);
        assertThat(symbolByNodeId).containsEntry(identifier.nodeId(), principal);
        assertThat(identifierUsages.get(principal)).containsExactly(identifier);
    }

    @Test
    void shouldResolveUnknownExternalIdentifierAsUnknownType() {
        SemanticSymbolResolver resolver = resolver(Map.of());

        assertThat(resolver.resolveIdentifier(identifier("missing"), null)).isEqualTo(UnknownType.INSTANCE);
        assertThat(externalSymbolsByName).containsKey("missing");
    }

    @Test
    void shouldValidateCurrentElementIdentifierScope() {
        SemanticSymbolResolver resolver = resolver(Map.of());

        assertThat(resolver.resolveIdentifier(identifier(LanguageSymbols.CURRENT_ELEMENT), null)).isEqualTo(UnknownType.INSTANCE);
        assertThat(issues).containsExactly(IssueCode.INVALID_CURRENT_ELEMENT);

        issues.clear();
        assertThat(resolver.resolveIdentifier(identifier(LanguageSymbols.CURRENT_ELEMENT), ScalarType.STRING))
                .isEqualTo(ScalarType.STRING);
        assertThat(issues).isEmpty();
    }

    private SemanticSymbolResolver resolver(Map<String, ExternalSymbolDescriptor> symbolsByName) {
        return new SemanticSymbolResolver(
                new ExternalSymbolCatalog(symbolsByName),
                symbolByNodeId,
                identifierUsages,
                assignmentUsages,
                externalSymbolsByName,
                internalSymbolsByName,
                internalTypes,
                (code, message, sourceSpan) -> issues.add(code));
    }

    private static IdentifierNode identifier(String name) {
        return new IdentifierNode(nodeId("identifier-" + name), SPAN, name);
    }

    private static LiteralNode literal(String name) {
        return new LiteralNode(nodeId("literal-" + name), SPAN, "1");
    }

    private static NodeId nodeId(String value) {
        return new NodeId(value);
    }
}
