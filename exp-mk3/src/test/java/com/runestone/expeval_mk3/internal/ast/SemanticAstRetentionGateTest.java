package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.grammar.ExpressionEvaluatorParser;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticAstRetentionGateTest {

    private static final String AST_PACKAGE = "com.runestone.expeval_mk3.internal.ast";
    private static final String AST_PACKAGE_PATH = AST_PACKAGE.replace('.', '/');

    @Test
    @DisplayName("AST records and result envelopes do not retain ANTLR parse-tree objects")
    void astRecordsAndResultEnvelopesDoNotRetainAntlrParseTreeObjects() {
        List<String> retainedFields = astTypes().stream()
                .flatMap(type -> retainedAntlrFields(type).stream())
                .toList();

        assertThat(retainedFields).isEmpty();
    }

    @Test
    @DisplayName("AST builder returns only success trees or diagnostic failures")
    void astBuilderReturnsOnlySuccessTreesOrDiagnosticFailures() {
        SemanticAstResult valid = buildResult("1 + 2");
        SemanticAstResult invalidLiteral = buildResult("d\"2024-02-31\"");

        assertThat(SemanticAstResult.class.getPermittedSubclasses())
                .containsExactlyInAnyOrder(SemanticAstSuccess.class, SemanticAstFailure.class);
        assertThat(valid).isInstanceOf(SemanticAstSuccess.class);
        assertThat(invalidLiteral).isInstanceOf(SemanticAstFailure.class);
        assertThat(((SemanticAstFailure) invalidLiteral).diagnostics()).isNotEmpty();
    }

    private static SemanticAstResult buildResult(String source) {
        ParseResult parseResult = new ExpressionParser().parse(source);
        assertThat(parseResult).isInstanceOf(ParseSuccess.class);
        return new SemanticAstBuilder().build((ParseSuccess) parseResult);
    }

    private static List<Class<?>> astTypes() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<Class<?>> types = new ArrayList<>();
        try {
            for (URL resource : Collections.list(classLoader.getResources(AST_PACKAGE_PATH))) {
                types.addAll(loadPackageClasses(classLoader, resource));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to discover AST package classes", exception);
        }
        return types.stream().distinct().toList();
    }

    private static List<Class<?>> loadPackageClasses(ClassLoader classLoader, URL resource)
            throws URISyntaxException {
        if (!"file".equals(resource.getProtocol())) {
            throw new IllegalStateException("Unsupported AST package resource protocol: " + resource.getProtocol());
        }
        try (Stream<Path> files = Files.list(Path.of(resource.toURI()))) {
            List<Class<?>> classes = new ArrayList<>();
            files
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(fileName -> fileName.endsWith(".class"))
                    .map(fileName -> fileName.substring(0, fileName.length() - ".class".length()))
                    .map(className -> loadClass(classLoader, AST_PACKAGE + "." + className))
                    .forEach(classes::add);
            return List.copyOf(classes);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Failed to list AST package resource: " + resource, exception);
        }
    }

    private static Class<?> loadClass(ClassLoader classLoader, String className) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Failed to load AST package class: " + className, exception);
        }
    }

    private static List<String> retainedAntlrFields(Class<?> type) {
        List<String> retainedFields = new ArrayList<>();
        for (RecordComponent component : type.getRecordComponents() == null ? new RecordComponent[0] : type.getRecordComponents()) {
            if (isAntlrType(component.getType()) || isAntlrGenericType(component.getGenericType())) {
                retainedFields.add(type.getName() + "#" + component.getName());
            }
        }
        for (Field field : type.getDeclaredFields()) {
            if (isAntlrType(field.getType()) || isAntlrGenericType(field.getGenericType())) {
                retainedFields.add(type.getName() + "#" + field.getName());
            }
        }
        return retainedFields;
    }

    private static boolean isAntlrType(Class<?> type) {
        return ParserRuleContext.class.isAssignableFrom(type)
                || Token.class.isAssignableFrom(type)
                || ParseTree.class.isAssignableFrom(type)
                || ExpressionEvaluatorParser.class.isAssignableFrom(type)
                || type.getName().startsWith("com.runestone.expeval_mk3.internal.grammar.")
                || type.getName().startsWith("org.antlr.v4.runtime.");
    }

    private static boolean isAntlrGenericType(Type type) {
        String typeName = type.getTypeName();
        return typeName.contains("com.runestone.expeval_mk3.internal.grammar.")
                || typeName.contains("org.antlr.v4.runtime.");
    }
}
