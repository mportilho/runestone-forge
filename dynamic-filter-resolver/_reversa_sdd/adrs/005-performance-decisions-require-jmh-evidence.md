# ADR 005 — Decisões De Performance Exigem Evidência JMH

Data inferida: 2026-02

## Status

🟢 **CONFIRMADO** — Aceito e vigente como prática do módulo.

## Contexto

🟢 **CONFIRMADO** — `docs/performance-history.md` registra experimentos PERF-001 a PERF-008 com hipótese, protocolo, resultado e decisão.

🟢 **CONFIRMADO** — O histórico Git contém commits de performance em sequência, incluindo `e150141`, `33a2f4d`, `3405adb`, `385532b`, `2884333`, `5014613` e `11b0f73`.

## Decisão

🟢 **CONFIRMADO** — Otimizações relevantes devem ser aceitas, ajustadas ou descartadas com base em benchmarks JMH registrados.

🟢 **CONFIRMADO** — Quando uma tentativa causa regressão, aplica-se patch ou reversão em vez de manter a mudança por hipótese.

## Consequências

🟢 **CONFIRMADO** — Cache semântico de annotations foi mantido após corrigir regressões com hash pré-computado e cópia defensiva.

🟢 **CONFIRMADO** — Cache bounded foi aceito apesar de regressão percentual em hit latency porque o impacto absoluto era baixo e reduzia risco de memória.

🟢 **CONFIRMADO** — Otimização de proxy PERF-006 foi descartada porque o ganho não compensou aumento de complexidade.

🟢 **CONFIRMADO** — Substituição de streams por loops em PERF-008 foi descartada por piorar o benchmark e reduzir benefício de legibilidade.

🟡 **INFERIDO** — A cultura técnica do módulo privilegia medição empírica sobre otimização intuitiva.
