# ADR 004 — OpenAPI Deve Expor Apenas Filtros Informados Pelo Usuário

Data inferida: 2024-12 a 2026-05

## Status

🟢 **CONFIRMADO** — Aceito e vigente.

## Contexto

🟢 **CONFIRMADO** — O commit `e593d47` registra “Fix: Do not show constant valued filters”.

🟢 **CONFIRMADO** — Os commits `c6140b2` e `f27f37e` registram correções para não mostrar filtros decorados no Swagger/OpenAPI.

🟢 **CONFIRMADO** — O código atual retorna imediatamente em `DynaFilterOperationCustomizer.customizeParameter` quando `filter.constantValues()` tem valores.

## Decisão

🟢 **CONFIRMADO** — A documentação OpenAPI deve remover o parâmetro técnico e expor apenas parâmetros que o chamador realmente pode fornecer.

🟢 **CONFIRMADO** — Filtros constantes e decorados não devem aparecer como query parameters comuns.

## Consequências

🟢 **CONFIRMADO** — O contrato público da API fica mais fiel ao que o consumidor pode enviar.

🟢 **CONFIRMADO** — Regras internas como `deleted=false` podem permanecer invisíveis ao usuário.

🟡 **INFERIDO** — Isso reduz confusão em Swagger UI e evita sugerir parâmetros que não alteram a consulta.

🟢 **CONFIRMADO** — `DynaFilterOperationCustomizer.customize` verifica `Disjunction.class` duas vezes e não verifica `DisjunctionFrom.class`; parâmetro anotado apenas com `@DisjunctionFrom` é ignorado pelo customizer (`DynaFilterOperationCustomizer.java:63-64`).

🟢 **CONFIRMADO PELO USUÁRIO** — A reconstrução deve corrigir a detecção e incluir `DisjunctionFrom.class` na condição inicial.
