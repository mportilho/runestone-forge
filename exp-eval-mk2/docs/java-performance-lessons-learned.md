# Lições Aprendidas dos Testes de Performance para Desenvolvimento Java

## Objetivo

Este documento consolida, em formato de práticas de engenharia, as principais lições extraídas dos testes e benchmarks registrados em:

- `docs/perf/performance-history.md`
- `docs/exp-eval-mk2-performance-analysis.md`
- `docs/perf-plan-user-function.md`

O foco aqui não é repetir o histórico dos experimentos, mas transformar os resultados em diretrizes úteis para desenvolvimento Java, especialmente em código de parsing, compilação e execução repetitiva.

## 1. Medir antes de otimizar

A principal lição do histórico é simples: otimização sem medição confiável produz ruído, não engenharia.

Casos como `PERF-003`, `PERF-004`, `PERF-005`, `PERF-007`, `PERF-008`, `PERF-013` e `PERF-016` mostram isso com clareza. Mudanças que pareciam promissoras em revisão de código foram descartadas porque os benchmarks mostraram regressão ou ganho dentro do ruído.

Prática recomendada:

- Formular hipótese explícita antes de alterar o código.
- Medir com JMH usando protocolo estável.
- Definir critério de aceite antes da mudança.
- Reverter rapidamente otimizações sem ganho mensurável.

Em Java, isso é especialmente importante porque JIT, escape analysis, alocação jovem e otimizações internas da JVM tornam a intuição humana pouco confiável em hot paths.

## 2. O hot path deve ser chato

As melhores otimizações vieram de tirar trabalho do caminho de execução repetida e movê-lo para fases amortizáveis.

Exemplos:

- `PERF-009`: criar o execution plan na compilação aumentou levemente `compile()`, mas reduziu fortemente o custo de `compute()`.
- `PERF-012`: pré-compilar o `MethodHandle` uma vez foi melhor do que adaptar a chamada a cada execução.
- `PERF-006`: warmup com corpus representativo reduziu drasticamente a latência fria do parser.

Prática recomendada:

- Pré-computar tudo o que for invariável.
- Preferir pagar custo único em construção/compilação quando a execução é repetida.
- Tratar parsing, resolução semântica, adaptação de handle e materialização de literais como custo de setup, não de runtime.

Em Java, o caminho quente ideal tende a ser um fluxo simples, previsível, com poucos branches, pouca alocação e pouca adaptação dinâmica.

## 3. Menos alocação quase sempre significa melhor throughput

O histórico do cenário `userFunction` mostrou que o gargalo mais consistente não era a matemática em si, mas a infraestrutura em volta da chamada: listas intermediárias, cópias de mapa, criação de escopo e recriação de objetos auxiliares.

Exemplos:

- `PERF-011`: remover listas intermediárias em chamada de função melhorou tempo e reduziu `B/op`.
- `PERF-012`: trocar `invokeWithArguments` por handle pré-adaptado reduziu mais alocação e latência.
- `PERF-014`: reutilizar avaliadores e remover cópia dupla de mapa trouxe ganho estrutural.
- `PERF-015`: eliminar `HashMap` por execução em expressões sem assignment foi uma das mudanças mais valiosas.

Prática recomendada:

- Tratar `B/op` como métrica de primeira classe, não como detalhe secundário.
- Desconfiar de `List`, `Map`, `Optional` e wrappers temporários em trechos executados muitas vezes.
- Usar arrays e estruturas pré-dimensionadas quando a aridade ou tamanho já é conhecido.
- Reutilizar objetos stateless em vez de recriá-los por chamada.

No ecossistema Java, muita perda de performance vem menos do algoritmo principal e mais da soma de pequenas alocações evitáveis.

## 4. Otimize primeiro os custos grandes, não os detalhes elegantes

Uma lição recorrente foi a diferença entre micro-otimizações atraentes e problemas reais do sistema.

Exemplos:

- `PERF-013` mostrou que atalhos em coerção e fast path de `RuntimeValueFactory` não atacavam o custo dominante.
- `PERF-016` mostrou que remover `Optional` do lookup não gerou ganho relevante.
- Em contraste, `PERF-014` e `PERF-015` melhoraram fortemente o sistema porque atacaram cópias de mapa e ciclo de vida de objetos por execução.

Prática recomendada:

- Localizar o maior custo antes de refinar o menor.
- Priorizar cópias, estruturas mutáveis recriadas e adaptações genéricas no hot path.
- Só entrar em micro-otimização depois de remover os custos estruturais.

Em Java, corrigir a arquitetura do caminho crítico costuma render mais do que ajustar instruções isoladas.

## 5. “Código mais simples” nem sempre significa “código mais rápido”

Os experimentos de gramática mostraram que simplificações conceituais podem piorar o comportamento do parser.

Exemplos:

- `PERF-003`, `PERF-004` e `PERF-005` degradaram todos os cenários medidos, embora as propostas parecessem mais limpas ou mais genéricas.
- `PERF-001` e `PERF-002` tiveram sucesso porque melhoraram o comportamento real da estratégia de parsing, não apenas a estética da gramática.

Prática recomendada:

- Não confundir legibilidade estrutural com custo de execução.
- Em ANTLR e em Java, validar refactors de regras quentes com benchmark real.
- Evitar generalizações que aumentem fan-out, indireção ou trabalho de decisão em tempo de execução.

Uma modelagem mais abstrata pode ser melhor para manutenção, mas precisa provar que não destrói o comportamento em produção.

## 6. Exceção não deve ser controle de fluxo em caminho frequente

A análise estática identificou um risco claro: usar tentativas de parse que falham por exceção durante inferência de tipo literal.

Mesmo quando o impacto está no caminho de compilação, essa prática tende a ser cara e a poluir o perfil da aplicação quando a compilação é frequente ou o cache é pouco efetivo.

Prática recomendada:

- Usar heurísticas baratas antes de chamar parse que lança exceção.
- Reservar exceções para eventos realmente excepcionais.
- Em fluxos comuns, preferir validações por forma, prefixo, tamanho ou delimitadores antes da conversão final.

Essa diretriz vale para Java em geral: exceções são ótimas para semântica de erro, ruins para controle normal de throughput.

## 7. O ciclo de vida dos objetos define a efetividade do cache

Os documentos mostram que performance não depende apenas do código interno, mas também de como o sistema é instanciado e reutilizado.

Exemplos:

- A análise de `ExpressionEnvironment` mostrou que um `environmentId` aleatório pode destruir a efetividade do cache se o ambiente for recriado com frequência.
- `PERF-006` só funciona porque o warmup depende de reuso de estruturas internas do parser.
- `PERF-015` só é seguro porque o sistema sabe, na construção, se a expressão tem ou não assignment.

Prática recomendada:

- Projetar APIs com reuso explícito em mente.
- Evitar gerar identidade nova para objetos semanticamente equivalentes sem necessidade.
- Diferenciar claramente objetos de configuração, objetos compilados e objetos de execução.

Em Java, grande parte da performance vem de manter vivas as estruturas certas pelo tempo certo.

## 8. Contratos de mutabilidade e thread-safety precisam ser explícitos

Algumas otimizações só foram possíveis porque o contrato real do runtime era single-thread por instância, com mutabilidade controlada.

Exemplos:

- O plano do cenário `userFunction` reconhece que buffers reutilizados e mapas compartilhados exigem clareza sobre mutabilidade.
- `PERF-015` adicionou guarda explícita para impedir `assign()` em escopos read-only, tornando o ganho seguro.

Prática recomendada:

- Documentar se uma instância é reutilizável, mutável e thread-safe.
- Colocar guardas de runtime quando uma otimização depende de pré-condição estrutural.
- Preferir falha imediata e explícita a corrupção silenciosa de estado.

Em Java, otimização segura quase sempre passa por contratos bem definidos de posse, isolamento e mutação.

## 9. Benchmark bom valida tanto ganho quanto diagnóstico

Os melhores experimentos não apenas responderam “ficou mais rápido?”, mas também “qual era o custo dominante?”.

Exemplos:

- `PERF-010` revelou a grande diferença de alocação no cenário `userFunction`.
- `PERF-013` foi valioso mesmo descartado, porque isolou que o problema restante não estava em coercion metadata nem em `RuntimeValueFactory`.
- `PERF-014` e `PERF-015` foram guiados por esse diagnóstico.

Prática recomendada:

- Medir `ns/op` e `B/op`.
- Usar benchmarks como instrumento de aprendizagem arquitetural.
- Registrar decisões aceitas e descartadas para evitar repetir tentativas improdutivas.

O benchmark deixa de ser só ferramenta de validação e passa a ser ferramenta de desenho de software.

## Resumo Prático

Como regra geral para desenvolvimento Java a partir destas lições:

- mover trabalho invariável para compilação, construção ou warmup;
- reduzir alocação antes de micro-otimizar sintaxe;
- reutilizar objetos stateless e buffers quando o contrato permitir;
- medir sempre com cenário representativo;
- desconfiar de abstrações genéricas no hot path;
- não usar exceção como fluxo normal;
- desenhar cache e identidade de objetos de acordo com o ciclo de vida real da aplicação;
- aceitar apenas otimizações com ganho mensurável e reverter o restante.

## Conclusão

Os testes do `exp-eval-mk2` reforçam uma lição clássica do desenvolvimento Java: performance sustentável não vem de truques isolados, mas de desenho consciente do caminho quente, controle de alocação, reuso de estruturas e disciplina de medição. O maior ganho apareceu quando o código ficou mais específico para o runtime real do sistema, e não quando ficou apenas mais genérico ou mais “bonito”.
