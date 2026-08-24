package com.runestone.dynafilter.core.transformer;

/**
 * Resolves transformer types while a filter plan is compiled.
 */
@FunctionalInterface
public interface FilterValueTransformerResolver {

    FilterValueTransformer resolve(Class<? extends FilterValueTransformer> transformerType);
}
