# Domain Docs

How the engineering skills should consume this repo's domain documentation when exploring the codebase.

## Layout

This is a multi-context repo.

- Read root `CONTEXT.md` first. In this repo, it is the context index.
- Read the module `CONTEXT.md` relevant to the task. Current module contexts include `exp-mk3/CONTEXT.md`.
- Read root `docs/adr/` for architecture decisions that touch the area being changed.
- If a context later adds its own `docs/adr/`, read those context-scoped ADRs too.
- If a future `CONTEXT-MAP.md` is added, treat it as the context index and use it to find the relevant context docs.

If any of these files do not exist for the current area, proceed silently. Do not flag their absence or suggest creating them upfront. The `/domain-modeling` skill, reached via `/grill-with-docs` and `/improve-codebase-architecture`, creates them lazily when terms or decisions get resolved.

## File structure

Current layout:

```text
/
+-- CONTEXT.md
+-- docs/adr/
|   `-- 0001-expression-corpus-as-executable-contract.md
`-- exp-mk3/
    `-- CONTEXT.md
```

Expected pattern for future module contexts:

```text
/
+-- CONTEXT.md
+-- docs/adr/
`-- <module>/
    +-- CONTEXT.md
    `-- docs/adr/
```

## Use the glossary's vocabulary

When your output names a domain concept in an issue title, refactor proposal, hypothesis, or test name, use the term as defined in the relevant `CONTEXT.md`. Do not drift to synonyms the glossary explicitly avoids.

If the concept you need is not in the glossary yet, that is a signal: either you are inventing language the project does not use, or there is a real gap to note for `/domain-modeling`.

## Flag ADR conflicts

If your output contradicts an existing ADR, surface it explicitly rather than silently overriding:

> Contradicts ADR-0001 (Use Expression Corpus as Executable Contract), but worth reopening because...
