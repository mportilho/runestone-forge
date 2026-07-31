package com.runestone.expeval_mk3.internal.plan;

/**
 * An optional rewrite applied to the non-optimized {@link ExecutionPlan} after
 * {@link ExecutionPlanBuilder#buildUnoptimized} produces it. No transformation is installed in this
 * phase; future optimization phases add implementations here instead of building a second runtime or a
 * frozen copy of the non-optimized builder.
 */
@FunctionalInterface
interface PlanTransformation {

    ExecutionPlan apply(ExecutionPlan plan);
}
