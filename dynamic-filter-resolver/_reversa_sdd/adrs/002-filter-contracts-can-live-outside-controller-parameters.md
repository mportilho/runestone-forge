# ADR 002 — Permitir Contratos De Filtro Externos Com `ConjunctionFrom` E `DisjunctionFrom`

Data inferida: 2024-03-25

## Status

🟢 **CONFIRMADO** — Aceito e vigente.

## Contexto

🟢 **CONFIRMADO** — O commit `06c7b8c` implementou `ConjunctionFrom`, `DisjunctionFrom`, `StatementFrom`, `FilterTarget` e fixtures de DTO/interface para filtros externos.

🟢 **CONFIRMADO** — Antes disso, filtros ficavam diretamente associados ao parâmetro ou tipo analisado; filtros externos permitem reaproveitar contratos de busca.

## Decisão

🟢 **CONFIRMADO** — Permitir que filtros sejam declarados em classes/interfaces externas e referenciados por annotations `From`, com entidade alvo declarada por `@FilterTarget` quando necessário.

## Consequências

🟢 **CONFIRMADO** — Contratos de filtro podem ser reutilizados por múltiplos controllers ou parâmetros.

🟢 **CONFIRMADO** — A extração de annotations ficou mais complexa, pois precisa percorrer tipo base, interfaces, superclasses, annotations diretas e meta-annotations.

🟢 **CONFIRMADO** — A validação precisa resolver a entidade alvo fora do tipo do parâmetro em alguns casos.

🟡 **INFERIDO** — Esse design favorece APIs com contratos de pesquisa nomeados e reutilizáveis, ao custo de exigir documentação clara sobre quando usar `@FilterTarget`.
