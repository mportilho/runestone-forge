# Expression Evaluator MK3

This context defines the language used around expression compilation and evaluation in the `exp-mk3` module.

## Language

**Caso de Expressao**:
A versioned example that describes an expression, its environment, inputs, expected result or expected diagnostic, and language coverage tags.
_Avoid_: Test fixture, corpus item

**Corpus de Expressoes**:
A versioned collection of expression cases used as the shared behavioral contract for parser, semantic resolver, runtime, migration, and differential verification.
_Avoid_: Test data, examples folder

**Plano Imutavel**:
A compiled, thread-safe, reusable representation of an expression that is executed with an isolated scope for each call.
_Avoid_: Compiled tree, executable expression internals

**Resultado Esperado**:
The value or diagnostic that an expression case declares as the correct behavior.
_Avoid_: Assertion payload, expected output

**Ambiente de Expressao**:
The compilation configuration that declares external symbols, functions, Java types, numeric mode, time zone, and limits used to interpret an expression case.
_Avoid_: Test context, evaluation setup

**Identificador de Ambiente**:
A canonical, stable identity derived from all compilation-relevant Ambiente de Expressao content so equivalent environments can share compiled plans safely.
_Avoid_: Object identity, cache tag, random environment id

**Simbolo Externo**:
A named value declared by an Ambiente de Expressao and supplied from outside the expression, optionally with a declared type or default value; absence is only invalid when the expression actually needs that value.
_Avoid_: Input variable, parameter, free variable

**Valor Padrao de Simbolo**:
The fallback value declared with a Simbolo Externo and validated as part of the Ambiente de Expressao when the symbol has a declared type.
_Avoid_: Runtime fallback, missing input handler

**Modo Numerico**:
The Ambiente de Expressao policy that determines numeric interpretation, planning, and execution behavior for numeric expressions; changing it changes the compiled expression identity.
_Avoid_: Performance flag, math optimization

**Tipo Vetor**:
The type of a vector value created by the expression language, preserving ordered elements and an element type.
_Avoid_: Java list type, generic collection

**Tipo Colecao**:
The type of an ordered or iterable group of values supplied externally or produced by navigation and collection operations, preserving an element type without implying vector-literal semantics.
_Avoid_: Vector alias, raw iterable

**Limite de Materializacao**:
An Ambiente de Expressao guard rail that bounds values created by the expression language, such as vector literals or materialized collection-operation results, without necessarily rejecting large external collections at entry.
_Avoid_: Parser size limit, input collection limit

**Tipo Mapa**:
The type of a text-keyed value map understood by the expression language, preserving the type of values reachable by textual keys.
_Avoid_: Generic Java map, object property bag

**Tipo Objeto**:
A nominal domain object type whose registered members can be used for navigation without making object compatibility structural.
_Avoid_: Structural record type, map-like object

**Tipo Java Registrado**:
A Java-backed Tipo Objeto whose navigable members are declared by the Ambiente de Expressao through a chosen exposure policy, from property accessors to optionally all public methods.
_Avoid_: Reflected class, automatic object shape

**Tipo Nulo**:
The type of a value statically known to be null; it is distinct from unknown type information and participates in null-aware inference.
_Avoid_: Unknown null, missing value type

**Tipo Desconhecido**:
The type assigned when the expression compiler cannot prove a more specific type yet; strict mode may reject remaining unknowns in the semantic model where execution would otherwise defer validation.
_Avoid_: Dynamic type, any type, untyped value

**Funcao Pura**:
A function whose call has no observable side effects and returns the same result for the same arguments within the same Ambiente de Expressao.
_Avoid_: Safe function, utility function

**Assinatura de Funcao**:
The language-level identity of a callable function, made from its name, arity, and parameter types; return type and Java reflection details do not disambiguate calls.
_Avoid_: Java method signature, method handle identity

**Funcao Dobravel**:
A Funcao Pura that may be evaluated during compilation when all arguments are compile-time constants.
_Avoid_: Constant function, cached function

**Funcao Embutida**:
An official function provided by the expression language through the Ambiente de Expressao; built-in function groups are expected to be registered as complete, validated sets.
_Avoid_: Helper method, default Java function

**Funcao de Assercao**:
A built-in `as*` function that locally asserts or converts a value to a requested expression type without enabling implicit conversion for surrounding operators.
_Avoid_: Global cast rule, type hint syntax

**Coercao de Borda**:
A type conversion allowed only at explicit boundaries, such as external values, declared defaults, final function-binding fallback, or API result conversion, without silently changing the meaning of internal expression operations; its configured profile is part of the Ambiente de Expressao identity.
_Avoid_: Implicit cast, dynamic conversion, overload priority rule

**Arquivo de Expressao**:
A complete expression source made of zero or more assignments followed by an optional result expression; syntactically valid empty files are represented and rejected later by semantic validation.
_Avoid_: Program, script, parse result

**Condicional de Expressao**:
A language construct that selects the first matching branch and has one semantic meaning whether written in classic `if then else endif` form or functional `if(...)` form.
_Avoid_: If function, separate conditional languages

**Coalescencia Nula**:
A lazy left-to-right expression that returns the first non-null operand from a `??` chain.
_Avoid_: Default operator, null fallback function

**Cadeia de Navegacao**:
A source-ordered access path from an expression receiver through property, method, subscript, filter, wildcard, or collection-operation links; safe navigation belongs to the individual link that declares it.
_Avoid_: Nested getter calls, path string, reflection chain

**Item Atual**:
The contextual value referenced by `@` inside filters and lambdas; parsing can recognize it anywhere, but semantic validation decides whether a current item context exists.
_Avoid_: At variable, implicit identifier, lambda parameter name

**Profundidade de Item Atual**:
The nesting depth of filters and lambdas that introduce an Item Atual; the Ambiente de Expressao can limit this depth as a guard rail for semantic resolution and execution frame layout.
_Avoid_: Filter depth, lambda count

**Valor Temporal Corrente**:
A dynamic expression value such as `currDate`, `currTime`, or `currDateTime` that is derived from the execution clock, not a constant literal captured during AST construction.
_Avoid_: Date literal, external symbol, compile-time constant

**Nome Reservado**:
A source identifier reserved by the expression language for built-in simple-name values such as current temporal values; an Ambiente de Expressao cannot declare an external symbol with the same name.
_Avoid_: Built-in function name, Java keyword

**Tag de Cobertura**:
A controlled marker that identifies which language constructs an expression case covers.
_Avoid_: Free-form label, test category

**Resultado de Parsing**:
The ephemeral outcome of parsing an expression source during compilation: either a valid internal parse tree with parser metadata, or one or more positioned expression diagnostics and no valid tree.
_Avoid_: Parse exception, partial parse tree

**Arvore Semantica de Expressao**:
A source-faithful, immutable expression tree that carries materialized literals, explicit source grouping, and unified syntax synonyms, but no type resolution, planning rewrites, or execution strategy.
_Avoid_: Parse tree, canonical plan, typed AST

**Identificador de No**:
A deterministic, compilation-local identity assigned to each node or navigation link of an Arvore Semantica de Expressao in stable pre-order traversal.
_Avoid_: UUID, source span key, persistent node id

**Igualdade Estrutural de Arvore**:
A comparison of expression tree shape, preserved source-form fields, operators, literals, and children that intentionally ignores node identities and source spans.
_Avoid_: Record equality, source-text equality, semantic equivalence

**Trecho de Fonte**:
A half-open character range in an expression source, with zero-based offsets for machines and one-based line and column numbers for human-facing diagnostics. Empty ranges identify insertion points such as missing tokens or end of source.
_Avoid_: Token position, raw ANTLR location

**Diagnostico de Expressao**:
A stable, categorized explanation of why an expression source cannot be accepted by a compilation phase, always tied to a source span when it originates from source text.
_Avoid_: Exception message, ANTLR error text
