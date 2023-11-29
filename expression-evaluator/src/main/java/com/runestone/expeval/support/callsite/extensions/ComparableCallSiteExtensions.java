package com.runestone.expeval.support.callsite.extensions;

import com.runestone.expeval.support.callsite.OperationCallSite;
import com.runestone.expeval.support.functions.others.ComparableFunctions;

import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

class ComparableCallSiteExtensions {

    static Map<String, OperationCallSite> comparableFunctionsFactory() {
        Map<String, OperationCallSite> extensions = new HashMap<>();
        OperationCallSite callSite;

        //noinspection rawtypes
        callSite = new OperationCallSite("max", MethodType.methodType(Comparable.class, Comparable[].class),
                (context, parameters) -> ComparableFunctions.max((Comparable[]) parameters[0]));
        extensions.put(callSite.getKeyName(), callSite);

        //noinspection rawtypes
        callSite = new OperationCallSite("min", MethodType.methodType(Comparable.class, Comparable[].class),
                (context, parameters) -> ComparableFunctions.min((Comparable[]) parameters[0]));
        extensions.put(callSite.getKeyName(), callSite);

        return extensions;
    }

}
