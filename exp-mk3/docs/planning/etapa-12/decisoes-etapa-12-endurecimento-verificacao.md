# Decisoes de Planejamento da Etapa 12 - Endurecimento e Verificacao

Este documento registra incrementalmente as decisoes tomadas durante o planejamento da Etapa 12 do
`exp-mk3`. Apenas decisoes fechadas entram aqui; questoes ainda abertas permanecem na arvore da sessao
de planejamento.

## Escopo do Marco M4

- A Etapa 11 de migracao v1 para v2 esta cancelada. Diagnosticos de migracao, migrador textual, corpus
  real v1 e verificacao diferencial v1 x v2 nao sao pre-requisitos nem entregas da Etapa 12.
- O M4 fecha o endurecimento e a verificacao reproduzivel da implementacao atual do `exp-mk3`.
- Publicacao, automacao de release, tagging e CI/CD ficam fora da Etapa 12. Esses passos serao executados
  manualmente pelo mantenedor quando forem necessarios.

## Modelo de Ameaca

- Tenants podem fornecer Fontes de Expressao Nao Confiaveis e dados de execucao nao confiaveis.
- O evaluator deve proteger o processo com limites verificaveis sobre o trabalho que ele proprio
  controla.
- O evaluator nao e um sandbox para codigo Java registrado pelo integrador. Ambientes de Expressao,
  Provedores de Funcoes e membros Java registrados continuam sendo componentes confiaveis e devem
  cumprir seus contratos de concorrencia, terminacao, nulidade e pureza.
- A protecao usa orcamentos deterministas de estrutura e trabalho. Nao ha timeout interno nem captura
  de `StackOverflowError`, `OutOfMemoryError` ou outro erro fatal da JVM; deadlines de chamada pertencem
  ao integrador.
- A compilacao limita tamanho UTF-16 da fonte, quantidade de tokens, profundidade sintatica geral e
  quantidade de nos da Arvore Semantica de Expressao. Numero de atribuicoes, comprimento de navegacao
  e construcoes semelhantes sao derivados desses orcamentos, sem knobs proprios.
- Todo limite tem default generoso, configuracao no Ambiente de Expressao e teto absoluto validado.
  Nao existe configuracao ilimitada. Defaults e tetos finais serao fechados contra corpus, stress e
  benchmarks no plano detalhado.
- A hipotese inicial a validar e: fonte 16.384/262.144 unidades UTF-16, tokens 4.096/65.536,
  profundidade 64/256 e nos AST 4.096/65.536, no formato `default/teto`. Para limites existentes, a
  hipotese preserva os defaults e adiciona tetos: Item Atual 32/64, materializacao 10.000/100.000 e
  fatorial 1.000/10.000. Stress pode reduzir um teto inseguro, mas nao eleva-lo apenas para fazer o
  teste passar.
- Valores nao confiaveis tambem serao limitados por comprimento textual, profundidade de valor,
  precisao numerica e magnitude de escala numerica em todas as bordas e resultados controlados pelo
  evaluator.
- A hipotese inicial `default/teto` para valores e trabalho e: texto 1.048.576/8.388.608 unidades
  UTF-16, profundidade de valor 64/256, precisao numerica 10.000/100.000 digitos, magnitude de escala
  10.000/100.000, padrao regex 1.024/8.192 unidades UTF-16 e trabalho de avaliacao
  1.000.000/100.000.000 unidades.
- Zero continua valido e desabilita efetivamente a capacidade correspondente. Configuracoes
  estruturais do cache permanecem estritamente positivas.
- `maxEvaluationWork` limita cumulativamente, por compilacao ou execucao, trabalho de custo variavel
  controlado pelo evaluator: visitas/copias de colecao, regex, expansao textual, fatorial, potencia,
  raiz, built-ins matematicos caros e conversoes/materializacoes de borda. Aritmetica simples e nos
  escalares de custo constante nao debitam. Trabalho interno de codigo Java confiavel registrado pelo
  integrador nao entra nessa contagem.
- O debito de colecao e incremental nas operacoes lazy ou com callbacks potencialmente efetivos. Nas
  operacoes puras em que uma otimizacao evita o percurso, como membership constante, usa custo
  semantico deterministico igual no plano otimizado e no Oraculo Sem Otimizacoes. `sortBy` combina
  extracao incremental de chaves com custo deterministico da ordenacao. Materializacao publica e
  conversoes de containers controladas pelo evaluator consomem o mesmo orcamento.
- Esgotamento de qualquer orcamento emite um unico diagnostico terminal no primeiro ponto excedido e
  interrompe a fase ou execucao. A acumulacao continua obrigatoria para erros semanticos independentes
  que nao representem esgotamento de recurso.
- Fonte acima do limite e rejeitada antes da criacao da chave do cache e nao e cacheada. O pipeline
  interno sem cache repete a validacao como defesa em profundidade.
- Violacao provavel sem executar codigo impuro e diagnostico de compilacao anterior ao folding;
  violacao dependente de entrada ou de execucao usa Checagem Diferida e diagnostico runtime.
- Constant folding de built-ins oficiais consome o mesmo Orcamento de Trabalho de Avaliacao por
  compilacao. Esgotamento e diagnostico terminal, nao tentativa de alocacao nem fallback para runtime.
  Provedores customizados foldable permanecem confiaveis e nao medidos; argumentos e resultados ainda
  obedecem aos limites de forma.
- Validacao numerica e orientada por prova: ocorre em bordas, antes de provedores, em operacoes
  expansoras nao limitadas pelo `MathContext` e no resultado publico. Nos intermediarios cuja metadata
  ja prova um teto seguro nao recebem checagem generica de precisao/escala.

## Custo no Caminho Quente

- Nao existe contador em todo `ExecutableNode`. O `ExecutionScope` escalar preserva seu layout; uma
  variante interna `BudgetedExecutionScope` com contador `int` e usada somente quando a Visao de
  Expressao executada pode percorrer containers.
- Operacoes puras de custo conhecido debitam em bloco. Somente loops lazy ou com callbacks debitam por
  item. Nenhum debito aloca.
- O caminho escalar deve manter zero `B/op` adicional e delta pareado dentro de +/-1%. Operacoes de
  colecao devem manter zero alocacao por visita e no maximo 5% de regressao pareada. Acima disso, perfil
  e tentativa de simplificacao sao obrigatorios; a protecao nao e removida silenciosamente.
- Se o subtipo de escopo alterar o layout escalar ou impedir inlining suficiente, o desenho deve ser
  refeito antes do fechamento.
- Um modulo interno `WorkCostPolicy` concentra formulas saturadas, debito e associacao com codigo/span.
  Toda operacao oficial de colecao e todo built-in oficial e classificado exaustivamente como
  `CONSTANT`, `METERED` ou `TRUSTED_UNMETERED`; a ultima categoria e automatica apenas para providers
  customizados.
- Regex debita custo saturado proporcional a padrao x texto; `replaceAll` e `split` debitam tambem
  matches e saida. Expansao para ao atingir comprimento textual ou materializacao, antes de publicar o
  resultado.
- O Orcamento de Trabalho de Avaliacao mede trabalho efetivamente realizado pelo tier. Folding paga na
  compilacao e nao novamente em runtime; lookup otimizado paga seu custo otimizado, sem varredura
  ficticia. Falhas `*_WORK_EXCEEDED` sao, portanto, excecao explicita a equivalencia estrita entre plano
  otimizado e Oraculo Sem Otimizacoes.
- Testes de equivalencia usam saldo amplo; suites proprias provam que cada tier nao subestima trabalho.
  Codigos e dimensoes gerais do orcamento sao estaveis, mas passar exatamente na fronteira nao e
  garantia entre otimizacoes ou trocas internas de algoritmo. A documentacao recomenda margem.
- Cada compilacao e execucao recebe saldo novo. Quota agregada, rate limiting e concorrencia por tenant
  pertencem a aplicacao hospedeira, sem contador global no evaluator.
- Execucao reentrante iniciada por Provedor de Funcoes recebe novo Escopo de Execucao e novo saldo; nao
  ha propagacao implicita por thread. O provider confiavel controla sua propria recursao.

## Retencao do Cache

- A decisao da Etapa 9 de limitar apenas por quantidade foi reaberta pelo novo modelo de Fonte de
  Expressao Nao Confiavel.
- `maximumEntries` permanece exato, com default 1.024 e teto 65.536. `maximumRetainedWeight` adiciona
  peso conservador do payload controlado pela fonte, com default equivalente a 64 Mi unidades e teto
  de 1 Gi unidade por Engine de Expressao.
- O peso cobre ao menos fonte da chave, nos de plano, diagnosticos e constantes dobradas. Componentes
  confiaveis compartilhados do Ambiente de Expressao nao entram. As unidades sao calibradas por JOL e
  nao prometem correspondencia byte a byte com o heap.
- Um peso-base derivado de `maximumRetainedWeight / maximumEntries`, combinado com o peso estimado da
  entrada, permite ao unico `maximumWeight` do Caffeine impor simultaneamente o limite exato de
  quantidade e o limite conservador de payload.
- Entrada individual acima do saldo e compilada e devolvida, mas nao permanece residente; isso nao e
  falha de compilacao.

## Expressoes Regulares

- Toda regex controlada pela linguagem usa engine de tempo linear. O alvo escolhido e RE2/J, sem
  fallback para `java.util.regex.Pattern`.
- A politica cobre operadores regex e built-ins que recebem regex dinamica, inclusive `replaceAll` e
  `split`. Recursos incompativeis com RE2/J sao rejeitados de forma estruturada.
- Built-ins preservam regex calculada em runtime. Padroes literais podem ser preparados ou dobrados;
  padroes dinamicos sao compilados por chamada dentro dos limites e falham no Trecho de Fonte da
  chamada, sem serem classificados como falha do Provedor de Funcoes.
- A incompatibilidade intencional com lookaround, backreferences e demais recursos que exigem
  backtracking faz parte da referencia da linguagem.

## Profundidade Estrutural

- `maxSyntaxDepth` representa a profundidade projetada da Arvore Semantica de Expressao, nao apenas
  delimitadores. A pre-validacao inclui grupos, containers, condicionais, argumentos, lambdas, filtros,
  prefixos unarios, potencia associativa a direita e cadeias associativas a esquerda.
- `maxAstNodeCount` permite formas largas dentro do total configurado; nenhuma cadeia recursiva aceita
  pode ultrapassar o teto de profundidade.

## Diagnosticos e Residuos da Migracao

- O codigo textual, a categoria, a severidade e a politica de Trecho de Fonte de cada Diagnostico de
  Expressao formam contrato publico estavel depois de M4, mesmo que o enum de implementacao permaneça
  interno. Mensagens e sugestoes podem evoluir sem alterar esse contrato.
- A ordem canonica usa primeiro o offset do trecho; no mesmo offset, `ERROR` antes de `WARNING`, depois
  categoria e codigo. Diagnosticos sem trecho aparecem no final.
- Cada codigo declara se o trecho primario e obrigatorio, opcional ou proibido. Sugestao e exigida
  somente quando existe correcao local nao ambigua; testes comparam a lista completa de diagnosticos.
- Entrada externa nula ou incoercivel produz `ExpressionExecutionException` com diagnostico sem trecho.
  Configuracao invalida do builder continua falhando com `IllegalArgumentException`.
- `DiagnosticCategory.MIGRATION`, fases de corpus exclusivamente dedicadas a migracao, casos com
  `code: TBD` e dependencias exclusivas do comparativo legado serao removidos.
- Sintaxe antiga recebe diagnosticos normais de parsing ou semantica. Nao ha classificacao,
  reconhecimento ou sugestao especial de migracao.

## Interface Publica de Limites

- Um unico tipo publico, final e imutavel `ExpressionResourceLimits` concentra defaults, builder,
  tetos e validacoes. O Ambiente de Expressao recebe e expoe esse valor por `resourceLimits(...)` e
  `resourceLimits()`.
- Os tres setters/getters diretos de limites atualmente existentes no Ambiente sao substituidos antes
  de M4; nao ha ponte deprecada porque a API ainda nao chegou a GA.
- `CacheConfig` permanece separado: seus limites pertencem ao ciclo de vida do Engine de Expressao,
  nao a interpretacao semantica de uma fonte em um Ambiente de Expressao.
- `DiagnosticCode` e a fonte interna unica de codigo textual, categoria, severidade, politica de trecho
  e politica de sugestao. Fabricas internas aceitam o tipo registrado; a API publica continua expondo
  `ExpressionDiagnostic.code()` textual, sem publicar o enum.

## Parser Concorrente

- O contexto reutilizavel de lexer/parser permanece por `ThreadLocal`; nao sera introduzido pool
  limitado sem evidencia de necessidade.
- A Etapa 12 deve provar compilacao concorrente de fontes distintas em threads de plataforma e
  virtuais, incluindo SLL, fallback LL e falhas, sem contaminacao nem retencao da entrada.
- Compilacao e execucao sao corretas em threads virtuais. Alto volume de compilacao pode preferir pool
  de plataforma para reaproveitar o contexto `ThreadLocal`; nao ha promessa de desempenho identico
  entre os dois modelos. Execucao de Plano Imutavel continua apropriada a ambos.

## Documentacao Publica

- `exp-mk3/README.md` oferece inicio rapido e indice.
- `docs/reference/language.md` documenta sintaxe, tipos, semantica, precedencia, Regex Linear e
  residuos; `docs/reference/diagnostics.md` documenta o registro de codigos.
- `docs/guides/api.md` cobre Engine, Ambiente, compilacao, visoes, concorrencia e Memoria de Calculo;
  `docs/guides/hardening.md` cobre modelo de ameaca, limites, providers confiaveis, deadline externo e
  tuning.
- `docs/perf/stage12-gates.md` documenta manifesto, ambiente, protocolo e interpretacao dos gates.
- Blocos marcados de expressao sao compilados/executados por teste; a tabela diagnostica corresponde
  ao registro; a tabela de precedencia possui testes AST associados. A referencia nao e gerada
  integralmente do `.g4`, pois a semantica pertence tambem ao resolvedor.

## Ordem de Implementacao

1. Baseline JMH/JOL e inventario final de gaps.
2. Limpeza da Etapa 11, skips e dependencia legada.
3. `ExpressionResourceLimits` e registro diagnostico.
4. Limites de fonte/tokens/profundidade/AST, spans UTF-16 e correcao da retencao LL.
5. Forma de valores, checks anteriores ao folding e RE2/J.
6. `BudgetedExecutionScope`, matriz de debito e materializacao no mesmo escopo.
7. Peso Retido de Compilacao no cache.
8. Stress, concorrencia e propriedades ampliadas.
9. Gates finais de desempenho/alocacao, documentacao e reconciliacao.

Cada incremento fecha primeiro com teste direcionado e depois com
`mvn -pl exp-mk3 -am test` verde.

## Verificacao e Publicacao

- O planejamento nao introduzira infraestrutura de CI/CD.
- Existem tres niveis locais: `mvn -pl exp-mk3 -am test` para a suite cotidiana; o perfil Maven
  `stage12-stress` para contencao e limites volumosos; e
  `exp-mk3/scripts/run-stage12-gates.sh` como orquestrador manual de suite, stress, JMH, alocacao e
  relatorios. Nenhum deles publica ou cria tag.
- A suite cotidiana inclui corpus, inventario diagnostico, limites pequenos, concorrencia
  representativa e propriedades essenciais, com meta de ate dois minutos na maquina de referencia.
- Stress proximo aos tetos roda em JVM filha Temurin 21 com `-Xms512m -Xmx512m -Xss1m`, timeout no
  processo e artefatos em `target/stage12/`. O teste prova rejeicao anterior ao esgotamento e nunca tenta
  provocar `OutOfMemoryError`.
- O stress concorrente usa pool de plataforma adaptado a CPU, 10.000 tarefas virtuais e ao menos 100
  mil compilacoes/execucoes misturando fontes, visoes, Memoria de Calculo, CSE, Item Atual, sucessos e
  falhas. Sincronizacao usa barreiras deterministas, nunca `sleep`; provedores de teste sao thread-safe.
- Propriedades essenciais usam 1.000 tentativas no teste normal; propriedades de hardening usam 10.000
  no perfil de stress. Geradores estruturais cobrem fontes validas/invalidas, valores profundos,
  orcamentos e plano otimizado contra Oraculo Sem Otimizacoes. A seed de falha e registrada e o caso
  minimizado vira Caso de Expressao de regressao; nao existe seed global fixa.
- Um manifesto JMH versionado lista somente benchmarks vinculantes: escalar, colecoes, cache,
  compilacao, regex e Memoria de Calculo. A avaliacao do JSON e automatica; benchmarks fora do
  manifesto permanecem caracterizacao.
- JMH `-prof gc` decide os gates repetiveis de alocacao; JOL prova o layout do escopo escalar; JFR e
  evidencia obrigatoria de fechamento e investigacao, mas nao possui parser automatico de aprovacao.
- A suite final nao admite testes skipped, disabled ou aborted. Casos sem plano sao classificados antes
  do teste de equivalencia e continuam cobertos pelo gate diagnostico apropriado.
- O contador de hardware de branches pendente da Etapa 10 foi dispensado. Os gates de software ja
  executados em Java 21 e a justificativa de desempenho registrada permanecem como evidencia final.
- O fechamento exige uma execucao integral do script manual no Temurin 21 de referencia, resultados
  JMH/JFR/JOL registrados em `docs/perf/performance-history.md`, zero teste pulado/desabilitado/abortado,
  documentacao reconciliada e nenhuma decisao aberta. Nao ha repeticao integral por numero arbitrario,
  tag ou publicacao.

## Artefatos do Planejamento

- O produto principal sera `etapa-12-endurecimento-verificacao.md` neste diretorio.
- Este registro de decisoes sera atualizado a cada rodada.
- O plano-mestre e o `CONTEXT.md` serao reconciliados com as decisoes fechadas.
- Um ADR so sera criado se surgir uma decisao arquitetural dificil de reverter, surpreendente sem
  contexto e resultante de um trade-off real.

## Decisoes Ainda Pendentes

- Nenhuma. A arvore de decisoes foi confirmada e consolidada em
  `etapa-12-endurecimento-verificacao.md`.
