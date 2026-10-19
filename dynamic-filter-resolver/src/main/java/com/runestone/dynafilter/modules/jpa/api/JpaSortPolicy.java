package com.runestone.dynafilter.modules.jpa.api;

/** Application-wide policy for direct statement-based repository sort/page overloads. */
public enum JpaSortPolicy {
    /** Transitional default. Retains the original permissive alias translation. */
    LEGACY,
    /** Only names in the effective, metamodel-validated declarative catalog are accepted. */
    DECLARED
}
