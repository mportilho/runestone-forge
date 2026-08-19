# Plano Detalhado - Etapa 7 - Otimizacoes de Compilacao

Spec: #112

Este plano detalha a Etapa 7 do `exp-mk3` depois do fechamento do M2 na Etapa 6. Ele consolida as decisoes registradas em `decisoes-etapa-7-otimizacoes-de-compilacao.md`, complementa o plano macro e usa o ADR 0019 como contrato de toda transformacao de plano.

## Objetivo

Aplicar um conjunto pequeno de transformacoes provadas entre o Modelo Semantico e o plano executado, e estabelecer o mecanismo de prova que as Etapas 8, 9, 12 e 13 vao reutilizar: **todo plano otimizado e validado por equivalencia contra o Oraculo Sem Otimizacoes**, gerado pela mesma pipeline e selecionavel apenas internamente.

A Etapa 7 nao especializa nos por operador e tipo, nao remove reflexao, nao cria cache de compilacao, nao implementa Memoria de Calculo e nao funde pipelines de colecao.

## Autoridade e Premissas

- ADRs aceitos, `CONTEXT.md`, o Corpus de Expressoes e os planos detalhados vigentes definem o contrato.
- O ADR 0019 e produto desta etapa e passa a ser normativo para toda transformacao de plano.
- O plano historico e o codigo existente sao evidencias, nao autoridade quando contradizem contratos posteriores.
- A API publica continua provisoria e pode mudar de forma incompativel antes da GA.
- Toda a suite existente deve permanecer verde; testes que contradizem contratos normativos sao atualizados atomicamente com codigo e corpus.
- Nao ha especializacao de nos, remocao de reflexao, pooling de escopo, cache de compilacao, Memoria de Calculo nem fusao de pipelines nesta etapa.

## Estado Atual e Estrategia de Reaproveitamento

A tabela e orientada por entregas, nao por classes. Uma classe existente pode ser mantida, dividida ou removida desde que o contrato da linha seja satisfeito.

| Entrega | Estado observado | Acao planejada |
|---|---|---|
| Construtor de plano sem otimizacoes com metadata completa do Modelo Semantico | Completo e exaustivo sobre a AST selada | Preservar |
| `PlanTransformation` como `ExecutionPlan -> ExecutionPlan` | Declarada, sem implementacao e inaplicavel sem protocolo de travessia em toda a familia de nos | Remover |
| `buildUnoptimized` como caminho de oraculo | Existe, mas nomeado como fase e comparado por forma | Renomear e redefinir |
| Teste de equivalencia de pipeline | Afirma mesma forma entre `build` e `buildUnoptimized` | Substituir por equivalencia de valor e de falha |
| Pureza por no no Modelo Semantico | Completa, incluindo pureza de metodo registrado da Etapa 6 | Consumir |
| Fatos numericos, valores preparados e Checagens Diferidas | Completos | Consumir |
| Sentinela `UNBOUND` no Escopo de Execucao | Existe para simbolo nao vinculado | Reusar para memo |
| Dobra de Constante | Ausente | Implementar |
| Leitura Dobrada no plano | Ausente | Implementar |
| Elisao de Assercao | Ausente; `convertScalar` ja e identidade quando o tipo casa | Implementar |
| `in` com lado direito constante | Varredura linear com `structuralEquals` em toda avaliacao | Implementar |
| Subexpressao Comum Memoizada | Ausente; frame fixo no `frameSize` semantico | Implementar condicionado a medicao |
| Benchmarks com conteudo dobravel | Ausentes; toda expressao medida e dirigida por Simbolo Externo | Implementar |
| Gate de desempenho | Caracterizacao sem limiar | Implementar |

## Gate de Entrada

O trabalho executavel comeca apenas depois de verificar os invariantes abaixo. Build verde isolado nao substitui o gate.

- Pureza registrada para todo no e todo elo de navegacao no Modelo Semantico, incluindo pureza de metodo registrado.
- Fato numerico presente para todo no de tipo `NUMBER`.
- Checagens Diferidas produzidas pelo resolver e associadas a nos existentes.
- Valores preparados presentes para literal e para regex.
- Layout de Frame canonico com `frameSize` cobrindo todo slot vinculado.
- Nenhuma excecao crua de navegacao no runtime, conforme criterio de aceite da Etapa 6.

Qualquer violacao e bug interno. A Etapa 7 nao redescobre semantica nem compensa metadata ausente para poder otimizar.

## Mecanismo de Transformacao

- A transformacao acontece na construcao do plano, nao em passe posterior sobre `ExecutableNode`.
- Motivo: `ExecutableNode` e interface simples, sem `children()` e sem `withChildren()`. Um passe posterior exigiria protocolo de travessia e reconstrucao em toda a familia de nos, pago apenas para reconstruir o que o construtor acabou de construir com toda a metadata na mao.
- O construtor opera em dois modos, com um unico campo booleano interno de folding.
- `build` produz o plano otimizado. `buildOracle`, que renomeia `buildUnoptimized`, produz o Oraculo Sem Otimizacoes.
- `PlanTransformation` e a lista vazia de transformacoes instaladas sao removidas.
- A selecao do oraculo e interna ao modulo: sem flag publica, sem propriedade de sistema, sem runtime duplicado.

## Contrato de Dobra de Constante

| Construto | Dobra quando | Nao dobra quando |
|---|---|---|
| Binario aritmetico, logico, comparacao, igualdade | ambos os operandos sao constantes e a operacao nao falha | qualquer operando nao constante, ou a operacao falha |
| Unario e postfix | operando constante e operacao nao falha | operando nao constante, ou fatorial fora do dominio ou do limite |
| Regex | lado esquerdo constante | lado esquerdo nao constante |
| `between` | valor e os dois limites constantes | qualquer parte nao constante |
| `??` | primeiro operando constante e nao nulo | primeiro operando nao constante |
| Condicional | condicao constante | condicao nao constante |
| Colecao literal | todos os elementos constantes | qualquer elemento nao constante |
| Chamada de funcao | descritor `foldable` e todos os argumentos constantes | funcao nao dobravel, ou qualquer argumento nao constante |
| Prefixo de navegacao | receptor constante e todo elo do prefixo puro | receptor nao constante, ou qualquer elo impuro |
| Leitura de Simbolo Externo | politica nao sobrescrevivel | politica sobrescrevivel |
| Leitura de Simbolo Interno | nunca nesta etapa | sempre |
| Item Atual e Valor Temporal Corrente | nunca | sempre |

- A subarvore constante e avaliada pelo proprio no executavel, com um escopo sentinela que lanca em leitura de frame e em acesso ao clock.
- O escopo sentinela transforma violacao de elegibilidade em bug interno alto e claro, em vez de dobra silenciosamente errada. Nao existe segundo avaliador constante para manter em paridade com o runtime.
- Falha em tempo de dobra deixa a subarvore nao dobrada, para falhar em execucao exatamente como falharia no oraculo. Nao ha diagnostico de compilacao novo e nao ha constante envenenada.
- A Checagem Diferida de um no e descartada apenas quando aquela subarvore dobra com sucesso; falha de checagem abandona a dobra e preserva no e checagem.
- Construto preguicoso pode descartar operando nao tomado porque o runtime ja e preguicoso. Operador eager nao pode, e por isso identidades aritmeticas continuam proibidas.

## Contrato de Leitura Dobrada

- O plano registra toda leitura de simbolo que virou constante, desde esta etapa, mesmo sem consumidor.
- Forma: lista imutavel de nome do simbolo, Identificador de No, Trecho de Fonte e valor dobrado.
- E metadata de construcao, nao de execucao: nenhum branch novo no caminho quente.
- A Etapa 10 consome esse registro para explicar valores que nao aparecem mais como leitura em execucao.

## Contrato de Elisao de Assercao

- `BoundaryCoercion.convertScalar` retorna o proprio valor quando a classe alvo ja o aceita, portanto assercao escalar sobre valor do tipo asserido e identidade provada.
- A elisao ocorre somente quando o tipo resolvido do argumento e exatamente o tipo asserido; qualquer outro caso mantem a chamada.
- Assercoes de colecao nao existem no catalogo atual. Quando existirem, sua elisao exige prova propria e nao esta autorizada por este plano.
- `not not x` reduz para `x`. Nenhuma outra reescrita algebrica esta autorizada.

## Contrato de `in` com Lado Direito Constante

| Tipo de elemento | Comparacao vigente | Representacao baixada |
|---|---|---|
| `NUMBER` | `compareTo == 0` | array ordenado com busca binaria por `compareTo` |
| `STRING`, `BOOLEAN`, `DATE`, `TIME`, `DATETIME` | `equals` | `HashSet` |
| colecao ou mapa | `structuralEquals` recursivo | nao baixa; apenas pre-avaliacao da constante |

- `HashSet<BigDecimal>` esta proibido: `1.0` e `1` sao iguais por `compareTo` e diferentes por `equals` e por hash, e isso mudaria o resultado de `in`.
- Elemento de tipo colecao ou mapa nao baixa porque `List.equals` e `Map.equals` usam `equals` nos numeros contidos e divergiriam de `structuralEquals`.
- Lado direito de tipo mapa continua com `containsKey` e ganha apenas a pre-avaliacao da constante.
- Limiar unico de oito elementos para as duas representacoes. Abaixo dele, apenas pre-avaliacao e varredura linear.
- O corpus recebe caso de `in` com escalas diferentes, como `1.0 in [1, 2]`, porque a suite de equivalencia nao pega essa quebra sem ele.

## Contrato de Subexpressao Comum Memoizada

Condicionado a medicao no incremento 6.

- Slots de memo sao apendice do frame no plano, a partir de `frameSize`, inicializados com o sentinela `UNBOUND` no template.
- O Layout de Frame semantico nao muda e a invariante do Modelo Semantico continua valendo como esta.
- A memo e preguicosa no lugar, sem icamento: cada ocorrencia computa ou le, e a primeira ocorrencia efetivamente executada falha exatamente onde o oraculo falharia.
- Icamento esta proibido porque introduziria falha inexistente no oraculo, como em `cond ? x/y : 0`.
- Elegibilidade: subarvore pura, com duas ou mais ocorrencias, que nao leia slot de Item Atual.
- Identidade por chave canonica estrutural derivada na construcao, com operador, tipos resolvidos, filhos e binding; vinculo, `FunctionDescriptor` e `Pattern` comparados por identidade; `Identificador de No` nunca como chave.
- Se o benchmark nao mostrar ganho, o item sai por decisao registrada no historico de desempenho, nao por silencio.

## Verificacao

Equivalencia significa, para a mesma fonte, o mesmo ambiente e as mesmas entradas: mesmo valor, mesma escala, mesma falha com mesmo codigo e mesmo trecho, mesma ordem de avaliacao observavel e mesmos efeitos observaveis.

- Corpus de Expressoes inteiro executado nas duas formas em todo build, sem exclusao e sem teste desabilitado.
- Testes de propriedade com jqwik sobre entradas aleatorias, cobrindo efeito, falha, escala e dominio real.
- Funcoes com efeito observavel fazem parte da suite de equivalencia, para provar que dobra e memo nao criam nem eliminam efeito.
- Ordem observavel e verificada por sonda de efeito que registra a sequencia de invocacoes, comparada como sequencia entre as duas formas. A prior art e a sonda de efeito do teste de politica de avaliacao escalar, que ja acumula a ordem das chamadas em lista; sem ela, a metade de efeitos da prova de equivalencia nao verifica nada.
- Caso por linha da tabela de Dobra de Constante, na coluna que dobra e na coluna que nao dobra.
- Caso de falha em tempo de dobra por construto: divisao por zero, potencia fora do dominio real, raiz fora do dominio real, fatorial fora do limite e Limite de Materializacao, cada um provando que o plano otimizado falha em execucao com o mesmo codigo e trecho do oraculo.
- Caso provando que Checagem Diferida sobrevive quando a dobra e abandonada.
- Caso de `in` com escalas diferentes e caso acima e abaixo do limiar de oito elementos, por tipo de elemento suportado.
- Caso de Leitura Dobrada com Simbolo Externo nao sobrescrevivel, e caso provando que simbolo sobrescrevivel nao dobra.
- Caso provando que override de Simbolo Externo nao sobrescrevivel continua rejeitado com o mesmo diagnostico nas duas formas, mesmo quando sua unica leitura foi dobrada: o simbolo permanece em `declaredSymbolsInCanonicalOrder` e seu slot permanece no template do frame. Dobrar a ultima leitura de um simbolo nao autoriza remover seu vinculo nem seu slot.
- Se o CSE entrar: caso de memo dentro de caminho preguicoso nao tomado, caso de subarvore dependente de Item Atual nao elegivel e caso de falha na primeira ocorrencia executada.

## Benchmarks e Gate

- Benchmarks novos, com conteudo dobravel: prefixo de navegacao constante, `in` com lado direito constante acima do limiar, assercao elidida e compilacao de expressao rica em constantes.
- Motivo: todos os benchmarks existentes sao dirigidos por Simbolo Externo, e a Dobra de Constante mediria aproximadamente zero neles.
- Gate para os benchmarks existentes: nao regressao dentro da banda de mais ou menos um por cento do protocolo vigente, com excecao apenas quando documentada, na forma do precedente da issue 85.
- Gate para os benchmarks novos: ganho mensuravel sobre a forma sem otimizacoes na mesma expressao.
- Protocolo identico ao das Etapas 5 e 6: tres forks, aquecimento e medicao equivalentes, profiler de alocacao, `ns/op` e `B/op`.
- Registro em `docs/perf/performance-history.md` com ambiente, comando, commit, expressoes e resultados.

## Incrementos de Implementacao

1. **Mecanismo e prova.** Modo de construcao com campo booleano de folding, `buildOracle`, remocao da `PlanTransformation`, inversao do teste de equivalencia de pipeline para valor e falha, execucao do corpus inteiro nas duas formas e suite de propriedade com jqwik.
2. **Dobra de Constante e Leitura Dobrada.** Tabela de dobra completa, escopo sentinela, politica de falha nao dobrada, descarte de Checagem Diferida somente em dobra bem-sucedida, prefixo de navegacao puro, Simbolo Externo nao sobrescrevivel e registro de Leitura Dobrada no plano.
3. **Elisao de Assercao e `not not x`.**
4. **`in` com lado direito constante.** Representacao por tipo de elemento, limiar unico e casos de escala no corpus.
5. **Desempenho.** Benchmarks dobraveis, gate de nao regressao e registro no historico.
6. **Subexpressao Comum Memoizada.** Slots de apendice, memo preguicosa no lugar, chave estrutural e decisao de permanencia por medicao.

Cada incremento fecha com `mvn -pl exp-mk3 -am test` verde. A decomposicao em issues ocorre depois do design registrado.

## Criterios de Aceite da Etapa 7

- Gate de entrada verificado antes do trabalho executavel.
- `PlanTransformation` removida e a construcao em dois modos no lugar, com `build` e `buildOracle`.
- Teste de equivalencia de pipeline afirmando valor e falha, nao forma.
- Corpus inteiro verde nas duas formas, sem exclusao.
- Propriedade de equivalencia verde sobre entradas aleatorias, incluindo funcoes com efeito, falha, escala e dominio real.
- Toda linha da tabela de Dobra de Constante com caso que dobra e caso que nao dobra.
- Falha em tempo de dobra provada nao dobrada, com codigo e trecho identicos ao oraculo, para divisao por zero, potencia, raiz, fatorial e Limite de Materializacao.
- Checagem Diferida preservada quando a dobra e abandonada.
- Leitura Dobrada registrada no plano para Simbolo Externo nao sobrescrevivel, e nao registrada para sobrescrevivel.
- Override de Simbolo Externo nao sobrescrevivel rejeitado identicamente nas duas formas, com simbolo e slot preservados apos a dobra da sua unica leitura.
- Elisao de Assercao apenas com tipo exato provado; `not not x` reduzido e nenhuma outra reescrita algebrica presente.
- `in` constante com representacao correta por tipo de elemento, sem `HashSet` para `NUMBER` e sem baixar elemento de colecao ou mapa.
- Se o CSE permanecer: memo preguicosa no lugar, sem icamento, sem elegibilidade dependente de Item Atual, com o Layout de Frame semantico inalterado.
- Benchmarks dobraveis executados, gate de mais ou menos um por cento aplicado aos existentes e resultados registrados no historico.
- ADR 0019 aceito, plano-mestre corrigido e `CONTEXT.md` com os quatro termos novos (Oraculo Sem Otimizacoes, Dobra de Constante, Leitura Dobrada e Elisao de Assercao), mais Subexpressao Comum Memoizada no incremento 6 apenas se o CSE permanecer.
- Toda a suite existente permanece verde.

## Fora de Escopo

- Eliminacao de atribuicao morta, que conflita com plano unico compartilhado entre visoes na Etapa 9.
- Reordenacao segura de curto-circuito, movida para candidata da Etapa 8.
- Reescritas de `%`, de potencia, de raiz e de identidades aritmeticas.
- Especializacao de nos por operador e tipo, `LambdaMetafactory`, `VarHandle` e pooling de escopo, que ficam na Etapa 8.
- Cache de compilacao, contador de execucoes e engine, que ficam na Etapa 9.
- Memoria de Calculo e slots de captura no plano unico, que ficam na Etapa 10.
- Fusao de pipelines de colecao e Tier 1, que ficam na Etapa 13.
- Flag publica, propriedade de sistema ou API que exponha a selecao do oraculo.
- Mudanca de gramatica e de API publica de operacoes de colecao.
- Publicacao de tickets durante o planejamento.

## Impacto nas Etapas Posteriores

- Etapa 8 especializa nos sobre um plano que ja passou por dobra e elisao, e herda o oraculo como criterio de aceite em vez de criar o seu; recebe tambem a reordenacao de curto-circuito como candidata condicionada a perfil.
- Etapa 9 mantem um unico plano por `(source, environmentId)` compartilhado entre visoes, e a Leitura Dobrada viaja dentro desse valor compartilhado sem reter AST, Modelo Semantico nem fonte duplicada.
- Etapa 10 consome a Leitura Dobrada e transfere slots/proveniencia ao no constante ou `MemoizedExecutableNode`, preservando ocorrencias alcancadas sem plano instrumentado.
- Etapa 11 permanece independente desta etapa; os diagnosticos de migracao nao dependem de otimizacao.
- Etapa 12 transforma os limiares desta etapa em gate permanente de CI, junto com perfil de alocacao e diferenciais.
- Etapa 13 reutiliza o mesmo oraculo para o Tier 1 e para a fusao de pipelines de colecao.
