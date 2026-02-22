# Checklist de Atividades de Qualidade

## Antes da mudança
- Definir hipótese de ganho e risco.
- Confirmar testes relevantes existentes.
- Rodar baseline funcional e baseline JMH.

## Durante a mudança
- Isolar alteração no hotspot.
- Evitar refatorações não relacionadas.
- Atualizar/adição de testes do comportamento alterado.

## Depois da mudança
- Rodar compilação e testes relevantes.
- Rodar JMH after com protocolo idêntico.
- Comparar before/after e decidir: aceitar, ajustar ou descartar.
- Atualizar histórico de performance.

## Evidências mínimas para concluir
- Lista de arquivos alterados.
- Resultado de testes.
- Resultado de benchmark com artefato JSON.
- Decisão final e riscos residuais.
