## Grammar Improvement Backlog

### Objetivo

Registrar todas as propostas de melhoria levantadas para `ExpressionEvaluatorV2.g4`, com status e racional, para testar uma por vez sem perder histórico.

### Estado Atual Aceito

- `ExpressionEvaluatorV2ParserFacade` com `SLL -> LL` fallback foi aceito como estratégia de integração do parser.
- A gramática foi melhorada no caminho de assignment escalar com:
  - `assignmentValue`
  - `genericEntity`
  - `referenceTarget`
  - `castExpression`
- Esse é o baseline atual a preservar antes de novos experimentos.

### Hotspots Confirmados

- `assignmentValue` no assignment escalar continua sendo uma decisão cara em entradas aninhadas.
- `allEntityTypes` ainda é hotspot em vetores e argumentos de função.
- `logicalComparisonExpression` segue sendo hotspot em comparações lógicas/mistas.

### Propostas

#### 1. `SLL -> LL` fallback na integração do parser

- Status: `ACCEPT`
- Escopo: integração Java, não a gramática em si.
- Hipótese: começar em `SLL` e cair para `LL` só quando necessário reduz o custo médio de parse.
- Resultado: aceito.
- Efeito medido:
  - `mathFlatAssignment`: `+97.51%`
  - `mathNestedDecisionAssignment`: `+99.57%`
  - `logicalMixedComparison`: `+32.96%`
  - `vectorAssignment`: `+99.09%`
- Observação: essa decisão mudou o baseline de comparação. Novos experimentos devem ser medidos sobre esse caminho.

#### 2. Tirar assignment escalar não tipado de `allEntityTypes`

- Status: `ACCEPT`
- Escopo: gramática.
- Mudança: criar um caminho próprio para assignment escalar com `assignmentValue` e `genericEntity`.
- Hipótese: reduzir a ambiguidade da decisão mais cara sem espalhar sintaxe genérica por toda a linguagem.
- Resultado: aceito.
- Efeito medido no caminho `SLL -> LL`:
  - `mathFlatAssignment`: `+14.03%`
  - `mathNestedDecisionAssignment`: `+18.36%`
  - `logicalMixedComparison`: `+10.81%`
  - `vectorAssignment`: `+5.15%`
- Observação: esse é o último estado aceito da gramática.

#### 3. Reutilizar `genericEntity` em argumentos de função e elementos de vetor

- Status: `DISCARD`
- Escopo: gramática.
- Mudança testada: substituir `allEntityTypes` por uma regra genérica compartilhada também em argumentos de função e vetores.
- Hipótese: remover os últimos usos quentes de `allEntityTypes`.
- Resultado: descartado.
- Efeito medido no caminho `SLL -> LL`:
  - `mathFlatAssignment`: `-47.62%`
  - `mathNestedDecisionAssignment`: `-43.96%`
  - `logicalMixedComparison`: `-50.52%`
  - `vectorAssignment`: `-25.20%`
- Conclusão: a generalização aumentou o custo total de predição mais do que ajudou.

#### 4. Extrair `scalarComparisonExpression` de `logicalComparisonExpression`

- Status: `DISCARD`
- Escopo: gramática.
- Mudança testada: separar comparações escalares em uma sub-rule, sem ampliar a linguagem aceita.
- Hipótese: reduzir fan-out no topo de `logicalComparisonExpression`.
- Resultado: descartado.
- Efeito medido no caminho `SLL -> LL`:
  - `mathFlatAssignment`: `-35.13%`
  - `mathNestedDecisionAssignment`: `-33.17%`
  - `logicalMixedComparison`: `-24.75%`
  - `vectorAssignment`: `-31.68%`
- Conclusão: a regra extra só deslocou o custo; não reduziu o tempo total.

#### 5. Aceitar comparação escalar genérica e validar tipos depois

- Status: `DISCARD`
- Escopo: gramática + semântica.
- Mudança testada:
  - aceitar comparações escalares cruzadas no parser
  - mover incompatibilidade de tipos para um validador semântico posterior
- Hipótese: uma gramática mais permissiva poderia reduzir custo do parser.
- Resultado: descartado.
- Efeito medido no caminho `SLL -> LL`:
  - `mathFlatAssignment`: `-40.66%`
  - `mathNestedDecisionAssignment`: `-30.43%`
  - `logicalMixedComparison`: `-62.08%`
  - `vectorAssignment`: `-41.77%`
- Conclusão: a ampliação da linguagem aumentou muito o custo de parse.

### Próximas Propostas Ainda Não Testadas

#### 6. Aquecimento dirigido por corpus real

- Status: `ACCEPT`
- Escopo: integração/runtime.
- Ideia: usar um conjunto pequeno de expressões reais para aquecer DFA e caches da estratégia `SLL -> LL`.
- Hipótese: reduzir latência fria sem mexer na gramática.
- Risco: baixo.
- Resultado: aceito.
- Corpus versionado:
  - `loan-payment-projection`
  - `portfolio-discount-allocation`
  - `customer-eligibility-gate`
  - `settlement-window-check`
- Efeito medido com `ExpressionEvaluatorV2WarmupBenchmark`:
  - `loan-payment-projection`: `+98.79%`
  - `portfolio-discount-allocation`: `+98.97%`
  - `customer-eligibility-gate`: `+99.21%`
  - `settlement-window-check`: `+98.92%`
- Conclusão: aquecer o parser com um corpus pequeno e representativo reduz fortemente a latência fria do caminho `SLL -> LL` sem alterar a linguagem aceita.

#### 7. Profiling com corpus real e ranking por decisão

- Status: `ACCEPT`
- Escopo: diagnóstico.
- Ideia: capturar inputs reais e medir `DecisionInfo` por cenário, não só entradas sintéticas.
- Hipótese: os hotspots de produção podem ser diferentes dos quatro cenários atuais.
- Risco: baixo.
- Resultado: aceito.
- Implementação:
  - `ExpressionEvaluatorV2ProfileMain` agora roda cenários sintéticos e corpus versionado
  - as entradas do corpus ficam em `src/test/resources`
  - o profiler imprime ranking por cenário e agregado
- Hotspots observados no corpus:
  - `assignmentValue` segue dominante quando há assignments genéricos/de decisão
  - `allEntityTypes` voltou a aparecer como hotspot material em vetores e argumentos de função
  - `logicalComparisonExpression` continua relevante em cenários lógicos mistos
- Conclusão: os cenários reais confirmam que o gargalo não está só no assignment escalar; `allEntityTypes` continua sendo um ponto sensível fora dos cenários sintéticos.

#### 8. Normalizar comparações booleanas por semântica, não por gramática

- Status: `PENDING`
- Escopo: gramática + semântica futura.
- Ideia: manter a forma sintática atual, mas criar uma camada semântica explícita para rejeitar operadores booleanos inválidos como `true > false`.
- Hipótese: melhora clareza do pipeline sem reabrir a regressão de parse da proposta 5.
- Risco: médio.
- Como validar:
  - não ampliar a gramática
  - adicionar validador pós-parse para boolean comparisons
  - medir custo adicional fora do parser

#### 9. Introduzir AST semântica separada da parse tree

- Status: `PENDING`
- Escopo: arquitetura.
- Ideia: parar de codificar tanta tipagem fina na gramática e concentrar resolução de tipo numa AST semântica posterior.
- Hipótese: reduz acoplamento entre sintaxe e semântica e facilita experimentos sem degradar a gramática principal.
- Risco: alto.
- Como validar:
  - criar transformação parse tree -> AST
  - preservar gramática atual primeiro
  - mover regras semânticas gradualmente

### Ordem Recomendada de Teste

1. Validador semântico para comparações booleanas inválidas sem ampliar a gramática.
2. Repetir o profiler do corpus após qualquer ajuste em `allEntityTypes` ou comparações lógicas.
3. AST semântica separada, se os ganhos de manutenção justificarem o custo estrutural.

### Protocolo de Medição

Para qualquer nova proposta:

- preservar o baseline aceito atual
- rodar `mvn -pl exp-eval-mk2 test`
- rodar o profiler de decisões
- rodar o mesmo JMH usado nos experimentos anteriores
- registrar o resultado em `docs/perf/performance-history.md`

### Critério de Decisão

- `ACCEPT`: ganho claro no caminho `SLL -> LL` com risco baixo ou controlado
- `ADJUST`: ganho pequeno ou inconclusivo
- `DISCARD`: regressão material ou aumento de complexidade sem benefício medido
