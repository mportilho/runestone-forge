# Checklist de Atividades de Qualidade

## Antes da mudança
- Definir hipótese de ganho e risco.
- Confirmar testes relevantes existentes.
- Confirmar que JMH está configurado no módulo (dependências + annotation processor).
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
- Decidir: aceitar, ajustar ou descartar.
- Criar (se ausente) e atualizar `docs/perf/performance-history.md` no módulo alvo.
- Em projeto multi-módulo, garantir que o histórico esteja no submódulo testado (ex.: `expression-evaluator/docs/perf/performance-history.md`).

## Evidências mínimas para concluir
- Lista de arquivos alterados.
- Resultado de testes.
- Resultado de benchmark com evidência reprodutível (JSON, txt ou saída registrada com score/erro/unidade).
- Tabela com `before ns/op`, `after ns/op` e `Melhoria (%)`.
- Decisão final e riscos residuais.
