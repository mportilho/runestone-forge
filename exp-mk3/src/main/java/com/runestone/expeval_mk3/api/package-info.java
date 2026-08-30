/**
 * Public entry points for compiling and executing expressions. Calculation memory intended for
 * persistence should be consumed through its indexed accessors; list projections trade convenience for
 * transient view and entry allocations. Captured values are canonical execution references rather than
 * detached snapshots, so persistence, copying, transactions, and resource ownership remain with the
 * consuming adapter.
 */
package com.runestone.expeval_mk3.api;
