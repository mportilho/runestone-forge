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
The compilation configuration that declares external symbols, functions, Java types, numeric mode, and limits used to interpret an expression case.
_Avoid_: Test context, evaluation setup

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

**Valor Temporal Corrente**:
A dynamic expression value such as `currDate`, `currTime`, or `currDateTime` that is derived from the execution clock, not a constant literal captured during AST construction.
_Avoid_: Date literal, external symbol, compile-time constant

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
