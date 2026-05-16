# ADR 003 — Definir `INNER` Como Join Type Padrão

Data inferida: 2024-02-20

## Status

🟢 **CONFIRMADO** — Aceito e vigente.

## Contexto

🟢 **CONFIRMADO** — O commit `a863cf9` registra explicitamente “Set default join type to INNER”.

🟢 **CONFIRMADO** — O código atual em `JpaPredicateUtils.getJoinType` retorna `JoinType.INNER` quando não há `ModJoinTypeLeft` nem `ModJoinTypeRight`.

## Decisão

🟢 **CONFIRMADO** — Usar `INNER` como join type default para paths compostos e permitir override via modificadores `ModJoinTypeLeft` e `ModJoinTypeRight`.

## Consequências

🟢 **CONFIRMADO** — O comportamento padrão restringe resultados a entidades com associação correspondente.

🟢 **CONFIRMADO** — Casos que precisam preservar entidade mesmo sem associação devem declarar explicitamente `LEFT`.

🟡 **INFERIDO** — O default privilegia semântica de filtro restritivo, comum em busca por atributos de associação.

🔴 **LACUNA** — O histórico não explica o motivo de negócio/técnico completo da escolha por `INNER` sobre `LEFT`; apenas confirma a decisão.
