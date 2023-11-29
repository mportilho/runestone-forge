package com.runestone.expeval.support.callsite;

/**
 * A functional interface for invoking a function during the expression evaluation.
 */
@FunctionalInterface
public interface CallSiteInvoker {

    /**
     * Invokes the function with the given parameters.
     *
     * @param context    the context of the current call
     * @param parameters the parameters to be passed to the function. Each position in the array represents a parameter
     * @return the result of the function
     */
    Object invoke(CallSiteContext context, Object[] parameters);

}
