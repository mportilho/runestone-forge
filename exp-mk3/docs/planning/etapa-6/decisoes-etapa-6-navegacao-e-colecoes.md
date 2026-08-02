# Decisoes de Planejamento da Etapa 6 - Navegacao, Subscritos, Filtros e Colecoes

Este documento consolida as decisoes tomadas durante o planejamento da Etapa 6 do `exp-mk3`, depois do fechamento do M1 na Etapa 5. Ele registra o estado final da arvore de decisoes e substitui premissas conflitantes do plano historico.

## Autoridade e Revalidacao

- ADRs aceitos, `CONTEXT.md`, Corpus de Expressoes e planos detalhados vigentes sao normativos.
- O plano historico e o codigo atual sao evidencias e substrato, nao autoridade quando contradizem contratos posteriores.
- O ADR 0018 e produto desta etapa e passa a ser normativo para a familia de Subscrito.
- O ADR 0018 complementa o ADR 0016 na familia de Subscrito, sem alterar seu historico.
- A API publica atual continua provisoria e nao exige compatibilidade antes da GA.
- Nenhum ticket deve ser publicado durante esta sessao; decomposicao em issues ocorre depois do design registrado.

## Enquadramento da Etapa 6

- A Etapa 6 e um gap plan sobre navegacao ja implementada, nao uma reimplementacao.
- Navegacao, subscritos, filtros, lambdas e operacoes de colecao ja funcionam ponta a ponta: parser, AST, resolver, planner, runtime e visoes publicas.
- O planner nao possui caminho de nao suportado para navegacao; seu unico modo de falha e guarda de bug interno.
- O produto principal da etapa e completude de verificacao e de diagnostico, nao funcionalidade nova.
- O gate M2 fecha com diagnostico estavel, matriz de verificacao completa e caracterizacao de desempenho registrada.
- A Etapa 6 nao implementa otimizacao, especializacao, cache, pooling, auditoria ou fusao.

## Gate de Entrada

- O trabalho executavel so comeca depois de verificar que os invariantes de entrada estao atendidos, e nao por build verde isolado.
- Vinculos de Navegacao completos para as nove formas resolvidas, validados por invariante do Modelo Semantico.
- Layout de Frame com slot de Item Atual por profundidade de aninhamento realmente usada.
- `maxCurrentItemDepth` aplicado no resolver para filtro e para lambda.
- Ausencia de fallback reflexivo por tipo desconhecido em qualquer caminho de runtime.
- Checagens Diferidas de limite de subscrito e de limite de materializacao produzidas pelo resolver.
- Qualquer violacao desse contrato e bug interno, nunca compensacao no runtime.

## Contratos de Linguagem Promovidos a Normativos

- Subscrito aceita apenas literais; indice, limite de fatia e chave por expressao nao existem na v2.
- A gramatica permanece congelada; o erro de parse de `x[i]` e o contrato.
- Estritez e propriedade do elo, nao da forma de subscrito.
- Elo sem `?.` afirma que o que ele nomeia existe; elo com `?.` tolera a ausencia.
- Indice negativo normaliza a partir do fim como `indice + tamanho`.
- Em elo estrito, indice normalizado fora de `[0, tamanho)` falha e chave textual ausente falha.
- Em elo seguro, indice fora de faixa e chave ausente produzem Valor Nulo de Runtime em vez de falha.
- `atributos?.["ausente"] ?? 0` e o idioma para entrada opcional.
- Indice fora de faixa com forma estaticamente conhecida e diagnostico de compilacao apenas em elo estrito; em elo seguro e resultado nulo legitimo.
- Limites de fatia normalizam do fim quando negativos e depois sao presos ao intervalo `[0, tamanho]`, nas duas formas de elo.
- Fatia com fim normalizado abaixo do inicio produz colecao vazia, assim como inicio igual ou acima do tamanho.
- Fatia nunca falha por limite de faixa; falha apenas por Limite de Materializacao.
- Navegacao por campo publico esta fora da v2; a exposicao continua sendo componente de record, getter bean e metodo registrado.
- Membro registrado, valor de mapa, elemento de colecao e retorno de provider nunca sao nulos.
- Violacao desse contrato de nao nulidade e falha de execucao com trecho de fonte, nao valor anulavel.
- Navegacao Segura nao propaga pela cadeia: `a?.b.c` e erro semantico e exige `a?.b?.c` ou `(a?.b ?? d).c`.
- Navegacao Segura tolera receptor nulo e ausencia legitima do proprio elo, e nada alem disso.
- Ela nao mascara receptor de tipo nao subscritavel, falha de acessor, erro de predicado, Limite de Materializacao nem falha de elo seguinte.

## Significado de Navegacao Segura

- A leitura anterior, de que `?.` cobre apenas receptor nulo, tornava o operador inerte.
- Nenhum valor ordinario pode ser nulo: default e override de Simbolo Externo sao nao nulos, atribuicao nula e rejeitada e membro registrado declara retorno nao nulo.
- Com aquela leitura, `?.` marcava resultado como possivelmente nulo sem nenhum caminho que produzisse nulo, e nenhum caso do Corpus exercitava `?.` devolvendo nulo.
- A familia de Subscrito e onde a linguagem precisa de uma nocao de ausencia legitima, e `?.` passa a ser como ela se expressa.
- Consequencia direta: `RUNTIME_NULL_RECEIVER` deixa de existir como codigo publico, revendo a decisao correspondente da rodada anterior.
- Receptor nulo em elo estrito continua barrado pelo resolver e permanece guarda de invariante interna, nao diagnostico publico.
- `RUNTIME_FORBIDDEN_NULL` fica restrito a elemento, chave, valor de mapa e retorno de membro que viole seu contrato.

## Semantica e Metadata

- Pureza de metodo registrado passa a ser declarada e propagada no Vinculo de Navegacao.
- Registro explicito de metodo ganha variante que declara pureza.
- Registro em lote de metodos publicos registra tudo como impuro, porque pureza nao se presume de descoberta por reflexao.
- Propriedades permanecem presumidas puras, incluindo componente de record e getter bean.
- A assimetria entre propriedade e metodo e intencional e deve estar escrita.
- Pureza e metadata semantica e pertence a esta etapa, porque a Etapa 7 nao pode redescobrir semantica para dobrar ou reusar subexpressoes.
- `OperationIdentity.CUSTOM` permanece como seam interno nao suportado, sem API publica e sem remocao.
- Entrada de Mapa e Item de Reducao nao possuem metodos; chamada sobre eles produz diagnostico proprio.
- Esse diagnostico lista os membros existentes e nao expoe o nome interno da classe que representa o item contextual.

## Diagnosticos

- Diagnosticos semanticos de navegacao passam a separar causa, nao construcao.
- `SEMANTIC_NAVIGATION_RECEIVER_NOT_SUPPORTED` cobre receptor incompativel com o elo.
- `SEMANTIC_UNKNOWN_MEMBER` cobre propriedade, metodo, membro contextual e operacao de colecao inexistentes.
- `SEMANTIC_OPERATION_ARGUMENT_MISMATCH` cobre aridade, lambda contra valor, tipo de argumento e restricao de valor de argumento.
- Nome de operacao de colecao inexistente deixa de usar `SEMANTIC_UNKNOWN_FUNCTION`, que fica exclusivo de chamada global.
- O restante permanece em `SEMANTIC_OPERATOR_TYPE_MISMATCH`; fatiar mais que essas tres causas nao agrega valor de teste.
- Codigos de runtime novos: `RUNTIME_MAP_KEY_NOT_FOUND`, `RUNTIME_MEMBER_ACCESS_FAILURE` e `RUNTIME_INVALID_OPERATION_ARGUMENT`.
- `RUNTIME_MAP_KEY_NOT_FOUND` e `RUNTIME_SUBSCRIPT_OUT_OF_BOUNDS` disparam apenas em elo estrito.
- Passam a ser emitidos do interior do runtime: `RUNTIME_SUBSCRIPT_OUT_OF_BOUNDS`, `RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED`, `RUNTIME_UNDEFINED_OPERATION` e `RUNTIME_FORBIDDEN_NULL`.
- Toda falha de navegacao passa pelo seam `RuntimeFailures`, com trecho de fonte do elo e causa preservada.
- Nenhuma excecao crua de navegacao sobrevive no runtime.
- Runtime continua parando na primeira falha real de execucao.

## Verificacao

- A matriz de Navegacao Segura e explicita por forma de elo, e nao coberta por tag generica.
- Cada forma cobre ausencia tolerada pelo elo seguro devolvendo null, a mesma ausencia falhando no elo estrito, e o erro real que o elo seguro nao pode mascarar.
- Filtros e lambdas aninhados sao testados ate `maxCurrentItemDepth`, com valor de Item Atual correto por nivel.
- Operacoes de colecao sao testadas sobre `List`, array e `Map`.
- Corpus inteiro executa verde, sem exclusao por fase ou tag e sem teste desabilitado.
- O unico caso ainda apoiado em excecao crua de Java migra para diagnostico de runtime tipado.
- Nao sao criadas Tags de Cobertura novas; a estrutura de teste ja exige caso para toda tag existente.
- A fase `semantic` do Corpus continua sem uso; diagnostico semantico permanece em caso de fase de runtime invalido.
- Introducao ou renomeacao de codigo atualiza atomicamente codigo, corpus e testes.

## Desempenho

- A Etapa 6 caracteriza, nao aprova por limiar.
- Benchmarks novos: filtro, subscrito e fatia, cadeia de propriedade e metodo sobre Tipo Java Registrado, e lambda aninhada em dois niveis.
- Protocolo identico ao da Etapa 5, com tres forks e profiler de alocacao.
- Resultados registrados em `docs/perf/performance-history.md` como caracterizacao de M2.
- Loops indexados continuam a regra no caminho quente; sem streams, sem fusao, sem especializacao.

## Documentacao e ADR

- A Etapa 6 possui plano detalhado e registro de decisoes proprios.
- O ADR 0018 registra a familia de Subscrito, o carater literal do payload e a assimetria estrito contra tolerante.
- Navegacao Segura sem propagacao e registrada no plano e no verbete existente do `CONTEXT.md`, fora do ADR 0018.
- `CONTEXT.md` recebe o termo Subscrito, que a familia ainda nao tinha.
- O plano-mestre e corrigido apenas onde premissas foram invalidadas.
- Premissas invalidadas corrigidas: sete operacoes oficiais contra as dez do ADR 0016, inline cache reflexivo por tipo desconhecido e navegacao por campo.

## Decomposicao

- Cinco incrementos, cada um fechando com `mvn -pl exp-mk3 -am test` verde.
- Incremento 1: pureza de metodo registrado e contrato de nao nulidade de membros no catalogo e no resolver.
- Incremento 2: tolerancia de ausencia em elo seguro e roteamento de falhas de navegacao pelo seam, com os tres codigos novos e os tres hoje inertes.
- Incremento 3: separacao dos tres codigos semanticos, com atualizacao atomica do corpus afetado.
- Incremento 4: matriz de Navegacao Segura, aninhamento, receptores `List`, array e `Map`, e migracao do caso de excecao crua.
- Incremento 5: quatro benchmarks e registro no historico de desempenho.
- A decomposicao em issues acontece depois desta sessao, nao durante.

## Impacto Posterior

- Etapa 7 usa a forma sem otimizacoes como oraculo e precisa preservar indice estrito, chave estrita e fatia presa.
- Etapa 7 passa a poder dobrar prefixo de navegacao puro porque a pureza de metodo registrado esta declarada.
- Etapa 8 especializa acessores com `LambdaMetafactory` e `VarHandle` sobre metadata ja registrada, sem fallback reflexivo.
- Etapa 9 mantem os vinculos de navegacao dentro do plano compartilhado entre visoes.
- Etapa 10 instrumenta elos de navegacao por identidade e trecho ja preservados.
- Etapa 11 mantem os diagnosticos de migracao independentes desta etapa.
- Etapa 12 transforma a caracterizacao de M2 em gate e amplia stress de profundidade e de materializacao.

## Decisoes Ainda Pendentes

- Nenhuma pendencia aberta no momento.
