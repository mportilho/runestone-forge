# Checklist de Atividades de Qualidade

## Antes da mudança
- Definir hipótese de ganho e risco.
- Confirmar testes relevantes existentes.
- Confirmar que JMH esta configurado no modulo (dependencias + annotation processor).
- Decidir se o cenário exige profiling de GC (`-prof gc`) por risco de alocacao/GC.
- Criar benchmark em `src/test/java` com package dedicado de performance (ex.: `...perf.jmh...`).
- Rodar baseline funcional e baseline JMH.

## Durante a mudança
- Isolar alteração no hotspot.
- Evitar refatorações não relacionadas.
- Atualizar/adição de testes do comportamento alterado.

## Depois da mudança
- Rodar compilação e testes relevantes.
- Rodar JMH after com protocolo idêntico.
- Comparar before/after em `ns/op` e calcular `Melhoria (%)`.
- Quando cabível, rodar before/after com `-prof gc` e comparar `gc.alloc.rate.norm`, `gc.alloc.rate`, `gc.count` e `gc.time`. Adicione percentual final de melhoria.
- Decidir: aceitar, ajustar ou descartar.
- Criar (se ausente) e atualizar `docs/perf/performance-history.md` no modulo alvo.
- Em projeto multi-modulo, garantir que o historico esteja no submodulo testado (ex.: `expression-evaluator/docs/perf/performance-history.md`).

## Evidências mínimas para concluir
- Lista de arquivos alterados.
- Resultado de testes.
- Resultado de benchmark com evidência reprodutível (JSON, txt ou saída registrada com score/erro/unidade).
- Tabela com `before ns/op`, `after ns/op` e `Melhoria (%)`.
- Quando `-prof gc` for aplicado, tabela/registro de métricas GC com evidência correspondente.
- Decisão final e riscos residuais.
