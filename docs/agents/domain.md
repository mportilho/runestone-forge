# Domain Docs

How engineering skills consume this repository's domain documentation.

## Before exploring

- Read `CONTEXT-MAP.md` at the repository root.
- Read the `CONTEXT.md` for each module relevant to the task.
- Read relevant system-wide ADRs under `docs/adr/`.
- Read relevant module ADRs under `<module>/docs/adr/`.

If a referenced directory or file does not exist, proceed silently. Create glossaries and ADRs lazily through the domain-modeling workflow when terms or durable decisions are resolved.

## Layout

This is a multi-context repository:

```text
/
├── CONTEXT-MAP.md
├── docs/adr/
└── <module>/
    ├── CONTEXT.md
    └── docs/adr/
```

Only contexts with established domain vocabulary need a `CONTEXT.md` entry and map entry.

## Vocabulary and decisions

- Use the canonical terms defined by the relevant module glossary.
- Do not replace a preferred term with one listed under `_Avoid_`.
- If a required concept is missing, reconsider the term or update the glossary through domain modeling.
- Surface conflicts with existing ADRs explicitly rather than silently overriding them.
