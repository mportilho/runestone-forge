# Expression Evaluation Context

This context describes the language used by `expression-evaluator`, especially the runtime that compiles, plans, executes, navigates, coerces, and audits expressions.

## Language

**Expression**:
A user-authored formula or predicate that can be compiled and evaluated repeatedly.
_Avoid_: Formula, script

**Math Expression**:
An Expression whose final result is numeric.
_Avoid_: Numeric formula

**Logical Expression**:
An Expression whose final result is boolean.
_Avoid_: Predicate, condition expression

**Assignment Expression**:
An Expression that produces named assignment results instead of a single scalar result.
_Avoid_: Assignment script

**Expression Environment**:
The configured catalog of functions, external symbols, type hints, conversion rules, and numeric settings used to compile and evaluate an Expression.
_Avoid_: Runtime config, context object

**Compiled Expression**:
An Expression after parsing, semantic resolution, and planning, ready for repeated evaluation.
_Avoid_: Parsed expression, prepared formula

**Semantic Model**:
The resolved meaning of an Expression, including symbol references, function bindings, member types, result types, and semantic issues.
_Avoid_: Validation result, type map

**Execution Plan**:
The executable form of a Compiled Expression, including assignment steps, the final result expression, external binding metadata, defaults, and audit sizing.
_Avoid_: Runtime tree, executable AST

**Execution Scope**:
The per-evaluation storage for external overrides, external defaults, internal assignments, dynamic instants, and optional audit collection.
_Avoid_: Variable map, evaluation context

**Read-only Execution Scope**:
An Execution Scope for evaluating a Compiled Expression with no Internal Symbol assignment writes and no Audit Trail collection.
_Avoid_: Stateless scope, immutable evaluation context

**Runtime Value**:
A plain Java value carried through evaluation, such as a number, boolean, text, temporal value, vector, object, or null.
_Avoid_: Runtime wrapper, boxed expression value

**Resolved Type**:
The semantic type assigned to an Expression node, such as scalar, vector, collection, map, object, null, or unknown.
_Avoid_: Java type, runtime class

**External Symbol**:
A named value supplied by the Expression Environment or by the caller at evaluation time.
_Avoid_: Variable, parameter

**Internal Symbol**:
A named value produced by an assignment inside an Expression.
_Avoid_: Local variable, temporary

**Dynamic Instant**:
A built-in temporal symbol resolved once per Execution Scope for current date, current time, or current datetime.
_Avoid_: Now value, clock variable

**Function Catalog**:
The available named functions that an Expression may call.
_Avoid_: Function registry, provider list

**Function Binding**:
The semantic decision that connects a function call in an Expression to one concrete catalog function and its return type.
_Avoid_: Function lookup, selected overload

**Function Invocation**:
The runtime act of evaluating arguments, coercing them, calling a bound function, normalizing the result, and recording audit when applicable.
_Avoid_: Method call, function execution

**Collection Function**:
A catalog function invoked through navigation with the current collection or map as its implicit first input.
_Avoid_: Navigation function, collection method

**Type Hint**:
A declared object type that lets object navigation resolve members semantically before evaluation.
_Avoid_: Schema, type annotation

**Member Binding**:
The semantic decision that connects a typed property or method navigation step to one concrete member and its resulting type.
_Avoid_: Member lookup, reflective access

**Object Navigation**:
Navigation from an object root through properties or methods, with typed access when type hints are available and dynamic access otherwise.
_Avoid_: Property chain, object traversal

**Collection Navigation**:
Navigation over vectors, lists, maps, filters, projections, map transforms, aggregations, and deep scans.
_Avoid_: Collection traversal, JSONPath-like access

**Scalar Aggregation**:
A Collection Navigation operation that reduces a collection or map to a scalar Runtime Value such as count, sum, average, minimum, maximum, or product.
_Avoid_: Terminal aggregation, aggregation shortcut

**Navigation Step**:
One operation inside Object Navigation or Collection Navigation, such as member access, index access, slice, filter, projection, aggregation, map transform, collection function, or deep scan.
_Avoid_: Access record, chain element

**Current Element**:
The element currently visible as `@` while evaluating a filter or map transform.
_Avoid_: Loop item, implicit variable

**Map Entry Context**:
The current map key and value visible while evaluating a map filter or map transform.
_Avoid_: Map sentinel, key/value hack

**Deep Scan**:
A recursive Collection Navigation step that searches nested values for a named property or for all reachable values.
_Avoid_: Recursive search, deep traversal

**Constant Folding**:
The compile-time reduction of deterministic Expression fragments into precomputed runtime values while preserving observable audit behavior.
_Avoid_: Precalculation, optimization pass

**Fold Barrier**:
A point where Constant Folding must stop because evaluation depends on per-run state, unknown values, dynamic instants, unsafe timing, or non-foldable behavior.
_Avoid_: Non-foldable case, optimization stop

**Audit Trail**:
The ordered record of variable reads, function calls, and assignments emitted during a single evaluation.
_Avoid_: Trace, log

**Audit Event**:
One entry in the Audit Trail, representing a variable read, function call, or assignment.
_Avoid_: Log event, trace event

**Expression Runtime**:
The part of the evaluator that turns a Compiled Expression into a result inside an Execution Scope.
_Avoid_: Engine internals, evaluator layer
