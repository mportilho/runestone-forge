# ADR 006 — Validar Metadados De Filtro Na Inicialização

Data inferida: 2024-11 a 2026-02

## Status

🟢 **CONFIRMADO** — Aceito e vigente.

## Contexto

🟢 **CONFIRMADO** — Os commits `a0b7f81` e `e69f53e` indicam trabalho em analisador de configuração de filtros no startup.

🟢 **CONFIRMADO** — O commit `2884333` moveu validação de filtros para metadados/warmup e registrou resultado em `docs/performance-history.md` como PERF-007.

🟢 **CONFIRMADO** — `FilterConfigurationAnalyserBeanPostProcessor` percorre beans `@RestController`, aquece/lista metadados e valida paths quando a entidade alvo é resolvível.

## Decisão

🟢 **CONFIRMADO** — Validar configuração de filtros e aquecer metadados no startup para reduzir trabalho por request e antecipar erro de configuração.

## Consequências

🟢 **CONFIRMADO** — Erros de path inválido podem aparecer na inicialização em vez de apenas no primeiro request.

🟢 **CONFIRMADO** — Validação por request foi removida do caminho quente do statement generator.

🟢 **CONFIRMADO** — PERF-007 registrou redução de `statementGenerator_searchPeopleAndGames` de `4.682` para `4.172` us/op.

🟡 **INFERIDO** — Aplicações consumidoras podem falhar mais cedo após upgrade, o que é desejável para configuração inválida, mas pode mudar o momento em que erro aparece.
