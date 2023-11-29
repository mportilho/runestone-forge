package com.runestone.expeval.support.callsite.extensions;

import com.runestone.expeval.support.callsite.OperationCallSite;
import com.runestone.expeval.support.callsite.OperationCallSiteFactory;
import com.runestone.expeval.support.functions.others.DateTimeFunctions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used to create the default operation call sites with common functions.
 *
 * @author Marcelo Portilho
 */
public class OperationCallSiteExtensions {

    private static Map<String, OperationCallSite> INSTANCE;
    private static final ReentrantLock lock = new ReentrantLock();

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
        if (INSTANCE == null) {
            try {
                lock.lock();
                if (INSTANCE == null) {
                    Map<String, OperationCallSite> defaultFunctions = new HashMap<>();
                    defaultFunctions.putAll(MathFunctionsExtension.mathFunctionsFactory());
                    defaultFunctions.putAll(FinancialFormulasExtension.financialFunctionsFactory());
                    defaultFunctions.putAll(OperationCallSiteFactory.createLambdaCallSites(DateTimeFunctions.class));
                    defaultFunctions.putAll(OperationCallSiteFactory.createLambdaCallSites(StringFunctionsExtension.class));
                    defaultFunctions.putAll(TrigonometryExtension.trigonometryFunctionFactory());
                    defaultFunctions.putAll(ComparableCallSiteExtensions.comparableFunctionsFactory());
                    INSTANCE = Collections.unmodifiableMap(defaultFunctions);
                }
            } finally {
                lock.unlock();
            }
        }
        return INSTANCE;
    }

}
