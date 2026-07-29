package com.runestone.expeval_mk3.api;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Merges built-in, custom, and reflected function registrations into a single {@link FunctionCatalog},
 * aggregating configuration problems from all three sources into one failure.
 */
final class FunctionCatalogAssembly {

    private FunctionCatalogAssembly() {
    }

    static FunctionCatalog assemble(
            BoundaryCoercion boundaryCoercion,
            MathContext mathContext,
            MathContext transcendentalMathContext,
            int maxMaterializedSize,
            JavaTypeCatalog javaTypes,
            List<FunctionDescriptor> functions,
            List<ReflectedFunctionImporter.ImportPlan> functionProviders,
            List<FunctionDescriptor> functionReplacements,
            List<ReflectedFunctionImporter.ImportPlan> functionReplacementProviders) {
        FunctionCatalog.Builder functionBuilder = FunctionCatalog.builder();
        List<ProviderConfigurationProblem> problems = new ArrayList<>();
        StandardBuiltInFunctions.registerAll(
                functionBuilder, javaTypes, boundaryCoercion, mathContext, transcendentalMathContext, maxMaterializedSize);

        List<FunctionDeclaration> registrations = new ArrayList<>();
        for (FunctionDescriptor descriptor : functions) {
            registrations.add(FunctionDeclaration.custom(descriptor));
        }
        for (ReflectedFunctionImporter.ImportPlan providerImport : functionProviders) {
            ReflectedFunctionImporter.ImportResolution resolution =
                    ReflectedFunctionImporter.resolveForEnvironment(
                            providerImport, javaTypes, boundaryCoercion, maxMaterializedSize);
            for (FunctionDescriptor descriptor : resolution.descriptors()) {
                registrations.add(FunctionDeclaration.imported(descriptor));
            }
            problems.addAll(resolution.failures());
        }
        registrations.sort(FunctionDeclaration.ORDER);
        for (FunctionDeclaration registration : registrations) {
            try {
                registration.register(functionBuilder);
            } catch (IllegalArgumentException exception) {
                problems.add(ProviderConfigurationProblem.fromDescriptor(
                        ProviderConfigurationProblem.Category.REGISTRATION_COLLISION,
                        registration.descriptor(),
                        exception.getMessage(),
                        exception));
            }
        }

        List<FunctionDeclaration> replacements = new ArrayList<>();
        for (FunctionDescriptor descriptor : functionReplacements) {
            replacements.add(FunctionDeclaration.custom(descriptor));
        }
        for (ReflectedFunctionImporter.ImportPlan replacementImport : functionReplacementProviders) {
            ReflectedFunctionImporter.ImportResolution resolution =
                    ReflectedFunctionImporter.resolveForEnvironment(
                            replacementImport, javaTypes, boundaryCoercion, maxMaterializedSize);
            problems.addAll(resolution.failures());
            if (resolution.descriptors().size() == 1) {
                replacements.add(FunctionDeclaration.imported(resolution.descriptors().getFirst()));
            } else if (resolution.descriptors().size() > 1) {
                problems.add(invalidReplacementCardinality(resolution.descriptors()));
            }
        }
        replacements.sort(FunctionDeclaration.ORDER);
        Set<FunctionSignature> replacedSignatures = new HashSet<>();
        for (FunctionDeclaration replacement : replacements) {
            if (!replacedSignatures.add(replacement.descriptor().signature())) {
                problems.add(ProviderConfigurationProblem.fromDescriptor(
                        ProviderConfigurationProblem.Category.REPLACEMENT_CARDINALITY,
                        replacement.descriptor(),
                        "more than one replacement declared for function signature: "
                                + replacement.descriptor().signature().canonical(),
                        null));
                continue;
            }
            try {
                replacement.replace(functionBuilder);
            } catch (IllegalArgumentException exception) {
                problems.add(ProviderConfigurationProblem.fromDescriptor(
                        ProviderConfigurationProblem.Category.REGISTRATION_COLLISION,
                        replacement.descriptor(),
                        exception.getMessage(),
                        exception));
            }
        }
        if (!problems.isEmpty()) {
            throw EnvironmentConfigurationException.of(problems);
        }
        FunctionCatalog catalog = functionBuilder.build();
        StandardBuiltInFunctions.validate(catalog);
        return catalog;
    }

    private static ProviderConfigurationProblem invalidReplacementCardinality(List<FunctionDescriptor> descriptors) {
        List<FunctionSignature> signatures = descriptors.stream()
                .map(FunctionDescriptor::signature)
                .distinct()
                .sorted()
                .toList();
        String message = signatures.size() == 1
                ? "more than one imported method converges on replacement target: " + signatures.getFirst().canonical()
                : "function replacement plan must import exactly one method; imported signatures: "
                        + signatures.stream().map(FunctionSignature::canonical).toList();
        return ProviderConfigurationProblem.fromDescriptor(
                ProviderConfigurationProblem.Category.REPLACEMENT_CARDINALITY,
                descriptors.getFirst(),
                message,
                null);
    }

    private record FunctionDeclaration(FunctionDescriptor descriptor, boolean imported) {

        private static final Comparator<FunctionDeclaration> ORDER = Comparator
                .comparing((FunctionDeclaration declaration) -> declaration.descriptor().signature())
                .thenComparing(declaration -> declaration.descriptor().implementationMetadata().owner())
                .thenComparing(declaration -> declaration.descriptor().implementationMetadata().memberName())
                .thenComparing(declaration -> declaration.descriptor().implementationMetadata().methodType());

        private FunctionDeclaration {
            Objects.requireNonNull(descriptor, "descriptor");
        }

        private static FunctionDeclaration custom(FunctionDescriptor descriptor) {
            return new FunctionDeclaration(descriptor, false);
        }

        private static FunctionDeclaration imported(FunctionDescriptor descriptor) {
            return new FunctionDeclaration(descriptor, true);
        }

        private void register(FunctionCatalog.Builder builder) {
            if (imported) {
                builder.registerImported(descriptor);
            } else {
                builder.register(descriptor);
            }
        }

        private void replace(FunctionCatalog.Builder builder) {
            if (imported) {
                builder.replaceImported(descriptor);
            } else {
                builder.replace(descriptor);
            }
        }
    }
}
