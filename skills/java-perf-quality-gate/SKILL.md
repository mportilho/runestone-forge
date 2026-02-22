---
name: java-perf-quality-gate
description: Conduzir análise de performance e qualidade em mudanças Java com foco em decisão baseada em evidência. Usar quando houver otimização de código, suspeita de regressão de latência/throughput, refatorações em hot paths, ou necessidade de validar e decidir aceitar/ajustar/descartar alterações usando testes funcionais e benchmark JMH antes/depois com registro histórico.
---

# Java Perf Quality Gate

## Overview

Aplicar um fluxo padronizado para melhorar código Java com segurança de comportamento e validação de performance por JMH. Produzir decisão objetiva baseada em dados e registrar o experimento em formato append-only.

## Workflow Operacional

1. Confirmar escopo e hipótese
- Delimitar o caminho quente, tipo de carga e risco de regressão.
- Definir métrica principal por cenário: latência (`us/op`), throughput, alocação ou combinação.
- Definir baseline técnico: commit, branch ou estado da árvore.

2. Reproduzir e medir baseline
- Compilar e validar testes existentes relevantes.
- Rodar JMH com parâmetros fixos e salvar JSON de baseline.
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

5. Medir after no mesmo protocolo
- Rodar os mesmos cenários JMH com parâmetros idênticos.
- Salvar artefato JSON after e capturar score + erro.
- Calcular deltas absolutos e percentuais.

6. Decidir com critérios explícitos
- Aceitar: ganho relevante ou regressão desprezível com benefício estrutural claro.
- Ajustar: ganho parcial com regressões relevantes ou risco funcional residual.
- Descartar: regressão sem contrapartida justificável.
- Se inferir causa, declarar explicitamente como inferência.

7. Registrar experimento e evidências
- Atualizar histórico append-only com:
- hipótese;
- mudanças;
- protocolo;
- resultados;
- decisão;
- lições aprendidas.
- Registrar atividades executadas nesta rodada (comandos e status).

## Critérios de Qualidade Obrigatórios

- Não concluir sem:
- teste funcional relevante passando;
- benchmark before/after comparável;
- decisão final documentada;
- lista de atividades executadas e limitações.
- Se benchmark falhar por ambiente, declarar bloqueio e impacto na confiança da decisão.
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
- Para template de histórico append-only: `references/performance-history-template.md`
- Para checklist de execução e evidência: `references/quality-activities-checklist.md`

## Anti-Patterns

- Concluir com opinião sem benchmark comparável.
- Misturar múltiplas otimizações sem isolamento de impacto.
- Aceitar regressão de latência sem justificativa explícita.
- Omitir comando executado e artefato de resultado.
