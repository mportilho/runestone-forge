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

**Tag de Cobertura**:
A controlled marker that identifies which language constructs an expression case covers.
_Avoid_: Free-form label, test category
