---
name: java-perf-quality-gate
description: Conduzir análise de performance e qualidade em mudanças Java com foco em decisão baseada em evidência. Usar quando houver otimização de código, suspeita de regressão de latência/throughput, refatorações em hot paths, ou necessidade de validar e decidir aceitar/ajustar/descartar alterações usando testes funcionais e benchmark JMH antes/depois com registro histórico.
---

# Java Perf Quality Gate

## Overview

Aplicar um fluxo padronizado para melhorar código Java com segurança de comportamento e validação de performance obrigatoriamente por JMH. Produzir decisão objetiva baseada em dados e registrar o experimento em formato append-only.

## Workflow Operacional

1. Confirmar escopo e hipótese
- Delimitar o caminho quente, tipo de carga e risco de regressão.
- Definir métrica principal por cenário em latência (`ns/op`) e, quando aplicável, throughput, alocacao e sinais de GC via `-prof gc`.
- Definir baseline técnico: commit, branch ou estado da árvore.

2. Reproduzir e medir baseline
- Garantir que o modulo possui JMH habilitado (dependências e annotation processor). Sem JMH, a execução fica bloqueada ate configurar.
- Antes de implementar a melhoria de desempenho, mapear os cenários da correção e garantir testes unitários com cobertura funcional suficiente para cada cenário.
- Compilar e validar os testes funcionais relevantes.
- Criar/ajustar benchmark em `src/test/java` em package dedicado de performance (ex.: `...perf.jmh...`).
- Rodar JMH com parâmetros fixos e salvar JSON de baseline.
- Quando cabível (suspeita de regressão de alocação/GC ou hot path sensível a memoria), rodar baseline adicional com `-prof gc` e salvar JSON dedicado.
- Confirmar que o benchmark representa o risco real e não só micro-op isolada.

3. Implementar melhoria mínima segura
- Alterar somente o necessário para atacar o hotspot.
- Preservar semântica pública e priorizar simplicidade do caminho quente.
- Evitar mudanças periféricas na mesma entrega.

4. Garantir segurança funcional
- Adicionar ou atualizar testes unitários/integrados cobrindo:
- comportamento nominal;
- casos de borda da otimização;
- invariantes de compatibilidade esperadas.
- Para cada cenário de correção de desempenho, usar cobertura minima de alta confiança antes da implementação: ao menos um caso nominal do hot path, um caso de borda e um caso de regressao/invariante (quando aplicável, justificar ausências).
- Apos implementar, reexecutar e complementar os testes conforme necessário para manter a confiança funcional.
- Evitar linguagem absoluta: o objetivo dos testes e aumentar confiança de preservação de comportamento, nao provar ausência total de regressão.

5. Medir after no mesmo protocolo
- Rodar os mesmos cenários JMH com parâmetros idênticos.
- Salvar artefato JSON after e capturar score + erro.
- Quando `-prof gc` foi usado no baseline, repetir no after e comparar `gc.alloc.rate.norm`, `gc.alloc.rate`, `gc.count` e `gc.time`.
- Calcular deltas absolutos e percentuais em `ns/op`.
- Reportar sempre `Melhoria (%) em ns/op` usando: `((before_ns_op - after_ns_op) / before_ns_op) * 100`.

6. Decidir com critérios explícitos
- Aceitar: ganho relevante ou regressão desprezível com benefício estrutural claro.
- Ajustar: ganho parcial com regressões relevantes ou risco funcional residual.
- Descartar: regressão sem contrapartida justificável.
- Se inferir causa, declarar explicitamente como inferência.

7. Registrar experimento e evidências
- Criar `docs/perf/performance-history.md` quando nao existir, sempre relativo ao modulo alvo.
- Em projeto multi-modulo, criar no submodulo onde o benchmark foi executado (ex.: `expression-evaluator/docs/perf/performance-history.md`), nunca na raiz agregadora.
- Atualizar histórico append-only com:
- hipótese;
- mudanças;
- protocolo;
- resultados;
- resultados de GC com `-prof gc` (quando aplicável);
- decisão;
- lições aprendidas.
- Registrar atividades executadas nesta rodada (comandos e status).

## Critérios de Qualidade Obrigatórios

- Não concluir sem:
- teste funcional relevante passando;
- cobertura funcional suficiente por cenário de correção validada antes de implementar a otimização;
- benchmark before/after comparavel com JMH;
- verificação before/after com `-prof gc` quando cabivel;
- benchmark localizado em `src/test/java` com package dedicado de performance;
- registro em `docs/perf/performance-history.md` do modulo alvo;
- tabela de resultados contendo `before ns/op`, `after ns/op` e `Melhoria (%)`;
- tabela/registro de métricas de GC no histórico quando `-prof gc` for usado;
- decisão final documentada;
- lista de atividades executadas e limitações.
- Se benchmark falhar por ambiente ou JMH nao estiver configurado, declarar bloqueio e impacto na confianca da decisao.
- Se houver risco de concorrência em medições (ex.: build/annotation processing em paralelo), rodar medições de forma sequencial e limpar artefatos.

## Formato de Entrega Esperado

1. Diagnóstico curto do problema.
2. Mudança proposta e risco.
3. Evidência de testes funcionais.
4. Evidência JMH antes/depois.
5. Decisão final: aceitar, ajustar ou descartar.
6. Registro de atividades executadas.

## Referências da Skill

- Para comandos e desenho de benchmark: `references/jmh-playbook.md`
- Para template de histórico append-only em `docs/perf/performance-history.md` do modulo alvo: `references/performance-history-template.md`
- Para checklist de execução e evidência: `references/quality-activities-checklist.md`

## Anti-Patterns

- Concluir com opinião sem benchmark comparável.
- Medir performance sem JMH ou com benchmark ad-hoc fora do fluxo da skill.
- Implementar melhoria de desempenho antes de validar cobertura funcional suficiente dos cenários da correção.
- Criar benchmark fora de `src/test/java` ou sem package dedicado de performance.
- Misturar múltiplas otimizações sem isolamento de impacto.
- Aceitar regressão de latência sem justificativa explícita.
- Omitir comando executado e artefato de resultado.
- Ignorar `-prof gc` em cenário com risco relevante de regressão de alocação/GC.
