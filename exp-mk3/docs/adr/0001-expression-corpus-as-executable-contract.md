# ADR 0001: Corpus de Expressoes as Executable Contract

## Status

Accepted

## Context

The `exp-mk3` module rebuilds the expression evaluator in incremental stages. Before implementing product behavior, Etapa 0 needs a shared contract that can guide parser, semantic resolver, runtime, migration, and differential verification work without depending on the previous evaluator implementation.

The language has enough surface area that examples must be versioned, structured, and mechanically validated. Free-form documentation would make coverage hard to audit and would not provide a stable input for later automated tests.

## Decision

Use YAML Casos de Expressao under `src/test/resources/corpus/` as the executable contract for the module.

Each Caso de Expressao records:

- A stable `id`.
- The validation `phase`.
- Whether the case is `valid` or `invalid`.
- The expression `source`.
- Optional Ambiente de Expressao and inputs.
- The Resultado Esperado or diagnostic.
- Controlled Tags de Cobertura.

The Corpus de Expressoes loader validates structure in Etapa 0 and intentionally does not parse, resolve, or execute expressions yet. Later stages will reuse the same Corpus de Expressoes as their behavioral contract by adding phase-specific assertions.

## Consequences

- The Corpus de Expressoes becomes a versioned source of truth for language behavior.
- Tags de Cobertura are controlled vocabulary, not ad hoc labels.
- Casos de Expressao can be shared by parser, semantic, runtime, migration, and differential tests.
- Etapa 0 can validate Corpus de Expressoes integrity before product behavior exists.
- Future behavior changes must update the corresponding Caso de Expressao instead of relying on undocumented expectations.

## Scope Notes

- Synthetic Casos de Expressao are required in Etapa 0 to cover the initial language surface.
- `real-v1` remains prepared for incremental real-world Casos de Expressao, but it is not required for the first Etapa 0 implementation.
- `diagnostic.code: TBD` is allowed only as an explicit Etapa 0 exception while stable diagnostic codes do not exist.
