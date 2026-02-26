/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
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

package com.runestone.expeval.support.callsite.extensions;

import com.runestone.expeval.support.callsite.OperationCallSite;
import com.runestone.expeval.support.callsite.OperationCallSiteFactory;
import com.runestone.expeval.support.functions.others.DateTimeFunctions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to create the default operation call sites with common functions.
 *
 * @author Marcelo Portilho
 */
public class OperationCallSiteExtensions {

    /**
     * Returns the default operation call sites with common functions.
     * <p>
     * The current default functions are:
     * <ul>
     *     <li>Math functions</li>
     *     <li>Financial functions</li>
     *     <li>DateTime functions</li>
     *     <li>String functions</li>
     *     <li>Trigonometry functions</li>
     * </ul>
     *
     * @return a cached map with the default operation call sites
     */
    public static Map<String, OperationCallSite> getDefaultOperationCallSites() {
        return DefaultOperationCallSitesHolder.INSTANCE;
    }

    private static final class DefaultOperationCallSitesHolder {
        private static final Map<String, OperationCallSite> INSTANCE = createDefaultOperationCallSites();

        private static Map<String, OperationCallSite> createDefaultOperationCallSites() {
            Map<String, OperationCallSite> defaultFunctions = new HashMap<>();
            defaultFunctions.putAll(MathFunctionsExtension.mathFunctionsFactory());
            defaultFunctions.putAll(FinancialFormulasExtension.financialFunctionsFactory());
            defaultFunctions.putAll(OperationCallSiteFactory.createLambdaCallSites(DateTimeFunctions.class));
            defaultFunctions.putAll(OperationCallSiteFactory.createLambdaCallSites(StringFunctionsExtension.class));
            defaultFunctions.putAll(TrigonometryExtension.trigonometryFunctionFactory());
            defaultFunctions.putAll(ComparableCallSiteExtensions.comparableFunctionsFactory());
            return Collections.unmodifiableMap(defaultFunctions);
        }
    }

}
