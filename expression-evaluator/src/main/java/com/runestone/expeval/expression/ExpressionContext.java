/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2022-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.expeval.expression;

import com.runestone.assertions.Certify;
import com.runestone.expeval.exceptions.ExpressionConfigurationException;
import com.runestone.expeval.support.callsite.OperationCallSite;
import com.runestone.expeval.support.callsite.OperationCallSiteFactory;
import com.runestone.expeval.support.callsite.extensions.OperationCallSiteExtensions;

import java.lang.invoke.MethodType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The expression context is used to provide variables and functions for the expression evaluation.
 *
 * @author Marcelo Portilho
 */
public class ExpressionContext {

    private Function<String, Object> variablesSupplier;
    private Map<String, Object> dictionary;
    private Map<String, OperationCallSite> functions;

    /**
     * Creates an expression context. Null can be passed when a parameter is not required.
     *
     * @param variablesSupplier a supplier for variables by name
     * @param dictionary        a dictionary of variables and it's values
     * @param functions         a collection of functions by name
     */
    public ExpressionContext(Function<String, Object> variablesSupplier, Map<String, Object> dictionary, Collection<OperationCallSite> functions) {
        this.variablesSupplier = variablesSupplier;
        this.dictionary = dictionary;
        this.functions = functions == null ? null : functions.stream().collect(Collectors.toMap(OperationCallSite::getKeyName, Function.identity()));
    }

    /**
     * Creates a empty expression context
     */
    public ExpressionContext() {
        this(null, null, null);
    }

    public ExpressionContext(Map<String, OperationCallSite> functions) {
        this.functions = functions;
    }

    /**
     * Finds a value by name. Searches first in the variables supplier and then in the dictionary.
     *
     * @param name the name of the value to be found
     * @return the value found or null if not found
     */
    public Object findValue(String name) {
        Object value;
        if (variablesSupplier != null && (value = variablesSupplier.apply(name)) != null) {
            return value;
        }
        initializeDictionaryMap();
        return dictionary.get(name);
    }

    /**
     * Finds a function by name. Searches for default functions if not found in its functions map.
     *
     * @param key the name of the function to be found
     * @return the found function or null if not found
     * @see OperationCallSiteExtensions
     */
    public OperationCallSite findFunction(String key) {
        initializeFunctionsMap();
        return functions.getOrDefault(key, OperationCallSiteExtensions.getDefaultOperationCallSites().get(key));
    }

    /**
     * Sets a new variables supplier
     *
     * @param variablesSupplier the new variables supplier
     */
    public void setVariablesSupplier(Function<String, Object> variablesSupplier) {
        this.variablesSupplier = variablesSupplier;
    }

    /**
     * Adds a new variable to the dictionary
     *
     * @param key   the name of the variable
     * @param value the value of the variable
     */
    public void putDictionaryEntry(String key, Object value) {
        Certify.requireNonBlank(key, "Dictionary entry name required");
        Objects.requireNonNull(value, "Dictionary entry value required");
        initializeDictionaryMap();
        dictionary.put(key, value);
    }

    /**
     * Adds a new dictionary to the context
     *
     * @param dictionary the dictionary to be added
     */
    public void putDictionary(Map<String, Object> dictionary) {
        Objects.requireNonNull(dictionary, "Cannot add a null dictionary to execution context");
        initializeDictionaryMap();
        this.dictionary.putAll(dictionary);
    }

    /**
     * Adds a new function to the context
     *
     * @param function the function to be added
     */
    public void putFunction(OperationCallSite function) {
        Objects.requireNonNull(function, "Function implementation is required");
        initializeFunctionsMap();
        functions.put(function.getKeyName(), function);
    }

    /**
     * Adds a new function to the context. Each position of the array of objects passed to the function represents a parameter.
     *
     * @param functionName the name of the function to be used in the expression
     * @param methodType   the method type of the function
     * @param function     the function implementation
     */
    public void putFunction(String functionName, MethodType methodType, Function<Object[], Object> function) {
        Certify.requireNonBlank(functionName, "Function name must be provided");
        Objects.requireNonNull(methodType, "Function method type must be provided");
        Objects.requireNonNull(function, "Function must be provided");
        OperationCallSite operationCallSite = new OperationCallSite(functionName, methodType, function);
        initializeFunctionsMap();
        functions.put(operationCallSite.getKeyName(), operationCallSite);
    }

    /**
     * Adds all public functions found in the given object to the expression context.
     * <p>
     * If the provider is an instance, all public methods will be added as functions.
     * <p>
     * If the provider is a {@link Class}, all public static methods will be added as functions.
     *
     * @param functionProvider the object containing the functions
     * @throws ExpressionConfigurationException if adding the same function twice
     */
    public void putFunctionsFromProvider(Object functionProvider) {
        try {
            Map<String, OperationCallSite> callSiteMap = OperationCallSiteFactory.createLambdaCallSites(functionProvider);
            List<String> overridingFunctions = callSiteMap.keySet().stream().filter(key -> findFunction(key) != null).toList();
            if (!overridingFunctions.isEmpty()) {
                throw new ExpressionConfigurationException("Cannot add the following functions because they are already defined: " + overridingFunctions);
            }
            callSiteMap.values().forEach(this::putFunction);
        } catch (Throwable e) {
            throw new ExpressionConfigurationException("Error while extracting functions from provider object", e);
        }
    }

    private void initializeFunctionsMap() {
        if (functions == null) {
            functions = new HashMap<>();
        }
    }

    private void initializeDictionaryMap() {
        if (dictionary == null) {
            dictionary = new HashMap<>();
        }
    }

}
