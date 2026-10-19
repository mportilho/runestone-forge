package com.runestone.dynafilter.modules.jpa.api;

/** Application-wide policy for direct statement-based repository sort/page overloads. */
public enum JpaSortPolicy {
    /** Explicit migration opt-in. Retains the original permissive alias translation. */
    LEGACY,
    /** Default. Only names in the effective, metamodel-validated declarative catalog are accepted. */
    DECLARED
}
